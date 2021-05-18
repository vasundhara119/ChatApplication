"use strict";

var userForms = document.querySelector(".user-forms");
var usernamePage = document.querySelector("#username-page");
var chatPage = document.querySelector("#chat-page");
var chatContainer = document.querySelector(".chat-container");
var createRoomForm = document.querySelector("#createRoomForm");
var joinRoomForm = document.querySelector("#joinRoomForm");
var messageForm = document.querySelector("#messageForm");
var messageInput = document.querySelector("#message");
var messageArea = document.querySelector("#messageArea");
var groupchat = document.querySelector("#group-chat");
var onechat = document.querySelector("#one-chat");
var connectingElement = document.querySelector(".connecting");

var stompClient = null;
var username = null;
let roomId = null;
var capacity = 2;

var colors = [
    '#2196F3', '#32c787', '#00BCD4', '#ff5652',
    '#ffc107', '#ff85af', '#FF9800', '#39bbb0'
];

function randomNumberGenerator(min, max) {
    return Math.floor((Math.random() * max) + min);
}

function validation(user) {
    var user = document.getElementById("name-to-create").value;
    var numbers = /^[0-9]+$/;

    if (user == "") {
        alert(" Username Cannot be empty");
        return false;
    } else if (user.match(numbers)) {
        alert("Username of String  only");
        return false;
    }
    return true;
}

function evaluation(username, roomId) {
    var user = document.getElementById("name-to-join").value;
    var roomId = document.getElementById("roomid").value;
    var numbers = /^[0-9]+$/;
    if ((user == "" && roomId == "")) {
        alert(" User and RoomId Required ,Cannot be empty");
        return false;
    } else if (user == "") {
        alert(" UserName Required ,Cannot be empty");
        return false;
    } else if (roomId == "") {
        alert(" roomId Required ,Cannot be empty");
        return false;
    } else if (user.match(numbers)) {
        alert("Username in Strings only");
        return false;
    }
    return true;
}

function groupOrOneCheck() {
    if (groupchat.checked) {
        document.getElementById('check').style.visibility = 'visible';
        document.getElementById("room-capacity").value = 5;
        document.getElementById("room-capacity").disabled = false;
        document.getElementById('check').style.visibility = 'visible';
    } else {
        document.getElementById('check').style.visibility = 'visible';
        document.getElementById("room-capacity").value = 2;
        document.getElementById("room-capacity").disabled = true;
    }
}

async function createRoom(event) {
    event.preventDefault();
    username = document.querySelector("#name-to-create").value.trim();
    capacity = document.querySelector("#room-capacity").value.trim();
    let varRoomIdAlreadyExists;
    if ((validation(username))) {
        do {
            roomId = randomNumberGenerator(101, 999);
            varRoomIdAlreadyExists = await roomIdAlreadyExists(roomId);
        } while (varRoomIdAlreadyExists);
        connect();
    }
}

function joinRoom(event) {
    event.preventDefault();
    username = document.querySelector("#name-to-join").value.trim();
    roomId = document.querySelector("#roomid").value.trim();
    if (evaluation(username, roomId)) {
        if (roomIdAlreadyExists(roomId)) {
            connect();
        } else {
            if (roomIdAlreadyExists(roomId) == false) {
                alert("Room Id " + roomId + " does not exists");
            } else if (roomIsNotFull(roomId) == false) {
                alert("This Room Capacity Is Full");
            }
        }
    }
}

function connect(event) {

    userForms.classList.add("hidden");
    chatPage.classList.remove("hidden");

    if (username) {
        document.querySelector("#chatpage-roomid").innerHTML = roomId;

        usernamePage.classList.add("hidden");
        chatPage.classList.remove("hidden");

        var socket = new SockJS("/ws");
        stompClient = Stomp.over(socket);

        stompClient.connect({}, onConnected, onError);
    }
}

function onConnected() {
    //subscribe to the public topic
    stompClient.subscribe("/topic/" + roomId, onMessageReceived);
    stompClient.subscribe("/topic/" + username, onInfoReceived)

    //Tell your name to the server
    stompClient.send("/app/chat.addUser", {},
        JSON.stringify({ sender: username, type: "JOIN", roomId: roomId, roomCapacity: capacity })
    )
    connectingElement.classList.add("hidden");
}

function onError() {
    connectingElement.textContent = 'Could not connect to WebSocket server. Please refresh this page to try again!';
    connectingElement.style.color = 'red';
}

function sendMessage(event) {
    var messageContent = messageInput.value.trim();
    if (messageContent && stompClient) {
        var chatMessage = {
            sender: username,
            content: messageInput.value,
            type: 'CHAT',
            roomId: roomId
        };
        stompClient.send("/app/chat.sendMessage/" + roomId, {}, JSON.stringify(chatMessage));
        messageInput.value = "";
    }
    event.preventDefault();
}

function onMessageReceived(payload) {
    var message = JSON.parse(payload.body);
    var messageElement = document.createElement('li');

    if (message.type === 'JOIN') {
        messageElement.classList.add('event-message');
        message.content = message.sender + ' joined!';
    } else if (message.type === 'LEAVE') {
        messageElement.classList.add('event-message');
        message.content = message.sender + ' left!';
        afterUserLeft(message.roomId);
    } else {
        messageElement.classList.add('chat-message');

        if (message.sender != username) {
            var avatarElement = document.createElement('i');
            var avatarText = document.createTextNode(message.sender[0]);
            avatarElement.appendChild(avatarText);
            avatarElement.style['background-color'] = getAvatarColor(message.sender);

            messageElement.appendChild(avatarElement);
        }
        var usernameElement = document.createElement('span');
        if (message.sender == username) {

            messageElement.classList.add('right-aligned');

            var usernameText = document.createTextNode("You");

        } else {
            var usernameText = document.createTextNode(message.sender);
        }

        usernameElement.appendChild(usernameText);
        messageElement.appendChild(usernameElement);
    }

    var textElement = document.createElement('p');
    var messageText = document.createTextNode(message.content);
    textElement.appendChild(messageText);

    messageElement.appendChild(textElement);

    messageArea.appendChild(messageElement);
    // messageAreaDiv = document.querySelector(".messageArea-div");
    // messageAreaDiv.scrollTop = messageAreaDiv.scrollHeight;
    chatContainer.scrollTop = chatContainer.scrollHeight;
}

function onInfoReceived(payload) {
    var info = JSON.parse(payload.body);
    console.log(info.content);
}

function getAvatarColor(messageSender) {
    var hash = 0;
    for (var i = 0; i < messageSender.length; i++) {
        hash = 31 * hash + messageSender.charCodeAt(i);
    }
    var index = Math.abs(hash % colors.length);
    return colors[index];
}

function roomIdAlreadyExists(roomId) {
    var outsideVar;
    $.ajax({
        url: "/roomid-exists?room-id=" + roomId,
        type: "get",
        async: false,
        success: function(alreadyExists) {
            outsideVar = alreadyExists
        },
        error: function() {
            console.log("Error occured in ajax call to roomid-exists");
        }
    });
    return outsideVar;
}

function roomIsNotFull(roomId) {
    var outsideVar;
    $.ajax({
        url: "/roomid-capacity-check?room-id=" + roomId,
        type: "get",
        async: false,
        success: function(isFull) {
            outsideVar = isFull;
        },
        error: function() {
            console.log("Error occured in ajax call to roomid-capacity-check");
        }
    });
    return outsideVar;
}

var loaded = false;

function afterUserLeft(roomId) {

    jQuery("#loader").show();
    var outsideVar;
    $.ajax({
        url: "/roomid-capacity-decrease?room-id=" + roomId,
        type: "get",
        async: false,
        success: function(abc) {
            console.log("User Left");
        },
        error: function() {
            console.log("Error occured in ajax call to roomid-capacity-decrease");
        }
    });

    return false;
}

createRoomForm.addEventListener("submit", createRoom, true);
joinRoomForm.addEventListener("submit", joinRoom, true);
messageForm.addEventListener("submit", sendMessage, true);