package lk.ijse.websocketchatapplication.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ChatController {

    @GetMapping("/")
    public String chatPage() {
        return "index"; // resources/static/index.html
    }
}
