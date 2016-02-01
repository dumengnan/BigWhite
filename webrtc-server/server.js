var static = require('node-static');
var http = require('http');
var file = new(static.Server)();

var app = http.createServer(function (req, res) {
  file.serve(req, res);
}).listen(3000);

var io = require('socket.io').listen(app);

io.sockets.on('connection', function (socket){
	
	console.log('connection','connection is start');
	//socket.emit('id',socket.id);

	socket.on('message', function (message){
		console.log('Got message:', message);
		message.from = message.to;
		socket.broadcast.to(message.to).emit('message',message);

	});

	socket.on('create or joined room', function (room) {
		var numClients = io.sockets.adapter.rooms[room] != undefined ? Object.keys(io.sockets.adapter.rooms[room]).length:0;
		
		console.log('Room ' + room + ' has ' + numClients + ' client(s)');

		if (numClients === 0){
		  io.sockets.in(room).emit('created',room);
		  socket.join(room);
		  socket.emit('created', room);
		} else if (numClients === 1) {
		  io.sockets.in(room).emit('join', room);
		  socket.join(room);
		  socket.emit('joined', room);
		} else { // max two clients
		  socket.emit('full', room);
		}
		socket.emit('emit(): client ' + socket.id + ' joined room ' + room);
		socket.broadcast.emit('broadcast(): client ' + socket.id + ' joined room ' + room);

  });

});