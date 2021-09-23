package com.connect.five.game.server;

import com.connect.five.game.server.classes.GameState;
import com.connect.five.game.server.classes.User;

import java.util.HashMap;
import java.util.Map;

public class TestUtils {

    public static GameState createGameWithTwoUserAndSetTurns(String playerOne, String playerTwo) {
        GameState setGameState = createGame(createUsers(new String[]{playerOne, playerTwo}));
        setGameState.setCurrentTurn(playerOne);
        setGameState.setNextTurn(playerTwo);
        return setGameState;
    }

    public static GameState createGame(Map<String, User> users) {
        GameState game = new GameState();
        game.setUsers(users);
        return game;
    }

    public static Map<String, User> createUsers(String[] userNames) {
        Map<String, User> users = new HashMap<>();
        for (int i = 0; i < userNames.length; i++) {
            users.put(userNames[i], new User(userNames[i], i+1));
        }
        return users;
    }
}
