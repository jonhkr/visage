package com.jonhkr.visage.publisher;

import com.jonhkr.visage.message.Message;

public interface MessagePublisher {
    void publish(Message message);
}
