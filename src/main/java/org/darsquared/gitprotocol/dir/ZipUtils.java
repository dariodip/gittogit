package org.darsquared.gitprotocol.dir;

import org.zeroturnaround.zip.ZipUtil;

import java.io.*;

public class ZipUtils {

    /**
     * Given the File object to zip and the output name, archives the file in a zip archive.
     * @param fileToZip File object of the directory/file to zip
     * @param fileName Output filename
     */
    public static void zipFile(File fileToZip, String fileName) {
        ZipUtil.pack(fileToZip, new File(fileName));
    }

}
