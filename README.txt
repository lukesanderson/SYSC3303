# SYSC3303
SYSC 3303 Term Project


*************************************************************
* 	Team 5						    * 
*	Authors: Adnan Hajar, Benjamin Tobalt,              *
*          Luke Sanderson , Owen Lee,                       * 
*          Xia Qiyang                                       * 
*    Date: May 31 2016                                      * 
*    Iteration: 4                                           *
*                                                           *
*                Sysc 3303 term Project                     *
*************************************************************

Description:
--------------
This project is a file transfer system based on the TFTP specification (RFC 1350). The
current iteration (4) implements a file transfer between a TFTP client and server, and passes through a error simulator.
The error simulator currently produces the following errors:
	
	- Duplicating Packets
	- Lost Packets
	- Delayed Packets
	- Corrupting the file (Error code 4), however improvement wish to be made.
	- Sending packets from an invalid address (Error Code 5), currently simulated from a different port.

Error Packets currently that may be produced are: Error codes 1,2,3,4,5,6.

Below is the explanation on how to properly run the Client/ErrSim/Server system. For the purposes of this project, 
the mode has been permanently assigned to "netascii", and will not be prompted for by the user.


NOTES:
--------
-For iteration 4, both the client and server produce the correct error packets for error codes 1,2,3,6. 

-Currently we have a timeout of 2.5 seconds (2,500 cycles) on both the server and client.

-Quiet and verbose have been semi-implemented, however they have not been a priority for this iteration due to the amount of testing we have needed to do.

-Error simulator is currently multithreaded, however this was a personal choice, and not required.

-For the purposes of this project, the mode has been permanently assigned to "netascii", and will not be prompted for by the user.

-We have also managed to send any text or image file that is less than 1 mB (our selected max file size), and have not ran into any other problems sending a different type of file so far.

-test.txt is on the client side to test, and test2.txt is on the server side to test.



Steps to run program:
-----------------
 1) Import the java project TFTP_SYSC3303_Team5

 2) In eclipse, right click Server.java --> select run as --> java application.

 3) In eclipse, right click ErrorInit.java --> select run as --> java application.
			
 4) In eclipse, right click Client.java --> select run as --> java application.

 5) After you run each application, a user interface will prompt the user for setup.

 6) To view the specific user input needed for each .java file listed above, we have written out a guide in the "CONSOLE COMMANDS" section below.
 	
 
Directory and Files sizes:
-------------------------
 1) The client directory where it stores and receives files is located in the project's "TFTP"package folder, wherever you have chosen to save.
 2) The server directory where it stores and receives files is located in the project's "server" package folder, wherever it is located when you choose to run the project.
 3) The max amount of bytes per file transfer is 1 mB. This value is has been chosen by the authors for the purpose of this assignment. 
 4) The min amount of bytes per files is any file that exists in the respective directory.


 
TESTING:
----------------
To test iteration 4, we needed to test a disk full on both the server and client directory, as well as file not found on both the server and client.
To test these cases:
		Disk Full on Client: We set the directory [(CLIENT_DIRECTORY) located as a String in Client.java] to a filepath of a full folder on a USB drive. Then attempt a read request of a file on the server.
		Disk Full on server: We set the directory [(SERVER_DIRECTORY) located as a String in RequestHandler.java] to a filepath of a full folder on a USB drive. Then attempt a write request of a file to the server.
		File not found on Client: Attempt to select a file that is not located in the TFTP package folder.
		File not found on server: Attempt to read a file that is not located in the Server package folder.
		File already exists: One way to test this, is to perform a write request, then attempt to perform another write request, which will throw the file already exists error.


Files tested:
	We have tested these type of files:
		-A 0 byte txt: Since it exists, it will transfer properly
		-A 512 byte file: Sends properly, and a 0 byte file after to show the end of transfer.
		-A 600 kB jpeg to test sending images over: Worked perfectly.
		-Other files include any size of file below 1 mB.
	We have include test.txt on the client and test2.txt on the server to test with.
 	
UML:
--------------
	 UML diagrams have been included with this iteration. The class diagram of the current system,
	 the rest being different Timing diagrams showing the error scenarios for this iteration (1, 2, 3, 6).

Server.java
----------------
	Server that waits for requests from clients and handles them.
	Creates a new response socket for each request from clients. Sends
  	a response code for write and read requests if they are valid.
	The server will not shutdown after a transfer, only after entering Q on the server console, or by manually terminating.

Client.java
-----------------
    	Client that connects to the intermediate host and sends packets over to server.
    	The Client establishes WRQ connections and RRQ connections with the server and
    	provides a steady-state file transfer.
	
ErrSim.java
-----------------
	Intermediate host that connects to client and creates error cases for a duplicated packet, lost pacet, delayed packet, invalid TFTP opcode and unknown transfer.
	The error simulator can also run in normal mode which just sends files from the client to the server. 
	

***************************************************************************************************************
***************************************************************************************************************
CONSOLE COMMANDS:
------------------

On the server
		V: to select verbose mode or 
		Q: to select quiet mode
		After, pressing Q again will shutdown the server.
		
On the client
		"N" for normal mode, or "T" for testing mode which is including the error simulator.
	then 		
 		typing "R" will select a read request
 		typing "W" will select a write request
 		typing "Q" will quit the client, but will not affect the server
	then
 		typing the file name e.g test.txt (must be in the directory) will select the file if the size permits, and it exists.
 
On the Error Simulator

 	When prompted on the Error Simulator console, 
	-To enter ACK or DATA :

		Typing (A) will select the ACK Packet to change
		Typing (D) will select the DATA Packet to change
	
	-To enter on which block you want to create the error on:
	
		Type the block number you wish to perform the ERROR on,
		once asked to do so. It must be a valid integer.

	-When prompted to enter lose, delay, normal or duplicate: 
		
		Typing 00 will enter normal mode
		Typing 01 will loose a packet  
		Typing 02 will duplicate a packet
		Typing 03 will delay a packet
		Typing 04 will create invalid TFTP opcode
		Typing 05 will create an unknown transfer

		Typing in invalid input will result in normal mode entering


***************************************************************************************************************
***************************************************************************************************************


Breakdown for iteration 4:
---------------
Adnan Hajar: Coding/Testing
Xia Qiyang: UML diagrams
Benjamin Tobalt: Coding/Testing
Luke Sanderson: Coding/Testing/README
Owen Lee: Coding/Testing


References:
THE TFTP PROTOCOL (REVISION 2): https://tools.ietf.org/html/rfc1350\


