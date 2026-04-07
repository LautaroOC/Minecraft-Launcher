package dev;

import java.io.*;
import java.net.URL;

public class Main {

    public static void main(String[] args) throws Exception {

        String minecraftVersionUrl = "https://piston-meta.mojang.com/v1/packages/0d9ace8a2ecfd1f4c782786f4b985a499240ff12/1.17.json";
        VersionDownloader versionDownloader = new VersionDownloader();
        versionDownloader.downloadVersion(minecraftVersionUrl);
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
