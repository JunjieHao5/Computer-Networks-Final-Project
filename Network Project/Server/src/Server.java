import java.io.*; 
import java.net.*;
import java.text.*;
import java.util.Date;
import java.io.File;
import java.io.FileWriter;

class Server {
	public static int reqnum = 0; // assign a reqnum number to each request that was sent from client, ex- first request will have reqnum number 1
	public static int handled = 0;// this number indicates till what number the requests have been handled, ex if handled = 4, it means that the request from 1 - 4 has been handled successfully
	
	public static void main(String argv[]) throws Exception {
	
		try {
			 ServerSocket welcomeSocket = new ServerSocket(6000);// create a socket to receive request from client
		      
		      	while(!welcomeSocket.isClosed()) { // while the socket created is open
		      		 

		      		 Socket connectionSocket = welcomeSocket.accept(); // open the socket and receive connection request from client
		      		 
		      		 new ServerThread(connectionSocket).start(); // create a new thread for each client

		             
		      	}
		      	
		      welcomeSocket.close(); // close the socket, but it might never called since the server should always be running to handle client request
		}catch(Exception e) {
			if(e instanceof BindException) {
    			System.out.println("Error: The server is not on yet, please retry later");
    		}else {
    			System.out.println(e);
    		}
		}
	} 

}


 class ServerThread extends Thread {	
	 
    public Socket connectionSocket;

    
    public ServerThread(Socket clientSocket) {
        this.connectionSocket = clientSocket;
    }
    
    // function that runs when start() is called by newly created thread
    // below function handles receiving math expression from client and sending back the result to the client.
    public void run() {
    	String clientSentence; 
        String result;
        int CurReqNum = 0;
        
    	long start = System.nanoTime(); // when the client connection is accepted and the thread is created for the client, start the timer
    	
		SimpleDateFormat formatter= new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss z"); // to format the date in a way that is easy to read for users
	    Date date = new Date(System.currentTimeMillis()); // assign current date and time to date variable
	    
	    String ipAddress = connectionSocket.getRemoteSocketAddress().toString(); // get the ipaddress of a client using getremotesocketaddress()
	    String Hostname = connectionSocket.getInetAddress().getCanonicalHostName().toString();
	    
	    printConnectedMsg(Hostname,ipAddress,date,formatter);// print info of the client when it is connected with the server
	    
 		BufferedReader inFromClient; // used to read input from user
 		DataOutputStream outToClient; // used to send outputs to the user
		
 		try {
			inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
			outToClient= new DataOutputStream(connectionSocket.getOutputStream());
			
			String statusMsg = MessageFormat.format("Connected with Client with IpAddress {0} Successfully, please enter a math expression with one operator(+,-,*,/,^) and two operands, ex - 44/4, 4^9",ipAddress);
			outToClient.writeBytes(statusMsg + '\n'); // function - send instruction
			
			 while(!connectionSocket.isClosed()) { // while the connection with client is still connected 
	           	 
				 clientSentence = inFromClient.readLine(); // receive any data from client
	           	 
	           	 if(clientSentence == null) { // if client's response is null, means that the connection has been terminated
	           		 connectionSocket.close(); // close the connection with the client
	           		 
	           		 long finish = System.nanoTime(); // as soon as the connection is disconnected, measure the time
	           		 long timeElapsed = (finish - start)/ 1_000_000_000; // subtract finish time with start time to calculate how long the client has been connected to the server
	           		 
	           		 printDisconMsg(ipAddress,timeElapsed,formatter);// print info of the client when it is disconnects from the server
	           		 
	           	 }else { // if client is still connected, respond
	           		 
	           		 	Server.reqnum++; // every time the server receives a request from client is gives reqnum number to each request
	           		 	
	           		 	CurReqNum = Server.reqnum; // assign static reqnum to local CurReqNum to maintain the value,because the static req num will change everytime its called by different thread
	           		 	
	           		 	String validExpCheck = calculate(clientSentence);
	           		 	result = validExpCheck!= null ? 
	           		 			 validExpCheck+ '\n':
	           		 			 "Error" + '\n';
	           		 	
	           		 	printReqMsg(clientSentence,Hostname,ipAddress,result);// when req received, display who client is and what expression was received
	           		 	
	           		 	waitForTurn(CurReqNum); // function to send the result back in a order the server received the req
	           		 	
	                    outToClient.writeBytes(result); // send back the result to the client
	                    Server.handled++; // increment the value of handled to show that the current reqnum has been successfully handled.
	           	 }
	           	 
	          }
		} catch (IOException e) {
			System.out.println(e);
		} 
    }
    
    // function to print info of the client when it is connected with the server on console and logfile
    public void printConnectedMsg(String Hostname,String ipAddress,Date date,SimpleDateFormat formatter) {
    	String connectedMsg = MessageFormat.format("Client Connected! Host: {0} with IPaddress {1} connected at {2} ",Hostname, ipAddress,formatter.format(date));
	    System.out.println(connectedMsg);
	    writeToFile(connectedMsg);// function - print connected clients info
    }
    // function to  print info of the client when it is disconnects from the server on console and logfile 
    public void printDisconMsg(String ipAddress,long timeElapsed,SimpleDateFormat formatter) {
    	String disconMsg = MessageFormat.format("Client with Ipaddress {0} has disconnected",ipAddress);
   		String disconInfo = MessageFormat.format("Client was connected for {0} seconds and got disconnected at {1}",timeElapsed,formatter.format(new Date(System.currentTimeMillis())));
	    System.out.println(disconMsg);
	    System.out.println(disconInfo);
	    writeToFile(disconMsg);
	    writeToFile(disconInfo); // function print and log Clients Info
    }
    // function to  print when req received, display who client is and what expression was received on console and logfile
    public void printReqMsg(String clientSentence,String Hostname,String ipAddress,String result) {
    	String reqMsg = MessageFormat.format("request number: {0}, Received expression = {1}, From Hostname: {2} with IpAddress:{3}, result = {4}",Server.reqnum,clientSentence,Hostname,ipAddress,result);
		System.out.println(reqMsg);
		writeToFile(reqMsg);// function print client req info
    }
    
    // function to send the result back in a order the server received the req
 	/* while loop under first check if the previous request has already been handled by comparing handled number with current reqnum number -1
 	if the condition is false, it means that the result of previous request has not yet sent, so it will loop infinitely 
 	until the previous thread sends back the result and increment the value of handled*/
    
    public void waitForTurn(int curThreadReqNum) {
    	while(Server.handled!=curThreadReqNum-1) {
		 	System.out.println("The previous request has not been handled yet"); // printout the reason the result of current request is not being sent
		}
    }
    
	// function to calculate expression received from client, capable of caculating expression with two operands with one operator, ex 4*22, 3+7 
    public String calculate(String exp) {		
    	
        String parsedExp = exp.replaceAll(" ",""); // remove all spaces
        String [] parsedArray; // array to store tokens
        
        Double res = 0.0;
        try {
            if(parsedExp.contains("-")) { // if operator is -
            	parsedArray = parsedExp.split("-"); // split using operator, so only the operands are left
            	res = Double.parseDouble(parsedArray[0]) - Double.parseDouble(parsedArray[1]);
            }else if(parsedExp.contains("+")) {// if operator is +
            	parsedArray = parsedExp.split("\\+");
            	res =  Double.parseDouble(parsedArray[0]) + Double.parseDouble(parsedArray[1]);
            }else if(parsedExp.contains("/")) {// if operator is /
            	parsedArray = parsedExp.split("/");
            	res =  Double.parseDouble(parsedArray[0]) / Double.parseDouble(parsedArray[1]);
            }else if(parsedExp.contains("*")) {// if operator is *
            	parsedArray = parsedExp.split("\\*");
            	res =  Double.parseDouble(parsedArray[0]) * Double.parseDouble(parsedArray[1]);
            }else if(parsedExp.contains("^")) {// if operator is ^
            	parsedArray = parsedExp.split("\\^");
            	res =  Math.pow(Double.parseDouble(parsedArray[0]),Double.parseDouble(parsedArray[1]));
            }
            String result = MessageFormat.format("{0, number, #.##}",res); // limit the decimal two 2nd place
            
            return result;
            
        }catch(NullPointerException e) {
        	return null;
        }    
	}
    
    // function to create a logfile to keep track of clients informations and activities
    public void createFile() {
    	try {
    	      File myObj = new File("LogFile.txt");
    	      if (myObj.createNewFile()) {
    	        System.out.println("File created: " + myObj.getName());
    	      } else {
    	        System.out.println("File already exists.");
    	      }
    	    } catch (IOException e) {
    	      System.out.println("An error occurred.");
    	      e.printStackTrace();
    	    }
    }
    
    // function to write in logfile,when it writes it appends to existing file, and it does not reset the file, since logfile should also keep track of all previous informations about clients
    public void writeToFile(String sentence) {
    	 try {
    	      FileWriter myWriter = new FileWriter("LogFile.txt",true);
    	      myWriter.write(sentence+"\n");
    	      myWriter.close();
    	    } catch (IOException e) {
    	      System.out.println("An error occurred.");
    	      e.printStackTrace();
    	    }
    }

}
 



        