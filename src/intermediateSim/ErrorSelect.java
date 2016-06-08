package intermediateSim;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Scanner;

/**
 * 
 * @author Luke Sanderson - Team 5 Systems and Computer Engineering, Carleton
 *         University
 * @version 1.0
 *
 */
public class ErrorSelect {

	public int blockNum = -1;
	public int OpCode = -1;
	public int mode = -1;
	private int vq = -1;
	public int corruption = -1;
	public boolean verboseMode = true;
	public InetAddress serverAddress = null;
	private static final Scanner READER = new Scanner(System.in);
	public boolean firstPacket = false;
	/**
     * Validates the IP address passed is of valid format
     * (ie XXX.XXX.XXX.XXX where values are not exceeded).
     *
     * @param ipAddress IP Address to validate the format
     * @return True if the IP Address is valid
     */
    public boolean validateIPAddress(String ipAddress) {
        final int upperLim = 255;
        final int lowerLim = 0;
        final int ipLength = 4;
        try{
        // Split the address by decimals
        String[] parts = ipAddress.split( "\\." );

        // Check each part so that it does not violate the upper and lower limit
        for (String s : parts) {
        	
            int i = Integer.parseInt(s);
            if ( (i < lowerLim) || (i > upperLim) ) {
                return true;
            }
        }

        // Check the length is valid
        if ( parts.length != ipLength ) {
            return true;
        }
        }catch (NumberFormatException e){
        	System.out.println("Invalid IP");
        	return true;
        }
        return false;
    }

	public void menu() throws IOException {

		
		System.out.println("Error Simulator \n");
	
		while (true) {
			System.out.println("Please enter the server IP or hit 'Enter' to continue with local address.");
			String serverIP = READER.nextLine();
			if (serverIP.isEmpty()) {
				serverAddress = InetAddress.getLocalHost();
				System.out.println("Sending to local host");
				break;
			}
			if (validateIPAddress(serverIP)) {
				continue;
			}
			try {
				serverAddress = InetAddress.getByName(serverIP);
				System.out.println("sending to ip: "+serverIP);
			} catch (UnknownHostException e) {
				System.out.println("");
			}
			break;
		
		}
		
		
		
		
		while (vq == -1) {
			System.out.println("Would you like to run the ErrorSim in (V)erbose or (Q)uiet mode?");
			String verbose = READER.nextLine();
			if (verbose.equalsIgnoreCase("v")) {
				verboseMode = true;
				vq = 1;
				System.out.println("Verbose mode selected.");
			} else if (verbose.equalsIgnoreCase("q")) {
				verboseMode = false;
				vq = 1;
				System.out.println("Quiet mode selected.");
			} else {
				System.out.println("Invalid input");
				vq = -1;
			}
		}
		
		
		
	

		// loop until valid choice selected
		while (OpCode == -1) {
			System.out.println("choose (A) to alter ACK packet, (D) to alter DATA packet");

			String choice = READER.nextLine(); // reads the input String
			if (choice.equalsIgnoreCase("A")) {
				OpCode = 01;
				System.out.println("ACK packet selected");
			} else if (choice.equalsIgnoreCase("D")) {
				OpCode = 02;
				System.out.println("DATA packet selected");
			} else {
				OpCode = -1;
				System.out.println("Invalid Input");
				
			}
		}

		// selects block, loops until a valid number selected
		System.out.println("Please enter the block number");
		while (true) {
			
			try {
				blockNum = Integer.parseInt(READER.nextLine());
			} catch (NumberFormatException n) {
				System.out.println("Not a number, please try again");
				continue;
			}
			break;
		}

		System.out.println("\nPlease enter the mode\n");
		System.out.println("00: Normal Operation\n");
		System.out.println("01: Lose the packet\n");
		System.out.println("02: Delay a packet\n");
		System.out.println("03: Duplicate a packet\n");
		System.out.println("04: corrupt the packet\n");
		System.out.println("05: create an unknown transfer ID\n");
		while (mode == -1) {
			String modeSelected = READER.nextLine(); // reads the input String
			
			// it runs threw all the possible answers if none are applicable it
			if (modeSelected.equalsIgnoreCase("00")) {
				mode = 00;
				System.out.println("Normal mode selected.");
			} else if (modeSelected.equalsIgnoreCase("01")) {
				mode = 01;
				System.out.println("Losing the packet");
			} else if (modeSelected.equalsIgnoreCase("02")) {
				mode = 02;
				System.out.println("Delaying the packet");
			} else if (modeSelected.equalsIgnoreCase("03")) {
				mode = 03;
				System.out.println("Duplicating the packet");
			} else if (modeSelected.equalsIgnoreCase("04")) {
				mode = 04;
				System.out.println("invalidating the opcode");
			} else if (modeSelected.equalsIgnoreCase("05")) {
				mode = 05;
				System.out.println("Sending from an unknown ID");
			} else {
				System.out.println("invalid choice. Please try again.");
				mode = -1;

			}
			
			if (mode == 04){
				System.out.println("How would you like to corrupt the selected packet?");
				System.out.println("01: corrupt the Opcode");
				System.out.println("02: corrupt the block number");
				System.out.println("03: send as opposite operation (i.e if its an ACK, send as data)");
				System.out.println("04: corrupt the mode (netascii or octet)");
				while(corruption == -1){
					String corrupt = READER.nextLine(); // reads the input String
					if (corrupt.equalsIgnoreCase("01")){ //opcode corrupt
						corruption = 1;
					}
					else if(corrupt.equalsIgnoreCase("02")){ //block corrupt
						corruption = 2;
					}
					else if(corrupt.equalsIgnoreCase("03")){ //change ACK or DATA
						corruption = 3;
					}
					else if(corrupt.equalsIgnoreCase("04")){ //change mode
						corruption = 4;
						firstPacket = true;
					}
					else{
						System.out.println("invalid choice. Please try again.");
						corruption = -1;
					}
					
				}
			}
		}
	}

}
