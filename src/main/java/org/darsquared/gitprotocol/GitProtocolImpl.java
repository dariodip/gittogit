package org.darsquared.gitprotocol;

import org.darsquared.gitprotocol.dir.ZipUtils;
import org.darsquared.gitprotocol.storage.Storage;
import org.zeroturnaround.zip.commons.FileUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

// TODO move zip/unzip in push and pull

public class GitProtocolImpl implements GitProtocol {

    public static final String GIT_FILE = ".git2git";
    public static final String GIT_FOLDER = "/git/";
    public static final String STD_ALGORITHM = "MD5";

    private final Storage storage;
    private boolean created;
    private File gitFolder;
    private File directory;

    // dependency injection B-)
    public GitProtocolImpl(Storage _storage) {
        this.storage = _storage;
        this.created = false;
    }

    public boolean createRepository(String _repo_name, File _directory) {
        if (created) {  // OPS! repo was already created!
            return false;
        }
        this.directory = _directory;
        try {
            this.gitFolder = this.checkRepo(_repo_name, _directory);  // repo already exists
        } catch (Exception e) {
            // TODO
            e.printStackTrace();
            return false;
        }
        try {
            // create a zip archive of the whole repo and store it in [_repo_name].zip
            File gitZipFile = new File(gitFolder.getAbsolutePath() + "/" + _repo_name + ".zip");
            ZipUtils.zipFile(_directory, gitZipFile.getAbsolutePath());

            // get the digest of the zip file in order to easily check if there were some changes
            String zipDigest = getZipDigest(gitZipFile.getAbsolutePath());
            File gitFile = new File(gitFolder.getAbsolutePath() + "/" + GIT_FILE);
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(gitFile));
            bufferedWriter.write(zipDigest + "\n");
            bufferedWriter.close();

        } catch (NoSuchAlgorithmException ex) {
            ex.printStackTrace(); //TODO
            return false;
        } catch (IOException e) {
            e.printStackTrace(); // TODO
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
        File newZipFile = new File(gitFolder.getAbsolutePath() + "/" + _repo_name + ".0.zip");
        ZipUtils.zipFile(this.directory, newZipFile.getAbsolutePath());  // create new temp zip

        File gitFile = new File(gitFolder.getAbsolutePath() + "/" + GIT_FILE); // file in which there are old hashes
        try {
            // read last line TODO
            BufferedReader br = new BufferedReader(new FileReader(gitFile));
            String thisLine = "";
            String oldHash = "";
            while ((thisLine = br.readLine()) != null) {  // read the last line of the file
                // TODO refactor to be more polite
                oldHash = thisLine;
            }
            String mdZip = getZipDigest(newZipFile.getAbsolutePath());  // get new hash
            System.out.println(oldHash);
            System.out.println(mdZip);
            if (mdZip.equals(oldHash)) {
                System.out.println("New commit equals to old one");
                //newZipFile.delete();
                return false; // you cannot commit an untouched repo! this does not make sense
            }
            File oldFile = new File(gitFolder.getAbsolutePath() + "/" + _repo_name + ".zip"); // delete old file
            if (!oldFile.delete()) {
                System.out.println("Cannot delete temporary file");
                return false;  // I cannot delete the file
            }
            if (!newZipFile.renameTo(oldFile)) {  // rename new file as old file
                System.out.println("Cannot rename temporary file");
                return false; // I cannot rename the file
            }

            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(gitFile, true));
            bufferedWriter.write(mdZip); // write new hash in the file containing hashes

            bufferedWriter.close();
            br.close();
        } catch (Exception e ){
            e.printStackTrace();
        }
        return true;
    }

    public String push(String _repo_name) {
        // TODO check conflicts
        if (!created) {
            return "";
        }
        File gitZipFile = new File(gitFolder.getAbsolutePath() + "/" + _repo_name + ".zip");
        // TODO check
        try {
            storage.put(_repo_name, Files.readAllBytes(Paths.get(gitZipFile.toURI())));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ""; // TODO
    }

    public String pull(String _repo_name) {
        // TODO check conflicts
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
        // TODO
        File gitFolder = new File(_directory.getAbsolutePath() + GIT_FOLDER);
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
        byte[] b = Files.readAllBytes(Paths.get(zipPath));
        byte[] hash = MessageDigest.getInstance(STD_ALGORITHM).digest(b);
        return mdToString(hash);
    }

    private String mdToString(byte[] md5bytes) {
        StringBuffer sb = new StringBuffer();
        for (byte md5byte : md5bytes) {
            sb.append(Integer.toString((md5byte & 0xff) + 0x100, 16).substring(1));
        }
        return sb.toString();
    }
}
