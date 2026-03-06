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
}
