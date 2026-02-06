package dev.arguments.game;

import java.util.Map;

public class GameRule {
    private String action;
    private Map<String, Boolean> features;

    public Map<String, Boolean> getFeatures() {
        return features;
    }

    public String getAction() {
        return action;
    }

    @Override
    public String toString() {
        return "action: " + action +
                " features: " + features;
    }
}
