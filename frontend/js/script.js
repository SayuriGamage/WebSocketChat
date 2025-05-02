// Check if socket already exists
if (typeof window.chatSocket === 'undefined') {
    // Initialize WebSocket connection
    window.chatSocket = new WebSocket("ws://localhost:8080/ws/chat");
    let senderName = "";
    const maxReconnectAttempts = 5;
    let reconnectAttempts = 0;

    // Connection opened
    window.chatSocket.onopen = function() {
        console.log("Successfully connected to chat server");
        reconnectAttempts = 0;
        displaySystemMessage("Connected to chat server");
    };

    // Handle messages
    window.chatSocket.onmessage = function(event) {
        try {
            const msg = JSON.parse(event.data);
            if (msg.type === "MESSAGE") {
                displayMessage(msg.sender, msg.content, msg.timestamp);
            } else if (msg.type === "ERROR") {
                console.error("Server error:", msg.message);
                displaySystemMessage("Error: " + msg.message, true);
            }
        } catch (e) {
            console.error("Error parsing message:", e);
            displayMessage("System", event.data, new Date().toISOString());
        }
    };

    // Handle errors
    window.chatSocket.onerror = function(error) {
        console.error("WebSocket error:", error);
        displaySystemMessage("Connection error occurred", true);
    };

    // Handle connection close
    window.chatSocket.onclose = function(event) {
        console.log("WebSocket closed:", event.code, event.reason);
        displaySystemMessage("Disconnected. Reconnecting...", true);

        if (reconnectAttempts < maxReconnectAttempts) {
            reconnectAttempts++;
            setTimeout(() => {
                // Reinitialize the connection
                if (typeof window.chatSocket === 'undefined' ||
                    window.chatSocket.readyState === WebSocket.CLOSED) {
                    window.chatSocket = new WebSocket("ws://localhost:8080/ws/chat");
                }
            }, 3000);
        } else {
            displaySystemMessage("Failed to reconnect. Please refresh the page.", true);
        }
    };

    // Message display functions
    function displayMessage(sender, content, timestamp) {
        const chatBox = document.getElementById('chat-box');
        if (!chatBox) return;

        const messageElement = document.createElement('div');
        messageElement.className = `message ${sender === senderName ? 'sent' : 'received'}`;

        messageElement.innerHTML = `
            <div class="sender">${sender}</div>
            <div class="text">${content}</div>
            <div class="timestamp">${formatTimestamp(timestamp)}</div>
        `;

        chatBox.appendChild(messageElement);
        chatBox.scrollTop = chatBox.scrollHeight;
    }

    function displaySystemMessage(text, isError = false) {
        const chatBox = document.getElementById('chat-box');
        if (!chatBox) return;

        const messageElement = document.createElement('div');
        messageElement.className = `system-message ${isError ? 'error' : ''}`;
        messageElement.textContent = text;
        chatBox.appendChild(messageElement);
        chatBox.scrollTop = chatBox.scrollHeight;
    }

    function formatTimestamp(timestamp) {
        try {
            return timestamp ? new Date(timestamp).toLocaleTimeString() : new Date().toLocaleTimeString();
        } catch (e) {
            return new Date().toLocaleTimeString();
        }
    }

    // Send message function
    window.sendChatMessage = function() {
        const sender = document.getElementById('sender').value.trim();
        const message = document.getElementById('message').value.trim();

        if (!sender || !message) {
            displaySystemMessage("Both name and message are required!", true);
            return;
        }

        senderName = sender;

        if (window.chatSocket.readyState === WebSocket.OPEN) {
            window.chatSocket.send(JSON.stringify({
                sender: sender,
                content: message
            }));
            document.getElementById('message').value = '';
        } else {
            displaySystemMessage("Not connected to server. Please wait...", true);
        }
    };

    // Initialize event listeners
    document.addEventListener('DOMContentLoaded', function() {
        const sendButton = document.getElementById('send');
        const messageInput = document.getElementById('message');

        if (sendButton) {
            sendButton.addEventListener('click', window.sendChatMessage);
        }

        if (messageInput) {
            messageInput.addEventListener('keypress', function(e) {
                if (e.key === 'Enter') {
                    window.sendChatMessage();
                }
            });
        }
    });
}