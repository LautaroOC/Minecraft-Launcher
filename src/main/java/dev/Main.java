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

        VersionDownloader versionDownloader = new VersionDownloader();
        versionDownloader.downloadVersion();
        versionDownloader.createDirectories();
        versionDownloader.downloadClient();
        versionDownloader.downloadLibraries();
        versionDownloader.argumentsReformat();
        versionDownloader.mapJvm();
        versionDownloader.createNatives();
        versionDownloader.downloadAssetIndex();
        versionDownloader.createAssetsIndexJson();
        versionDownloader.downloadAssetsObjects();

        LauncherBuilder launcherBuilder = new LauncherBuilder(versionDownloader.getLibrariesDirPathRelative(), versionDownloader.getAssetsDirPathRelative(),
                versionDownloader.getClientVersionFilePath(), versionDownloader.getNativesDirPathRelative(), versionDownloader.getMinecraftPathRelative(),
                versionDownloader.getGame(), versionDownloader.getJvm(), versionDownloader.getLibraries(), versionDownloader.getVersionJson());

        launcherBuilder.classpathBuilder();
        launcherBuilder.jvmflagsBuilder();
        launcherBuilder.mainclassBuilder();
        launcherBuilder.gameargsBuilder();
        launcherBuilder.commandBuilder();

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
