package intermediateSim;


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

}
