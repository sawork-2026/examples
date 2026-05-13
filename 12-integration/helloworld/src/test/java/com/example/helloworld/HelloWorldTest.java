package com.example.helloworld;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.PollableChannel;
import org.springframework.messaging.support.GenericMessage;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class HelloWorldTest {

    @Autowired
    private MessageChannel inputChannel;

    @Autowired
    private PollableChannel outputChannel;

    @Test
    void serviceActivatorShouldGreet() {
        inputChannel.send(new GenericMessage<>("World"));
        String result = (String) outputChannel.receive(5000).getPayload();
        assertEquals("Hello World", result);
    }

    @Test
    void serviceActivatorShouldHandleMultipleMessages() {
        inputChannel.send(new GenericMessage<>("Alice"));
        inputChannel.send(new GenericMessage<>("Bob"));

        assertEquals("Hello Alice", outputChannel.receive(5000).getPayload());
        assertEquals("Hello Bob", outputChannel.receive(5000).getPayload());
    }
}
