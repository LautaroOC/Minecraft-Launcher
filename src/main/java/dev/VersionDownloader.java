package dev;

import dev.arguments.game.Game;
import dev.arguments.game.GameArgumentObject;
import dev.arguments.game.GameRule;
import dev.arguments.jvm.Jvm;
import dev.arguments.jvm.JvmArgumentObject;
import dev.arguments.jvm.JvmRule;
import dev.assetIndex.AssetObject;
import dev.assetIndex.Assets;
import dev.libraries.Artifact;
import dev.libraries.Library;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class VersionDownloader {

    private VersionJson versionJson;
    private ObjectMapper objectMapper;
    private Path versionsDirPathRelative;
    private Path versionDirPathRelative;
    private Path librariesDirPathRelative;
    private Path indexesDirPathRelative;
    private Path objectsDirPathRelative;
    private Path clientVersionFilePath;
    private Path nativesDirPathRelative;
    private Path minecraftPathRelative;
    private Path assetsDirPathRelative;
    private ArrayList<Library> libraries;
    private String assetsJsonText;
    private Assets assetsMap;
    private Jvm jvm;
    private Game game;

    public void downloadVersion() throws MalformedURLException, IOException {
        objectMapper = new ObjectMapper();
        URL MOJANG_META_VERSION = new URL("https://piston-meta.mojang.com/v1/packages/a58855d96a196f67d2240cd903011463e73df88f/1.21.json");
        try (InputStream in = MOJANG_META_VERSION.openStream()) {
            versionJson = objectMapper.readValue(in, VersionJson.class);
        }

    }

    public void downloadClient() throws MalformedURLException, IOException {

        String downloadClientUrl = versionJson.getDownloads().getClient().getUrl();
        URL CLIENT_VERSION = new URL(downloadClientUrl);
        ByteArrayOutputStream byteArrayOutputStreamClient = new ByteArrayOutputStream();
        try (InputStream inputStreamClient  = CLIENT_VERSION.openStream()) {

            int bytesRead = 0;
            byte[] bytesArray = new byte[8192];

            while ((bytesRead = inputStreamClient.read(bytesArray)) != -1) {
                byteArrayOutputStreamClient.write(bytesArray, 0, bytesRead);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        byte[] clientBytes = byteArrayOutputStreamClient.toByteArray();
        Path clientVersionFileName = Paths.get("client-" + versionJson.getId() + ".jar");
        clientVersionFilePath = versionDirPathRelative.resolve(clientVersionFileName);
        Files.write(clientVersionFilePath, clientBytes);

    }

    public void downloadLibraries() throws MalformedURLException, IOException {

        //How many libraries
        libraries = versionJson.getLibraries();
        int librariesAmount = libraries.size();

        //Getting all the attributes we will need for every single library
        for (int i = 0; i < librariesAmount; i++) {
            Library library = libraries.get(i);
            String name = library.getName();
            if (library.getDownloads().getArtifact() != null) {
                Artifact artifact = library.getDownloads().getArtifact();
                Path artifactPath = Paths.get(artifact.getPath());
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

                Path artifactLibrariesPathRelative = librariesDirPathRelative.resolve(artifactPath);
                Files.createDirectories(artifactLibrariesPathRelative.getParent());
                byte[] artifactBytes = byteArrayOutputStreamArtifact.toByteArray();
                Files.write(artifactLibrariesPathRelative, artifactBytes);
            }
        }
    }

    public void argumentsReformat() {

        //Reformat of the Arguments
        List<JsonNode> gameArgument = versionJson.getArguments().getGame();

        game = new Game();

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
    }

    public void mapJvm() {
        List<JsonNode> jvmArgument = versionJson.getArguments().getJvm();
        jvm = new Jvm();

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
    }

    public void createNatives() throws IOException {
        Path nativesDirName = Paths.get("natives");
        nativesDirPathRelative = versionDirPathRelative.resolve(nativesDirName);
        Files.createDirectories(nativesDirPathRelative);

        //Adding the natives into the directory
        for (int i = 0; i < libraries.size(); i++) {
            Library library = libraries.get(i);

            if (library.getRules() != null) {
                if (library.getName().contains("native") && (library.getRules().getFirst().getOs().getName().equals("linux"))) {
                    Path fileZipPath = librariesDirPathRelative.resolve(library.getDownloads().getArtifact().getPath());

                    ZipInputStream zis = new ZipInputStream(Files.newInputStream(fileZipPath));
                    ZipEntry zipEntry;

                    while ((zipEntry = zis.getNextEntry()) != null) {
                        if (!zipEntry.isDirectory() && zipEntry.getName().endsWith(".so")) {
                            //Im getting all the name with the path i think. Not just the fileName
                            Path target = nativesDirPathRelative.resolve(zipEntry.getName()).normalize();

                            if (!target.startsWith(nativesDirPathRelative)) {
                                throw new IOException("Bad zip entry: " + zipEntry.getName());
                            }

                            OutputStream out = Files.newOutputStream(target);
                            zis.transferTo(out);
                        }
                        zis.closeEntry();
                    }
                }
            }
        }
    }

    public void downloadAssetIndex() throws MalformedURLException, IOException{
        //Getting the assetIndex
        String input;
        String assetURL = versionJson.getAssetIndex().getUrl();
        URL ASSETS_URL = new URL(assetURL);
        BufferedReader assetIn = new BufferedReader(new InputStreamReader(ASSETS_URL.openStream()));

        while ((input = assetIn.readLine()) != null) {
            assetsJsonText = assetsJsonText.concat(input);
        }

        //Parsing the asset to JSON.
        assetsMap = objectMapper.readValue(assetsJsonText, Assets.class);
    }

    public void createAssetsIndexJson() throws IOException {
        //Saving the json into the index dir
        Files.write(
                Paths.get(indexesDirPathRelative + versionJson.getId() + ".json"),
                assetsJsonText.getBytes(StandardCharsets.UTF_8)
        );

    }

    public void DownloadAssetsObjects() throws IOException {
        for (Map.Entry<String, AssetObject> entry : assetsMap.getObjects().entrySet()) {
            String hash = entry.getValue().getHash();
            String dir = hash.substring(0, 2);

            //creating the dir inside assets/objects/
            Path hashObjectsDirPathRelative = objectsDirPathRelative.resolve(dir);
            Files.createDirectories(hashObjectsDirPathRelative);

            //Downloading the full hash file.
            //https://resources.download.minecraft.net/<first2>/<fullhash>
            String downloadUrl = "https://resources.download.minecraft.net/";
            downloadUrl = downloadUrl.concat(dir + "/" + hash);

            URL HASH_URL = new URL(downloadUrl);
            try (InputStream hashAssetIn = HASH_URL.openStream();
                 OutputStream out = Files.newOutputStream(hashObjectsDirPathRelative)) {
                hashAssetIn.transferTo(out);
            }
            System.out.println("downloading each asset object...");
        }

        System.out.println("finished downloading assets objects");
    }

    public void commandBuilder() throws IOException, InterruptedException {
        //Writing the java command.
        String JVM_FLAGS = "";
        String CLASSPATH = "";
        String MAIN_CLASS = "";
        String GAME_ARGS = "";

        //Classpath
        for (int i = 0; i < libraries.size(); i++) {
            Library library = libraries.get(i);
            String libraryPath = librariesDirPathRelative + library.getDownloads().getArtifact().getPath();
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
        CLASSPATH = CLASSPATH.concat(":" + clientVersionFilePath.toString());

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

        //MAIN CLASS
        MAIN_CLASS = MAIN_CLASS.concat(versionJson.getMainClass());

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

    public File newFile(File destinationDir, ZipEntry zipEntry) throws IOException {
        String fileName = new File(zipEntry.getName()).getName();
        File destFile = new File(destinationDir, fileName);
        String destDirPath = destinationDir.getCanonicalPath();
        String destFilePath = destFile.getCanonicalPath();
        if (!destFilePath.startsWith(destDirPath + File.separator)) {
            throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
        }

        return destFile;
    }

    public void createDirectories() throws IOException {
        String userHomeName = System.getProperty("user.home");
        Path userHomePath = Paths.get(userHomeName);
        Path minecraftDir = Paths.get(".minecraft");
        minecraftPathRelative = userHomePath.resolve(minecraftDir);

        Path versionsDirName = Paths.get("versions");
        versionsDirPathRelative = minecraftPathRelative.resolve(versionsDirName);
        Path versionDirName = Paths.get(versionJson.getId());
        versionDirPathRelative = versionsDirPathRelative.resolve(versionDirName);
        Path librariesDirName = Paths.get("libraries");
        librariesDirPathRelative = minecraftPathRelative.resolve(librariesDirName);
        Path assetsDirName = Paths.get("assets");
        assetsDirPathRelative = librariesDirPathRelative.resolve(assetsDirName);
        Path indexesDirName = Paths.get("indexes");
        indexesDirPathRelative = assetsDirPathRelative.resolve(indexesDirName);
        Path objectsDirName = Paths.get("objects");
        objectsDirPathRelative = assetsDirPathRelative.resolve(objectsDirName);

        //Creating .minecraft and its subdirs
        Files.createDirectories(minecraftPathRelative);
        Files.createDirectories(versionsDirPathRelative);
        Files.createDirectories(librariesDirPathRelative);
        Files.createDirectories(versionDirPathRelative);

    }

}
