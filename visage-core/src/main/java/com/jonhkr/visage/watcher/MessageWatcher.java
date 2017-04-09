package com.jonhkr.visage.watcher;

import com.jonhkr.visage.message.Message;

import java.util.function.Consumer;

public interface MessageWatcher {
    void start();
    void stop();
    void onMessage(Consumer<Message> consumer);
}
