package dev.arguments.jvm;

public class JvmRule {
    private String action;
    private Os os;

    public String getAction() {
        return action;
    }

    public Os getOs() {
        return os;
    }

    @Override
    public String toString() {
        return "action: " + action +
                " os: "  + os;
    }

}
