package com.jonhkr.visage.parser;

import com.jonhkr.visage.message.Message;

import java.io.BufferedReader;

public interface MessageParser {
    Message parse(String message);
    Message parse(BufferedReader reader);
}
