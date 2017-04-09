/*
 *    Copyright 2017 Jonas Trevisan
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.jonhkr.visage.watcher;

import com.jonhkr.visage.message.Message;
import com.jonhkr.visage.parser.MessageParser;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;

@RequiredArgsConstructor
public class FileSystemMessageWatcher implements MessageWatcher {

    private final static Logger LOGGER = LogManager.getLogger(FileSystemMessageWatcher.class);

    private final ExecutorService workerExecutor = Executors.newSingleThreadExecutor();
    private final ExecutorService consumersExecutor = Executors.newCachedThreadPool();
    private final List<Consumer<Message>> consumers = new ArrayList<>();

    private final Path path;
    private final MessageParser messageParser;


    private Watcher watcher;

    @Override
    public void start() {
        if (watcher != null) {
            LOGGER.error("Cannot start an already started watcher.");
            return;
        }

        LOGGER.info("Starting file system watcher on \"{}\".", path);
        FileSystem fs = path.getFileSystem();

        try {
            WatchService service = fs.newWatchService();
            path.register(service, ENTRY_CREATE);

            watcher = new Watcher(service);

            workerExecutor.submit(watcher);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void stop() {
        try {
            watcher.stop();
            workerExecutor.shutdown();
            consumersExecutor.shutdown();
            workerExecutor.awaitTermination(10, TimeUnit.SECONDS);
            consumersExecutor.awaitTermination(10, TimeUnit.MINUTES);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onMessage(Consumer<Message> consumer) {
        consumers.add(consumer);
    }

    @RequiredArgsConstructor
    private class Watcher implements Runnable {

        private final WatchService service;
        private boolean stop = false;

        private int retries = 0;

        @Override
        public void run() {
            while(retries < 3) {
                try {
                    loop();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                } catch (ClosedWatchServiceException e) {
                    LOGGER.info("Watch service closed.");
                } finally {
                    if (!stop) {
                        LOGGER.warn("Watcher stopped abnormally.");

                        if (retries < 2) {
                            LOGGER.info("Attempting to start watching again.");
                        } else {
                            LOGGER.warn("Retry attempts exceeded, stopping operation.");

                            FileSystemMessageWatcher.this.stop();
                        }
                    }
                }

                if (stop) {
                    break;
                }

                retries++;
            }
        }

        private void loop() throws InterruptedException {
            for (;;) {
                WatchKey key = service.take();

                if (stop) {
                    return;
                }

                for (WatchEvent<?> event : key.pollEvents()) {
                    if (stop) {
                        return;
                    }

                    if (event.kind() == OVERFLOW) {
                        continue;
                    }

                    WatchEvent<Path> ev = (WatchEvent<Path>) event;
                    Path filename = ev.context();

                    LOGGER.debug("New message found with id: {}", filename);

                    Path child = path.resolve(filename);

                    try {
                        deliver(messageParser.parse(Files.newBufferedReader(child)));
                    } catch (Exception e) {
                        LOGGER.debug(e.getMessage(), e);
                        LOGGER.warn("Failed to parse message with id: {}", filename);
                    }
                }

                if (!key.reset()) {
                    break;
                }
            }
        }

        void stop() throws IOException {
            stop = true;
            service.close();
        }
    }

    private void deliver(Message message) {
        consumersExecutor.submit(() ->
                consumers.forEach((c) -> consumersExecutor.submit(() -> c.accept(message))));
    }

}
