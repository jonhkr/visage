package com.jonhkr.visage;

import com.jonhkr.visage.message.Header;
import com.jonhkr.visage.message.Message;
import com.jonhkr.visage.parser.DefaultMessageParser;
import org.junit.Test;

import static org.junit.Assert.*;

public class DefaultMessageParserTest {

    private final String validMessage = "header1: header1-value\n" +
            "header-2: header2 value *\n" +
            "Valid-Header: Valid header :)\n" +
            "\n" +
            "\n" +
            "payload data";

    @Test
    public void parseMessageHeadersTest() {
        DefaultMessageParser parser = new DefaultMessageParser();
        Message message = parser.parse(validMessage);

        assertNotNull(message);
        assertNotNull(message.getHeaders());
        assertEquals(3, message.getHeaders().size());
        assertTrue(message.getHeaders().contains(new Header("header1", "header1-value")));
        assertTrue(message.getHeaders().contains(new Header("header-2", "header2 value *")));
        assertTrue(message.getHeaders().contains(new Header("Valid-Header", "Valid header :)")));
    }

    @Test
    public void parseMessagePayloadTest() {
        DefaultMessageParser parser = new DefaultMessageParser();
        Message message = parser.parse(validMessage);

        assertNotNull(message.getPayload());
        assertNotNull(message.getPayload().getData());
        assertEquals("payload data", message.getPayload().getData().toString());
    }
}