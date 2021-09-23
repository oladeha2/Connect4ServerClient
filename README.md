# ameen-int

Github repo representing take home assignment submission. 

### Set Up
To set up so the code can be tested please use the Intellij IDE. The following must be considered and set up for the submission to work.
1. IDE must be set to use Java 11 
2. Project can be opened and imported using the pom.xml file
3. Install all maven dependencies using the IDE or the command `mvn clean install`. This should be done automatically when the project is opened in the IDE
4. To set up the correct application contexts so the server and the two clients can be run please do the following:
    1. In top right corner of the IDE (beside the run button) press 'Edit Configurations' this should open the configurations dialogue
    2. Click the plus button and select 'Spring Boot'. In the main class input box enter the following `com.connect.five.game.server.ServerApplication`. Make sure to give the new configuration a name of your choosing. This is the class that houses the main server application. It will allow you to start the server when ready
    3. Follow the same steps as above for creating a new configuration. Instead of selecting  'Spring Boot' select the 'Application' option when creating the configuration. Give this configuration a name like 'Player One' and in the main class text input enter `com.connect.five.game.client.Connect4Client`. This will be the first player client.
    4. Follow the same steps as step 3 but give the new configuration the name 'Player 2'. This will be the second players client. 
5. Run the server configuration, by selecting it in the drop down and pressing the green run button in the top right corner. 
6. After the server has been started run both clients in any order and the game can then be started by following the steps printed to the command line

### How the Game Works for the Player
The client is asked to enter their name on start up. After entering their name, they can check on command whether a player has joined their game based on options provided to them. Once a player has joined they will be told this in response to one of their requests. When two players are present, the user has two options:
1. Checking if it is their turn. If it is their turn they will be instructed to play. Their turn will be prevented from being out of bounds of the board and also validated that they are not playing in a column that is full, this is done via a check on the server side. 
2. Leaving the game. Once the player leaves the game will end for them. When the other player requests to check if it is their turn, they will be informed that they have won.

In general for the case of winners the user will be informed of who the winner of the game is after they have requested a check if it their turn.

### Testing
For testing, the entire server use cases are fully united tested using Junit. In regards to testing the client some issues were ran into in regards to dealing with user input. 
Initially for processing using input a wrapper class was created called `UserInputReader`. Processing of the input was done via the `readInput` function of that class. Each call to this function took in an enum type that represented the type of input at that current moment (for example taking in the users name at the start of the game).
The intention was to use mockito to mock out when  `UserInputReader.readInput(InputType)` was so i could return what was necessary for the input type requested.. This can be seen `Connect4ClientTest`. When the test was ran, it appeared the `setUp()` function for the client did not run correctly. I tried calling the `setUp()` function of the client in the `BeforeEach` of the 
test class and it appeared to be successfully called there. My conclusion was it was something to do with the `@Test` annotation when running tests. Solutions online were quite convoluded and did not really mesh with how I wrote the code for the client. With more time, 
figuring out how to properly test the client is something I would focus on. 

Note: Mockito was used in this case as it was mentioned that this is what is used at the company for testing. I wanted to show my knowledge of it and how some of your dev choices have to be made to ensure you can test it successfully. 
For example the `HttpRequester` class was created to allow the calls made to the server during testing the client mockable using mockito

Tests can be run via the IDE or using the `mvn clean test` command

### Potential Improvements
1. Creating the game to be able to run multiple games at the same time
2. Improved testing of client
3. Throwing some custom exceptions on the server side.

### Most Interesting and Challenging Parts of Take Home Challenge
1. The length and depth of the challenge was a bit more than I expected
2. Writing a java class that processed user input. I have not written one of these since college lol.
3. Checking for diagonal wins on the board after each turn. Having to account for both directions of a diagonal and also adding into the solution the fact that it is not possible to have a diagonal win from every place in the board took a while to figure out. 
4. Writing a comprehensive set of tests for the server. Did not expect this from a coding challenge, but its presence makes sense given how important testing is and how much it was stressed as important during the previous interviews. 

Appreciate the opportunity you guys have given me. 

Look forward to hearing from you guys again. 

Ameen. 
