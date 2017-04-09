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
import com.jonhkr.visage.parser.DefaultMessageParser;
import com.jonhkr.visage.parser.MessageParser;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class FileSystemMessageWatcherTest {

    private File messagesDirectory;
    private FileSystemMessageWatcher watcher;

    @Before
    public void setup() throws IOException {
        messagesDirectory = new File("test/" + UUID.randomUUID() + "/");
        messagesDirectory.mkdirs();

        MessageParser messageParser = new DefaultMessageParser();
        watcher = new FileSystemMessageWatcher(messagesDirectory.toPath(), messageParser);
        watcher.start();
    }

    @After
    public void after() throws IOException {
        watcher.stop();

        Files.walkFileTree(Paths.get("test"), new SimpleFileVisitor<Path>() {

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                super.visitFile(file, attrs);

                Files.delete(file);

                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                super.postVisitDirectory(dir, exc);

                Files.delete(dir);

                return FileVisitResult.CONTINUE;
            }
        });
    }


    @Test
    public void createTestMessageFileTest() throws Exception {
        createTestMessageFile("valid-message.txt");
        assertTrue(new File(messagesDirectory.getAbsolutePath() + "/valid-message.txt").exists());
    }

    @Test
    public void parseMessageFileTest() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);

        List<Message> messages = new ArrayList<>();

        watcher.onMessage((m) -> {
            messages.add(m);
            latch.countDown();
        });

        createTestMessageFile("valid-message.txt");

        latch.await(1, TimeUnit.MINUTES);

        assertEquals(1, messages.size());
        assertEquals("payload data", messages.get(0).getPayload().getData().toString());
    }

    @Test
    public void wontStopAfterInvalidMessage() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);

        List<Message> messages = new ArrayList<>();

        watcher.onMessage((m) -> {
            messages.add(m);
            latch.countDown();
        });

        createTestMessageFile("invalid-message.txt");
        createTestMessageFile("valid-message.txt");

        latch.await(1, TimeUnit.MINUTES);

        assertEquals(1, messages.size());
    }

    private void createTestMessageFile(String fileName) throws URISyntaxException, IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        URL url = classLoader.getResource(String.format("watcher/messages/%s", fileName));

        if (url == null) {
            throw new IllegalArgumentException("Could not locate file " + fileName);
        }

        Files.copy(Paths.get(url.toURI()), Paths.get(String.format("%s/%s", messagesDirectory.getAbsolutePath(), fileName)));
    }

}