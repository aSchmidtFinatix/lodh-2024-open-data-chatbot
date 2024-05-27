async function fetchDataFromAPI(url, options) {
    try {
        const response = await fetch(url, options);
        return await response.json();
        //console.log(result.conversationToken);
        //console.log(response.json());
    } catch (error) {
        console.error(error);
    }
}

var postConversationsURL = 'http://localhost:8080/api/conversations'
var postConversationsOptions = {
    method: 'POST',
    headers: {
        'Access-Allow-Origin': "*"
    }
}

/* get whole messages list to append it to the chat body */
async function getMessages (conversationToken) {
    let getMessagesURL = `https://raw.githubusercontent.com/api/conversations/${conversationToken}/messages`
    getMessagesOptions = {
        method: 'GET',
        headers: {
            'conversationToken': conversationToken
        }
    }
    result = fetchDataFromAPI(getMessagesURL, getMessagesOptions);
    return result;
}
var conversationToken
function establishConversation() {
    // if we don't have conversationToken, make a post and retrieve it. Also when we do have it already, load the list of messages.
    conversationToken = localStorage.getItem('conversationToken');
    if (true || !conversationToken)
    {

        fetchDataFromAPI(postConversationsURL, postConversationsOptions).then((result) => {
            conversationToken = result.conversationToken
            localStorage.setItem('conversationToken', conversationToken)
        })
    } else {
        //messages = getMessages(conversationToken);
        // Function to display messages in the chat interface

        /* for testing purposes only */
        const messages = [{
            id: "1",
            sender: "user",
            timestamp: "2024-04-14T10:00:00",
            conversation: "123456",
            text: "Hello, how can I help you?"
        },
            {
                id: "2",
                sender: "AI",
                timestamp: "2024-04-14T10:01:00",
                conversation: "123456",
                text: "Hello! I'm here to assist you."
            },
            // Add more messages here...
        ];

        var chatMessages = document.getElementById('chat-messages');
        messages.forEach(message => {
            const messageElement = document.createElement('div');
            messageElement.textContent = `${message.sender}: ${message.text}`;
            // Add CSS class based on the sender
            if (message.sender === 'user') {
                messageElement.classList.add('message', 'user-message');
            } else if (message.sender === 'AI') {
                messageElement.classList.add('message', 'ai-message');
            }
            chatMessages.appendChild(messageElement);
        });
    }
}


async function postMessage (conversationToken,message) {
    let postConversationsMessagesURL = `http://localhost:8080/api/conversations/${conversationToken}/messages`
    let postConversationsMessagesOptions = {
        method: 'POST',
        headers: {
            'content-type': 'application/json',
            'conversationToken': conversationToken,
        },
        body: JSON.stringify({
            'message': message
        })
    }
    let result
    fetchDataFromAPI(postConversationsMessagesURL, postConversationsMessagesOptions).then((response) => {
        let answer = response.response
        addMessageToChat(answer, "ai");
    });
    return result
}

// Function to send message
function sendMessage(event) {
    let messageInput = document.querySelector('#message-input');
    let message = messageInput.value;
    addMessageToChat(message, "user");
    messageInput.value = ''; // Clear input field after sending message
    postMessage(conversationToken, message)
}

function addMessageToChat(message, author) {
    let chatMessages = document.getElementById('chat-messages');
    let messageContainer = document.createElement('div');
    messageContainer.className = 'message-container ' + author + '-message';
    let authorImage = document.createElement("img");
    authorImage.className = 'avatar';
    authorImage.src = "../static/assets/oda_v2_sm.png";
    authorImage.width = "32";
    authorImage.height = "32";
    let messageContent = document.createElement('div');
    messageContent.className = 'message-content';
    let senderHeadline = document.createElement('span');
    senderHeadline.innerText = author === 'user' ? 'Du ' : 'ODA ';
    let timeHeadline = document.createElement('span');
    timeHeadline.innerText = new Date().toLocaleTimeString();
    let messageNode = document.createElement('p');
    messageNode.innerText = message;
    messageContent.appendChild(senderHeadline);
    messageContent.appendChild(timeHeadline);
    messageContent.appendChild(messageNode);
    messageContainer.appendChild(authorImage);
    messageContainer.appendChild(messageContent);
    chatMessages.appendChild(messageContainer);
}

function addToConversation(answer) {
    // Simulate AI response (replace with actual AI response)
    let chatMessages = document.getElementById('chat-messages');
    let aiResponseDiv = document.createElement('div');
    aiResponseDiv.className = 'message ai-message'; // AI message
    console.log(conversationToken);

    aiResponseDiv.textContent = answer;
    chatMessages.appendChild(aiResponseDiv);
}

waitForElements()
function waitForElements() {
    let chatboxButton = document.querySelector('.chat-box button');
    if(!chatboxButton) {
        setTimeout(waitForElements, 200);
    } else {
        initializeApp();
    }
}

function initializeApp() {
    establishConversation();
    addEventListeners();
}

function addEventListeners() {
    // Event listener for send button click
    document.querySelector('#send-message-button').addEventListener('click', function() {
        sendMessage(conversationToken);
    });

    // Event listener for Enter key press
    document.querySelector('.chat-box input[type="text"]').addEventListener('keypress', function(e) {
        if (e.key === 'Enter') {
            sendMessage();
        }
    });
}
