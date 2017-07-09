var express = require('express');
var app =express();
var server = (require)('http').createServer(app)
var io = require('socket.io').listen(server)
var sql_query = ""
var mysql = require('mysql');
var connection = mysql.createConnection({
	host:'localhost',
	user: 'root',
	password: '',
	database: 'sampleDB'
});

connection.connect(function(error){
	if(!!error){
		console.log('Could Not connect to database');
		process.exit(1);
		}
	else {
		console.log('connected');
	}
});
users= [];
connections = [];

server.listen(process.env.PORT || 3000);
console.log('Server Running .... ');



app.get('/',function(req,res){
        res.sendFile(__dirname +'/index1.html')
        });

io.sockets.on('connection', function(socket){
        connections.push(socket);
        console.log('connected : %s sockets connected', connections.length);
        
        //Disconect
		socket.on('disconnect',function(data){
			connections.splice(connections.indexOf(socket),1);
		console.log('Disconnected : %s sockets connected', connections.length);			
		});
        
        
        //Send Message
        socket.on('send message',function(SPEED,RPM,VIN){
            console.log(SPEED,RPM,VIN);
                sql_query = "update main set speed = '" + SPEED + "', RPM = '" + RPM + "' where VIN = '" + VIN + "' "
                connection.query(sql_query,function(error,result){
                if(!!error){
                    console.log('Error at Updating server');
                    process.exit(1);
                }
        });
                });
            
            

        });