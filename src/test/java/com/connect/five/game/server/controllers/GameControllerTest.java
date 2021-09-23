package com.connect.five.game.server.controllers;

import com.connect.five.game.server.TestUtils;
import com.connect.five.game.server.classes.GameState;
import com.google.gson.Gson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;


public class GameControllerTest {

    GameController gameController;
    Gson gson;

    @BeforeEach
    private void setUp() {
        gameController = new GameController();
        gson = new Gson();
    }

    @Test
    public void testOk() {
        String okay = gameController.getStatus();
        assertEquals(okay, "OK");
    }

    @Test
    public void allowsFirstPlayerToJoinGameAndGivesCorrectToken() {
        String userName =  "test";
        GameState expectedGameState = TestUtils.createGame(TestUtils.createUsers(new String[]{userName}));
        expectedGameState.setCurrentTurn(userName);
        GameState resultGameState = gameController.joinGame(userName);
        assertEquals(gson.toJson(expectedGameState), gson.toJson(resultGameState));
    }

    @Test
    public void allowsSecondPlayerToJoinGameAndGivesCorrectToken() {
        String playerOne = "test";
        String playerTwo = "test2";
        GameState expectedGameSate = TestUtils.createGame(TestUtils.createUsers(new String[]{playerOne, playerTwo}));
        expectedGameSate.setCurrentTurn(playerOne);
        expectedGameSate.setNextTurn(playerTwo);
        gameController.joinGame(playerOne);
        GameState result = gameController.joinGame(playerTwo);
        assertEquals(gson.toJson(expectedGameSate), gson.toJson(result));
    }

    @Test
    public void doesNotAllowPlayerWithSameNameToJoinGame() {
        assertThrows(ResponseStatusException.class, () -> {
           gameController.joinGame("playerOne");
           gameController.joinGame("playerOne");
        });
    }

    @Test
    public void doesNotAllowThirdPlayerToEnterGame() {
        gameController.joinGame("playerOne");
        gameController.joinGame("playerTwo");
        assertThrows(ResponseStatusException.class, () -> gameController.joinGame("player3"));
    }

    @Test
    public void allowsPlayerToLeave() {
        String playerOne = "p1";
        String playerTwo = "p2";
        GameState expectedGame = TestUtils.createGame(TestUtils.createUsers(new String[]{playerOne}));
        expectedGame.setWinner(playerOne);
        gameController.setGame(TestUtils.createGame(TestUtils.createUsers(new String[]{playerOne, playerTwo})));
        GameState resultingGame = gameController.leaveGame(playerTwo);
        assertEquals(gson.toJson(resultingGame), gson.toJson(expectedGame));
    }

    @Test
    public void doesNotAllowPlayerToLeaveIfTwoPlayersNotInGame() {
        String playerOne = "p1";
        gameController.setGame(TestUtils.createGame(TestUtils.createUsers(new String[]{playerOne})));
        assertThrows(ResponseStatusException.class, () -> gameController.leaveGame(playerOne));
    }

    @Test
    public void doesNotAllowPlayerToLeaveIfPlayerNameDoesNotExist() {
        String playerOne = "p1";
        gameController.setGame(TestUtils.createGame(TestUtils.createUsers(new String[]{playerOne})));
        assertThrows(ResponseStatusException.class, () -> gameController.leaveGame("test"));
    }

    @Test
    public void successfulPlayerMove() {
        String playerOne = "p1";
        gameController.setGame(TestUtils.createGameWithTwoUserAndSetTurns(playerOne, "p2"));
        GameState res = gameController.takeTurn(playerOne, 1);
        assertEquals(1, res.getGrid()[5][0]);
    }

    @Test
    public void successivePlayerMovesAreStackedOnEachOther() {
        String playerOne = "p1";
        String playerTwo = "p2";
        gameController.setGame(TestUtils.createGameWithTwoUserAndSetTurns(playerOne, playerTwo));
        gameController.takeTurn(playerOne, 1);
        GameState res = gameController.takeTurn(playerTwo, 1);
        assertEquals(2, res.getGrid()[4][0]);
    }

    @Test
    public void throwsExceptionWhenPlayerTriesToMoveWhenItsNotTheirTurn(){
        String playerTwo = "p2";
        gameController.setGame(TestUtils.createGameWithTwoUserAndSetTurns("p1", playerTwo));
        assertThrows(ResponseStatusException.class, () -> gameController.takeTurn(playerTwo, 1));
    }

    @Test
    public void throwsExceptionWhenNonExistentUserTriesToMove(){
        gameController.setGame(TestUtils.createGameWithTwoUserAndSetTurns("p1", "p2"));
        assertThrows(ResponseStatusException.class, () -> gameController.takeTurn("does not exist", 1));
    }

    @Test
    public void throwsExceptionWhenMoveOutOfBounds(){
        String playerOne = "p1";
        String playerTwo = "p2";
        gameController.setGame(TestUtils.createGameWithTwoUserAndSetTurns(playerOne, playerTwo));
        assertThrows(ResponseStatusException.class, () -> gameController.takeTurn(playerOne, 10));
    }


    @Test
    public void throwsExceptionWhenPlayerMovesInFullColumn() {
        String playerOne = "p1";
        gameController.setGame(TestUtils.createGameWithTwoUserAndSetTurns(playerOne, "p2"));
        for (int i = 0; i <= 5; i ++) {
            gameController.getGame().getGrid()[i][0] = 1; // fill the columns to top
        }
        assertThrows(ResponseStatusException.class, () -> gameController.takeTurn(playerOne, 1));
    }

    @Test
    public void verticalWinner() {
        String playerOne = "p1";
        gameController.setGame(TestUtils.createGameWithTwoUserAndSetTurns(playerOne, "p2"));
        for (int i = 5; i >= 2 ; i--) {
            gameController.getGame().getGrid()[i][3] = 1; // fill the columns till one move away from win
        }
        GameState gameWon = gameController.takeTurn(playerOne, 4);
        assertEquals(playerOne, gameWon.getWinner());
    }

    @Test
    public void horizontalWinner() {
        String playerOne = "p1";
        gameController.setGame(TestUtils.createGameWithTwoUserAndSetTurns(playerOne, "p2"));
        for (int i = 0; i <= 3 ; i++) {
            gameController.getGame().getGrid()[5][i] = 1; // fill the columns till one move away from win
        }
        GameState gameWon = gameController.takeTurn(playerOne, 5);
        assertEquals(playerOne, gameWon.getWinner());
    }

    @Test
    public void diagonalWinnerNegativeSlope() {
        String playerOne = "p1";
        gameController.setGame(TestUtils.createGameWithTwoUserAndSetTurns(playerOne, "p2"));
        for(int i = 1; i <= 4; i++) {
            gameController.getGame().getGrid()[i][i] = 1;
        }
        GameState gameWon = gameController.takeTurn(playerOne, 4);
        assertEquals(playerOne, gameWon.getWinner());
    }

    @Test
    public void diagonalWinnerPositiveSlope() {
        String playerOne = "p1";
        gameController.setGame(TestUtils.createGameWithTwoUserAndSetTurns(playerOne, "p2"));
        for (int i = 0; i < 4; i++){
            gameController.getGame().getGrid()[4-i][i+1] = 1;
        }
        GameState gameWon = gameController.takeTurn(playerOne, 1);
        assertEquals(playerOne, gameWon.getWinner());
    }
}
