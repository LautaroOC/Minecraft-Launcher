package dev.arguments.jvm;

import java.util.ArrayList;

public class Jvm {
    private ArrayList<JvmArgumentObject> argumentObject;
    private ArrayList<String> flags;

    public Jvm() {
        argumentObject = new ArrayList<JvmArgumentObject>();
        flags = new ArrayList<String>();
    }

    public ArrayList<JvmArgumentObject> getArgumentObject() {
        return argumentObject;
    }

    public ArrayList<String> getFlags() {
        return flags;
    }

    public void addArgumentObject(JvmArgumentObject jvmArgumentObject) {
        argumentObject.add(jvmArgumentObject);
    }

    public void addFlags(String flag) {
        flags.add(flag);
    }

}
