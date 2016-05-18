package intermediateSim;

import java.net.DatagramPacket;


public class Delay implements Runnable {
	private int delay;
	private DatagramPacket data;
	private ErrorSim errS;


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
