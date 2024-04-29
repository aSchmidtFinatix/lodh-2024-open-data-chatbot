var express = require('express');

var app = express();

app.use(express.static(__dirname + '/public'));

var port = 3000

app.listen(port);
console.log('Server is running and listening on port ' + port);
