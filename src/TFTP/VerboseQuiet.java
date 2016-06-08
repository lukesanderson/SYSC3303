package TFTP;

import java.net.DatagramPacket;

public class VerboseQuiet {

	private static final int BLOCK_OFFSET= 32767;
	
	
	
	public VerboseQuiet(boolean verbose) {
		
	}
	
	public void printThis(boolean important, String input){
	
	if(important == true){
		System.out.print(input);
	}
	else if(important){
		System.out.print("");
	}
	}
	
	/**
	 * This method prints a detailed set of output statements about the packet
	 * if verbose mode is true.
	 * 
	 * @param boolean important - true or false
	 * @param p - packet to create print statements for
	 */
	public void printThis2(boolean important, DatagramPacket p ){
		
		int i = 1;
		
		if(important){
			
			if(i == 1){
			//print opcode
			int opcodeInt = ((p.getData()[0] & 0xff) << 8) | (p.getData()[1] & 0xff);
			System.out.println("opcode: " + opcodeInt);
			 
			 //print block number
			int blockInt =  ((p.getData()[2] & 0xff) << 8) | (p.getData()[3] & 0xff);
			System.out.println("block:  " + blockInt);
			
			int receivedNumber = ((p.getData()[2] & 0xff) << 8) | (p.getData()[3] & 0xff);
			
			if(receivedNumber > BLOCK_OFFSET){
				receivedNumber += BLOCK_OFFSET;
			}
			}
			
			
			
			if(p.getLength() == 4){
				
				System.out.print("packet: ");
				System.out.print(p.getData()[0]);
				System.out.print(p.getData()[1]);
				System.out.print(p.getData()[2]);
				System.out.print(p.getData()[3]);
				System.out.println("\n");
				
			}else{
				
				System.out.print("Bytes: ");
				for (byte b : p.getData()) {
					System.out.print(b);
				}
				System.out.println("\n");
			}
			
		}
		
	}	
}