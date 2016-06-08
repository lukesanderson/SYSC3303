# SYSC3303
SYSC 3303 Term Project


*************************************************************
* 	Team 5						    * 
*	Authors: Adnan Hajar, Benjamin Tobalt,              *
*          Luke Sanderson , Owen Lee,                       * 
*          Xia Qiyang                                       * 
*    Date: May 10 2016                                      * 
*    Iteration: 1                                           *
*                                                           *
*                Sysc 3303 term Project                     *
*************************************************************

Description:
--------------
This project is a file transfer system based on the TFTP specification (RFC 1350). The
current iteration (1) implements a file transfer between a TFTP client and server, and passes through a error simulator.
The error simulator currently produces no errors, and only passes data between the client and server.
Below is the explanation on how to properly run the Client/Server system. For the purposes of this project, 
the mode has been permanently assigned to "netascii", and will not be prompted for by the user.


NOTES:
-Sockets do not have timeouts implemented yet. A timeout on client could be implemented, but a timeout on
port 69 must be implemented. 
-Modifications to the file selection can be made so it searches in the current directory, instead of typing the full file path.
-A verbose/quiet mode still needs to be implemented.
-Proper directories for the server and client need to be made.



Steps to run program:
-----------------
 1) Run ERRSIM.java
 2) Run Server.java
 3) Run TFTPClient.java
 4) On the client, use the console user interface to send requests
 5) When prompted on the client by the console,
 	typing "R" will select a read request
 	typing "W" will select a write request
 	typing "Q" will quit the client, but will not affect the server
 	typing the file name will select the file to transfer
 6) When selecting a file, a full file name must be inputted or else it will not correctly find the file
 		e.g. "M:\main.c" will be accepted. "README.TXT" will not. A directory must be fully specified.
 7) On the server, typing "Q" will attempt to gracefully shutdown the file transfer, letting current transfers to be completed, while preventing new transfers, after which it will shutdown
 
 Directory and Files sizes:
 -------------------------
 1) The client stores files and receives files in the current project workspace, so wherever the files are running from.
 2) The server directory also is the current project workspace, future modifications will be made so that the client and server do not share the same directory.
 3) The max amount of bytes per file accepted is 65535 bytes, but the block will only hold 512 bytes in each packet. 
 4) The min amount of bytes per files is anything greater than 0 because the file must exist, however the server can send back 0 bytes if a multiple of 512 and the transfer has been completed.
 
 Bytes to be tested:
 	0 (tested)
 	1 (tested)
 	512 
 	513
 	511
 	1024
 	
 UML:
 ------------
 Five UML diagrams have been included with this project. One being the class diagram of the current system, and the other
 four being WRQ/RRQ steady state and initization UCM diagrams. In future iterations the UCM diagrams for the WRQ and RRQ can
 be combined into one diagram each, with the alternate flows being demonstrated with a different colour/style lines.

Server.java
----------------
	Server that waits for requests from clients and handles them.
	Creates a new response socket for each request from clients. Sends
  	a response code for write and read requests if they are valid.

TFTPClient.java
-----------------
    	Client that connects to the intermediate host and sends packets over to server.
    	The Client establishes WRQ connections and RRQ connections with the server and
    	provides a steady-state file transfer.
	
ERRSIM.java
-----------------
	Intermediate host that connects to client and passes on packets (client to server, and server to
	client).

Breakdown:
---------------
Adnan Hajar: Coding/Testing
Xia Qiyang: Coding/Testing
Benjamin Tobalt: Coding/Testing
Luke Sanderson: UML/README
Owen Lee: UML/README


References:
THE TFTP PROTOCOL (REVISION 2): https://tools.ietf.org/html/rfc1350
