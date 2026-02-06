package dev;

import dev.arguments.game.Game;
import dev.arguments.game.GameArgumentObject;
import dev.arguments.jvm.Jvm;
import dev.arguments.jvm.JvmArgumentObject;
import dev.arguments.game.GameRule;
import dev.arguments.jvm.JvmRule;
import dev.assetIndex.AssetObject;
import dev.assetIndex.Assets;
import dev.libraries.Artifact;
import dev.libraries.Library;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.FieldPosition;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class Main {
    public static void main(String[] args) throws Exception {

        URL MOJANG_META_VERSION = new URL("https://piston-meta.mojang.com/v1/packages/a58855d96a196f67d2240cd903011463e73df88f/1.21.json");
        BufferedReader in = new BufferedReader(new InputStreamReader(MOJANG_META_VERSION.openStream()));

        String jsonText = "";
        String input;
        while ((input = in.readLine()) != null) {
            jsonText = jsonText.concat(input);
        }

        System.out.println(jsonText);

        ObjectMapper objectMapper = new ObjectMapper();
        VersionJson versionJson = objectMapper.readValue(jsonText, VersionJson.class);

        System.out.println("ID: " + versionJson.getId());
        System.out.println("Arguments: " + "game: " + versionJson.getArguments().getGame() + "jvm: " + versionJson.getArguments().getJvm());
        System.out.println("Downloads: " + versionJson.getDownloads().getClient());
        System.out.println("Main class: " + versionJson.getMainClass());
        System.out.println("Java version: " + versionJson.getJavaVersion().getMajorVersion());
        System.out.println("Libraries: " + versionJson.getLibraries());

        String userHomePath = System.getProperty("user.home");
        String minecraftDir = ".minecraft";
        String minecraftPath = userHomePath + "/" + minecraftDir;
        //Creating .minecraft
        boolean createMinecraftDirectory = new File(minecraftPath).mkdir();

        //clientDownloadURl
        String downloadClientUrl = versionJson.getDownloads().getClient().getUrl();

        //There is no path in the downloads.client in any of them at least in this version.
        //Path is made by the launcher
        String versionPath = minecraftPath + "/versions/";
        //Versions directory creation
        boolean createVersionsDirectory = new File(versionPath).mkdir();
        //Creating the version directory
        String versionDirectoryPath = versionPath + "/" + versionJson.getId();
        boolean createVersionDirectory = new File(versionDirectoryPath).mkdir();
        //Getting the clientFile
        URL CLIENT_VERSION = new URL(downloadClientUrl);
        ByteArrayOutputStream byteArrayOutputStreamClient = new ByteArrayOutputStream();
        try (BufferedInputStream inputStreamClient =
                     new BufferedInputStream((CLIENT_VERSION.openStream()))) {

            int bytesRead = 0;
            byte[] bytesArray = new byte[8192];
            while ((bytesRead = inputStreamClient.read(bytesArray)) != -1) {
                byteArrayOutputStreamClient.write(bytesArray, 0, bytesRead);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        byte[] clientBytes = byteArrayOutputStreamClient.toByteArray();
        String versionClientPathString = versionDirectoryPath + "/" + "client-" + versionJson.getId() + ".jar";
        Path versionClientPath = Paths.get(versionClientPathString);
        Files.write(versionClientPath, clientBytes);


        //Libraries path
        String librariesPath = minecraftPath + "/libraries/";
        boolean createLibrariesDirectory = new File(librariesPath).mkdir();
        //How many libraries
        ArrayList<Library> libraries = versionJson.getLibraries();
        int librariesAmount = libraries.size();

        //Getting all the attributes we will need for every single library
        for (int i = 0; i < librariesAmount; i++) {
            Library library = libraries.get(i);
            String name = library.getName();
            if (library.getDownloads().getArtifact() != null) {
                Artifact artifact = library.getDownloads().getArtifact();
                String artifactPathString = artifact.getPath();
                String artifactSha1 = artifact.getSha1();
                String artifactUrlString = artifact.getUrl();

                URL artifactURL = new URL(artifactUrlString);
                ByteArrayOutputStream byteArrayOutputStreamArtifact = new ByteArrayOutputStream();
                try (BufferedInputStream bufferedInputStream =
                             new BufferedInputStream(artifactURL.openStream())) {

                    int bytesRead = 0;
                    byte[] byteArray = new byte[8192];

                    while ((bytesRead = bufferedInputStream.read(byteArray)) != -1) {
                        byteArrayOutputStreamArtifact.write(byteArray, 0, bytesRead);
                    }

                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                String artifactPathStringInLibraries = librariesPath + artifactPathString;
                Path artifactPath = Paths.get(artifactPathStringInLibraries);
                Files.createDirectories(artifactPath.getParent());
                byte[] artifactBytes = byteArrayOutputStreamArtifact.toByteArray();
                Files.write(artifactPath, artifactBytes);
            }
        }

        //Reformat of the Arguments
        //Game
        List<JsonNode> gameArgument = versionJson.getArguments().getGame();

        Game game = new Game();

        for (int i = 0; i < gameArgument.size(); i++) {
            JsonNode object = gameArgument.get(i);

            if (object.isTextual()) {
                String value = object.stringValue();
                game.addGameArgument(value);
            } else if (object.isObject()) {

                List<GameRule> rules = Arrays.asList(
                        objectMapper.treeToValue(object.get("rules"), GameRule[].class)
                );

                List<String> finalValues = new ArrayList<>();
                if (object.get("value").isArray()) {
                    List<String> values = Arrays.asList(
                            objectMapper.treeToValue(object.get("value"), String[].class)
                    );
                    finalValues.addAll(values);
                } else if (object.get("value").isTextual()) {
                    String value = objectMapper.treeToValue(object.get("value"), String.class);
                    finalValues.add(value);
                }

                game.addGameArgumentObject(new GameArgumentObject(rules, finalValues));
            }
        }

        //Jvm
        List<JsonNode> jvmArgument = versionJson.getArguments().getJvm();
        Jvm jvm = new Jvm();

        for (int i = 0; i < jvmArgument.size(); i++) {

            JsonNode argument = jvmArgument.get(i);

            if (argument.isObject()) {

                //Rules
                JsonNode rulesJsonNode = argument.get("rules");
                //Rules siempre viene en Array
                List<JvmRule> finalRules = Arrays.asList(
                        objectMapper.treeToValue(rulesJsonNode, JvmRule[].class)
                );

                //Value
                JsonNode valueJsonNode = argument.get("value");
                List<String> finalValue = new ArrayList<String>();

                //Si el value viene en array
                if (valueJsonNode.isArray()) {
                    List<String> value = Arrays.asList(
                            objectMapper.treeToValue(valueJsonNode, String[].class)
                    );
                    finalValue.addAll(value);
                }
                //Si el value viene solo en String
                else if (valueJsonNode.isTextual()) {
                    String value = objectMapper.treeToValue(valueJsonNode, String.class);
                    finalValue.add(value);
                }

                //Resultado final para agregar a la lista de los argumentObject de un objecto Jvm
                jvm.addArgumentObject(new JvmArgumentObject(finalRules, finalValue));

                //Si el arguemento no es un objeto es un stringg
            } else if (argument.isTextual()) {
                String flag = objectMapper.treeToValue(argument, String.class);
                //Agregar a la lista de flags del objeto jvm
                jvm.addFlags(flag);
            }
        }


        //System.out.println(game.getArguments());
        //System.out.println(game.getArgumentObjectArrayList());
        //System.out.println("jvm flags: " +jvm.getFlags());
        //System.out.println("jvm objeto de rules y value: " +jvm.getArgumentObject());

        //Creating the natives directory
        String nativesDirPath = versionDirectoryPath + "/natives";
        boolean creationNativesDirectory = new File(nativesDirPath).mkdir();

        //Adding the natives into the directory
        for (int i = 0; i < libraries.size(); i++) {
            Library library = libraries.get(i);

            if (library.getRules() != null) {
                if (library.getName().contains("native") && (library.getRules().getFirst().getOs().getName().equals("linux"))) {
                    String fileZipPath = librariesPath + library.getDownloads().getArtifact().getPath();
                    File destFile = new File(nativesDirPath);

                    ZipInputStream zis = new ZipInputStream(new FileInputStream(fileZipPath));
                    ZipEntry zipEntry;

                    while ((zipEntry = zis.getNextEntry()) != null) {
                        if (!zipEntry.isDirectory() && zipEntry.getName().endsWith(".so")) {
                            File nativeFile = newFile(destFile, zipEntry);

                            try (FileOutputStream fos = new FileOutputStream(nativeFile)) {
                                byte[] buffer = new byte[4096];
                                int len;
                                while ((len = zis.read(buffer)) > 0) {
                                    fos.write(buffer, 0, len);
                                }
                            }
                        }
                        zis.closeEntry();
                    }
                    zis.close();
                }
            }

        }


        //Creating the assets directories
        String assetsStringPath = minecraftPath + "/assets/";
        boolean createAssetsDir = new File(assetsStringPath).mkdir();
        String assetsIndexPath = assetsStringPath + "indexes/";
        boolean createAssetsIndexeDir = new File(assetsIndexPath).mkdir();
        String assetObjectsPath = assetsStringPath + "objects/";
        boolean createAssestsObjects = new File(assetObjectsPath).mkdir();

        //Getting the assetIndex
        String assetURL = versionJson.getAssetIndex().getUrl();
        URL ASSETS_URL = new URL(assetURL);
        BufferedReader assetIn = new BufferedReader(new InputStreamReader(ASSETS_URL.openStream()));

        String assetJsonText = "";
        while ((input = assetIn.readLine()) != null) {
            assetJsonText = assetJsonText.concat(input);
        }

        //Parsing the asset to JSON.
        Assets assetsMap = objectMapper.readValue(assetJsonText, Assets.class);

        //Saving the json into the index dir
        Files.write(
                Paths.get(assetsIndexPath + versionJson.getId() + ".json"),
                assetJsonText.getBytes(StandardCharsets.UTF_8)
        );

        System.out.println("dsadasdasdasda");

        for (Map.Entry<String, AssetObject> entry : assetsMap.getObjects().entrySet()) {
            String hash = entry.getValue().getHash();
            String dir = hash.substring(0, 2);

            //creating the dir inside assets/objects/
            boolean createHashDir = new File(assetObjectsPath + "/" + dir).mkdir();

            //Downloading the full hash file.
            //https://resources.download.minecraft.net/<first2>/<fullhash>
            String downloadUrl = "https://resources.download.minecraft.net/";
            downloadUrl = downloadUrl.concat(dir + "/" + hash);

            URL HASH_URL = new URL(downloadUrl);
            try (InputStream hashAssetIn = HASH_URL.openStream()) {

                File hashFile = new File(assetObjectsPath + dir + "/" + hash);
                FileOutputStream fos = new FileOutputStream(hashFile);

                byte[] buffer = new byte[4096];
                int len;
                while ((len = hashAssetIn.read(buffer)) != -1) {
                    fos.write(buffer, 0, len);
                }

                System.out.println("downloading each hash");
            }
            System.out.println("downloading each hash");
        }

        System.out.println("finished hashes");
        //Writing the java command.
        String JVM_FLAGS = "";
        String CLASSPATH = "";
        String MAIN_CLASS = "";
        String GAME_ARGS = "";

        //Classpath
        for (int i = 0; i < libraries.size(); i++) {
            Library library = libraries.get(i);
            String libraryPath = librariesPath + library.getDownloads().getArtifact().getPath();
            String name = library.getName();
            boolean isPlatformSpecific =
                    name.contains("natives-")
                            || name.contains("linux")
                            || name.contains("macos")
                            || name.contains("windows");

            if (!isPlatformSpecific) {
                CLASSPATH += ":" + libraryPath;
            }
        }
        CLASSPATH = CLASSPATH.concat(":" + versionClientPathString);

        //JVM FLAGS
        for (int i = 0; i < jvm.getFlags().size(); i++) {
            JVM_FLAGS = JVM_FLAGS.concat(" " + jvm.getFlags().get(i));
        }

        //Reemplazar por los valores necesarios
        JVM_FLAGS = JVM_FLAGS.replace("${natives_directory}", nativesDirPath);
        JVM_FLAGS = JVM_FLAGS.replace("${launcher_name}", "Launcher");
        JVM_FLAGS = JVM_FLAGS.replace("${launcher_version}", "1");
        JVM_FLAGS = JVM_FLAGS.replace("${classpath}", CLASSPATH);
        System.out.println("JVM FLAGS:");
        System.out.println(JVM_FLAGS);

        //MAIN CLASS
        MAIN_CLASS = MAIN_CLASS.concat(versionJson.getMainClass());

        //GAME ARGS
        for (int i = 0; i < game.getArguments().size(); i++) {
            GAME_ARGS = GAME_ARGS.concat(" " + game.getArguments().get(i));
        }
        //Reemplazar por los valores necesarios para los game arguments
        GAME_ARGS = GAME_ARGS.replace("${auth_player_name}", "Steve");
        GAME_ARGS = GAME_ARGS.replace("${version_name}", versionJson.getId());
        GAME_ARGS = GAME_ARGS.replace("${game_directory}", minecraftPath);
        GAME_ARGS = GAME_ARGS.replace("${assets_root}", assetsStringPath); // no tengo dir de assests todavia.
        GAME_ARGS = GAME_ARGS.replace("${assets_index_name}", versionJson.getId());
        GAME_ARGS = GAME_ARGS.replace("${auth_uuid}", "00000000-0000-0000-0000-000000000000");
        GAME_ARGS = GAME_ARGS.replace("${auth_access_token}", "0");
        GAME_ARGS = GAME_ARGS.replace("${user_type}", "legacy");
        GAME_ARGS = GAME_ARGS.replace("${version_type}", "release");


        //El comando completo hardocdeo el argumento de jvm para linux por ahora y no tengo en cuenta ninguna de las rules en los argumentos.
        //String javaCommand = "java -Xss1M" + JVM_FLAGS + MAIN_CLASS + GAME_ARGS;

        List<String> command = new ArrayList<>();
        command.add("java");
        command.add("-Xss1M");

        command.addAll(Arrays.asList(JVM_FLAGS.trim().split("\\s+")));

        command.add(MAIN_CLASS);

        command.addAll(Arrays.asList(GAME_ARGS.trim().split("\\s+")));

        ProcessBuilder pb = new ProcessBuilder(command);

        pb.directory(new File(minecraftPath));

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

    public static File newFile(File destinationDir, ZipEntry zipEntry) throws IOException {
        String fileName = new File(zipEntry.getName()).getName();
        File destFile = new File(destinationDir, fileName);
        String destDirPath = destinationDir.getCanonicalPath();
        String destFilePath = destFile.getCanonicalPath();
        if (!destFilePath.startsWith(destDirPath + File.separator)) {
            throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
        }

        return destFile;
    }

}
