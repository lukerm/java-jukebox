import java.io.*;
import java.util.*;

// Important: to run this you must have java and mpg123 installed:
//  
// sudo apt-get install default-jre
// sudo apt-get install mpg123

public class Jukebox {

    String rootFolder = "/"; 
    boolean quietMode = false; // Should info be printed? 
    
    // Random number generator for tracks. 
    Random random = new Random();

    public static void main(String[] args){
	
	Jukebox j; 
	
	// Check supplied directory/file exists. 
    
	// Tidy up argument. (Does getOpt exist for Java?)
	if(args.length == 0) j = new Jukebox("/", false);
	else if(args.length == 1 || args.length == 2) {
	  
	    // Check if the file/directory exists. 
	    File file = new File(args[0]);
	    if(!file.exists()){
		System.out.println(args[0] + " is not a file nor directory.");
		displayUsage();
		System.exit(1);
	    }
	    
	    if(args.length == 1) j = new Jukebox(args[0], false);
	    if(args.length == 2){
		if(!(args[1].equals("-q")||args[1].equals("--quiet"))){
		    displayUsage(); System.exit(1);
		}
		else j = new Jukebox(args[0], true);
	    }
	}
	// Too many arguments. 
	else{ displayUsage(); System.exit(1); }
    
    }
    
    // Constructor. 
    public Jukebox(String rootFolder, boolean quietMode){
    
	this.rootFolder = rootFolder; this.quietMode = quietMode; 
	ArrayList<String> songs = getSongs(rootFolder);
	
	if(songs.size() == 0) System.out.println("No MPEG tracks available in " + rootFolder);
	else{
	
	    while(songs.size() > 0){
	    
		// Random track from the playlist. 
		int nextTrack = random.nextInt(songs.size());
		// For representing the subprocess which will
		// play the track. 
		Process proc = null; 
	    
		try{
		    File file = new File(songs.get(nextTrack));
		    String fullpath = file.getAbsolutePath();
		    String name = file.getName();
		    // Remove this track from songs so that it 
		    // not played again. 
		    songs.remove(nextTrack);
		    
		    // Track info, unless suppressed by quiet == true. 
		    if(!quietMode){
			int ind = name.indexOf(getExtension(name));
			System.out.println("Now playing: " + name.substring(0,ind));
			System.out.println("Location   : " + fullpath);
		    }
		    
		    // Set up the SkipThread.
		    SkipThread skip = new SkipThread(Thread.currentThread());
		    skip.start();
		
		    // Create the process which actually plays the 
		    // track. This is done using the mpg123 player. 
		    // Note: if there is an error, such as a non-
		    // existent file, the process will just die. 
		    String[] params = {"mpg123", fullpath};
		    proc = Runtime.getRuntime().exec(params);
		    
		    Scanner read = new Scanner (System.in); 
		    proc.waitFor();
		    
		} 
		// Interrupted by another Thread. This means to 
		// skip to the next track (first kill proc). 
		catch(InterruptedException e){ proc.destroy(); continue; }
		catch(Exception e){ e.printStackTrace(); }
	    
	    }
	    
	    if(songs.size() == 0) System.out.println("End of playlist.");
	    
	}
    }
    
    // Detects the tree hierarchy under the rootFolder, and stores
    // all files of the correct format. 
    public ArrayList<String> getSongs(String rootFolder){
    
	// This is essentially a playlist. 
	ArrayList<String> toReturn = new ArrayList<String>();
	
	File file = new File(rootFolder);
	
	if(file.exists() && file.isDirectory()){
	    File[] subFiles = file.listFiles();
	    
	    for(int f = 0; f < subFiles.length; f++){
		
		File subFile = subFiles[f];
		
		if(subFile.isFile()){
		    // Check the extension.
		    if(checkExtension(subFile.getName())){
			// Add to the playlist. 
			toReturn.add(subFile.getAbsolutePath());
		    }
		}
		// Is a directory. 
		else{
		    // Recursive call to this method. 
		    toReturn.addAll(getSongs(subFile.getAbsolutePath()));
		}
	    }
	}
	// In this case, the user has supplied a file, 
	// so check it has the correct extension, and
	// add it to the trivial playlist (which will
	// consist of one song). 
	else if(file.exists() && file.isFile()){
	    if(checkExtension(file.getName())){
		// Add to the playlist. 
		toReturn.add(file.getAbsolutePath());
	    }
	}
	
	// Return statement.
	return toReturn;
    
    }
    
    // Checks the extension of the given filename. Returns
    // true if it is MPEG (supported by mpg123). 
    public boolean checkExtension(String filename){
    
	if     ( filename.endsWith(".mp3") ) return true;
	else if( filename.endsWith(".MP3") ) return true;
	else if( filename.endsWith(".mp2") ) return true;
	else if( filename.endsWith(".MP2") ) return true;
	else if( filename.endsWith(".mp1") ) return true;
	else if( filename.endsWith(".MP1") ) return true;
	else 				      return false; 
    
    }
    
    // Returns the extension of the filename, if it is one
    // of the types supported by mpg123. 
    public String getExtension(String filename){
    
	if     ( filename.endsWith(".mp3") ) return ".mp3";
	else if( filename.endsWith(".MP3") ) return ".MP3";
	else if( filename.endsWith(".mp2") ) return ".mp2";
	else if( filename.endsWith(".MP2") ) return ".MP2";
	else if( filename.endsWith(".mp1") ) return ".mp1";
	else if( filename.endsWith(".MP1") ) return ".MP1";
	else 				      return ""; 
    
    }
    

    // A Thread that will be used to allow the 
    // user to skip to the next track. 
    private class SkipThread extends Thread {
    
	Thread target = null;
	String command = ""; 
    
	// Supply a Thread object as an argument,
	// that which gave birth to this Thread. 
	public SkipThread(Thread mother){
	    
	    // Daemon Thread, killed when user
	    // kills the main program. 
	    setDaemon(true);
	    // Set the target Thread. 
	    this.target = mother;
	
	}
	
	// Run method. 
	public void run(){
	
	    // For reading the user input. 
	    Scanner scan = new Scanner(System.in);
	    
	    // Continue in an infinite loop, which
	    // is stopped when the main program is
	    // stopped. 
	    while(true){
		// User's command stored here 
		// when entered is pressed. 
		command = scan.nextLine();
		// Trim.
		command = command.trim(); 
		
		// Interpret command. 
		if(command.equalsIgnoreCase("s"))
		    target.interrupt();
		else if(command.equalsIgnoreCase("skip"))
		    target.interrupt();
		else if(command.equalsIgnoreCase("next"))
		    target.interrupt();
		// Unrecognized command, do nothing. 
		else continue;
		
	    }
	}
    }
    
    //
    private static void displayUsage(){
	System.out.println("Usage: java Jukebox directory [-q]");
	System.out.println("       java Jukebox MPEG file [-q]");
    }

    // Private method for reading InputStreams. Not used. 
    private static String streamToString(java.io.InputStream is) {
	java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
	return s.hasNext() ? s.next() : "";
    }

}
