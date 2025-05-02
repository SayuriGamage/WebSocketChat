package lk.ijse.websocketchatapplication.controller;

import lk.ijse.websocketchatapplication.entity.ChatMessage;
import lk.ijse.websocketchatapplication.repository.ChatMessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/messages")
public class MessageController {

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @GetMapping
    public List<ChatMessage> getRecentMessages() {
        return chatMessageRepository.findTop100ByOrderByTimestampDesc();
    }
}