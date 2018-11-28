package com.turjuman.cas;

import java.nio.file.FileSystems;
import java.nio.file.PathMatcher;
import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.*;

import static java.nio.file.FileVisitResult.*;
import static java.nio.file.FileVisitOption.*;

import java.util.*;

public class MainClass {

    public static void main(String[] args) {
        System.out.println(args);
        String src= args.length==0 ?"/home/hshatnawi/Aragats.jpg" : args[0];
        File file = new File(src);
        Path filename= Paths.get(src);
        String fileHash=ContentAddressableStorage.getLocation(filename.toString());
        System.out.println("File Name is : "+filename);
        System.out.println("SHA-1 HASH for the file: "+fileHash);
        System.out.println("Path created for file based on hash: "+ContentAddressableStorage.getFilePath(fileHash,file));

    }

}





