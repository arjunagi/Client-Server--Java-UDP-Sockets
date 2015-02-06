package client;

/*
 * class ClientServerUtility
 * This class contains utility methods for common functionalities
 * present both in Server and Client classes. 
 * 1. Convert message string to message bytes
 * 2. Check syntax of file name
 * 3. Get integrity value
 * 4. Compare the received integrity value with calculated one.
 */
public class ClientServerUtility {
	
	/*
	 * messageInBytes(String assembledMessage)
	 * This class converts the message into bytes.
	 * 
	 * assembledMessage: The message(request or response) which has to be converted to bytes
	 * @return: Return the byte array of the message.
	 */
	public byte[] messageInBytes(String assembledMessage) {
		byte[] messageBytes = assembledMessage.getBytes();// get the byte form of the message string
		int byteToInteger; //convert the byte form to int. Used only for printing.
		System.out.print("\nMessage to be sent in byte form: ");
		for(int i=0; i<messageBytes.length; i++) {
			byteToInteger = messageBytes[i] & 0xff;//convert byte to int
			System.out.printf("%d,", byteToInteger);
		}
		return messageBytes;
	}//end of messageInBytes()
	
	/*
	 * isFileNameSyntaxCorrect(String file, String typeOfMessage)
	 * Checks if the syntax of file name is correct. Returns true if correct, else false.
	 * 
	 * file - the file name whose syntax has to be checked.
	 * typeOfMessage - is it a request or a response
	 * @return: true if correct, else false.
	 */
	public boolean isFileNameSyntaxCorrect(String file, String typeOfMessage) {	
		if(file.contains(Character.toString('.'))) {
			//Check if a period is present in the file name
			int dot = file.indexOf(".");
			String name = file.substring(0, dot);//file name without extension.
			String extension = file.substring(dot+1, file.length());	
			if(name.matches("^[a-zA-Z][a-z A-Z 0-9_]*$")) {
				//name should only be alphanumeric with "_"
				if(extension.matches("^[a-z A-Z 0-9]*$")) {
					//extension should be alphanumeric only
			    return true;
				}
				else
					if(typeOfMessage.equals("request"))
						//Throw exception only when processing request message. For response, response code 2 has to be sent.
					  throw new IllegalArgumentException("The sytax of the extension part of the file name is wrong!");
			}
			else
				if(typeOfMessage.equals("request"))
				//Throw exception only when processing request message. For response, response code 2 has to be sent.
				  throw new IllegalArgumentException("The sytax of the name part of the file name is wrong!");
		}	
		if(typeOfMessage.equals("request"))
		  //Throw exception only when processing request message. For response, response code 2 has to be sent.
		  throw new IllegalArgumentException("The file name must contain a period (.) between the name and extension.");
		
		return false;
	}// end of isFileNameSyntaxCorrect()
	
	/*
	 * getIntegrityCheckValue(String assembledRequest)
	 * Calculates the integrity value of the message without the integrity check field.
	 * 
	 * assembledRequest - the request whose integrity value has to be calculated.
	 * @return: valueInCharacterForm - the calculated integrity value in string 
	 */
	public String getIntegrityCheckValue(String assembledRequest) {
		String valueInCharacterForm = ""; //integrity value in string form
		int[] integrityCheckData = new int[8000];//The 16 bit words
    int asciiValue1,asciiValue2; // 8 bit words which are concatenated to get 16 bit word 
    String binary, binary1, binary2; // binary values of the ascii values.
    int numberOfWords=0; //number of 16 bit words generated
    for(int i=0;i<assembledRequest.length();i=i+2)
    {	
      if(i>assembledRequest.length())
    	  break; //when i goes greater than length of string.
      asciiValue1=assembledRequest.charAt(i); //The even numbered characters
      if((i==assembledRequest.length()-1)&&(assembledRequest.length()%2!=0))
	      asciiValue2=0; //When there are odd number of characters, we append 0 to make it even.  	
	    else
	      asciiValue2=assembledRequest.charAt(i+1);//The odd numbered characters
    
      binary1 = Integer.toBinaryString(asciiValue1);//convert even numbered characters to binary
      binary2 = Integer.toBinaryString(asciiValue2);//convert odd numbered characters to binary
      binary1=String.format("%8s", binary1).replace(' ', '0');//If the binary is less than 8 bits, append 0s
      binary2=String.format("%8s", binary2).replace(' ', '0');//If the binary is less than 8 bits, append 0s
      binary=binary1+binary2;//append the consecutive even and odd binaries to get 16bit word
      int binaryWordToInt = Integer.parseInt(binary, 2);//convert the combined binary(in string format) to int
      integrityCheckData[i/2]=binaryWordToInt;//Store the combined values in an int array.
      numberOfWords++;
    }
    int s=0; 
    for (int i=0;i<numberOfWords;i++)
    {
    	int index=s^integrityCheckData[i];
    	s=(7919*index)%65536;
    }
      valueInCharacterForm=String.valueOf(s);
      if(!valueInCharacterForm.matches("^[0-9]*$")) { 
  			System.out.print("\nIntegrity check value is incorrect!");
  			System.exit(0);
  		}
		return valueInCharacterForm;
	}// end of getIntegrityCheckValue()
	
	/*
	 * isIntegrityValueOfMessageCorrect(String receivedMessage, String typeofMessage)
	 * Compares the integrity value received in the message and the calculated integrity value.
	 * 
	 *  typeOfMessage - Is the message a request or response
	 *  @return: Returns true if both are same, else false.
	 */
	public Boolean isIntegrityValueOfMessageCorrect(String receivedMessage, String typeofMessage) {
		String[] splitMessage = receivedMessage.split("\r\n");//split the received message with CR+LF as delimiter 
		String messageWithoutIntegrityValue = "";//The part of message without integrity value. Required to calculate integrity value 
		String integrity = "";
		
		if(typeofMessage.equals("response"))
		{
			String contentAndIntegrityValue = "";//The file content and integrity value will be a single string
		  for(int i=0; i<3; i++) {
			  //Store the 1st line, response code and content length in the string first
			  messageWithoutIntegrityValue = messageWithoutIntegrityValue + splitMessage[i] + "\r\n";
		  }
		  for(int i=3; i<splitMessage.length-1; i++) {
			  //The content of the file and integrity value will be a single string as there is no CRLF at end of file content
			  contentAndIntegrityValue = contentAndIntegrityValue + splitMessage[i] + "\r\n";
		  }
		  int contentLength = Integer.parseInt(splitMessage[2]); //length of file content
		  String fileContent = contentAndIntegrityValue.substring(0, contentLength); //content of the requested file. Removing the appended integrity value
		  messageWithoutIntegrityValue = messageWithoutIntegrityValue + fileContent;//append only the file content
		  integrity = contentAndIntegrityValue.substring(contentLength);//Extract the integrity value
		}
		else if(typeofMessage.equals("request")) {
		  integrity = splitMessage[2];//In request, integrity value is the 3rd field
			messageWithoutIntegrityValue = splitMessage[0]+"\r\n"+splitMessage[1]+"\r\n";//Calculate the integrity value of the first 2 fields
		}
		
		integrity = integrity.replaceAll("\\r\\n", "");//remove the CR+LF which is present after the integrity value
		if(getIntegrityCheckValue(messageWithoutIntegrityValue).matches(integrity)) {
			//The integrity value received as part of the response matches the calculated integrity value
			System.out.print("\n\nThe calculated integrity value of the message matches the integrity check field of the response");
			return true;
		}
	  //The integrity value received as part of the response does not match the calculated integrity value
		System.out.print("\n\nThe calculated integrity value of the message does not match the integrity check field of the response");
		return false;
	}//end of isIntegrityValueOfMessageCorrect()
	
}//end of class ClientServerUtility
