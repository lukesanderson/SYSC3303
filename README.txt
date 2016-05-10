# SYSC3303
SYSC 3303 Term Project


*************************************************************
* 	Team 5						    * 
*	Authors: Adnan Hajar, Benjamin Tobalt,              *
*          Luke Sanderson , Owen Lee,                       * 
*          Xia Qiyang                                       * 
*    Date: May 9 2016                                       * 
*    Iteration: 1                                           *
*                                                           *
*                Sysc 3303 term Project                     *
*************************************************************

Description:
--------------
This is the explanation on how to properly run the Client/Server system. 

Steps to run program:
-----------------
 1) Run ERRSIM.java
 2) Run Server.java
 3) Run TFTPClient.java
 
 4) On the client, use the console user interface to send requests
 5) Verbose/Quiet**
 6) How to perform WRQ + RRQ ** //where the client will save the file


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



NOTES TO COMPLETE:
-Mention directory in server side.
-specify where client stores and receives
-size of files accepted
-test bytes
