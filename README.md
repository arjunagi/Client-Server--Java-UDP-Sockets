# Client-Server----Java-UDP-Sockets
A Java program that enables reliable data transfer between a server and a client over the UDP protocol. The application has a customized acknowledgement mechanism and integrity check algorithm in order to ensure the reliable data transfer and data security over UDP.
The client requests for the contents of one of the files available at the server. The server responds with the contents, if present, or sends the appropriate error message.


# Executing the application:
You will need two separate instances(workspaces) of Eclipse to execute this application - one for Client and other for Server.
Create seperate projects for the client and server packages and add the Client.java and Server.java files in respective src folders of the projects. Then follow the below steps:
1. Create 3 files file_A.txt, file_B.txt and file_C.txt.
2. Add the path of these files in Server.java file (line 165)
3. Add the ClientServerUtility.java file in the source folders of server and client packages. This utility class contains functionalities common to both client and server and hence should be present in both packages.
4. First, run the server package. The server should always be running and expecting requests from client.
5. Next, run the client package. You will then get an option to request the contents of any of the 3 files created in step 1. Select a file and verify the output. 


# Testing the Client class using JUnit
Install the JUnit4, Mockito and powermock JARs. Add these jars to the project buildpath.
1. Create a new test folder in the project and add it to the build path.
2. Add the ClientTest.java JUnit test file to this folder.
3. Run the file as JUnit test. (No inputs required from user as the required information is already provided in the test class)

#Testing the ClientServerUtility class using JUnit
Only JUnit JARs required.
1. Add the ClientServerUtilityTest.java JUnit test file to test folder created above.   
2. Run the file as JUnit test. (No inputs required from user as the required information is already provided in the test class)
