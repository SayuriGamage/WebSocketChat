package lk.ijse.websocketchatapplication.dto;

public class ChatMessageDTO {
    private String sender;
    private String message;

    public ChatMessageDTO() {}

    public ChatMessageDTO(String sender, String message) {
        this.sender = sender;
        this.message = message;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
