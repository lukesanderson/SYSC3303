package intermediateSim;

import java.net.DatagramPacket;

/**
 * Simulates a delayed packet by causing the thread to sleep, 
 * then sending the packet normally.
 * 
 * @author Luke Sanderson - Team 5
 * Systems and Computer Engineering,
 * Carleton University
 * @version 2.0
 *
 */
public class Delay implements Runnable {
	private int delay;
	private DatagramPacket data;
	private ErrorSim errS;

	/**
	 * Receives the delay time from ErrorSim
	 * 
	 * @param delay
	 * @param data
	 * @param errS
	 */
	public Delay(int delay, DatagramPacket data, ErrorSim errS) {
		this.delay = delay;
		this.data = data;
		this.errS = errS;
	}
	
	@Override
	public void run() {
		try{
			Thread.sleep(delay);
		} catch(InterruptedException e){
			System.out.println("Interrupted while trying to delay packet sending.");
		}
		
		errS.sendPacket(data);
	}
}
