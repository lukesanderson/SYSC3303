package intermediateSim;

import java.io.IOException;
import java.util.Scanner;


public class ErrorSelect {
	
	
	public int blockNum = -1;
	public int OpCode = -1;
	public int mode = -1;
	private static final Scanner READER = new Scanner(System.in);
	public void menu() throws IOException {


		System.out.print("\nError Simulator \n");
		System.out.println("choose (A) to alter ACK packet, (D) to alter DATA packet");
		
		String choice = READER.nextLine(); // reads the input String

		//loop until valid choice selected
		while(OpCode == -1){
		if (choice.equalsIgnoreCase("A")) {
			OpCode = 01;
			System.out.println("ACK packet selected");
		} else if (choice.equalsIgnoreCase("D")) {
			OpCode = 02;
			System.out.println("DATA packet selected");
		} else {
			OpCode = -1;
		}
		}

		// selects block, loops until a valid number selected
		System.out.println("Please enter the block number in the format 00");
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
		System.out.println("04: create invalid TFTP opcode\n");
		System.out.println("05: create an unknown transfer ID\n");
		while(mode == -1){
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
		} else if(modeSelected.equalsIgnoreCase("04")){
			mode = 04;
			System.out.println("invalidating the opcode");
		} else if(modeSelected.equalsIgnoreCase("05")){
			mode = 05;
			System.out.println("Sending from an unknown ID");
		}else {
			System.out.println("invalid choice. Please try again.");
			mode = -1;
			
		}
		}
	}
	
	

}
