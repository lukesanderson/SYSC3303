# SYSC3303
SYSC 3303 Term Project


*************************************************************
* 	Team 5						    * 
*	Authors: Adnan Hajar, Benjamin Tobalt,              *
*          Luke Sanderson , Owen Lee,                       * 
*          Xia Qiyang                                       * 
*    Date: May 9 2016                                       * 
* Version: 1                                                *
*                                                           *
*                Sysc 3303 term Project                     *
*************************************************************

This is the explanation on how to properly run the Client/Server system. 

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
------------------
	 Intermediate host that connects to client and passes on packets (client to server, and server to
	 client).
	 
	

To Run Program
---------------
1) 


NOTES TO COMPLETE:
-Mention directory in server side.
-specify where client stores and receives
-size of files accepted
-test bytes
