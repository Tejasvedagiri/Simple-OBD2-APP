var express = require('express');
var app =express();
var server = (require)('http').createServer(app)
var io = require('socket.io').listen(server)
var sql_query = ""
var Name= [];
var Speed = [];
var RPM = [];
var V_N = [];
var count="";
var mysql = require('mysql');
var connection = mysql.createConnection({
	host:'localhost',
	user: 'root',
	password: '',
	database: 'sampleDB'
});

connection.connect(function(error){
	if(!!error){
		console.log('Could not connect to DATABASE');
         process.exit(1);
		}
	else {
		console.log('connected');
	}
});
users= [];
connections = [];

server.listen(321);
console.log('Server Running .... ');

app.get('/',function(req,res){
        res.sendFile(__dirname +'/index.html')
        });

//Counting Drivers
sql_query = "select count(*) as cnt from main";
connection.query(sql_query,function(error,result){
         var row=result[0].cnt;
             count=row;
        if(!!error){
         console.log('Error at Updating server');
                     process.exit(1);
                    }
                else {
                        console.log(count);

                    }
                });



io.sockets.on('connection', function(socket){
        connections.push(socket);

        console.log('connected : %s sockets connected', connections.length);
        
        
         socket.on('send message',function(data){
            sql_query = "select * from main"
         connection.query(sql_query,function(error,rows,fields){
         if(!!error){
         console.log('Error at Updating server');
                    }
                else {
                        for(var i =0 ; i<count ; i++)
                        {
                                Name[i]=rows[i].Name;
                                Speed[i]=rows[i].Speed;
                                RPM[i]=rows[i].RPM;
                                V_N[i]=rows[i].Vehicle_No;                            
                        }   
                    }
                });
         io.sockets.emit('new message',Name,Speed,RPM,V_N,count);
         });
         socket.on('disconect',function(data){
        connections.splice(connection.indexOf(socket),1);
        console.log('Disconnected : %s sockets connected');
         });
         
        //Disconect

  });

            
