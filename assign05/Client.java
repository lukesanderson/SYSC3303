package testing;

/* Client class for Iteration 1
* Team 5 - 5000000
* @author: team 5
*/

/* the following code deals with the client part of this exercise.
*in the following exercise the client is send a Read Write or Test message to the Errsim which then be sended to the server
* further explanation about how the connection between the errSim and the server will is explained in the two other classes.*/


import java.io.*;
import java.net.*;
import java.util.Scanner;
import java.util.Arrays;

public class Client {
   DatagramPacket sendPacket, receivePacket;		//creat two DatagramPacket to send and receive data from and to the ErrSim
   DatagramSocket sendReceiveSocket;			//We only need one datagramsocket since we are never  								//sending and receiving at the same time
   private Scanner input;		
   private BufferedInputStream in;	// stream to read in file
   public static enum Mode {NORMAL, TEST};		//enum serving for different mode
   public static enum Decision {RRQ, WRQ};		//same for decision both enum are inputted in the consol of the client
   public static final int MAX_DATA = 512;	//maximum number of bytes in data block



/******************** Start Of Code ******************************/


/*********************************************************************************************
*******************************     Constructor     *****************************************
*********************************************************************************************
*********************************************************************************************/
   public Client() {
       try {
           sendReceiveSocket = new DatagramSocket();	//creat the datagram socket
       } catch (SocketException se) {   	//catch Socket exception error if applicable
           se.printStackTrace();
           System.exit(1);
       }
   }


/*********************************************************************************************
*********************************************************************************************
***************************** TFTPClient Main   *********************************************
*********************************************************************************************/


   public static void main(String args[]) throws IOException {			
       Client c = new Client();				
       System.out.println("open Client Program!\n");
       c.inter();	
   }




/*********************************************************************************************
*********************************************************************************************
************************      void inter() throws IOException    ****************************
*********************************************************************************************/

   public void inter() throws IOException {	
       String fileName = "test0.txt";			//make a fileName for test cases for later iterations
       String mode = "netascii";			// The used mode
       Decision request = Decision.RRQ;		//default decision which is Read
       input = new Scanner(System.in);			//run a new scanner to scan the input from the user

       System.out.println("choose (R)ead Request, (W)rite Request, or (Q)uit?");
       String choice = input.nextLine();	//reads the input String	



	// it runs threw all the possible answers if none are applicable it recursively go back to inter()
       if (choice.equalsIgnoreCase("R")) {				
           request = Decision.RRQ;
           System.out.println("Client: send a read request.");
       } else if (choice.equalsIgnoreCase("W")) {	
           request = Decision.WRQ;
           System.out.println("Client:  send a write request.");
       } else if (choice.equalsIgnoreCase("Q")) {	
           System.out.println("Goodbye!");
           System.exit(1);
       } else {									
           System.out.println( "invalid choice.  Please try again...");
           inter();
       }	   



	//gets a file directory from the user 
       System.out.println("Please choose a file to modify.  Type in a file name: ");
      
       
       fileName = input.nextLine();	
       File f = new File(fileName);
       
       //tests if the file exists
       if(f.exists() && !f.isDirectory()) { 
       // do something

	   //decide if it s a read or a write
       if (request == Decision.RRQ) {  		   
           System.out.println("Client:" + fileName + ", receive in " + mode + " mode.\n");
           read(fileName, mode);	

       } else if (request == Decision.WRQ) {
           System.out.println("Client:" + fileName + ", send in " + mode + " mode.\n");
           write(fileName, mode);	
       }
       
       

   }else{
	   //if no file exists with that name ask them to try again
	   System.out.println("That file does not exist.\n");
	   inter();
   }
       
   }

	
   public void read (String fileName, String mode) throws IOException {
	   
       // new stream to write bytes to, and turn into request byte array to be sent
       ByteArrayOutputStream req = new ByteArrayOutputStream();
       req.reset();

       /* ADDed code .... 7:19 pm */
       
          
       File myFile = new File (fileName);
       byte R_sig[] =new byte[]{0x00,0x01};
	      byte b2[] = new byte[]{0x00};
       
       
       
       FileInputStream fin = new FileInputStream(myFile);
      // BufferedInputStream in = new BufferedInputStream(fis);
      byte fileContent[] = new byte[(int)myFile.length()];
      fin.read(fileContent);
      
      byte File[] = new byte[R_sig.length+fileContent.length+b2.length+mode.length()+10];
      System.arraycopy(R_sig, 0, File, 0, R_sig.length);
      System.arraycopy(fileContent, 0, File, R_sig.length, fileContent.length);    
      System.arraycopy(b2, 0, File, R_sig.length+fileContent.length, b2.length);  
  //    System.arraycopy(mode, 0, File, R_sig.length+fileContent.length+b2.length, mode.length());  		USED in next iteration
 //    System.arraycopy(b2, 0, File, R_sig.length+fileContent.length+b2.length+mode.length(), b2.length);  
      	
      
      
      
      int r = (int) myFile.length();
      int c = 0;
     /**old version
      *  sendPacket = new DatagramPacket(null, r, InetAddress.getLocalHost(), 23);
      
  
      for (int i = 0; i < myFile.length(); i++) {
          c = r < 512 ? r : 512;
          sendPacket.setData(Arrays.copyOfRange(fileContent, i, c));
          sendPacket.setLength(c);
          try {
              sendReceiveSocket.send(sendPacket);
              System.out.println("Client: Read Request sent using port " + 
                  sendReceiveSocket.getLocalPort() + ".");
              // print byte info on packet being sent
              System.out.print("Containing " + sendPacket.getLength() + " bytes: \n");

          } catch (IOException e) {
              e.printStackTrace();
              System.exit(1);
          }
          r -= 512;
          i += 511;
      }
        */
   
    		  sendPacket =  new DatagramPacket(Arrays.copyOfRange(File, 0,File.length),512, InetAddress.getLocalHost(), 23);
      
      
      for (int i = 0; i < File.length; i++) {
          c = r < 512 ? r : 512;
          
          sendPacket.setData(Arrays.copyOfRange(File, i,File.length));
          sendPacket.setLength(c);

          try {
              sendReceiveSocket.send(sendPacket);
              System.out.println("Client: Read Request sent using port " + 
                  sendReceiveSocket.getLocalPort() + ".");
              // print byte info on packet being sent
              System.out.print("Containing " + sendPacket.getLength() + " bytes: \n");

          } catch (IOException e) {
              e.printStackTrace();
              System.exit(1);
          }
          r -= 512;
          i += 511;
          
          
          try {
              Thread.sleep(1000);
          } catch (InterruptedException e ) {
              e.printStackTrace();
              System.exit(1);
          }
      }
      
      
      fin.close();
      
       
       /*ends here*/
       
       
       // write read request bytes to stream
    /*   req.write(0);
       req.write(1);
       req.write(fileName.getBytes());
       req.write(0);
       req.write(mode.getBytes());
       req.write(0);

       // form request byte array
       byte[] request = new byte[0];
       request = req.toByteArray();

       // form the request packet and send it
    //this has been stolen
     //  sendPacket = new DatagramPacket(request, request.length, InetAddress.getLocalHost(), 23);
       // till here
       
       try {
           sendReceiveSocket.send(sendPacket);
           System.out.println("Client: Read Request sent using port " + 
               sendReceiveSocket.getLocalPort() + ".");
           // print byte info on packet being sent
           System.out.print("Containing " + sendPacket.getLength() + " bytes: \n");

       } catch (IOException e) {
           e.printStackTrace();
           System.exit(1);
       }
*/
       byte[] received = new byte[MAX_DATA]; // initialize for do-while loop

       do {
           // prepare for receiving data packet
           byte[] data = new byte[MAX_DATA + 4];
           receivePacket = new DatagramPacket(data, data.length);
           System.out.println("\nClient: Waiting for DATA.\n");

           // block until a ACK packet is received from sendReceiveSocket
           try {
               System.out.println("Waiting...");
               sendReceiveSocket.receive(receivePacket);
           } catch (IOException e) {
               System.out.print("IO Exception: likely:");
               System.out.println("Receive Socket Timed Out.\n" + e);
               e.printStackTrace();
               System.exit(1);
           }

           int size = 4;
           while (size < data.length) {
               if (data[size] == 0) {
                   break;
               }
               size++;
           }
           received = new byte[size - 4];
           System.arraycopy(data, 4, received, 0, size-4);

           // process the received DATA 
           System.out.println("\nClient: DATA received: ");
           System.out.println("From host: " + receivePacket.getAddress() + " : " + receivePacket.getPort());
           System.out.print("Containing " + received.length + " bytes: ");

           System.out.println("\nClient: reading data to file: " + fileName);

           byte[] response = new byte[4];		// data opcode and block number
           response[0] = 0;
           response[1] = 4;
           response[2] = 0;
           response[3] = data[3];

           // form the ACK packet and send it
           sendPacket = new DatagramPacket(response, 4, receivePacket.getAddress(), receivePacket.getPort());
           try {
               sendReceiveSocket.send(sendPacket);
               System.out.println("\nClient: ACK sent using port " + 
                   sendReceiveSocket.getLocalPort() + ".");
               // print byte info on packet being sent
           }catch (IOException e) {
               e.printStackTrace();
               System.exit(1);
           }

       } while (!(received.length < MAX_DATA));
       inter();
   }

   public void write (String fileName, String mode) throws IOException {
       // new stream to write bytes to, and turn into request byte array to be sent
       ByteArrayOutputStream req = new ByteArrayOutputStream();
       req.reset();
       
      

       // write write request bytes to stream
       File myFile = new File (fileName);
       byte R_sig[] =new byte[]{0x00,0x01};
	      byte b2[] = new byte[]{0x00};
       
       
       
       FileInputStream fin = new FileInputStream(myFile);
      // BufferedInputStream in = new BufferedInputStream(fis);
      byte fileContent[] = new byte[(int)myFile.length()];
      fin.read(fileContent);
      
      byte File[] = new byte[R_sig.length+fileContent.length+b2.length+mode.length()+10];
      System.arraycopy(R_sig, 0, File, 0, R_sig.length);
      System.arraycopy(fileContent, 0, File, R_sig.length, fileContent.length);    
      System.arraycopy(b2, 0, File, R_sig.length+fileContent.length, b2.length);  
  //    System.arraycopy(mode, 0, File, R_sig.length+fileContent.length+b2.length, mode.length());  		USED in next iteration
 //    System.arraycopy(b2, 0, File, R_sig.length+fileContent.length+b2.length+mode.length(), b2.length);  
      	
      
      
      
      int r = (int) myFile.length();
      int c = 0;
     /**old version
      *  sendPacket = new DatagramPacket(null, r, InetAddress.getLocalHost(), 23);
      
  
      for (int i = 0; i < myFile.length(); i++) {
          c = r < 512 ? r : 512;
          sendPacket.setData(Arrays.copyOfRange(fileContent, i, c));
          sendPacket.setLength(c);
          try {
              sendReceiveSocket.send(sendPacket);
              System.out.println("Client: Read Request sent using port " + 
                  sendReceiveSocket.getLocalPort() + ".");
              // print byte info on packet being sent
              System.out.print("Containing " + sendPacket.getLength() + " bytes: \n");

          } catch (IOException e) {
              e.printStackTrace();
              System.exit(1);
          }
          r -= 512;
          i += 511;
      }
        */
   
    		  sendPacket =  new DatagramPacket(Arrays.copyOfRange(File, 0,File.length),512, InetAddress.getLocalHost(), 23);
      
      
      for (int i = 0; i < File.length; i++) {
          c = r < 512 ? r : 512;
          
          sendPacket.setData(Arrays.copyOfRange(File, i,File.length));
          sendPacket.setLength(c);

          try {
              sendReceiveSocket.send(sendPacket);
              System.out.println("Client: Read Request sent using port " + 
                  sendReceiveSocket.getLocalPort() + ".");
              // print byte info on packet being sent
              System.out.print("Containing " + sendPacket.getLength() + " bytes: \n");

          } catch (IOException e) {
              e.printStackTrace();
              System.exit(1);
          }
          r -= 512;
          i += 511;
          
          
          try {
              Thread.sleep(1000);
          } catch (InterruptedException e ) {
              e.printStackTrace();
              System.exit(1);
          }
      }
      
      
      fin.close();

       // receive ACK from server
       byte ack[] = new byte[4];
       receivePacket = new DatagramPacket(ack, ack.length);
       try {
           // Block until a datagram is received via sendReceiveSocket.
           sendReceiveSocket.receive(receivePacket);
       } catch(IOException e) {
           e.printStackTrace();
           System.exit(1);
       }

       // process the received ACK
       System.out.println("\nClient: ACK received: ");
       System.out.println("From host: " + receivePacket.getAddress() + " : " + receivePacket.getPort());
       System.out.print("Containing " + receivePacket.getLength() + " bytes: " + Arrays.toString(ack) + "\n");

       byte[] data = new byte[MAX_DATA];	// the data chunk to read from the file
       byte[] response = new byte[4];		// data opcode and block number
       response[0] = 0;
       response[1] = 3;
       response[2] = 0;
       response[3] = 1;

       in = new BufferedInputStream(new FileInputStream(fileName));

       // reads the file in 512 byte
       while ((in.read(data)) != -1) {
           // cut off zero bytes
           int size = 0;
           while (size < data.length) {
               if (data[size] == 0) {
                   break;
               }
               size++;
           }

           byte[] transfer = new byte[response.length + size];	// byte array to send to Server

           // copy opcode, blocknumber, and data into array to send to Server
           System.arraycopy(response, 0, transfer, 0, 4);
           System.arraycopy(data, 0, transfer, 4, size);			

           // send the data packet to the server via the send socket
           sendPacket = new DatagramPacket(transfer, transfer.length, receivePacket.getAddress(), receivePacket.getPort());
           //try {
               sendReceiveSocket.send(sendPacket);
               System.out.println("\n\nClient: DATA packet sent using port " + 
                   sendReceiveSocket.getLocalPort());
               // print byte info on packet being sent to Server
               System.out.print("Containing " + sendPacket.getLength() + " bytes: \n");
               System.out.println(Arrays.toString(transfer));
          /* } catch (IOException e) {
               e.printStackTrace();
               System.exit(1);
           }	*/

           // increase the block number after each block is sent
           if (response[3] == 127) {
               response[3] = 0;
           } else {
               response[3] = (byte)(response[3] + 1);
           }

           // prepare for receiving packet with ACK
           receivePacket = new DatagramPacket(ack, ack.length);
           System.out.println("\nClient: Waiting for ACK.\n");

           // block until a ACK packet is received from sendReceiveSocket
           try {        
               System.out.println("Waiting...");
               sendReceiveSocket.receive(receivePacket);
           } catch (IOException e) {
               System.out.print("IO Exception: likely:");
               System.out.println("Receive Socket Timed Out.\n" + e);
               e.printStackTrace();
               System.exit(1);
           }

           // process the received ACK
           System.out.println("\nClient: ACK received: ");
           System.out.println("From host: " + receivePacket.getAddress() + " : " + receivePacket.getPort());
           System.out.print("Containing " + receivePacket.getLength() + " bytes: " + Arrays.toString(ack));
       }
       in.close(); 
       inter();
   }

   public void exit() {
       sendReceiveSocket.close();
   }
}
