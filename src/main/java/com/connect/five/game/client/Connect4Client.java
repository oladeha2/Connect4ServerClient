package com.connect.five.game.client;

import com.connect.five.game.server.classes.GameState;
import com.connect.five.game.server.classes.HttpRequester;
import com.connect.five.game.server.classes.UserInputReader;
import com.connect.five.game.server.enums.InputType;

import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.net.URISyntaxException;



@Getter
@Setter
public class Connect4Client {

    private String userName;
    private GameState currentGame;
    private boolean exitGame = false;
    private UserInputReader reader;
    private HttpRequester requester = new HttpRequester();

    public Connect4Client(UserInputReader inputReader) {
        this.reader = inputReader;
    }

    public void setUp() throws IOException, URISyntaxException, InterruptedException {
        System.out.println("You Are Now Playing Connect 5");
        boolean validName = false;
        while (!validName) {
            System.out.println("Please Enter Your Name To Begin...");
            String name = reader.readInput(InputType.USER_NAME);
            GameState game = this.joinGame(name);
            if (game == null) {
                System.out.println("A Player in This Game Already Has That Name");
            } else {
                System.out.println("A Game Has Been Found :)");
                this.userName = name;
                this.currentGame = game;
                validName = true;
                this.startGame();
            }
        }
    }

    private void displayBoard() {
        for(int r = 0; r < this.currentGame.getROWS(); r ++) {
            for (int c = 0; c < this.currentGame.getCOLUMNS(); c++){
                if (c == this.currentGame.getCOLUMNS() - 1) {
                    System.out.print(String.format("[%d]\n", this.currentGame.getGrid()[r][c]));
                } else {
                    System.out.print(String.format("[%d]", this.currentGame.getGrid()[r][c]));
                }
            }
        }
    }

    private void displayMenu() throws IOException, URISyntaxException, InterruptedException {
        while (!exitGame) {
            System.out.println("Please Select An Option... ");
            if (this.currentGame.getNextTurn() == null) {
                System.out.println("Press [a] To Check if Another Player Has Joined");
            } else {
                System.out.println("Press [b] to Check if it is your turn to Play");
                System.out.println("Press [c] to Exit the Game");
            }
            String option = reader.readInput(InputType.MENU_OPTION);
            switch (option) {
                case "a": checkPlayerJoined();
                    break;
                case "b": checkTurnAndPlay();
                    break;
                case "c": leaveGame();
                    break;
                default: System.out.println("Please Enter A Valid Option....");
                    break;
            }
        }
        System.out.println("Thanks For Playing");
    }


    private void leaveGame() {
        if (currentGame.getUsers().size() < 2) {
            System.out.println("You Can Not Leave A Game That Does Not Have 2 Players");
        } else {
            this.currentGame = this.requester.makePostRequest("http://localhost:8080/leave/" + this.userName);
            this.exitGame = true;
        }
    }

    private void checkTurnAndPlay() throws InterruptedException, IOException, URISyntaxException {
        this.currentGame = this.requester.makeGetRequest("http://localhost:8080/state");
        if (!this.currentGame.getWinner().isEmpty()) {
            System.out.println(String.format("GAME IS OVER %s WON", this.currentGame.getWinner()));
            this.exitGame = true;
            return;
        }
        if (!this.currentGame.getCurrentTurn().equals(this.userName)) {
            System.out.println("It is Currently Not Your Turn to Play. Please Check Again Later...");
            return;
        } else {
            this.displayBoard();
            System.out.println(String.format("It is Your Turn Your Game Token is %d",
                    this.currentGame.getUsers().get(this.userName).getGameToken()));
            boolean correctValue = false;
            while (!correctValue) {
                System.out.println("Please Enter A Value Between 1 - 9");
                String value = reader.readInput(InputType.PLAY_TURN);
                try {
                    int number = Integer.parseInt(value);
                    if (number >=1 && number <= 9) {
                        System.out.println("Process Move");
                        correctValue  = this.processMove(value);
                    } else {
                        System.out.println("The Value You Entered Was Not Within 1 and 9 Please Try Again");
                    }
                } catch (NumberFormatException e) {
                    System.out.println("The Number You Have Entered Is Non Numerical");
                }
            }
        }
    }

    private boolean processMove(String move) {
        this.displayBoard();
        GameState game = this.requester.makePostRequest("http://localhost:8080/play/" + this.userName + "/" + move);
        if (game != null) {
            this.currentGame = game;
            System.out.println(String.format("You Played in Position %s Here is the Way The Board Looks", move));
            this.displayBoard();
            if (this.currentGame.getWinner().equals(this.userName)) {
                System.out.println("CONGRATS.. YOU ARE THE CONNECT 5 MASTER");
                this.exitGame = true;
            } else if (this.currentGame.getWinner().isEmpty()) {
                System.out.println("STILL NO WINNER.. KEEP PLAYING");
            } else { // not sure that this code can ever actually happen so may have to be removed
                System.out.println("YOU LOST.... LOL");
                this.exitGame = true;
            }
            return true;
        } else {
            System.out.println("The Row You Have Tried to Play In is Full Please Try Again");
        }
        return false;
    }

    private void checkPlayerJoined() {
        this.currentGame = this.requester.makeGetRequest("http://localhost:8080/state");
        if (this.currentGame.getNextTurn() != null) {
            System.out.println(String.format("A Player Has Joined Your Game. Their Name is %s. You Should Now Be Able To Start Playing",
                    this.currentGame.getNextTurn()));
        } else {
            System.out.println("No One Has Joined Yet");
        }
    }

    private void startGame() throws IOException, URISyntaxException, InterruptedException {
        this.displayMenu();
    }

    private GameState joinGame(String name) {
        System.out.println("Finding you a game.....");
        return this.requester.makePostRequest("http://localhost:8080/join/" + name);
    }

    public static void main(String[] args) throws IOException, URISyntaxException, InterruptedException {
        UserInputReader reader = new UserInputReader();
        Connect4Client client = new Connect4Client(reader);
        client.setUp();
    }
}
