async function fetchDataFromAPI(url, options) {
  try {
    const response = await fetch(url, options);
    const result = await response.json();
    //console.log(result.conversationToken);
    //console.log(response.json());
  } catch (error) {
    console.error(error);
  }
}

var postConversationsURL = 'https://raw.githubusercontent.com/api/conversations'
var postConversationsOptions = {
  method: 'POST'/*,
  headers: {
    'content-type': 'application/x-www-form-urlencoded',
    'Accept-Encoding': 'application/gzip',
    'X-RapidAPI-Key': '2a628a2aa7msh21b2b2ef151e50bp1d9caejsn507946985984',
  }*/
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

function establishConversation() {
  // if we don't have conversationToken, make a post and retrieve it. Also when we do have it already, load the list of messages.
  let conversationToken = localStorage.getItem('conversationToken');
    if (conversationToken===null)
    {
      //result = fetchDataFromAPI(postConversationsURL, postConversationsOptions);
      //conversationToken = result.conversationToken;
      conversationToken = "3fa85f64-5717-4562-b3fc-2c963f66afa6"; //only for test
      localStorage.setItem('conversationToken', conversationToken)
      console.log(conversationToken); // only for test
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
  let postConversationsMessagesURL = `https://raw.githubusercontent.com/api/conversations/${conversationToken}/messages`
  let postConversationsMessagesOptions = {
    method: 'POST',
    headers: {
      'content-type': 'application/json',
      'conversationToken': conversationToken,
      'message': message
    }
  }
  let result = fetchDataFromAPI(postConversationsMessagesURL, postConversationsMessagesOptions);
  return(result);
}

// Function to send message
function sendMessage(conversationToken) {
  let messageInput = document.querySelector('.chat-box input[type="text"]');
  let message = messageInput.value;
  let chatMessages = document.getElementById('chat-messages');
  let newMessage = document.createElement('div');
  newMessage.className = 'message user-message'; // User message
  newMessage.textContent = message;
  chatMessages.appendChild(newMessage);
  messageInput.value = ''; // Clear input field after sending message

  // Simulate AI response (replace with actual AI response)
  let aiResponseDiv = document.createElement('div');
  aiResponseDiv.className = 'message ai-message'; // AI message
  console.log(conversationToken);
  let aiResponse = postMessage(conversationToken, message)
  aiResponseDiv.textContent = aiResponse;
  chatMessages.appendChild(aiResponseDiv);
}

function waitForElements() {
  let chatboxButton = document.querySelector('.chat-box button');
  if(!chatboxButton) {
    setTimeout(waitForElements, 200);
  } else {
    initializeApp();
  }
}

function initializeApp() {
  addEventListeners();
  establishConversation();
}

function addEventListeners() {
  // Event listener for send button click
  document.querySelector('.chat-box button').addEventListener('click', function() {
    sendMessage(conversationToken);
  });
  
  // Event listener for Enter key press
  document.querySelector('.chat-box input[type="text"]').addEventListener('keypress', function(e) {
    if (e.key === 'Enter') {
      sendMessage();
    }
  });
}
