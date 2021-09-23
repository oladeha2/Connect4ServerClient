package com.connect.five.game.server.classes;

import com.connect.five.game.server.enums.InputType;
import java.util.Scanner;

//Class is created like this to allow for mocked testing based on Input Type
public class UserInputReader {
    public  String readInput(InputType inputType) {
        Scanner scanner = new Scanner(System.in);
        String option = scanner.next();
        return option;
    }
}
