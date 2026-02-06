package dev.arguments.game;

import dev.arguments.jvm.JvmArgumentObject;

import java.util.ArrayList;

public class Game {
    private ArrayList<String> arguments;
    private ArrayList<GameArgumentObject> argumentObjectArrayList;

    public Game() {
        arguments = new ArrayList<String>();
        argumentObjectArrayList = new ArrayList<GameArgumentObject>();
    }

    public ArrayList<String> getArguments() {
        return arguments;
    }

    public ArrayList<GameArgumentObject> getArgumentObjectArrayList() {
        return argumentObjectArrayList;
    }

    public void addGameArgumentObject(GameArgumentObject gameArgumentObject) {
        argumentObjectArrayList.add(gameArgumentObject);
    }

    public void addGameArgument(String argument) {
        arguments.add(argument);
    }
}
