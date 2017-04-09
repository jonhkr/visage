package com.jonhkr.visage;

import com.jonhkr.visage.aws.sqs.SqsMessagePublisher;
import com.jonhkr.visage.parser.DefaultMessageParser;
import com.jonhkr.visage.parser.MessageParser;
import com.jonhkr.visage.publisher.MessagePublisher;
import com.jonhkr.visage.watcher.FileSystemMessageWatcher;
import com.jonhkr.visage.watcher.MessageWatcher;

import java.io.File;
import java.nio.file.Path;

public class Server {
    public static void main(String[] args) throws InterruptedException {

        MessageParser parser = new DefaultMessageParser();
        MessagePublisher messagePublisher = new SqsMessagePublisher();

        Path path = new File("/Users/jonhkr/messages").toPath();

        MessageWatcher watcher = new FileSystemMessageWatcher(path, parser);

        watcher.start();
        watcher.onMessage(messagePublisher::publish);


        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                super.run();

                watcher.stop();
            }
        });

        Thread.currentThread().join();
    }
}
