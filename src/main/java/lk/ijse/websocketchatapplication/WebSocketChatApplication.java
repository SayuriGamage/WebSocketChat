package lk.ijse.websocketchatapplication;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories("lk.ijse.websocketchatapplication.repository")
@EntityScan("lk.ijse.websocketchatapplication.entity")
public class WebSocketChatApplication {
    public static void main(String[] args) {
        SpringApplication.run(WebSocketChatApplication.class, args);
    }
}
