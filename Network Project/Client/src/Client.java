import java.io.*; 
import java.net.*; 
import java.text.*;
import java.util.Date;
class Client { 

    public static void main(String argv[]) throws Exception 
    { 
        String instruction; 
        String modifiedSentence; 
        
        BufferedReader inFromUser = 
          new BufferedReader(new InputStreamReader(System.in)); 
        
//        String Hostname = InetAddress.getLocalHost().toString();
//        System.out.println(Hostname);
        
        Socket clientSocket = new Socket("127.1.0.0", 6000);
        
    	DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream()); 
    	        
    	BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream())); 
    	
        String UserInput = new String();
        	
        int NumofInput = 0;
        
        instruction = inFromServer.readLine();
        System.out.println(instruction);
        
        while(true) {
        	
        	System.out.println("Please Enter Basic Math Expressions Or Press x to quit");
        	
        	UserInput = inFromUser.readLine();
        	
        	if(UserInput.toLowerCase().equals("x")) {
        		
        		if(NumofInput>=3) {break;}
        		else {
        			System.out.println("Please Enter At Least 3 Math Expression");continue;
        		}
        		
        	}else {
        		NumofInput++;
        	}
        	
	        outToServer.writeBytes(UserInput + '\n'); 
	        
	        modifiedSentence = inFromServer.readLine(); 
	
	        System.out.println("FROM SERVER: " + modifiedSentence);
                    
        }
        	 
	        clientSocket.close();
	        SimpleDateFormat formatter= new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss z"); // to format the date in a way that is easy to read for users
        	String disconInfo = MessageFormat.format("Disconnected from server at {0}",formatter.format(new Date(System.currentTimeMillis())));
        	System.out.println(disconInfo);
	        
          
    } 
      
} 

        
// Question to ask
//1. does the name that client sends to server, can it be ipaddress or does it have to be a name?
//2. is running a client code multiple times a valid solution to create multiple clients?
//3. what is message format? for protocol