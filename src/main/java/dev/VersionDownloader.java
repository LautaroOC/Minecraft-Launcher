package dev;

import dev.arguments.game.Game;
import dev.arguments.game.GameArgumentObject;
import dev.arguments.game.GameRule;
import dev.arguments.jvm.Jvm;
import dev.arguments.jvm.JvmArgumentObject;
import dev.arguments.jvm.JvmRule;
import dev.assetIndex.AssetObject;
import dev.assetIndex.Assets;
import dev.downloads.DownloadObject;
import dev.libraries.Artifact;
import dev.libraries.Library;
import dev.libraries.Rule;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileAttribute;
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


    public VersionJson getVersionJson() {
        return versionJson;
    }

    public Path getVersionsDirPathRelative() {
        return versionsDirPathRelative;
    }

    public Path getVersionDirPathRelative() {
        return versionDirPathRelative;
    }

    public Path getLibrariesDirPathRelative() {
        return librariesDirPathRelative;
    }

    public Path getIndexesDirPathRelative() {
        return indexesDirPathRelative;
    }

    public Path getObjectsDirPathRelative() {
        return objectsDirPathRelative;
    }

    public Path getClientVersionFilePath() {
        return clientVersionFilePath;
    }

    public Path getNativesDirPathRelative() {
        return nativesDirPathRelative;
    }

    public Path getMinecraftPathRelative() {
        return minecraftPathRelative;
    }

    public Path getAssetsDirPathRelative() {
        return assetsDirPathRelative;
    }

    public ArrayList<Library> getLibraries() {
        return libraries;
    }

    public String getAssetsJsonText() {
        return assetsJsonText;
    }

    public Assets getAssetsMap() {
        return assetsMap;
    }

    public Jvm getJvm() {
        return jvm;
    }

    public Game getGame() {
        return game;
    }


    public void downloadVersion(String url) throws MalformedURLException, IOException {
        objectMapper = new ObjectMapper();
        URL MOJANG_META_VERSION = new URL(url);
        try (InputStream in = MOJANG_META_VERSION.openStream()) {
            versionJson = objectMapper.readValue(in, VersionJson.class);
        }

    }

    public void downloadClient() throws MalformedURLException, IOException {

        String downloadClientUrl = versionJson.getDownloads().getClient().getUrl();
        URL CLIENT_VERSION = new URL(downloadClientUrl);
        ByteArrayOutputStream byteArrayOutputStreamClient = new ByteArrayOutputStream();
        try (InputStream inputStreamClient = CLIENT_VERSION.openStream()) {

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
        System.out.println("Descargando natives");
        downloadNatives();
        System.out.println("Descarga de natives finalizada");
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

    public void downloadNatives() throws MalformedURLException, IOException {
        for (Library library : libraries) {
            if (library.getNatives() != null) {
                if (library.getNatives().containsKey("linux")) {
                    Map<String, DownloadObject> classifiers = library.getDownloads().getClassifiers();
                    if (classifiers.containsKey("natives-linux")) {
                        DownloadObject natives = classifiers.get("natives-linux");
                        Path nativesLibrariesPathRelative = librariesDirPathRelative.resolve(natives.getPath());
                        String urlString = natives.getUrl();
                        URL URL = new URL(urlString);
                        try (InputStream inputStream = URL.openStream()) {
                            Files.createDirectories(nativesLibrariesPathRelative.getParent());
                            Files.copy(inputStream, nativesLibrariesPathRelative);
                        }
                    }
                }
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
            //int minecraftVersion = Integer.parseInt(versionJson.getAssetsVersion());
            System.out.println(library.getNatives());
            if (library.getNatives() != null) {
                System.out.println("Descomprimiendo natives...");
                if (library.getNatives().containsKey("linux")) {
                    Map<String, DownloadObject> classifiers = library.getDownloads().getClassifiers();
                    DownloadObject natives = classifiers.get("natives-linux");
                    if (natives != null && natives.getPath() != null) {
                        Path fileZipPath = librariesDirPathRelative.resolve(natives.getPath());
                        if (Files.exists(fileZipPath)) {
                            ZipInputStream zis = new ZipInputStream(Files.newInputStream(fileZipPath));
                            ZipEntry zipEntry;
                            while ((zipEntry = zis.getNextEntry()) != null) {
                                if (!zipEntry.isDirectory() && zipEntry.getName().endsWith(".so")) {
                                    Path target = nativesDirPathRelative.resolve(zipEntry.getName()).normalize();
                                    if (!target.startsWith(nativesDirPathRelative)) {
                                        throw new IOException("Bad zip entry: " + zipEntry.getName());
                                    }
                                    Files.createDirectories(target.getParent());
                                    try (OutputStream out = Files.newOutputStream(target)) {
                                        zis.transferTo(out);
                                    }
                                }
                                zis.closeEntry();
                            }
                        }
                    }
                }
            }
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
                            Files.createDirectories(target.getParent());
                            try (OutputStream out = Files.newOutputStream(target)) {
                                zis.transferTo(out);
                            }

                        }
                        zis.closeEntry();
                    }
                }
            }
        }
        System.out.println("Se descomprimieron todos los natives");
    }

    public void downloadAssetIndex() throws MalformedURLException, IOException {
        //Getting the assetIndex
        String input;
        String assetURL = versionJson.getAssetIndex().getUrl();
        assetsJsonText = "";
        URL ASSETS_URL = new URL(assetURL);

        try (BufferedReader assetIn = new BufferedReader(new InputStreamReader(ASSETS_URL.openStream()))) {
            while ((input = assetIn.readLine()) != null) {
                assetsJsonText = assetsJsonText.concat(input);
            }

        }
        //Parsing the asset to JSON.
        assetsMap = objectMapper.readValue(assetsJsonText, Assets.class);
    }

    public void createAssetsIndexJson() throws IOException {
        //Saving the json into the index dir
        String indexFileName = versionJson.getId() + ".json";
        Files.write(
                indexesDirPathRelative.resolve(indexFileName),
                assetsJsonText.getBytes(StandardCharsets.UTF_8)
        );

    }

    public void downloadAssetsObjects() throws IOException {
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
            if (!Files.exists(hashObjectsDirPathRelative.resolve(hash))) {
                try (InputStream hashAssetIn = HASH_URL.openStream();
                     OutputStream out = Files.newOutputStream(Files.createFile(hashObjectsDirPathRelative.resolve(hash)))) {
                    hashAssetIn.transferTo(out);
                }
                System.out.println("downloading each asset object...");
            }
        }

        System.out.println("finished downloading assets objects");
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
        assetsDirPathRelative = minecraftPathRelative.resolve(assetsDirName);
        Path indexesDirName = Paths.get("indexes");
        indexesDirPathRelative = assetsDirPathRelative.resolve(indexesDirName);
        Path objectsDirName = Paths.get("objects");
        objectsDirPathRelative = assetsDirPathRelative.resolve(objectsDirName);

        //Creating .minecraft and its subdirs
        Files.createDirectories(minecraftPathRelative);
        Files.createDirectories(versionsDirPathRelative);
        Files.createDirectories(librariesDirPathRelative);
        Files.createDirectories(versionDirPathRelative);
        Files.createDirectories(assetsDirPathRelative);
        Files.createDirectories(indexesDirPathRelative);
        Files.createDirectories(objectsDirPathRelative);

    }

}
