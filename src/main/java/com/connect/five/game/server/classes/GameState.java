package com.connect.five.game.server.classes;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;
import java.util.HashMap;

@Getter
@Setter
public class GameState {

    private Map<String, User> users = new HashMap<>();
    private int[][] grid = new int[6][9];
    private String winner = "";
    private String currentTurn = "";
    private String nextTurn;
    private final int ROWS = 6;
    private final int COLUMNS = 9;
}
