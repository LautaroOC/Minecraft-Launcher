package dev;

import dev.manifest.Manifest;
import dev.manifest.VersionManifest;

import java.io.*;
import java.net.URL;

public class Main {

    public static void main(String[] args) throws Exception {

        //String minecraftVersionUrl = "https://piston-meta.mojang.com/v1/packages/7a5aa5f3e3fba022efe0752660a5c7cd2dff2d16/1.7.json";
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
