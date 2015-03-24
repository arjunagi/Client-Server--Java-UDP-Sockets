package client;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Scanner;

/*
 * class Client
 * This class takes the file name from the user and sends it as a request 
 * to the sever. The server responds with the contents of the file which
 * is displayed by the client. 
 * This class also handles the following error scenarios:
 * 1. Integrity check failure
 * 2. Malformed request
 * 3. Non-existent file
 * 4. Wrong protocol version 
 */
public class Client {
	static int resendCount = 1; //Number of times the request is resent. Used when response code received is 1 (integrity check failure).
	
	public static void main(String[] args) throws Exception {

		ClientServerUtility utility= new ClientServerUtility();
		Scanner inp =  new Scanner(System.in);
		String file = ""; 
		String viewMoreFiles = "yes"; //Input by user - if more files have to be viewed
		
		while(viewMoreFiles.matches("yes")) {
			System.out.print("Please type the name of the required file from the list below: \n");
			System.out.print("1. file_A.txt \n2. file_B.txt \n3. file_C.txt\n\n");
			file = inp.next(); //Get the file name of the required file.					
			if(utility.isFileNameSyntaxCorrect(file, "request")) {
				messageHandling(file, utility); //If the syntax of the file name is correct, proceed with handling the request.
			}
			System.out.print("\n\nDo you want to view more files?(yes/no)");
			viewMoreFiles = inp.next(); //Continue till the response by user is no.
		}// end of while()
		
	}// end of main()

	/*
	 * messageHandling(String file, ClientServerUtility utility)
	 * This function sends the request and handles the response sent by the server.
	 * file - The file whose contents have to be viewed
	 * utility - Object of ClientServerUtility class
	 */
	public static void messageHandling(String file, ClientServerUtility utility) throws Exception{
		DatagramSocket clientSocket = null; //Socket using which the data will be sent
		Boolean responseIntegrityValueMatches = false; //Compare the values of the integrity field and calculated integrity value.
		String receivedResponse = "";
		while(responseIntegrityValueMatches == false) {
			//if the values of the integrity field and calculated integrity value in the response don't match,
			//keep re-sending the message till the correct integrity value is received.
			clientSocket = handleRequest(file,utility); //get the socket details via which messages are sent and received.
			receivedResponse = handleResponse(clientSocket, file, utility);
		  responseIntegrityValueMatches = utility.isIntegrityValueOfMessageCorrect(receivedResponse, "response");
		}
		//handle the received response
		processResponse(receivedResponse,file,clientSocket,utility);
	}//end of messageHandling()
  
	
	/*
	 * handleRequest(String file, ClientServerUtility utility)
	 * This function sends the request to the server in byte form.
	 * 
	 * file - The file whose contents have to be viewed
	 * utility - Object of ClientServerUtility class
	 * @return: clientSocket - The socket details via which the request was sent. Required to listen to response.
	 */
	public static DatagramSocket handleRequest(String file, ClientServerUtility utility) throws Exception {
		byte[] requestMessageInBytes = utility.messageInBytes(generateRequestMessage(file, utility));//The request has to be sent as bytes
		DatagramSocket clientSocket = sendRequestToServer(requestMessageInBytes); //Get the socket details via which the request was sent
		return clientSocket;
	}//end of handleRequest()

	
	/*
	 * String generateRequestMessage(String file, ClientServerUtility utility)
	 * This function assembles all the fields(request line, file name and integrity check) of the request.
	 * 
	 * file - The file whose contents have to be viewed
	 * utility - Object of ClientServerUtility class 
	 * @return: assembledRequest - the final assembled request
	 */
	public static String generateRequestMessage(String file, ClientServerUtility utility) {
		if (null == file)
			throw new IllegalArgumentException("\nNo file selected!!");
		else {
	    String assembledRequest = "";// the final assembled request in String format
	    String firstLine = "ENTS/1.0 Request\r\n";// First line (request line)
	  	assembledRequest = assembledRequest + firstLine + file + "\r\n"; // "\r" and "\n" is for the CR+LF after filename.extension
	    String integrityCheckValue = utility.getIntegrityCheckValue(assembledRequest); //get the integrity check value for the request
	    assembledRequest = assembledRequest + integrityCheckValue + "\r\n";// appending the integrity check value and the CR+LF to the assembled request
	    System.out.printf("\nThe sent request is :\n%s", assembledRequest);
	    return assembledRequest;
		}
	}// generateRequestMessage()
	
	/*
	 * sendPacketToServer(byte[] cipherBytes, InetAddress serverDetails)
	 * This function creates a socket and sends the request to the server.
	 * 
	 * cipherBytes - encrypted data bytes which have to be transmitted
	 * serverDetails - The object which contains the IP address of the server.
	 * @return  - DatagramSocket object which has the details of the client socket
	 */
	public static DatagramSocket sendRequestToServer(byte[] requestBytes) throws Exception {
		// Get the IP of the server.
		// Here we get the details of local host as we are using the same computer(same IP)
		// but use different instances of Eclipse to act as client and server. 
		InetAddress serverDetails = InetAddress.getLocalHost();
		int clientPort = 1027;
		DatagramSocket clientSocket = null;//Socket using which the data will be sent
		
		if(null != serverDetails) {
			//send the packet to the server and store the used socket details
		  //Create a DatagramPacket object for the packets to be sent
			DatagramPacket packetToBeSent = new DatagramPacket(requestBytes, requestBytes.length, serverDetails, clientPort);
			clientSocket = new DatagramSocket();//Socket using which the data will be sent
	    clientSocket.send(packetToBeSent); // send the packet through the socket
	    return clientSocket;
		}
		else {
			//Server IP was not found
			System.out.print("\nServer could not be located!");
			System.exit(0);
			return null;
		}
	}// end of sendPacketToServer()
	
	/*
	 * String handleResponse(DatagramSocket clientSocket,String file, ClientServerUtility utility)
	 * This function receives the response from the server is bytes and then converts it into String.
	 * 
	 * clientSocket - The socket details via which the request was sent. Required to listen to response.
	 * file - The file whose contents have to be viewed
	 * utility - Object of ClientServerUtility class
	 * @return: receivedResponse - the received response in String
	 */
	public static String handleResponse(DatagramSocket clientSocket, String file, ClientServerUtility utility) throws Exception {
		byte[] responseInBytes = receiveResponseFromServer(clientSocket, file, utility);
	  String receivedResponse = new String(responseInBytes, 0, responseInBytes.length);//Store the received message in a string
	  System.out.printf("\n\nThe received response is : \n%s", receivedResponse);
	  return receivedResponse;
	}// end of handleResponse()
	
	/*
	 * receiveResponseFromServer(DatagramSocket clientSocket, String file, ClientServerUtility utility)
	 * This function receives the response from the server. If no response is received is 1s,
	 * the request is resent after 2s, 4s and 8s. Still if no response is received then timeout.
	 *   
	 * clientSocket : DatagramSocket object which has the details of the client socket
	 * file - The file whose contents have to be viewed
	 * utility - Object of ClientServerUtility class
	 * @return: responseFromServer - The response from the server in byte form
	 */
	public static byte[] receiveResponseFromServer(DatagramSocket clientSocket, String file, ClientServerUtility utility) throws Exception{
		byte[] responseFromServer = new byte[100000]; // To receive response from the server.
		DatagramPacket receivedPackets = new DatagramPacket(responseFromServer, responseFromServer.length); // DatagramPacket object to store the received packet

			while (resendCount <= 16) {
			clientSocket.setSoTimeout(resendCount*1000); //Block the receive() for i*1000s - If no byte is received in this interval, timeout happens
			try {
			  clientSocket.receive(receivedPackets); // Receive the packet from the socket
			  responseFromServer = receivedPackets.getData();//Store the received bytes
			  return responseFromServer;
			}
			catch(Exception e) {
				resendCount = resendCount*2; //Double the time interval after each timeout
				if(resendCount <= 8) {
					//Try re-sending the request 
					messageHandling(file, utility);
				}
				else if(resendCount == 16) {
				//Handle exception when 4th timeout occurs. When re-sending the request the 4th time, i value is 8.
				//So if timeout occurs even after 4th timeout (8*2 = 16), throw an error. 
				System.out.println("\nNo response received from server. " + e.getMessage());
				System.exit(0);
				}
			}
		}//end of for loop
		return responseFromServer;
	}//end of receiveResponseFromServer()
	
	/*
	 * processResponse(String receivedResponse, String file, DatagramSocket clientSocket, ClientServerUtility utility)
	 * This function processes the response based on the response code.
	 * 
	 * clientSocket : DatagramSocket object which has the details of the client socket
	 * file - The file whose contents have to be viewed
	 * utility - Object of ClientServerUtility class
	 * receivedResponse - The response from the server
	 */
	public static void processResponse(String receivedResponse, String file, DatagramSocket clientSocket, ClientServerUtility utility) throws Exception{
		String[] splitResponse = receivedResponse.split("\r\n");//split the response into separate fields with CRLF as delimiter 
		String responseCode = splitResponse[1];//The response code will be the 2nd field.
		
		if(responseCode.matches("0")) {
			//The response is OK
			String fileContent = "";//The content of the requested file
			String contentAndIntegrityValue = "";//The file content and integrity value will be a single string as there is no CRLF at the end of file
			for(int i=3; i<splitResponse.length-1; i++) {
				//Combine all the lines of the file content and integrity value into 1 string
				contentAndIntegrityValue = contentAndIntegrityValue + splitResponse[i] + "\r\n";
			}
			int contentLength = Integer.parseInt(splitResponse[2]);
			fileContent = contentAndIntegrityValue.substring(0, contentLength);//Extract only the file content
			System.out.printf("\n\nThe contents of the requested file is: \n%s",fileContent);
		}
		else if(responseCode.matches("1")) {
			//Integrity check failure. Re-send the request if required.
			Scanner inp = new Scanner(System.in);
			System.out.print("\nError: Integrity check failure. The request has one or more bit errors");
			System.out.print("\nDo you want to resend the request message? (yes/no)");
			String resendRequest = inp.next();
			if(resendRequest.matches("yes")) {
				messageHandling(file, utility); //Re-send request and be ready to receive response again.
			}
			else {
				System.exit(0);
			}	
		}
		else if(responseCode.matches("2")) {
			//Syntax of the request is wrong.
			System.out.print("\nError: Malformed request. The syntax of the request message is not correct");
			clientSocket.close();
			System.exit(0);
		}
		else if(responseCode.matches("3")) {
			//The requested file does not exist in the server.
			System.out.print("\nError: Non-existent file. The file with the requested name does not exist");
			clientSocket.close();
			System.exit(0);
		}
		else if(responseCode.matches("4")) {
			//Protocol version is wrong. It should only be 1.0
			System.out.print("\nError: Wrong protocol version. The version in the request is different from 1.0");
			clientSocket.close();
			System.exit(0);
		}
	}//end of processResponse()
	
}// end of class Client
