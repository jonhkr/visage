package com.jonhkr.visage.message;

import lombok.Data;

import java.util.List;

@Data
public class Message {
    private final List<Header> headers;
    private final Payload payload;
}
