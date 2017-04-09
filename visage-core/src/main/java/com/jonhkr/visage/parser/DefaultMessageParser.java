package com.jonhkr.visage.parser;

import com.jonhkr.visage.message.Header;
import com.jonhkr.visage.message.Message;
import com.jonhkr.visage.message.Payload;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

public class DefaultMessageParser implements MessageParser {

    private final static String HEADER_SEPARATOR = ":";

    @Override
    public Message parse(String message) {
        return parse(new BufferedReader(new StringReader(message)));
    }

    @Override
    public Message parse(BufferedReader reader) {
        try {
            return new Message(parseHeaders(reader), readPayload(reader));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private List<Header> parseHeaders(BufferedReader reader) throws IOException {
        List<Header> headers = new ArrayList<>();

        String line;
        String lastLine = null;

        while((line = reader.readLine()) != null) {
            if (line.isEmpty() && line.equals(lastLine)) {
                break;
            }

            if (line.isEmpty()) {
                lastLine = line;
                continue;
            }

            String[] parts = line.split(HEADER_SEPARATOR, 2);

            if (parts.length != 2) {
                throw new RuntimeException("Invalid header: \"" + line + "\"");
            }

            headers.add(new Header(parts[0].trim(), parts[1].trim()));
            lastLine = line;
        }

        return headers;
    }

    private Payload readPayload(BufferedReader reader) throws IOException {
        StringBuffer stringBuffer = new StringBuffer();
        char[] cbuff = new char[256];
        int c;
        while ((c = reader.read(cbuff)) != -1) {
            stringBuffer.append(cbuff, 0, c);
        }

        return new Payload(stringBuffer);
    }
}
