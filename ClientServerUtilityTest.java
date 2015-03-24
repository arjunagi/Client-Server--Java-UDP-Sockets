package client;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

public class ClientServerUtilityTest {
	
	String mockData = "ENTS/1.0 Request\r\nfileName\r\nintegrityValue\r\n";
	ClientServerUtility clientServerUtilityObj;

	/*
	 * Create new ClientServerUtility object before every test method
	 */
	@Before
	public void setUp() throws Exception {
		clientServerUtilityObj = new ClientServerUtility();
	}
	
	@Test
  public void testConstructedObject() {
    //Testing if the above constructed object is not null
    assertNotNull(clientServerUtilityObj);
  }// end of testConstructedObject()
  
	/*
	 * messageInBytesShouldReturnMessageInBytes()
	 * This test checks if the message is converted to bytes.
	 */
	@Test
	public void messageInBytesShouldReturnMessageInBytes() {
		assertArrayEquals(mockData.getBytes(), clientServerUtilityObj.messageInBytes(mockData));
	}//end of messageInBytesShouldReturnMessageInBytes()

	
	/*
	 * The next 5 methods tests isFileNameSyntaxCorrect() method.
	 */
	
	/*
	 * isFileNameSyntaxCorrectShouldReturnTrue()
	 * This test checks if the name of the requested file has the correct syntax
	 */
	@Test
	public void isFileNameSyntaxCorrectShouldReturnTrue() {
		String fileName = "directors_message.txt";
		assertTrue(clientServerUtilityObj.isFileNameSyntaxCorrect(fileName, "request"));
	}// end of isFileNameSyntaxCorrectShouldReturnTrue()
	
	/*
	 * fileNameWithoutExtensionShouldThrowException()
	 * This test checks if the name of the requested file has the correct syntax.
	 * The file name has no extension. Hence it should throw an exception.
	 */
	@Test (expected = IllegalArgumentException.class)
	public void fileNameWithoutExtensionShouldThrowException() {
		String fileName = "directors_message";
		assertFalse(clientServerUtilityObj.isFileNameSyntaxCorrect(fileName, "request"));
	}// end of fileNameWithoutExtensionShouldThrowException()

	/*
	 * fileNameWithSpecialCharacterShouldThrowException()
	 * This test checks if the name of the requested file has the correct syntax.
	 * The file name has special characters. Hence it should throw an exception.
	 */
	@Test (expected = IllegalArgumentException.class)
	public void fileNameWithSpecialCharacterShouldThrowException() {
		String fileName = "$$$$directors_message@@@@@.txt";
		assertFalse(clientServerUtilityObj.isFileNameSyntaxCorrect(fileName, "request"));
	}//end of fileNameWithSpecialCharacterShouldThrowException()
	
	/*
	 * fileNameDoesNotStartWithAlphabetsShouldThrowException()
	 * This test checks if the name of the requested file has the correct syntax.
	 * The file name does not start with alphabets. Hence it should throw an exception.
	 */
	@Test (expected = IllegalArgumentException.class)
	public void fileNameDoesNotStartWithAlphabetsShouldThrowException() {
		String fileName = "1234directors_message.txt";
		assertFalse(clientServerUtilityObj.isFileNameSyntaxCorrect(fileName, "request"));
	}// end of fileNameDoesNotStartWithAlphabetsShouldThrowException()
	
	/*
	 * extensionHasSpecialCharactersShouldThrowException()
	 * This test checks if the name of the requested file has the correct syntax.
	 * The extension should be alphanumeric, but here it contains special characters. Hence it should throw an exception.
	 */
	@Test (expected = IllegalArgumentException.class)
	public void extensionHasSpecialCharactersShouldThrowException() {
		String fileName = "directors_message.@@@txt";
		assertFalse(clientServerUtilityObj.isFileNameSyntaxCorrect(fileName, "request"));
	}// end of extensionHasSpecialCharactersShouldThrowException()
	
	
	
	/*
	 * getIntegrityCheckValueShouldReturnIntegrityValueAsString()
	 * This test checks if the returned integrity value matches the expected integrity value for the gives message.
	 */
	@Test
	public void getIntegrityCheckValueShouldReturnIntegrityValueAsString() {
		String mockDataWithoutIntegrityValue = "ENTS/1.0 Request\r\nfileName\r\n"; //We calculate integrity value for a message without the integrity field.
		assertEquals("11482", clientServerUtilityObj.getIntegrityCheckValue(mockDataWithoutIntegrityValue));
	}//end of getIntegrityCheckValueShouldReturnIntegrityValueAsString()

	/*
	 * isIntegrityValueOfMessageCorrectShouldReturnTrue()
	 * This test checks if the integrity value of the "request" is correct.
	 */
	@Test
	public void isIntegrityValueOfRequestMessageCorrectShouldReturnTrue() {
		String mockDataWithIntegrityValue = "ENTS/1.0 Request\r\nfileName\r\n11482\r\n";
		assertTrue(clientServerUtilityObj.isIntegrityValueOfMessageCorrect(mockDataWithIntegrityValue, "request"));
	}//end of isIntegrityValueOfRequestMessageCorrectShouldReturnTrue()
	
}//end of test class ClientServerUtilityTest
