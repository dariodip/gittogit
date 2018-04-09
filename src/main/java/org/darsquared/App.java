package org.darsquared;

import org.beryx.textio.TextIO;
import org.beryx.textio.TextIoFactory;
import org.beryx.textio.TextTerminal;
import org.darsquared.gitprotocol.GitProtocol;
import org.darsquared.gitprotocol.GitProtocolImpl;
import org.darsquared.gitprotocol.storage.DHTStorage;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Logger;

public class App {
    private static final String help = "Available commands:\n" +
            "\tcreate - create a new repository in local folder\n" +
            "\tadd [option]- add files to source control\n" +
            "\tcommit [-m] - commit with message the changes\n" +
            "\tpull - pull from remote repository\n" +
            "\tpush - push to remote repository\n" +
            "\texit - exit from the application\n";

    private static Logger logger = Logger.getLogger(App.class.getName());

    public static void main(String[] args) {
        TextIO textIO = TextIoFactory.getTextIO();
        TextTerminal terminal = textIO.getTextTerminal();
        terminal.println("************ WELCOME INTO DSQUAREDGIT ************");
        terminal.printf(help);
        int id = textIO.newIntInputReader()
                .read("Inserisci il tuo id: ");

        GitProtocol git = null;
        try {
            git = new GitProtocolImpl(new DHTStorage(id, 4000, "127.0.0.1", 4000));
        } catch (IOException e) {
            logger.severe("Error while instantiating GitProtocol");
            e.printStackTrace();
            return;
        }
        boolean interactive = true;
        while (interactive) {
            String input = textIO.newStringInputReader()
                    .read("> git ");
            logger.info("input: "+input);
            String[] typed = input.split(" ");
            logger.info("input splitted: "+ Arrays.toString(typed));
            String cmd = typed[0];
            logger.info("catched command in input:"+cmd);
            switch (cmd) {
                case "create":
                    logger.info("create");
                    if (typed.length < 3) {
                        terminal.println("Create usage: create repo_name directory_name\n");
                    }
                    else {
                        String repoName = typed[1];
                        File directory = new File(typed[2]);
                        terminal.println(git.createRepository(repoName,directory)?"Repository successfully created!":"Error while creating repository...");
                    }
                    break;
                case "add":
                    logger.info("add");
                    if(typed.length < 3) {
                        terminal.println("Add usage: add repo_name file_names [...]\n");
                    }
                    else {
                        String repoName = typed[1];
                        ArrayList<File> filesToAdd = new ArrayList<>();
                        for (String filename: typed) {
                            File file = new File(filename);
                            filesToAdd.add(file);
                        }
                        terminal.println(git.addFilesToRepository(repoName,filesToAdd)?"Added files to repository!":"Error while add");
                    }
                    break;
                case "commit":
                    logger.info("commit");
                    if(typed.length < 2) {
                        terminal.println("Commit usage: commit repo_name [\"message\"]\n");
                    }
                    else if (typed.length < 3) {
                        String repoName = typed[1];
                        String message = textIO.newStringInputReader()
                                .read("\tcommit message:");
                        terminal.println(git.commit(repoName,message)?"Committed successfully!":"Error while commit...");
                    }
                    else {
                        String repoName = typed[1];
                        String message = input.substring(input.indexOf('"'),input.indexOf('"',input.indexOf('"')));
                        terminal.println(git.commit(repoName,message)?"Committed successfully!":"Error while commit...");
                    }
                    break;
                case "pull":
                    logger.info("pull");
                    if (typed.length < 2) {
                        terminal.println("Pull usage: pull repo_name\n");
                    }
                    else {
                        String repoName = typed[1];
                        terminal.println(git.pull(repoName));
                    }
                    break;
                case "push":
                    logger.info("push");
                    if (typed.length < 2) {
                        terminal.println("Push usage: pull repo_name\n");
                    }
                    else {
                        String repoName = typed[1];
                        terminal.println(git.push(repoName));
                    }
                    break;
                case "exit":
                    logger.info("exit");
                    interactive = false;
                    break;
                default:
                    logger.info("default");
                    terminal.println("Unknown command...");
                    terminal.printf(help);
                    break;
            }
        }
        //TODO can do some log stuff prior to exit
        terminal.dispose();
        textIO.dispose();
    }
}
