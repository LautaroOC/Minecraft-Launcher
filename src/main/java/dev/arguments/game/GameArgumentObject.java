package dev.arguments.game;

import dev.arguments.jvm.JvmRule;

import java.util.List;

public class GameArgumentObject {

    private List<GameRule> rules;
    private List<String> value;

    public GameArgumentObject(List<GameRule> rules, List<String> value)  {
        this.rules = rules;
        this.value = value;
    }

    public List<GameRule> getRules() {
        return rules;
    }

    public List<String> getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "rules: " + rules +
                " value: " + value;
    }
}
