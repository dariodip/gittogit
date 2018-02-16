package org.darsquared.gitprotocol.dir;

import org.zeroturnaround.zip.FileSource;
import org.zeroturnaround.zip.ZipEntrySource;
import org.zeroturnaround.zip.ZipUtil;

import java.io.*;
import java.util.List;

public class ZipUtils {

    /**
     * Given the File object to zip and the output name, archives the file in a zip archive.
     * @param fileToZip File object of the directory/file to zip
     * @param fileName Output filename
     */
    public static void zipFile(File fileToZip, String fileName) {
        ZipUtil.pack(fileToZip, new File(fileName));
    }

    public static void addFiles(List<File> toZip, String filename) {
        ZipEntrySource[] entries = new ZipEntrySource[toZip.size()];
        for (int i = 0; i < toZip.size(); i++) {
            File f = toZip.get(i);
            entries[i] = new FileSource(f.getName(), f);
        }
        ZipUtil.addEntries(new File(filename), entries, new File(filename));
    }

    public static void unzipFile(File zipFile, String outDirectory) {
        ZipUtil.unpack(zipFile, new File(outDirectory));
    }
}
