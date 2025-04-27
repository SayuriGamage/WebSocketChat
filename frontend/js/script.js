var socket = new WebSocket("ws://localhost:8080/ws/chat");

socket.onmessage = function(event) {
    var msg = JSON.parse(event.data);
    $('#chat-box').append('<div><b>' + msg.sender + ':</b> ' + msg.content + '</div>');
    $('#chat-box').scrollTop($('#chat-box')[0].scrollHeight);
};

$('#send').click(function() {
    var sender = $('#sender').val();
    var message = $('#message').val();

    if (sender.trim() === "" || message.trim() === "") {
        alert("Name and message cannot be empty!");
        return;
    }

    var chatMessage = {
        sender: sender,
        content: message
    };

    socket.send(JSON.stringify(chatMessage));
    $('#message').val('');
});

$('#message').keypress(function(e) {
    if (e.which == 13) {
        $('#send').click();
    }
});
