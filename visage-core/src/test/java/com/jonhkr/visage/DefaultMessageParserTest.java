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