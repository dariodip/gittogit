package org.darsquared.gitprotocol.dir;

import org.apache.commons.codec.digest.DigestUtils;
import org.darsquared.gitprotocol.Commit;
import org.darsquared.gitprotocol.dir.exception.NotADirectoryException;

import java.io.*;
import java.security.NoSuchAlgorithmException;
import java.util.*;

/**
 * Repository class abstracts a repository:
 *      a folder with all their files and a digest of that.
 */
public class Repository implements Serializable {

    private static final long serialVersionUID = 1L;
    private final ArrayList<File> files;
    private final ArrayList<Commit> commits;
    private String repoName;
    private String rootDirectory;
    private String digest;


    public Repository(String rootDirectory, String repoName) throws NotADirectoryException, IOException, NoSuchAlgorithmException {
        this.rootDirectory = rootDirectory;
        this.repoName = repoName;
        this.files = getAllFiles();
        this.digest = getFolderDigest();
        this.commits = new ArrayList<Commit>();
    }

    public ArrayList<File> getFiles() {
        return files;
    }

    public String getRootDirectory() {
        return rootDirectory;
    }

    public String getDigest() {
        return digest;
    }

    public ArrayList<Commit> getCommits() {
        return commits;
    }

    public boolean addFiles(List<File> files) {
        if (files.size() < 1) return false;
        files
                .parallelStream()
                .filter(f -> f.getAbsolutePath().startsWith(this.rootDirectory))
                .forEach(f -> this.files.add(f));
        return true;
    }

    public void computeDigest() throws IOException, NoSuchAlgorithmException {
        this.digest = getFolderDigest();
    }

    public void addCommit(String message, String repoName) throws IOException, NoSuchAlgorithmException {
        String digest = getFolderDigest();
        Commit c = new Commit(message, repoName, digest);
        commits.add(c);
    }

    public boolean replaceFiles() {
        // TODO
        return false;
    }

    private ArrayList<File> getAllFiles() throws NotADirectoryException {
        ArrayList<File> files = new ArrayList<File>();
        File root = new File(this.rootDirectory);
        if (!root.isDirectory()) {
            throw new NotADirectoryException("Root is not a directory");
        }
        File[] filesInRoot = root.listFiles();
        Arrays.
                stream(filesInRoot != null ? filesInRoot : new File[0])
                .parallel()
                .forEach(f -> files.add(f));
        return files;
    }

    public String getLastDigest() {
        return getDigests().get(0);
    }

    public ArrayList<String> getDigests() {
        ArrayList<String> digests = new ArrayList<>(commits.size());
        commits
                .parallelStream()
                .sorted((c1, c2) -> c1.getTimestamp().compareTo(c2.getTimestamp()))
                .forEach(c -> digests.add(c.getDigest()));
        return digests;
    }

    private String getFolderDigest() throws NoSuchAlgorithmException, IOException {
        File folder = new File(rootDirectory);
        assert (folder.isDirectory());  // We already checked it, but redo it just for safety
        Vector<FileInputStream> fileStreams = new Vector<FileInputStream>();
        collectInputStreams(folder, fileStreams, true);
        SequenceInputStream seqStream = new SequenceInputStream(fileStreams.elements());

        try {
            String md5Hash = DigestUtils.md5Hex(seqStream);
            seqStream.close();
            return md5Hash;
        }
        catch (IOException e) {
            throw new RuntimeException("Error reading files to hash in " + folder.getAbsolutePath(), e);
        }
    }

    private void collectInputStreams(File dir,
                                     List<FileInputStream> foundStreams,
                                     boolean includeHiddenFiles) {

        File[] fileList = dir.listFiles();
        assert (fileList != null);
        Arrays.sort(fileList, (f1, f2) -> f1.getName().compareTo(f2.getName())); // Need in reproducible order

        for (File f : fileList) {
            if (!includeHiddenFiles && f.getName().startsWith(".")) {
                // Skip it
            }
            else if (f.isDirectory()) {
                collectInputStreams(f, foundStreams, includeHiddenFiles);
            }
            else {
                try {
                    foundStreams.add(new FileInputStream(f));
                }
                catch (FileNotFoundException e) {
                    throw new AssertionError(e.getMessage() + ": file should never not be found!");
                }
            }
        }

    }

}
