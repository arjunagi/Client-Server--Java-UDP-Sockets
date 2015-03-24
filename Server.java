package server;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;

/*
 * class Server
 * This class receives the request from the client for contents of a file 
 * and responds with the contents of the file if there is no error.
 * This class also handles the following error scenarios:
 * 1. Integrity check failure
 * 2. Malformed request
 * 3. Non-existent file
 * 4. Wrong protocol version 
 */
public class Server {

	public static void main(String[] args) {
    
		ClientServerUtility utility = new ClientServerUtility();
    int serverPortNumber = 1027;//assign port number for the server to send response through
    String responseToBeSent = "";
    byte[] responseToBeSentInBytes; //Response message in byte form
    InetAddress ipAddressOfClient = null;
	  try {
		  ipAddressOfClient = InetAddress.getLocalHost(); //Get the IP address of the host
	  } 
	  catch (UnknownHostException e1) {
		  e1.printStackTrace(); //Could not get host information
	  }
	 
    System.out.println("The server is waiting for client to send the request:");
    try
    {
    	DatagramSocket serverSocket= new DatagramSocket(serverPortNumber);
    	byte[] receivedRequest=new byte[100000];//byte array to store the received request
    	 
    	while(true) {
    		DatagramPacket receivedData=new DatagramPacket(receivedRequest,receivedRequest.length);
    		String receivedDataString = receiveRequest(receivedRequest, serverSocket, receivedData);//Convert the received bytes to string
    		System.out.printf("\n\nReceived request : \n%s", receivedDataString);
    		String[] splitRequest =receivedDataString.split("\r\n");//split the received request using CRLF as delimiter
    		int responseCode = generateResponseCode(receivedDataString, utility, splitRequest);//Get response code based on the received request	
        String fileContent = "";
      		
      		switch(responseCode) {
      		  //Case 0: response is OK
      		  case 0: fileContent=fileRead(splitRequest[1], utility);//Read the content of the requested file.
      			   			responseToBeSent = generateResponseMessage(responseCode, fileContent, utility);
    			          break;
      		  //Case 1: Integrity check failure      
      		  case 1: responseToBeSent = generateResponseMessage(responseCode, fileContent, utility);      
      		          break;
		        //Case 2: Malformed request  
      		  case 2: responseToBeSent = generateResponseMessage(responseCode, fileContent, utility);      
                    break;
            //Case 3: Non-existent file        	
      		  case 3: responseToBeSent = generateResponseMessage(responseCode, fileContent, utility);      
                    break;
            //Case 4: Wrong protocol version        
      		  case 4: responseToBeSent = generateResponseMessage(responseCode, fileContent, utility);      
                    break;           
            //Any other response code      
            default: System.out.print("\nWrong response code generated!");
          	  			 System.exit(0);
      		}//end of switch() 
      		
      		System.out.printf("\n\nSent response : \n"+responseToBeSent);
          responseToBeSentInBytes = utility.messageInBytes(responseToBeSent);//convert response message to bytes
          ipAddressOfClient = receivedData.getAddress();//get the IP address of the client from the received request
          //Datagram object for response which has to be sent to client. Retrieve the port number from the received client 
      		DatagramPacket response = new DatagramPacket(responseToBeSentInBytes,responseToBeSentInBytes.length,ipAddressOfClient,receivedData.getPort());
		      serverSocket.send(response);//send the response to the client IP using the server socket
    	}//end of while()
    	
    }//end of try{} 
 	 catch (Exception e)
 	 {
 		 //Did not receive request from client
 		 e.printStackTrace();
 		 System.out.println("There is an error in the server :");
 	 }
    
	}//end of main()
	
	/*
	 * String receiveRequest(byte[] messageBuffer, DatagramSocket serverSocket, DatagramPacket receivedData)
	 * This class receives the request sent by the client using the server socket.
	 * 
	 * receivedRequest - byte array buffer to store the received bytes and convert it to String
	 * serverSocket  - The socket information of the server
	 * receivedData - DatagramPacket object to receive the request
	 * @return: The received request in String format 
	 */
	public static String receiveRequest(byte[] receivedRequest, DatagramSocket serverSocket, DatagramPacket receivedData) throws Exception {
		serverSocket.receive(receivedData);//receive the request bytes via the server socket
		System.out.print("Received request bytes from client : ");
		for (int i=0;i<receivedData.getLength();i++)
		  System.out.printf("%d,",receivedRequest[i]);
		return new String(receivedRequest,0,receivedData.getLength());//Convert the received bytes to string
	}

	/*
	 * int generateResponseCode(String receivedDataString, ClientServerUtility utility, String[] splitRequest)
	 * This class generates response code required to be sent in response message.
	 * 
	 * receivedDataString - Received request
	 * utility - ClientServerUtility class object
	 * slitRequest - Pointer to the start of array of the split request
	 * @return: Response code
	 */
	public static int generateResponseCode(String receivedDataString, ClientServerUtility utility, String[] splitRequest) throws Exception{
  	Boolean requestIntegrityMatches = utility.isIntegrityValueOfMessageCorrect(receivedDataString, "request");//Check whether the integrity value received as part of message matches the calculated matches
  	String[] firstline=splitRequest[0].split("/");//split the first line to get version number
  	String filename = splitRequest[1];//The 2nd line of the request is the file name
  	int responseCode = 0;
  	if(!requestIntegrityMatches)
  		responseCode = 1;//Integrity check failure
  	else
    {
  		String versionnumber=(firstline[1].substring(0,3));//version number should be 1.0 always
  		 if(!versionnumber.equals("1.0"))
  			 responseCode = 4;//Wrong protocol version	 
  		 else if(((splitRequest[0]==null || splitRequest[1]==null || splitRequest[2]==null) || (!(firstline[0].equals("ENTS")) || (firstline[1].compareTo("1.0 Request") == 1))) || (!utility.isFileNameSyntaxCorrect(filename, "response")))
  			 responseCode = 2;//Malformed request
  		 else if((!(filename.equals("file_A.txt")) && !(filename.equals("file_B.txt")) && !(filename.equals("file_C.txt"))) || (fileRead(filename, utility).equals("File not Present")))
  			 responseCode = 3;//Non-existent file
  	}	
		return responseCode;
	}
	
	/*
	 * String generateResponseMessage(int responseCode, String fileContent, ClientServerUtility utility)
	 * This class generates the response which has to be sent to the client. The response contains file content if there are no errors.
	 * 
	 * responseCode - The response code which indicates if there is an error
	 * fileContent - The content of the requested file. Will be non-null only when response code is 0. Not used for other response code scenarios.
   * @return: Return the response which has to be sent to the client
	 */
	public static String generateResponseMessage(int responseCode, String fileContent, ClientServerUtility utility) {
		String firstLineToSend="ENTS/1.0 Response\r\n";//first line of the response message
		String responseToBeSent = "";
		if(responseCode == 0)
		  responseToBeSent = firstLineToSend+responseCode+"\r\n"+fileContent.length()+"\r\n"+fileContent;//Include file content only if response code is 0
		else
			responseToBeSent=firstLineToSend+responseCode+"\r\n"+"0"+"\r\n";//send response with appropriate response code
		String integrityValueToSend = utility.getIntegrityCheckValue(responseToBeSent);//Calculate the integrity check value which has to be included in the response
    return (responseToBeSent = responseToBeSent+integrityValueToSend+"\r\n");//append the integrity value to the response string
	}
	
	/*
	 * String fileRead(String filename, ClientServerUtility utility)
	 * This class reads the contents of the requested file. If the file is not present, it returns the appropriate string
	 * 
	 * fileName - The name of the requested file
	 * @return - The contents of the file. 
	 */
	public static String fileRead(String fileName, ClientServerUtility utility) throws Exception {	
		FileReader fr = null;//FileReader object
		try {
			//Assign the request file to the FileReader object
			fr = new FileReader("file path"+fileName);
		}
		catch (FileNotFoundException e) {
			//If the requested file was not found
			System.out.println("\nThe requested file is not present in the server. ");
			return "File not Present";
		}
		BufferedReader br = new BufferedReader(fr);//To read characters from FileReader object 
		try {
			StringBuilder fileContent = new StringBuilder();//Read the contents of the files line by line and store in a String
      String line = br.readLine();//Read the first line
      while (line != null) {
          fileContent.append(line);//add to the fileContent String
          fileContent.append(System.getProperty("line.separator"));//Append the line separator used by the system.
          line = br.readLine();//read the next line till end of file is reached.
      }
      return fileContent.toString();
		} 
		catch (IOException e) {
			//When file exists in the given path but is empty.
			System.out.print("\nNo contents in the file!!");
			System.exit(0);
		}
		return "";
 }//end of fileRead()  
    
}//end of class Server
