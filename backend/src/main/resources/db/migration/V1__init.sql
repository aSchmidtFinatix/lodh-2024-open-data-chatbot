CREATE TABLE conversation (
                        token UUID PRIMARY KEY
);
CREATE TABLE message (
                         id UUID PRIMARY KEY,
                         sender VARCHAR(255),
                         text TEXT,
                         timestamp VARCHAR(255),
                         conversation_token UUID,
                         FOREIGN KEY (conversation_token) REFERENCES conversation (token)
);
