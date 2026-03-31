package dev;

import dev.assetIndex.AssetObject;
import dev.assetIndex.Assets;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

public class IntegrityChecker {
    private IssueReport issueReport;

    public IntegrityChecker() {
        issueReport = new IssueReport();
    }

    public boolean checkFileIntegrity(String path, String sha1, long expectedSize) throws IOException {
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
    public void assetsFilesIntegrity(Path minecraftPathRelative, Assets assets) throws IOException {

        Path assetsPath = Paths.get("assets");
        Path assetsPathRelative = minecraftPathRelative.resolve(assetsPath);
        Path indexesPath = Paths.get("indexes");
        Path indexesPathRelative = assetsPathRelative.resolve(indexesPath);
        Path objectsPath = Paths.get("objects");
        Path objectsPathRelative = assetsPathRelative.resolve(objectsPath);

        if (!Files.exists(assetsPathRelative)) {
            issueReport.addIssue(new Issue(IssueType.MISSING_DIRECTORY, "No hash its a directory", 0, "No url its a directory", assetsPathRelative));
        }
        if (!Files.exists(indexesPathRelative)) {
            //check assets json
            issueReport.addIssue(new Issue(IssueType.MISSING_DIRECTORY, "No hash its a directory", 0, "No url its a directory", assetsPathRelative));
        }
        if (Files.exists(objectsPathRelative)) {
            //check assets hash dirs
            for (Map.Entry<String, AssetObject> entry : assets.getObjects().entrySet()) {
                String hash = entry.getValue().getHash();
                String hashName = hash.substring(0, 2);
                Path hashDirName = Paths.get(hashName);
                Path hashPathRelative = objectsPathRelative.resolve(hashDirName);
                long fileSize = entry.getValue().getSize();
                //https://resources.download.minecraft.net/<first2>/<fullhash>
                String downloadUrl = "https://resources.download.minecraft.net/";
                downloadUrl = downloadUrl.concat(objectsPathRelative + "/" + hash);

                if (Files.exists(hashPathRelative)) {
                    //check that the assets are inside this dir
                    Path hashFileName = Paths.get(hash);
                    Path hashFilePath = hashPathRelative.resolve(hashFileName);
                    if (Files.exists(hashFilePath)) {
                        if (!(fileSize == Files.size(hashFilePath))) {
                            System.out.println("Assets hash file exists but different size");
                            issueReport.addIssue(new Issue(IssueType.SIZE_MISMATCH, "No sha1 on objects", entry.getValue().getSize(), downloadUrl, hashFilePath));
                        }
                    } else {
                        System.out.println("Hash file doesnt exist");
                    }
                }
            }
        }
        else {
            issueReport.addIssue(new Issue(IssueType.MISSING_DIRECTORY, "No hash its a directory", 0, "No url its a directory", assetsPathRelative));
        }
    }
    public void checkVersionIntegrity(Path minecraftPath, VersionJson versionJson, Assets assets) throws IOException{
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
