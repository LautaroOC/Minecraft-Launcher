package dev.arguments.jvm;


import java.util.List;

public class JvmArgumentObject {
    private List<JvmRule> rules;
    private List<String> value;

    public JvmArgumentObject(List<JvmRule> rules, List<String> value) {
        this.rules = rules;
        this.value = value;
    }

    public List<JvmRule> getRules() {
        return rules;
    }

    public List<String> getValue() {
        return value;
    }

    @Override
    public String toString() {
       return "rules: "  + rules +
               " value: " + value;
    }
}
