package dev;

import dev.arguments.game.Game;
import dev.arguments.jvm.Jvm;
import dev.libraries.Library;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LauncherBuilder {
    private String JVM_FLAGS = "";
    private String CLASSPATH = "";
    private String MAIN_CLASS = "";
    private String GAME_ARGS = "";
    private Path librariesDirPathRelative;
    private Path assetsDirPathRelative;
    private Path clientVersionFilePath;
    private Path nativesDirPathRelative;
    private Path minecraftPathRelative;
    private Game game;
    private Jvm jvm;
    private ArrayList<Library> libraries;
    private VersionJson versionJson;

    public LauncherBuilder(Path librariesDirPathRelative, Path assetsDirPathRelative, Path clientVersionFilePath, Path nativesDirPathRelative, Path minecraftPathRelative, Game game, Jvm jvm, ArrayList<Library> libraries, VersionJson versionJson) {
        this.librariesDirPathRelative = librariesDirPathRelative;
        this.assetsDirPathRelative = assetsDirPathRelative;
        this.clientVersionFilePath = clientVersionFilePath;
        this.nativesDirPathRelative = nativesDirPathRelative;
        this.minecraftPathRelative = minecraftPathRelative;
        this.game = game;
        this.jvm = jvm;
        this.libraries = libraries;
        this.versionJson = versionJson;
    }

    public void classpathBuilder() {
        //Classpath
        List<String> librariesClassPaths = new ArrayList<>();
        for (int i = 0; i < libraries.size(); i++) {
            Library library = libraries.get(i);
            Path librariesPath = librariesDirPathRelative.resolve(library.getDownloads().getArtifact().getPath());
            String name = library.getName();

            boolean isPlatformSpecific =
                    name.contains("natives-")
                            || name.contains("linux")
                            || name.contains("macos")
                            || name.contains("windows");

            if (!isPlatformSpecific) {
                //CLASSPATH += ":" + librariePath.toString();
                librariesClassPaths.add(librariesPath.toString());
            }
        }
        librariesClassPaths.add(clientVersionFilePath.toString());
        CLASSPATH = String.join(":", librariesClassPaths);
    }

    public void jvmflagsBuilder() {
        //JVM FLAGS
        for (int i = 0; i < jvm.getFlags().size(); i++) {
            JVM_FLAGS = JVM_FLAGS.concat(" " + jvm.getFlags().get(i));
        }

        //Reemplazar por los valores necesarios
        JVM_FLAGS = JVM_FLAGS.replace("${natives_directory}", nativesDirPathRelative.toString());
        JVM_FLAGS = JVM_FLAGS.replace("${launcher_name}", "Launcher");
        JVM_FLAGS = JVM_FLAGS.replace("${launcher_version}", "1");
        JVM_FLAGS = JVM_FLAGS.replace("${classpath}", CLASSPATH);
        System.out.println("JVM FLAGS:");
        System.out.println(JVM_FLAGS);
    }

    public void mainclassBuilder() {
        //MAIN CLASS
        MAIN_CLASS = MAIN_CLASS.concat(versionJson.getMainClass());
    }

    public void gameargsBuilder() {
        //GAME ARGS
        for (int i = 0; i < game.getArguments().size(); i++) {
            GAME_ARGS = GAME_ARGS.concat(" " + game.getArguments().get(i));
        }
        //Reemplazar por los valores necesarios para los game arguments
        GAME_ARGS = GAME_ARGS.replace("${auth_player_name}", "Steve");
        GAME_ARGS = GAME_ARGS.replace("${version_name}", versionJson.getId());
        GAME_ARGS = GAME_ARGS.replace("${game_directory}", minecraftPathRelative.toString());
        GAME_ARGS = GAME_ARGS.replace("${assets_root}", assetsDirPathRelative.toString());
        GAME_ARGS = GAME_ARGS.replace("${assets_index_name}", versionJson.getId());
        GAME_ARGS = GAME_ARGS.replace("${auth_uuid}", "00000000-0000-0000-0000-000000000000");
        GAME_ARGS = GAME_ARGS.replace("${auth_access_token}", "0");
        GAME_ARGS = GAME_ARGS.replace("${user_type}", "legacy");
        GAME_ARGS = GAME_ARGS.replace("${version_type}", "release");

    }

    public void commandBuilder() throws IOException, InterruptedException {
        //El comando completo hardocdeo el argumento de jvm para linux por ahora y no tengo en cuenta ninguna de las rules en los argumentos.
        //String javaCommand = "java -Xss1M" + JVM_FLAGS + MAIN_CLASS + GAME_ARGS;
        List<String> command = new ArrayList<>();
        command.add("java");
        command.add("-Xss1M");

        command.addAll(Arrays.asList(JVM_FLAGS.trim().split("\\s+")));

        command.add(MAIN_CLASS);

        command.addAll(Arrays.asList(GAME_ARGS.trim().split("\\s+")));

        ProcessBuilder pb = new ProcessBuilder(command);

        pb.directory(new File(minecraftPathRelative.toString()));

        pb.redirectErrorStream(true);
        System.out.println("COMMAND:");
        for (String c : command) {
            System.out.println(c);
        }
        Process process = pb.start();

        try (BufferedReader bufferedReader =
                     new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                System.out.println("[MC] " + line);
            }
        }

        int exitCode = process.waitFor();
        System.out.println("Minecraft exited with code " + exitCode);
    }

}
