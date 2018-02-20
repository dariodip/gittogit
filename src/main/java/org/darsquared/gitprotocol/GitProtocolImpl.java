package org.darsquared.gitprotocol;

import org.darsquared.gitprotocol.dir.ZipUtils;
import org.darsquared.gitprotocol.storage.Storage;
import org.zeroturnaround.zip.commons.FileUtils;

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

    private final Storage storage;
    private boolean created;
    private File gitFolder;
    private File directory;

    // dependency injection B
    public GitProtocolImpl(Storage _storage) throws IOException{
        this.storage = _storage;
        this.created = false;
    }

    public boolean createRepository(String _repo_name, File _directory) {
        if (created) {
            return false;
        }
        this.directory = _directory;
        try {
            this.gitFolder = this.checkRepo(_repo_name, _directory.getParentFile());
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        try {
            // create a zip archive of the full repo
            File gitZipFile = new File(gitFolder.getAbsolutePath() + "/" + _repo_name + ".zip");
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
        this.created = true;
        return true;
    }

    public boolean addFilesToRepository(String _repo_name, List<File> files) {
        if (!created) {
            return false;
        }
        String filename = gitFolder.getAbsolutePath() + "/" + _repo_name + ".zip";
        ZipUtils.addFiles(files, filename);
        return true;
    }

    public boolean commit(String _repo_name, String _message) {
        if (!created) {
            return false;  // repo does not exist
        }
        File gitZipFile = new File(gitFolder.getAbsolutePath() + "/" + _repo_name + ".zip.0");
        ZipUtils.zipFile(this.directory, gitZipFile.getAbsolutePath());  // temp zip

        File gitFile = new File(gitFolder.getAbsolutePath() + "/" + GIT_FILE); // file in which there is old hash
        try {
            BufferedReader br = new BufferedReader(new FileReader(gitFile));
            String currentHash = br.readLine();  // read old hash
            String mdZip = getZipDigest(gitZipFile.getAbsolutePath());  // get new hash
            if (mdZip.equals(currentHash)) {
                return false; // you cannot commit an untouched repo! this does not make sense
            }
            File oldFile = new File(gitFolder.getAbsolutePath() + "/" + _repo_name + ".zip"); // delete old file
            if (!oldFile.delete()) {
                return false;
            }
            gitFile.renameTo(oldFile); // rename new file as old file

            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(gitFile, false));
            bufferedWriter.write(mdZip); // write new hash

            bufferedWriter.close();
            br.close();
        } catch (Exception e ){
            e.printStackTrace();
        }
        return true;
    }

    public String push(String _repo_name) {
        if (!created) {
            return "";
        }
        File gitZipFile = new File(gitFolder.getAbsolutePath() + "/" + _repo_name + ".zip");
        try {
            storage.put(_repo_name, Files.readAllBytes(Paths.get(gitZipFile.toURI())));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ""; // TODO
    }

    public String pull(String _repo_name) {
        String zipPath = gitFolder.getAbsolutePath() + "/" + _repo_name + ".zip";
        byte[] data = null;
        try {
            data = (byte[]) storage.get(_repo_name);
        } catch (IOException ex) {
            ex.printStackTrace();
            return ""; // TODO
        } catch (ClassNotFoundException cnfe) {
            cnfe.printStackTrace();
            return ""; // TODO
        }
        try {
            FileOutputStream fos = new FileOutputStream(gitFolder.getAbsolutePath() + "/" + _repo_name + ".zip");
            fos.write(data);
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        ZipUtils.unzipFile(new File(zipPath), this.directory.getAbsolutePath());
        return "";
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
