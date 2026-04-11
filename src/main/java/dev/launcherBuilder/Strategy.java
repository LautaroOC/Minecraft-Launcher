package dev.launcherBuilder;

public interface Strategy {
    boolean classPathBuilder();
    boolean jvmFlagsBuilder();
    boolean mainClassBuilder();
    boolean gameArgsBuilder();
    boolean commandBuilder();
}
