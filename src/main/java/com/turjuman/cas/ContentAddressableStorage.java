package com.turjuman.cas;

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

public class ContentAddressableStorage {
    private final static HashFunction fun = Hashing.sha1();
    private final static String collisionFilePath = "collisions.json";

    public static @NotNull String getLocation(@NotNull String path) {
        return fun.hashString(path, StandardCharsets.UTF_8).toString();
    }

    public static FileReader getCollisionsFileReader() throws IOException {
        File collisionFile = new File(collisionFilePath);
        collisionFile.createNewFile();
        return new FileReader(collisionFile);
    }

    public static void writeCollisionsFile(HashMap<String, HashSet<String>> hashMap) throws IOException {
        File collisionFile = new File(collisionFilePath);
        FileWriter fileWriter = new FileWriter(collisionFile);
        Gson gson = new Gson();
        fileWriter.write(gson.toJson(hashMap));
        fileWriter.flush();
        fileWriter.close();
    }

    public static Path getFilePath(String hash, File file) {
        return getFilePath(hash, file, "");

    }

    public static Path getFilePath(String hash, File file, String parentPath) {
        Path path = null;

        try {
            Path locationPath = Paths.get(parentPath
                    , hash.substring(0, 4)
                    , hash.substring(4, 8)
                    , hash.substring(8, 12)
                    , hash.substring(12));
            boolean directory_has_contents = false;
            path = Files.createDirectories(locationPath);

            Iterator<Path> directoryIterator = Files.newDirectoryStream(locationPath).iterator();
            if (directoryIterator.hasNext()) {
                directory_has_contents = true;

            }

            // check if there is a collision
            FileReader collisionFile = getCollisionsFileReader();
            Gson gson = new Gson();
            HashMap<String, HashSet<String>> collisions = new HashMap<>();
            collisions = gson.fromJson(collisionFile, new TypeToken<HashMap<String, HashSet<String>>>() {
            }.getType());
            String imageFileName = file.getName();
            if (collisions != null && collisions.containsKey(hash)) // there is a collision already
            {
                // HashSet returns true if add method tries to add new  item
                // pathExists equals true when add method returns true
                HashSet<String> hashSet = new HashSet<>();
                hashSet.add(file.getAbsolutePath());
                HashSet<String> result = collisions.get(hash);
                boolean pathExists = result.add(file.getAbsolutePath());
                result.addAll(hashSet);
                // in case of collision, make the path ends with image file name
                if (pathExists)
                    locationPath = Paths.get(locationPath.toString(), "dir_" + imageFileName);
                else if (directory_has_contents && directoryIterator != null)
                    while (directoryIterator.hasNext()) {
                        if (directoryIterator.next().endsWith("dir_" + imageFileName)) {
                            locationPath = Paths.get(locationPath.toString(), "dir_" + imageFileName);
                            path = Files.createDirectories(locationPath);
                            break;
                        }
                    }
            }
            // Check if path already exists  && path has contents  && image is not one of the contents -> there is a new collision
            else if (Files.exists(locationPath) && directory_has_contents && !Files.exists(Paths.get(locationPath.toString(), imageFileName))) {
                HashSet<String> hashSet = new HashSet<>();
                hashSet.add(file.getAbsolutePath());
                if (collisions == null)
                    collisions = new HashMap<>();
                collisions.putIfAbsent(hash, hashSet);
                writeCollisionsFile(collisions); // Add this new collision to the JSON collision file
                locationPath = Paths.get(locationPath.toString(), "dir_" + imageFileName);
                path = Files.createDirectories(locationPath);

            }

        } catch (IOException e) {
            System.out.println("Couldn't create directories :" + e.getStackTrace());
        }

        return path;
    }


}
