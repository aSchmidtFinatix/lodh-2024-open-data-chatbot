async function fetchDataFromAPI(url, options) {
    try {
        increaseLoadingCounter();
        const response = await fetch(url, options);
        const json = await response.json();
        decreaseLoadingCounter();
        return json;
        //console.log(result.conversationToken);
        //console.log(response.json());
    } catch (error) {
        console.error(error);
        decreaseLoadingCounter();
    }
}

let postConversationsURL = 'http://localhost:8080/api/conversations';
let postConversationsOptions = {
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
let conversationToken
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

        let chatMessages = document.getElementById('chat-messages');
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
        addToConversation(answer);
    });
    return result
}

// Function to send message
function sendMessage() {
    let messageInput = document.querySelector('.chat-box input[type="text"]');
    let message = messageInput.value;
    let chatMessages = document.getElementById('chat-messages');
    let newMessage = document.createElement('div');
    newMessage.className = 'message user-message'; // User message
    newMessage.textContent = message;
    chatMessages.appendChild(newMessage);
    messageInput.value = ''; // Clear input field after sending message
    postMessage(conversationToken, message)
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

let _loadCounter = 0;
function increaseLoadingCounter() {
    _loadCounter++;
    checkLoadingSpinner();
}

function decreaseLoadingCounter() {
    _loadCounter--;
    checkLoadingSpinner();
}

function checkLoadingSpinner() {
    const spinner = document.querySelector('.loading-spinner');
    if (!spinner) { return; }

    spinner.setAttribute('data-counter', `${_loadCounter}`);
    if (_loadCounter > 0) {
        spinner.classList.add('active');
    } else {
        spinner.classList.remove('active');
    }
}

function addEventListeners() {
    // Event listener for mobile aside toggle
    document.querySelector('.mobile-toggle').addEventListener('click', () => {
        document.querySelector('.chat-aside').classList.toggle('visible');
    });

    // Event listener for send button click
    document.querySelector('.chat-box button').addEventListener('click', () => {
        sendMessage(conversationToken);
    });

    // Event listener for Enter key press
    document.querySelector('.chat-box input[type="text"]').addEventListener('keypress', e => {
        if (e.key === 'Enter') {
            sendMessage();
        }
    });
}
