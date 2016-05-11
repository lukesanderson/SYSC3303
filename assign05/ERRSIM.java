package project;

/*
 * ERRSIM class for Iteration 1
 * Team 5 - 5000000
 * @author: team 5
 */
// This class is the error simulator for a simple TFTP server 
// based on UDP/IP. The simulator receives a read or write packet from a client and
// passes it on to the server.  Upon receiving a response, it passes it on to the 
// client.
// One socket (23) is used to receive from the client, and another to send/receive
// from the server.  A new socket is used for each communication back to the client. 
 */
import java.io.*;
import java.net.*;

public class ERRSIM {
	
	// UDP datagram packets and sockets used to send / receive
    private DatagramPacket sendPacket, receivePacket;
    private DatagramSocket receiveSocket, sendReceiveSocket;
    private  byte[] data; 

    public ERRSIM()
    {

        try {
        	// Construct a datagram socket and bind it to port 23
            // on the local host machine. This socket will be used to
            // receive UDP Datagram packets from clients.
            receiveSocket = new DatagramSocket(23);
            // Construct a datagram socket and bind it to any available
            // port on the local host machine. This socket will be used to
            // send and receive UDP Datagram packets from the server.

            sendReceiveSocket = new DatagramSocket();
        } catch (SocketException se) {
            se.printStackTrace();
            System.exit(1);
        }
    }

    public void ForwardPacket()
    {
       
        int clientPort, j=0;
        for(;;) { // loop forever
        	
            // Construct a DatagramPacket for receiving packets up
            // to 100 bytes long (the length of the byte array).
            data = new byte[100];
            receivePacket = new DatagramPacket(data, data.length);

            System.out.println("Simulator: Waiting for packet.");
            // Block until a datagram packet is received from receiveSocket.

            try {
                receiveSocket.receive(receivePacket);
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(1);
            }
            // Process the received datagram.
            System.out.println("Simulator: Packet received:");
            System.out.println("From host: " + receivePacket.getAddress());
            clientPort = receivePacket.getPort();
            System.out.println("Host port: " + clientPort);
            System.out.println("Length: " + receivePacket.getLength());
            System.out.println("Containing: " );

            // print the bytes
            for (j=0;j<receivePacket.getLength();j++) {
                System.out.println("byte " + j + " " + data[j]);
            }
            // Form a String from the byte array, and print the string.
            String received = new String(data,0,receivePacket.getLength());
            System.out.println(received);
            
            //mulithread
            Runnable serverConnection = new serverConnection(sendPacket, receivePacket, sendReceiveSocket,data);
            new Thread(serverConnection).start();

        } 

    }

    public static void main( String args[] )
    {
        ERRSIM s = new ERRSIM();
        s.ForwardPacket();

    }
} 


class serverConnection implements Runnable
{
    private DatagramPacket receivePacket;
    private DatagramPacket sendPacket;
    private DatagramSocket sendReceiveSocket;
    private DatagramSocket sendSocket;
    private byte[] data; 

    public serverConnection (DatagramPacket sendPacket, DatagramPacket recievePacket, DatagramSocket sendReceiveSocket, byte[] Data)
    {
        this.sendReceiveSocket = sendReceiveSocket;
        this.sendPacket = sendPacket;
        this.receivePacket = recievePacket;
        this.data = Data;
    }

    public void run()
    {
        int  j=0;
      //  byte[] data = new byte[100];

        sendPacket = new DatagramPacket(data, receivePacket.getLength(),
            receivePacket.getAddress(), 69);

        System.out.println("Simulator: sending packet.");
        System.out.println("To host: " + sendPacket.getAddress());
        System.out.println("Destination host port: " + sendPacket.getPort());
        System.out.println("Length: " + sendPacket.getLength());
        System.out.println("Containing: ");
        for (j=0;j<sendPacket.getLength();j++) {
            System.out.println("byte " + j + " " + data[j]);
        }
        
        // Construct a datagram packet that is to be sent to a specified port
        // on a specified host.
        // The arguments are:
        //  data - the packet data (a byte array). This is the response.
        //  receivePacket.getLength() - the length of the packet data.
        //     This is the length of the msg we just created.
        //  receivePacket.getAddress() - the Internet address of the
        //     destination host. Since we want to send a packet back to the
        //     client, we extract the address of the machine where the
        //     client is running from the datagram that was sent to us by
        //     the client.
        //  receivePacket.getPort() - the destination port number on the
        //     destination host where the client is running. The client
        //     sends and receives datagrams through the same socket/port,
        //     so we extract the port that the client used to send us the
        //     datagram, and use that as the destination port for the TFTP
        //     packet.

        try {
            // Construct a new datagram socket and bind it to any port
            // on the local host machine. This socket will be used to
            // send UDP Datagram packets.

            sendReceiveSocket.send(sendPacket);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

        System.out.println("Simulator: Packet received:");
        System.out.println("From host: " + receivePacket.getAddress());
        System.out.println("Host port: " + receivePacket.getPort());
        System.out.println("Length: " + receivePacket.getLength());
        System.out.println("Containing: ");
        
        // print the bytes
        for (j=0;j<receivePacket.getLength();j++) {
            System.out.println("byte " + j + " " + data[j]);
        }
        
        sendPacket = new DatagramPacket(data, receivePacket.getLength(),
            receivePacket.getAddress(), receivePacket.getPort());
        
        System.out.println( "Simulator: Sending packet:");
        System.out.println("To host: " + sendPacket.getAddress());
        System.out.println("Destination host port: " + sendPacket.getPort());
        System.out.println("Length: " + sendPacket.getLength());
        System.out.println("Containing: ");
        
        // print the bytes
        for (j=0;j<sendPacket.getLength();j++) {
            System.out.println("byte " + j + " " + data[j]);
        }
        
        // Send the datagram packet to the client via a new socket.


        try {
            sendSocket = new DatagramSocket();
        } catch (SocketException se) {
            se.printStackTrace();
            System.exit(1);
        }

        try {
            sendSocket.send(sendPacket);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
        
        //output
        System.out.println("Simulator: packet sent using port " + sendSocket.getLocalPort());
        System.out.println();
        
        // We're finished with this socket, so close it.
        sendSocket.close();
    }// end of loop

}
