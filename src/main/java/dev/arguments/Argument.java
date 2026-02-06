package dev.arguments;

import tools.jackson.databind.JsonNode;

import java.util.List;

public class Argument {

    private List<JsonNode> game;
    private List<JsonNode> jvm;

    public List<JsonNode> getGame() {
        return game;
    }

    public List<JsonNode> getJvm() {
        return jvm;
    }

}
