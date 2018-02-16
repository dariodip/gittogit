package org.darsquared.gitprotocol;


import org.darsquared.gitprotocol.dir.ZipUtils;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

public class GitProtocolImpl implements GitProtocol {

    public static final String GIT_FILE = ".git2git";
    public static final String GIT_FOLDER = "/.git/";
    public static final Charset STD_ENCODING = StandardCharsets.UTF_8;
    public static final String STD_ALGORITHM = "SHA-1";

    public boolean createRepository(String _repo_name, File _directory) {
        File gitFolder = null;
        try {
            gitFolder = this.checkRepo(_repo_name, _directory.getParentFile());
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        try {
            // create a zip archive of the full repo
            String repoNameMd = getNameDigest(_repo_name);  // get SHA-1 of the repo's name
            File gitZipFile = new File(gitFolder.getAbsolutePath() + "/" + repoNameMd + ".zip");
            ZipUtils.zipFile(_directory, gitZipFile.getAbsolutePath());

            // get the digest of the zip file in order to easily check if there were some changes
            String zipDigest = getZipDigest(gitZipFile.getAbsolutePath());
            File gitFile = new File(gitFolder.getAbsolutePath() + "/" + GIT_FILE);
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(gitFile));
            bufferedWriter.write(zipDigest);
            bufferedWriter.close();

        } catch (NoSuchAlgorithmException ex) {
            ex.printStackTrace(); //TODO
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public boolean addFilesToRepository(String _repo_name, List<File> files) {

        return false;

    }

    public boolean commit(String _repo_name, String _message) {
        return false;
    }

    public String push(String _repo_name) {
        return null;
    }

    public String pull(String _repo_name) {
        return null;
    }


    public boolean deleteRepo(File _directory) {
        File gitFolder = new File(_directory.getParentFile().getAbsolutePath() + GIT_FOLDER);
        String[] entries = gitFolder.list();
        if (entries == null) {
            return gitFolder.delete(); // already empty
        }
        for(String s: entries){
            File currentFile = new File(gitFolder.getPath(),s);
            if (!currentFile.delete()) {
                return false;
            }
        }
        return gitFolder.delete();
    }

    private File checkRepo(String _repo_name, File _directory) throws  Exception{
        if (! _directory.isDirectory()) {  // the repo is not a directory
            throw new Exception("The repo is not a directory");
        }
        File gitFolder = new File(_directory.getAbsolutePath() + GIT_FOLDER);

        if (gitFolder.exists()) { // there is already a repo
            throw new Exception("There is already a repo");
        }
        if (!gitFolder.mkdir()) {  // cannot create git folder
            throw new Exception("Cannot create git folder");
        }
        return gitFolder;
    }

    private String getNameDigest(String _repo_name) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance(STD_ALGORITHM);
        byte[] md5bytes = md.digest(_repo_name.getBytes(STD_ENCODING));
        return mdToString(md5bytes);
    }

    private String getZipDigest(String zipPath) throws NoSuchAlgorithmException, IOException  {
        MessageDigest md = MessageDigest.getInstance(STD_ALGORITHM);
        try (InputStream is = Files.newInputStream(Paths.get(zipPath));
             DigestInputStream dis = new DigestInputStream(is, md))
        { }
        byte[] digest = md.digest();
        return mdToString(digest);
    }

    private String mdToString(byte[] md5bytes) {
        StringBuffer sb = new StringBuffer();
        for (byte md5byte : md5bytes) {
            sb.append(Integer.toString((md5byte & 0xff) + 0x100, 16).substring(1));
        }
        return sb.toString();
    }
}
