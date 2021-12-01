import java.io.*; 
import java.net.*; 
import java.text.*;
import java.util.Date;
class Client { 

    public static void main(String argv[]) throws Exception 
    { 
        String instruction;// string to store instruction that was sent from the server
        String expResult;  // string to store result of math expresssion returned from server
        BufferedReader inFromUser = 
          new BufferedReader(new InputStreamReader(System.in)); // used to read users input
        
        
        Socket clientSocket = new Socket("127.1.0.0", 6000); // create socket to connect with the server
        
        // reaching here means that socket was successfully accepted by the server
        
    	DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());  // used to send data to the server
    	        
    	BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream())); // used to receive data from the server
    	
        String UserInput = new String(); // string to store users input
        	
        int NumofInput = 0; // used to count number of input user has entered
        
        instruction = inFromServer.readLine(); // store the instruction about how to use the calculator from the server
        System.out.println(instruction); // print out the instruction here
        
        while(true) {
        	
        	System.out.println("Please Enter Basic Math Expressions Or Press x to quit");
        	
        	UserInput = inFromUser.readLine(); // get users input
        	
        	if(UserInput.toLowerCase().equals("x")) { // if user wants to exit
        		
        		if(NumofInput>=3) {break;} // first check if user has entered at least 3 expressions, if true break out of infinite loop
        		else { 
        			System.out.println("Please Enter At Least 3 Math Expression");continue; // if user has not entered 3 expression,print instruction
        		}
        		
        	}else {
        		NumofInput++; // as user entered expression add one
        	}
        	
	        outToServer.writeBytes(UserInput + '\n'); // send math expression to server
	        
	       expResult = inFromServer.readLine();  // read the result from the server
	
	        System.out.println("FROM SERVER: " +expResult); 
                    
        }
        	 
	        clientSocket.close(); // close the socket
	        SimpleDateFormat formatter= new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss z"); // to format the date in a way that is easy to read for users
        	String disconInfo = MessageFormat.format("Disconnected from server at {0}",formatter.format(new Date(System.currentTimeMillis())));
        	System.out.println(disconInfo); // print when client disconnected from the server
	        
          
    } 
      
} 

        
// Question to ask
// name of host not being returned in some cases
// what does request mean, does it actually have to send some message to the server, or cleintSocket.close() is this considered a request?
// is my way of handling in order a valid way
// can port number and ip address be hard coded?