package lk.ijse.websocketchatapplication.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lk.ijse.websocketchatapplication.entity.ChatMessage;
import lk.ijse.websocketchatapplication.repository.ChatMessageRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private final List<WebSocketSession> sessions = new CopyOnWriteArrayList<>();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final DateTimeFormatter TIMESTAMP_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final long PING_INTERVAL = 30000; // 30 seconds

    private final ChatMessageRepository chatMessageRepository;

    public ChatWebSocketHandler(ChatMessageRepository chatMessageRepository) {
        this.chatMessageRepository = chatMessageRepository;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessions.add(session);
        System.out.println("New connection established: " + session.getId());

        if (session.isOpen()) {
            session.sendMessage(new PingMessage());
        }

        sendPreviousMessages(session);
    }

    private void sendPreviousMessages(WebSocketSession session) throws IOException {
        try {
            List<ChatMessage> lastMessages = chatMessageRepository.findTop100ByOrderByTimestampDesc();
            System.out.println("Sending " + lastMessages.size() + " previous messages");

            for (ChatMessage message : lastMessages) {
                sendChatMessage(session, message.getSender(), message.getContent(), message.getTimestamp());
            }
        } catch (Exception e) {
            System.err.println("Error sending previous messages: " + e.getMessage());
            sendError(session, "Error loading chat history");
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        try {
            System.out.println("Received message: " + message.getPayload());

            ObjectNode jsonNode = (ObjectNode) objectMapper.readTree(message.getPayload());

            if (!jsonNode.has("sender") || !jsonNode.has("content")) {
                sendError(session, "Invalid message format");
                return;
            }

            String sender = jsonNode.get("sender").asText();
            String content = jsonNode.get("content").asText();

            if (sender.isBlank() || content.isBlank()) {
                sendError(session, "Sender and content cannot be empty");
                return;
            }

            // Save to database
            ChatMessage chatMessage = new ChatMessage();
            chatMessage.setSender(sender);
            chatMessage.setContent(content);
            chatMessage.setTimestamp(LocalDateTime.now());
            chatMessageRepository.save(chatMessage);
            System.out.println("Message saved to database");

            // Broadcast to all clients
            broadcastMessage(sender, content, LocalDateTime.now());

        } catch (Exception e) {
            System.err.println("Error processing message: " + e.getMessage());
            sendError(session, "Error processing your message");
        }
    }

    private void broadcastMessage(String sender, String content, LocalDateTime timestamp) throws IOException {
        ObjectNode response = objectMapper.createObjectNode();
        response.put("type", "MESSAGE");
        response.put("sender", sender);
        response.put("content", content);
        response.put("timestamp", timestamp.format(TIMESTAMP_FORMATTER));

        TextMessage textMessage = new TextMessage(response.toString());

        for (WebSocketSession webSocketSession : sessions) {
            try {
                if (webSocketSession.isOpen()) {
                    webSocketSession.sendMessage(textMessage);
                }
            } catch (Exception e) {
                System.err.println("Error broadcasting to session " + webSocketSession.getId() + ": " + e.getMessage());
                sessions.remove(webSocketSession);
            }
        }
    }

    private void sendChatMessage(WebSocketSession session, String sender, String content, LocalDateTime timestamp) throws IOException {
        ObjectNode jsonMessage = objectMapper.createObjectNode();
        jsonMessage.put("type", "MESSAGE");
        jsonMessage.put("sender", sender);
        jsonMessage.put("content", content);
        jsonMessage.put("timestamp", timestamp.format(TIMESTAMP_FORMATTER));

        if (session.isOpen()) {
            session.sendMessage(new TextMessage(jsonMessage.toString()));
        }
    }

    private void sendError(WebSocketSession session, String errorMessage) throws IOException {
        ObjectNode error = objectMapper.createObjectNode();
        error.put("type", "ERROR");
        error.put("message", errorMessage);

        if (session.isOpen()) {
            session.sendMessage(new TextMessage(error.toString()));
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        System.err.println("Transport error for session " + session.getId() + ": " + exception.getMessage());
        sessions.remove(session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        System.out.println("Connection closed: " + session.getId() + " - " + status.getReason());
        sessions.remove(session);
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }
}