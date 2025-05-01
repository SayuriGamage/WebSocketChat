
var socket = new WebSocket("ws://localhost:8080/ws/chat");
var senderName = "";

$(document).ready(function () {
    $('#send').click(function () {
        var sender = $('#sender').val();
        var message = $('#message').val();

        if (sender.trim() === "" || message.trim() === "") {
            alert("Name and message cannot be empty!");
            return;
        }

        senderName = sender;

        var chatMessage = {
            sender: sender,
            content: message
        };

        socket.send(JSON.stringify(chatMessage));
        $('#message').val('');
    });

    $('#message').keypress(function (e) {
        if (e.which == 13) {
            $('#send').click();
        }
    });

    socket.onmessage = function (event) {
        var msg = JSON.parse(event.data);
        var messageClass = msg.sender === senderName ? 'sent' : 'received';
        var timestamp = new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });

        $('#chat-box').append(`
            <div class="message ${messageClass}">
                <div class="sender">${msg.sender}</div>
                <div class="text">${msg.content}</div>
                <div class="timestamp">${timestamp}</div>
            </div>
        `);
        $('#chat-box').scrollTop($('#chat-box')[0].scrollHeight);
    };
});
