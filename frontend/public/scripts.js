async function fetchDataFromAPI(url, options) {
  try {
    const response = await fetch(url, options);
    return await response.json();
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
  conversationToken = localStorage.getItem('conversationToken');
  if (!conversationToken)
  {
    fetchDataFromAPI(postConversationsURL, postConversationsOptions).then((result) => {
      conversationToken = result.conversationToken
      localStorage.setItem('conversationToken', conversationToken)
    })
  } else {
    // for testing purposes only
    const messages = [{
        id: "1",
        sender: "user",
        timestamp: "2024-04-14T10:00:00",
        conversation: "123456",
        text: "This is an examplary user message. The conversation token could not be retrieved from the backend."
      },
      {
        id: "2",
        sender: "AI",
        timestamp: "2024-04-14T10:01:00",
        conversation: "123456",
        text: "This is an examplary chatbot message. Please make sure the backend service is available and then reload the page."
      },
    ];
      
    var chatMessages = document.getElementById('chat-messages');
    messages.forEach(message => {
      const messageElement = document.createElement('div');
      messageElement.textContent = `${message.sender}: ${message.text}`;
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

function sendMessage() {
  let messageInput = document.querySelector('.chat-box input[type="text"]');
  let message = messageInput.value;
  let chatMessages = document.getElementById('chat-messages');
  let newMessage = document.createElement('div');
  newMessage.className = 'message user-message';
  newMessage.textContent = message;
  chatMessages.appendChild(newMessage);
  messageInput.value = '';
  postMessage(conversationToken, message)
}

function addToConversation(answer) {
  let chatMessages = document.getElementById('chat-messages');
  let aiResponseDiv = document.createElement('div');
  aiResponseDiv.className = 'message ai-message';
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
  document.querySelector('.chat-box button').addEventListener('click', function() {
    sendMessage(conversationToken);
  });
  
  document.querySelector('.chat-box input[type="text"]').addEventListener('keypress', function(e) {
    if (e.key === 'Enter') {
      sendMessage();
    }
  });
}
