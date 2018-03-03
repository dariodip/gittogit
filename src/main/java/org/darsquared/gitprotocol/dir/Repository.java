package org.darsquared.gitprotocol.dir;

import org.apache.commons.codec.digest.DigestUtils;
import org.darsquared.gitprotocol.Commit;
import org.darsquared.gitprotocol.dir.exception.NotADirectoryException;

import java.io.*;
import java.nio.file.Files;
import java.security.NoSuchAlgorithmException;
import java.util.*;

/**
 * {@code Repository} class abstracts a repository:
 *      a folder with all their;
 *      a digest of that folder;
 *      a set of commits (see class {@link Commit}.
 */
public class Repository implements Serializable {

    private static final long serialVersionUID = 1L;

    private final Map<File, byte[]> filemap;

    private final ArrayList<File> files;        // list of files
    private final ArrayList<Commit> commits;    // list of commits
    private String repoName;                    // name of the repository
    private String rootDirectory;               // root directory of the repository
    private String digest;                      // digest with the current file state

    /**
     * Creates a repository.
     * @param rootDirectory  directory in which there are files to track
     * @param repoName  name of the repository
     * @throws NotADirectoryException  thrown if {@code rootDirectory} is not a directory
     * @throws IOException  thrown if there were problems in accessing the filesystem
     * @throws NoSuchAlgorithmException  thrown if there were some error in digesting file content
     */
    public Repository(String rootDirectory, String repoName) throws NotADirectoryException, IOException, NoSuchAlgorithmException {
        this.rootDirectory = rootDirectory;
        this.repoName = repoName;
        this.files = getAllFiles();
        this.digest = getFolderDigest();
        this.commits = new ArrayList<Commit>();
        this.filemap = new HashMap<>();
        for (File f: this.files) {
            this.filemap.put(f, Files.readAllBytes(f.toPath()));
        }
    }

    /**
     * Returns the filemap of the repository
     * @return map of file-bytearray
     */
    public Map<File, byte[]> getFilemap() {
        return filemap;
    }

    /**
     * Returns all the files of the repository
     * @return list of files
     */
    public ArrayList<File> getFiles() {
        return files;
    }

    /**
     * Returns root directory
     */
    public String getRootDirectory() {
        return rootDirectory;
    }

    /**
     * Returns last digest of the repository
     * @return MD5 of the full repository
     */
    public String getDigest() {
        return digest;
    }

    /**
     * Returns a list of all the commits.
     * n.b. the list is just a clone in order to prevent unauthorized access to commits.
     * @return List of {@link Commit}
     */
    public List<Commit> getCommits() {
        return (ArrayList<Commit>) commits.clone();
    }

    /**
     * Adds new files to the repository so that they can be tracked.
     * @param files {@link List} of {@link File}.
     * @return true if the file list is correct, false otherwise.
     */
    public boolean addFiles(List<File> files) {
        if (files.size() < 1) return false;
        files
                .parallelStream()
                .filter(f -> f.getAbsolutePath().startsWith(this.rootDirectory))
                .forEach(f -> this.files.add(f));
        //TODO aggiornare filemap
        return true;
    }

    /**
     * Adds a new commit to the set.
     * @param message Commit message
     * @param repoName name of the repository
     * @throws IOException thrown if file access goes bad
     * @throws NoSuchAlgorithmException thrown if digest goes bad
     */
    public void addCommit(String message, String repoName) throws IOException, NoSuchAlgorithmException {
        String digest = getFolderDigest();
        Commit c = new Commit(message, repoName, digest);
        commits.add(c);
    }

    /**
     * Replace all the edited files with the new ones.
     * @return true if ok, false otherwise
     * @param filemap map of file edited and their content to update in local
     */
    public boolean replaceFilesFromMap(Map<File,byte[]> filemap) {
        for(File editedFile: filemap.keySet()) {
            OutputStream os = null;
            try {
                File localFile = new File(getRootDirectory() + "/" +editedFile.getName());
                os = new FileOutputStream(localFile);
                os.write(filemap.get(editedFile));
                os.flush();
                os.close();
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
            this.filemap.put(editedFile,filemap.get(editedFile));
        }
        return true;
    }

    /**
     * Replace all the edited files with the new ones.
     * @return true if ok, false otherwise
     * @param editedFiles list of file edited to update in local
     */
    public boolean replaceFiles(List<File> editedFiles) {
        // TODO https://www.journaldev.com/861/java-copy-file
        // https://github.com/chenlonggang/postman/tree/master/src/org/chen/p2p
        List<File> toRename = new ArrayList<>(editedFiles.size());
        for (File editedFile: editedFiles) {
            InputStream is = null;
            OutputStream os = null;
            try {
                is = new FileInputStream(editedFile);
                File tempFile = new File(getRootDirectory() + "/" +editedFile.getName() + ".tmp");
                os = new FileOutputStream(tempFile);
                toRename.add(tempFile);
                byte[] buffer = new byte[1024];
                int length;
                while ((length = is.read(buffer)) > 0) {
                    os.write(buffer, 0, length);
                }
                is.close();
                os.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        editedFiles.forEach(f -> f.delete());
        toRename.forEach(f -> f.renameTo(new File(f.getAbsolutePath().substring(0, f.getAbsolutePath().length() - 4))));
        return false;
    }

    /**
     * Returns the {@link ArrayList} of all the files.
     * @return ArrayList of {@link File}
     * @throws NotADirectoryException thrown if the root directory is a file instead
     */
    private ArrayList<File> getAllFiles() throws NotADirectoryException {
        ArrayList<File> files = new ArrayList<File>();
        File root = new File(this.rootDirectory);
        if (!root.isDirectory()) {
            throw new NotADirectoryException("Root is not a directory");
        }
        fileList(root.getAbsolutePath(), files);
        return files;
    }

    /**
     * Recursive method to list all files from a folder (including all the subfolders).
     * @param directoryName (local or global) root directory
     * @param files {@link ArrayList} of files
     */
    private void fileList(String directoryName, ArrayList<File> files) {
        File directory = new File(directoryName);

        // get all the files from a directory
        File[] fList = directory.listFiles();
        assert fList != null;
        for (File file : fList) {
            if (file.isFile()) {
                files.add(file);
            } else if (file.isDirectory()) {
                fileList(file.getAbsolutePath(), files);
            }
        }
    }

    /**
     * Returns the digests of all the commits
     * @return java.util.ArrayList of digests
     */
    public ArrayList<String> getDigests() {
        ArrayList<String> digests = new ArrayList<>(commits.size());
        commits
                .parallelStream()
                .sorted((c1, c2) -> c1.getTimestamp().compareTo(c2.getTimestamp()))
                .forEach(c -> digests.add(c.getDigest()));
        return digests;
    }

    /**
     * Compute digest of the whole folder.
     * @return digest of the folder.
     * @throws NoSuchAlgorithmException something goes bad with digest
     * @throws IOException something goes bad with IO
     */
    private String getFolderDigest() throws NoSuchAlgorithmException, IOException {
        File folder = new File(rootDirectory);
        assert (folder.isDirectory());  // We already checked it, but redo it just for safety
        Vector<FileInputStream> fileStreams = new Vector<FileInputStream>();
        collectInputStreams(folder, fileStreams, true);
        SequenceInputStream seqStream = new SequenceInputStream(fileStreams.elements());

        try {
            String md5Hash = DigestUtils.md5Hex(seqStream);
            seqStream.close();
            this.digest = md5Hash;
            return md5Hash;
        }
        catch (IOException e) {
            throw new RuntimeException("Error reading files to hash in " + folder.getAbsolutePath(), e);
        }
    }

    /**
     *
     * Recursive method to list all FileInputStream from a folder (including all the subfolders).
     * @param dir (local or global) root directory
     * @param foundStreams {@link ArrayList} of files
     * @param includeHiddenFiles true if you want to exclude hidden files
     */
    private void collectInputStreams(File dir, List<FileInputStream> foundStreams, boolean includeHiddenFiles) {

        File[] fileList = dir.listFiles();
        assert (fileList != null);
        Arrays.sort(fileList, (f1, f2) -> f1.getName().compareTo(f2.getName())); // Need in reproducible order

        for (File f : fileList) {
            if (includeHiddenFiles || !f.getName().startsWith(".")) {
                if (f.isDirectory()) {
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

}
