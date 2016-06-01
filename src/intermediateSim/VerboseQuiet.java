package intermediateSim;

import java.net.DatagramPacket;

//Create new VerboseQuiet instance, then call the method on it
//e.g. VerboseQuiet vQ = new VerboseQuiet(false);

/**
 * 
 * @author luke sanderson
 * 
 * Used to differentiate verbose modes and quiet modes on the Error Simulator
 */
public class VerboseQuiet {
	private boolean verbose = true;
	
	public VerboseQuiet(boolean verbose) {
		this.verbose = verbose;
	
	}
	
	public void printThis(boolean important, String input){
	
	if(verbose){
		System.out.print(input);
	}
	else if(important){
		System.out.print(input);
	}
	}
	
	/**
	 * This method prints a detailed set of output statements about the packet
	 * if verbose mode is true.
	 * 
	 * @param boolean important - true or false
	 * @param p - packet to create print statements for
	 */
	public void printThisSent(boolean important, DatagramPacket p){
		
		//prints bytes for the packet 
		System.out.println("Bytes:");
		for (byte b : p.getData()) {
			System.out.print(b);
		}
		
		System.out.println();
		
		
		
	}

}
