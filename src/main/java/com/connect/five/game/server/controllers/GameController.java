package com.connect.five.game.server.controllers;

import com.connect.five.game.server.classes.GameState;
import com.connect.five.game.server.classes.User;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.server.ResponseStatusException;

@Getter
@Setter
@RestController
public class GameController {

    private GameState game = new GameState();
    Logger logger = LoggerFactory.getLogger(GameController.class);

    @GetMapping("/status")
    public String getStatus() {
        return "OK";
    }

    @PostMapping("/join/{userName}")
    public GameState joinGame(@PathVariable String userName) {
        logger.info(String.format("User %s is joining", userName));
        if (game.getUsers().size() >= 2) {
            logger.error("Two Users Are Already Playing A Game");
            throw new ResponseStatusException(HttpStatus.METHOD_NOT_ALLOWED, "Two Users Are Already Playing A Game");
        }
        if (game.getUsers().isEmpty()) {
            game.getUsers().put(userName, new User(userName, 1));
            game.setCurrentTurn(userName);
        } else {
            if (game.getUsers().containsKey(userName)){
                logger.error("User with this name already exists");
                throw new ResponseStatusException(HttpStatus.METHOD_NOT_ALLOWED, "User with this name already exists");
            }
            game.getUsers().put(userName, new User(userName, 2));
            game.setNextTurn(userName);
        }
        return game;
    }

    @PostMapping("/leave/{username}")
    public GameState leaveGame(@PathVariable String username){
        if (!game.getUsers().containsKey(username)) {
            throw new ResponseStatusException(HttpStatus.METHOD_NOT_ALLOWED, "Username Does Not Exist");
        }
        if (game.getUsers().size() < 2) {
            logger.error(String.format("User %s Can Not Leave Without Another Player",username));
            throw new ResponseStatusException(HttpStatus.METHOD_NOT_ALLOWED, "Can Not Leave Game Without Another Player");
        }
        for(String player : game.getUsers().keySet()) {
            if(!player.equals(username)) {
                game.setWinner(player);
                game.getUsers().remove(username);
            }
            break;
        }
        return game;
    }


    @GetMapping("/state")
    public GameState getGameState() {
        return game;
    }

    // return some sort of object that holds the state of the
    @PostMapping("/play/{userName}/{place}")
    public GameState takeTurn(@PathVariable String userName, @PathVariable int place) {
        if (place < 1 || place > 9) {
            logger.error("The Board only has 9 places");
            throw new ResponseStatusException(HttpStatus.METHOD_NOT_ALLOWED, "The Board only has 9 places");
        }
        if (!game.getCurrentTurn().equals(userName)) {
            logger.error("Please Wait Till it is Your Turn to Play");
            throw new ResponseStatusException(HttpStatus.METHOD_NOT_ALLOWED, "Please Wait Till it is Your Turn to Play");
        }
        if (!game.getUsers().containsKey(userName)) {
            logger.error("The User Entered Does Not Exist so this Move Can Not be Made");
            throw new ResponseStatusException(HttpStatus.METHOD_NOT_ALLOWED, "The User Entered Does Not Exist so this Move Can Not be Made");
        }
        User currentUser = game.getUsers().get(userName);
        boolean moved = false;
        int playedPosition = -1;
        for (int i = game.getROWS() - 1; i >= 0; i--) {
            if (game.getGrid()[i][place - 1] == 0) {
                game.getGrid()[i][place - 1] = currentUser.getGameToken();
                playedPosition = i;
                moved = true;
                break;
            }
        }
        logger.info(String.format("The Played Position is (%d, %d) by Player (%s)", playedPosition, place - 1, currentUser.getName()));
        if (!moved) {
            logger.error("This Column is Full");
            throw new ResponseStatusException(HttpStatus.METHOD_NOT_ALLOWED, "This Column is Full");
        }

        checkWinner(playedPosition, place - 1, currentUser);

        if (game.getWinner().isEmpty()){
            game.setCurrentTurn(game.getNextTurn());
            game.setNextTurn(userName);
        }
        // flip the turns for the game this is only if the game does not have a winner yet
        return game;
    }

    private void checkWinner(int playedPosition, int place, User currentUser) {
        // check the horizontal based on the row played in
        int horizontalMatch;
        int[] horizontalRow = game.getGrid()[playedPosition]; // get the row that the token has dropped on the board
        for (int columnInRow = 1; columnInRow < horizontalRow.length - 4; columnInRow++){
            // create sliding window to do checks
            horizontalMatch = 0;
            for (int i = columnInRow; i < columnInRow + 4; i++) {
                if (horizontalRow[i - 1] == horizontalRow[i] && horizontalRow[i] == currentUser.getGameToken()) {
                    horizontalMatch++;
                }
            }
            if (horizontalMatch == 4) { // five matches in the sliding window means winner // will have to do this check for all the matches
                logger.info(String.format("Winner on Horizontal Match. Winner Is: [%s]", currentUser.getName()));
                game.setWinner(currentUser.getName());
                return;
            }
        }

        // check the vertical for a group of five -> only ever have to look down
        // only possible vertical combination to get five in a row is if you have just played in the top two spaces on the game grid this is based on the dimensions of the grid given for the game
        if (playedPosition == 1 || playedPosition == 0){
            int verticalMatch = 0;
            for (int i = playedPosition + 1; i < playedPosition + 5; i++){
                if (game.getGrid()[i-1][place] == game.getGrid()[i][place] && game.getGrid()[i][place] == currentUser.getGameToken()) {
                    verticalMatch++;
                }
            }
            if (verticalMatch == 4){
                logger.info(String.format("Winner on Vertical Match. Winner Is: [%s]", currentUser.getName()));
                game.setWinner(currentUser.getName());
                return;
            }
        }

        // check positive and negative diagonals - try and not iterate over the whole board to do this
        for(int row = game.getROWS() - 1; row > game.getROWS() - 3; row--){
            for (int col = 0; col < game.getCOLUMNS() - 4; col++) {
                if (game.getGrid()[row][col] == currentUser.getGameToken()
                        && game.getGrid()[row - 1][col + 1] == currentUser.getGameToken()
                        && game.getGrid()[row - 2 ][col + 2] == currentUser.getGameToken()
                        && game.getGrid()[row - 3][col + 3] == currentUser.getGameToken()
                        && game.getGrid()[row - 4][col + 4] == currentUser.getGameToken()) {
                    logger.info(String.format("Winner on Positive Diagonal Match. Winner Is: [%s]", currentUser.getName()));
                    game.setWinner(currentUser.getName());
                    return;
                } else if (game.getGrid()[row][col] == currentUser.getGameToken()
                        && game.getGrid()[row - 1][8 - col - 1] == currentUser.getGameToken()
                        && game.getGrid()[row - 2][8 - col - 2] == currentUser.getGameToken()
                        && game.getGrid()[row - 3][8 - col - 3] == currentUser.getGameToken()
                        && game.getGrid()[row - 4][8 - col - 4] == currentUser.getGameToken()) {
                    logger.info(String.format("Winner on Negative Diagonal Match. Winner Is: [%s]", currentUser.getName()));
                    game.setWinner(currentUser.getName());
                    return;
                }
            }
        }


    }
}
