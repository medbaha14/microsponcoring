package com.example.microsponsoringbackend.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class WebSocketControllerTest {

    @Autowired
    private WebSocketController webSocketController;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Test
    public void testWebSocketControllerExists() {
        assertNotNull(webSocketController);
    }

    @Test
    public void testSimpMessagingTemplateExists() {
        assertNotNull(messagingTemplate);
    }

    @Test
    public void testPingMessage() {
        // This is a simple test to verify the controller can handle ping messages
        String testMessage = "test ping";
        String result = webSocketController.handlePing(testMessage, null);
        assertEquals("Pong: " + testMessage, result);
    }
}
