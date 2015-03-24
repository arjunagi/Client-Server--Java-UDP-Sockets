package client;

import static org.junit.Assert.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;


@RunWith(PowerMockRunner.class)                                                     // This is to be used when we use PowerMock
@PrepareForTest({DatagramSocket.class, Client.class, DatagramPacket.class})         // This is to be used when we use PowerMock 
public class ClientTest {
     
	  String fileName = "directors_message.txt";
    Client clientObj;
    private DatagramSocket clientSocketMock;
    
    //This can be used to Mock objects - Using Mockito
    @Mock
    private DatagramPacket dataPacketMock;
    private byte[] requestBytesMock = "ENTS/1.0 Request\r\nfileName\r\nintegrityValue\r\n".getBytes();
    private byte[] responseBytesMock = "ENTS/1.0 Response\r\n0\r\nlength of  data\r\nData\r\nintegrity value\r\n".getBytes();

    /*
  	 * Create new Client object before every test method
  	 */
    @Before
    public void setUp() throws Exception {
        clientObj = new Client();
    }


    @Test
    public void testConstructedObject() {
      //Testing if the above constructed object is not null
      assertNotNull(clientObj);
    }

    
    @Ignore
  	public void testMain() throws Exception {
  		Client.main(new String[] {"arg1", "arg2"}); //Use Client and not clientObj as static method belongs to class, not object
  	}
    
   
    /*
     * This method tests if the messageHandling() calls the handleRequest(), handleResponse()
     * and isIntegrityValueOfMessageCorrect() methods.
     */
    @Test
    public void testMessageHandlingUsingMocks() throws Exception {
    	
      ClientServerUtility utilityMock =  Mockito.mock(ClientServerUtility.class); //Mocking the dependent class ClientServerUtility
      clientSocketMock = Mockito.mock(DatagramSocket.class); //Mocking the dependent class DatagramSocket
      
      // Use Power mock to create new constructor initialization mocks. Here "clientSocketMock" mock object is returned whenever new "DatagramSocket" obj is created
      PowerMockito.whenNew(DatagramSocket.class).withNoArguments().thenReturn(clientSocketMock);
      
      // Use Power mock -mockStatic to initialize final/static class objects. To make all the methods of the class static.
      PowerMockito.mockStatic(DatagramPacket.class);
      dataPacketMock = PowerMockito.mock(DatagramPacket.class);
      //Mock the behavior of the DatagramPacket constructor.
      PowerMockito.whenNew(DatagramPacket.class).withArguments(Mockito.any(byte[].class), Mockito.anyInt()).thenReturn(dataPacketMock);

      //Mocking the behaviors of the mock objects . Hence if the below methods are called on mocks, then you can send any value you want
      Mockito.when(utilityMock.messageInBytes(Mockito.anyString())).thenReturn(requestBytesMock ); //for handleRequest()
      Mockito.when(dataPacketMock.getData()).thenReturn(requestBytesMock ); // in receiveResponseFromServer()
      Mockito.when(utilityMock.isIntegrityValueOfMessageCorrect(Mockito.anyString(),Mockito.anyString())).thenReturn(true);
           
      //Calling the test method
      clientObj.messageHandling("directors_message.txt", utilityMock);
           
      //Since nessageHandling is a void method, we are verifying if the mock objects were indeed called only once.
      Mockito.verify(utilityMock, Mockito.times(1)).messageInBytes(Mockito.anyString());
      Mockito.verify(utilityMock, Mockito.times(1)).isIntegrityValueOfMessageCorrect(Mockito.anyString(), Mockito.anyString());
    }// end of testMessageHandlingUsingMocks()
    
    
    /* 
  	 * handleRequestShouldReturnDatagramSocketObj()
  	 * This test checks if the handleRequest(String file, ClientServerUtility utility) method 
  	 * returns a non-null DatagramSocket object (which contains the details of the client socket). 
  	 */
    @Test
  	public void handleRequestShouldReturnDatagramSocketObj() throws Exception {
  		ClientServerUtility utilityMock = Mockito.mock(ClientServerUtility.class); //Create a mock object of ClientServerUtility class
  		Mockito.when(utilityMock.messageInBytes(Mockito.anyString())).thenReturn(requestBytesMock);
  		
  		DatagramSocket clientSocketMock = PowerMockito.mock(DatagramSocket.class);
  		PowerMockito.whenNew(DatagramSocket.class).withNoArguments().thenReturn(clientSocketMock); //Mocking the constructor of DatagramSocket
  		
  		assertSame(clientObj.handleRequest(fileName, utilityMock),clientSocketMock);
  	}//end of handleRequestShouldReturnDatagramSocketObj()

    
    /*
  	 * generateRequestMessageShouldReturnMessage()
  	 * This method tests generateRequestMessage() and checks if the generated request 
  	 * is correct by comparing it with the expected request message.
  	 */
  	@Test
  	public void generateRequestMessageShouldReturnMessage() throws Exception {
  		ClientServerUtility utilityMock = Mockito.mock(ClientServerUtility.class); //Create a mock object of ClientServerUtility class
  		String assembledRequest = "ENTS/1.0 Request\r\n" + fileName + "\r\n"; //Create the message with the first and the second lines.
  		Mockito.when(utilityMock.getIntegrityCheckValue(assembledRequest)).thenReturn("21"); //We need to get the integrity value of the created message. This value will be the 3rd line.
  		String expectedRequest = "ENTS/1.0 Request\r\ndirectors_message.txt\r\n"+utilityMock.getIntegrityCheckValue(assembledRequest)+"\r\n"; //Create the expected message with the integrity value
  		assertEquals(expectedRequest, Client.generateRequestMessage(fileName, utilityMock)); //assert if the generated request message is same as the expected message
  	}//end of generateRequestMessageShouldReturnMessage()
  	
  	
  	/*
  	 * sendRequestToServerShouldReturnDataGramSocketObj()
  	 * This method tests if the request is sent and the returns the socket details 
  	 * which is required to receive the response.  
  	 */
  	 @Test
     public void sendRequestToServerShouldReturnDataGramSocketObj() throws Exception {
  		 byte[] requestToBeSent = "ENTS/1.0 Request\r\ndirectors_message.txt\r\n21\r\n".getBytes();
       DatagramSocket clientSocketMock = PowerMockito.mock(DatagramSocket.class);
       PowerMockito.whenNew(DatagramSocket.class).withNoArguments().thenReturn(clientSocketMock);        
       assertSame(clientObj.sendRequestToServer(requestToBeSent), clientSocketMock);
     }//end of sendRequestToServerShouldReturnDataGramSocketObj()
  	 
  	 
  	 /*
  		* handleResponseShouldReturnReceivedResponse()
  		* This method checks if handleResponse() receives the expected response from the server.
  		*/
  		@Test
  		public void handleResponseShouldReturnReceivedResponse() throws Exception {
  			ClientServerUtility utilityMock = Mockito.mock(ClientServerUtility.class); //Create a mock object of ClientServerUtility class
  		  // Use Power mock -mockStatic to initialize final/static class objects. To make all the methods of the class static.
        PowerMockito.mockStatic(DatagramPacket.class);
        PowerMockito.mockStatic(DatagramSocket.class);
        dataPacketMock = PowerMockito.mock(DatagramPacket.class);
        clientSocketMock = PowerMockito.mock(DatagramSocket.class);
        
        //Mock the behavior of the DatagramPacket constructor.
        PowerMockito.whenNew(DatagramPacket.class).withArguments(Mockito.any(byte[].class), Mockito.anyInt()).thenReturn(dataPacketMock);
       
        //Mocking the behaviors of the mock objects . Hence if the below methods are called on mocks, then you can send any value you want
        Mockito.when(dataPacketMock.getData()).thenReturn(responseBytesMock ); // in receiveResponseFromServer()
        
  			assertEquals(new String(responseBytesMock), clientObj.handleResponse(clientSocketMock, fileName, utilityMock));
  		} //end of handleResponseShouldReturnReceivedResponse()
  	
      
  		/*
       * This method checks if receiveResponse() receives the expected response BYTES from the server.
       */
  		@Test
  		public void receiveResponseFromServerShouldReturnResponseBytes() throws Exception {
  			ClientServerUtility utilityMock = Mockito.mock(ClientServerUtility.class); //Create a mock object of ClientServerUtility class
  		  // Use Power mock -mockStatic to initialize final/static class objects. To make all the methods of the class static.
        PowerMockito.mockStatic(DatagramPacket.class);
        PowerMockito.mockStatic(DatagramSocket.class);
        dataPacketMock = PowerMockito.mock(DatagramPacket.class);
        clientSocketMock = PowerMockito.mock(DatagramSocket.class);
        
        //Mock the behavior of the DatagramPacket constructor.
        PowerMockito.whenNew(DatagramPacket.class).withArguments(Mockito.any(byte[].class), Mockito.anyInt()).thenReturn(dataPacketMock);

        //Mocking the behaviors of the mock objects . Hence if the below methods are called on mocks, then you can send any value you want
        Mockito.when(dataPacketMock.getData()).thenReturn(responseBytesMock); // in receiveResponseFromServer()
        
  			assertEquals(responseBytesMock, clientObj.receiveResponseFromServer(clientSocketMock, fileName, utilityMock));  			
  		} //end of receiveResponseFromServerShouldReturnResponseBytes()
  	 
}// end of Test class ClientTest.java
