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
import java.net.MalformedURLException;
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







        //System.out.println(game.getArguments());
        //System.out.println(game.getArgumentObjectArrayList());
        //System.out.println("jvm flags: " +jvm.getFlags());
        //System.out.println("jvm objeto de rules y value: " +jvm.getArgumentObject());




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

        for (Map.Entry<String, AssetObject> entry : assetsMap.getObjects().entrySet()) {
            String hash = entry.getValue().getHash();
            String dir = hash.substring(0, 2);

            //creating the dir inside assets/objects/
            boolean createHashDir = new File(assetObjectsPath + "/" + dir).mkdir();

            //Downloading the full hash file.
            //https://resources.download.minecraft.net/<first2>/<fullhash>
            String downloadUrl = "https://resources.download.minecraft.net/";
            downloadUrl = downloadUrl.concat(dir + "/" + hash);

            String pathname = assetObjectsPath + dir + "/" + hash;
            URL HASH_URL = new URL(downloadUrl);
            try (InputStream hashAssetIn = HASH_URL.openStream();
                 FileOutputStream fos = new FileOutputStream(pathname)) {

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



    public static boolean checkFileIntegrity(String path, String sha1, long expectedSize) throws IOException {
        Path p = Paths.get(path);
        boolean exists = Files.exists(p);
        if (exists) {
            //check SHA1 for later dont want to get into this right now doesnt seem that necesary
            //check size
            long fileSize = Files.size(p);
            if (fileSize != expectedSize) {
                return false;
            }
            return true;
        } else {
            return false;
        }
    }

    public static void downloadFile(String path, String url) throws IOException {
        URL missingFileUrl = new URL(url);
        try (InputStream in = missingFileUrl.openStream();
             FileOutputStream fos = new FileOutputStream(path)) {
            byte[] buffer = new byte[4096];
            int len;
            while ((len = in.read(buffer)) != -1) {
                fos.write(buffer, 0, len);
            }
        }
        System.out.println("File download complete");
    }

    public static void minecraftIntegrity() {

    }

    public static void assetsFilesIntegrity(Path minecraftPathRelative, Assets assets) throws IOException {

        Path assetsPath = Paths.get("assets");
        Path assetsPathRelative = minecraftPathRelative.resolve(assetsPath);
        Path indexesPath = Paths.get("indexes");
        Path indexesPathRelative = assetsPathRelative.resolve(indexesPath);
        Path objectsPath = Paths.get("objects");
        Path objectsPathRelative = assetsPathRelative.resolve(objectsPath);

        if (Files.exists(assetsPathRelative)) {

        }
        if (Files.exists(indexesPathRelative)) {
            //check assets json
        }
        if (Files.exists(objectsPathRelative)) {
            //check assets hash dirs
            for (Map.Entry<String, AssetObject> entry : assets.getObjects().entrySet()) {
                String hash = entry.getValue().getHash();
                String hashName = hash.substring(0, 2);
                Path hashDirName = Paths.get(hashName);
                Path hashPathRelative = objectsPathRelative.resolve(hashDirName);
                long fileSize = entry.getValue().getSize();

                if (Files.exists(hashPathRelative)) {
                    //check that the assets are inside this dir
                    Path hashFileName = Paths.get(hash);
                    Path hashFilePath = hashPathRelative.resolve(hashFileName);
                    if (Files.exists(hashFilePath)) {
                        if (!(fileSize == Files.size(hashFilePath))) {
                            System.out.println("Assets hash file exists but different size");
                        }
                    } else {
                        System.out.println("Hash file doesnt exist");
                    }
                }
            }
        }
    }


    public static void checkVersionIntegrity(Path minecraftPath, VersionJson versionJson, Assets assets) throws IOException{
        //All necessary directories paths
        //ESTA TODO MAL EL DISEÑO PERO LA IDEA VA POR AHI
        String assetsDir = minecraftPath + "/assets/";
        //assets dirs
        String indexesDir = assetsDir + "/indexes";
        String objectsDir = assetsDir + "/objects";
        String downloadsDir = minecraftPath + "/downloads";
        String librariesDir = minecraftPath + "/libraries";
        String logsDir = minecraftPath + "/logs";
        String resourcepacksDir = minecraftPath + "/resourcepacks";
        String savesDir = minecraftPath + "/saves";
        String versionsDir = minecraftPath + "/versions";
        //maybe options.txt

        if (Files.exists(minecraftPath)) {

                if (Files.exists(Paths.get(downloadsDir))) {
                    if (Files.exists(Paths.get(librariesDir))) {
                        if (Files.exists(Paths.get(logsDir)));
                        if (Files.exists(Paths.get(resourcepacksDir))) {
                            if (Files.exists(Paths.get(savesDir))) {
                                if (Files.exists(Paths.get(versionsDir))) {

                                }
                            }
                        }
                    }
                }
            }
        }


}
