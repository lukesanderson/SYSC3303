package testing;
import java.io.*;
import java.net.*;
import java.util.*;

public class Server {
	
// types of requests we can receive
public static enum Request { READ, WRITE, ERROR};
// responses for valid requests
public static final byte[] readResp = {0, 3, 0, 1};
public static final byte[] writeResp = {0, 4, 0, 0};
public static final byte[] invalidResp = {0, 5};
public static int off=0;


// UDP datagram packets and sockets used to send / receive
private DatagramPacket  receivePacket;
private DatagramSocket receiveSocket;

public Server()
{
   try {
   //Construct a datagram socket and bind it to port 69 on the local host machine. 
  //This socket will be used to receive UDP Datagram packets.
      receiveSocket = new DatagramSocket(69);
   } catch (SocketException se) {
      se.printStackTrace();
      System.exit(1);
   }
}

public void receiveAndSendTFTP() throws FileNotFoundException, IOException
{
   byte[] data,
          response = new byte[4];
         
   Request req; // READ, WRITE or ERROR
   
   
   byte CTid = 0;
   int len, j=0, k=0;
   
   
   for(;;) { // loop forever
    // Construct a DatagramPacket for receiving packets up to 
    //100 bytes long (the length of the byte array).
	  
      data = new byte[512];
      receivePacket = new DatagramPacket(data, data.length);
      System.out.println("Server: Waiting for packet.");
      
      // Block until a datagram packet is received from receiveSocket.
      try {
         receiveSocket.receive(receivePacket);
      } catch (IOException e) {
         e.printStackTrace();
         System.exit(1);
      }

      // Process the received datagram.
      System.out.println("Server: Packet received:");
      System.out.println("From host: " + receivePacket.getAddress());
      System.out.println("Host port: " + receivePacket.getPort());
      System.out.println("Length: " + receivePacket.getLength());

      
      
      // Get a reference to the data inside the received datagram.
      data = receivePacket.getData();
      System.out.println("data:" + data);
      

	  
      // If it's a read, send back DATA (03) block 1
      // If it's a write, send back ACK (04) block 0
      // Otherwise, ignore it
      if (data[0]!=0){ req = Request.ERROR; // bad
      response = invalidResp;
      
      }
      else if (data[1]==1){ req = Request.READ; // could be read
      response = readResp;
      
      }
      else if (data[1]==2) {req = Request.WRITE; // could be write
      response = writeResp;
      
      }
      else {req = Request.ERROR; // bad
      response = invalidResp;
      
      }
      
      len = receivePacket.getLength();
      
     /* if (req!=Request.ERROR) { // check for filename
          // search for next all 0 byte
          for(j=2;j<len;j++) {
              if (data[j] == 0) break;
         }*/
        

    
      
      /*
       * Thread CreateResponse = new ConnectManager(response, receivePacket.getAddress(),CTid,data);
	  CreateResponse.start();
      
       * 
       * 
       */
  
      
      
      if(response==writeResp) {

      	System.out.println( "Server: write responce starting");
         
    	  Thread CreateResponse = new ConnectManager(response, receivePacket.getAddress(),CTid,data,off);
    	  CreateResponse.start();
      } else if (response== readResp){

      	System.out.println( "Server: read responce starting");
         
    	  Thread CreateResponse = new ConnectManager(response, receivePacket.getAddress(),CTid,data,off);
    	  CreateResponse.start();
      } else {

      	System.out.println( "Server: error responce starting");
         
    	  Thread CreateResponse = new ConnectManager(response, receivePacket.getAddress(),CTid,data,off);
    	  CreateResponse.start();
      }
    
      } // end of if 
   }// end of loop


public static void main( String args[] ) throws FileNotFoundException, IOException
{
   Server c = new Server();
   c.receiveAndSendTFTP();
}
}


class ConnectManager extends Thread
{
	byte[] data, resp = new byte[5], sending, outdata;
    byte CTid=0;
    public static int off;
    
    
  //we need choose STid for this client;
    Random randomGenerator2 = new Random();
    int ServerTID = randomGenerator2.nextInt(127), blocknumber = 1;

    Request req; // READ, WRITE or ERROR
    byte STid = (byte) ServerTID;
    String filename, mode;
	// types of requests we can receive
	public static enum Request { READ, WRITE, ERROR};

	// UDP datagram packets and sockets used to send / receive
	private DatagramPacket sendPacket;// receivePacket;
	private DatagramSocket sendSocket; //dataSocket;

    public ConnectManager(byte[] response, InetAddress address, byte port,byte[] Data,int Off) {
    	this.CTid = port;
    	resp[0] = response[0];
    	resp[1] = response[1];
    	resp[response.length]= STid;
    	sendPacket = new DatagramPacket(resp, resp.length,
                address, port);
    	data = Data;
    	off =Off;
    			
    }

    public void run() {
   
    	System.out.println( "Server: Sending packet:");
        System.out.println("To host: " + sendPacket.getAddress());
        System.out.println("Destination host port: " + CTid);
        System.out.println("Length: " + sendPacket.getLength());


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

        System.out.println("Server: packet sent using port " + sendSocket.getLocalPort());
        

        // We're finished verification
        //we should start to wait data;
    /*    try {
        	      dataSocket = new DatagramSocket(STid);
        	   } catch (SocketException se) {
        	      se.printStackTrace();
        	      System.exit(1);
        	   }
        */
       	BufferedOutputStream out = null;
		try {
			out = new BufferedOutputStream(new FileOutputStream("tessts.txt"));
		} catch (FileNotFoundException e1) {

			e1.printStackTrace();
		}
      
		 System.out.println("position 1");
		
      
        	/*data = new byte[100];
            receivePacket = new DatagramPacket(data, data.length);
            System.out.println("waiting on a packet ...");*/
            
            // Block until a datagram packet is received from receiveSocket.
           /* try {
            	 System.out.println("position 2");
            	//dataSocket.send(receivePacket);
          
               dataSocket.receive(receivePacket);
                	 System.out.println("position 3");
            } catch (IOException e) {
               e.printStackTrace();
               System.exit(1);
            }*/
            outdata = new byte[512];
       //     data = receivePacket.getData();
            System.arraycopy(data,0,outdata,0,data.length);
           // int i =0 ;
         /*   for(;;){
            	i++;
            	 System.out.println("" + outdata[i]);
            	
            	
            	if(i== 10 ){break;}
            	
            }*/
            
            
         /*   try {
				out.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}*/
       
                
            try {
            	
            	out.write(outdata);
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
      
     
      
         	 System.out.println("last position");
            
       
         	 
            //server send back ACK packet for each block;
            resp[0] = 0x00;
        	resp[1] = 0x04;
        	resp[2] = 0x00;
        	resp[3] = (byte) blocknumber;
        	blocknumber++;
        	sendPacket = new DatagramPacket(resp, resp.length,sendPacket.getAddress(),CTid);
        			//receivePacket.getAddress(), CTid);   	

             try {
                sendSocket.send(sendPacket);
             } catch (IOException e) {
                e.printStackTrace();
                System.exit(1);
             }
        
        
        
        //exit thread
        //Thread Exit = new ExitThread();
        //Exit.start();
        
        
    }
}

class ExitThread extends Thread {
	
	String userInput, exitVerify = "q";
	
	public ExitThread() {
    }

    public void run() {
    	System.out.println("Type q to quit: ");
        BufferedReader ended = new BufferedReader(new InputStreamReader(System.in));
        try {
            userInput = ended.readLine();
          } catch (IOException e) {
              System.out.println("Error - to exit please type q and then press enter");
              System.exit(1);
          }
        
        if (userInput.equals(exitVerify))
        {   
        	System.out.println("Server terminating...");
            System.out.print("DONE");
        	System.exit(1);
        }
        
    }
	/*private shutdown() {
    for(Client client : clients){
        client.shutdown();
    }
	Server.shutdown();
	System.out.println("Server shutdown complete");
	System.out.println("Threads are closed. Closing the console in 5 seconds.");
	/* add a sleeping method();
	*/
}
