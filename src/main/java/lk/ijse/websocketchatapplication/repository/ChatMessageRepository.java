package lk.ijse.websocketchatapplication.repository;

import lk.ijse.websocketchatapplication.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findTop100ByOrderByTimestampDesc();
}