package com.github.stephengardner.mother.commands;

import com.github.stephengardner.mother.Mother;
import com.github.stephengardner.mother.data.LogEntry;
import com.ullink.slack.simpleslackapi.SlackUser;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.SQLException;
import java.util.ArrayList;

public class CmdLogs implements CommandExecutor {

    private Mother mom;

    public CmdLogs(Mother mom) {
        this.mom = mom;
    }

    @Override
    @SuppressWarnings("Duplicates")
    public boolean onCommand(SlackUser user, String[] args, String threadTimestamp) {
        ArrayList<LogEntry> logs;

        if (args.length != 1) return false;

        try {
            logs = mom.getDatabase().lookupLogsByThreadID(args[0]);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }

        if (logs.isEmpty()) return false;

        String fileName = String.format("log-%s.txt", args[0]);
        File logFile = new File(fileName);

        try {
            buildLogFile(logFile, logs);
            mom.getSession().sendFile(mom.getChannel(), Files.readAllBytes(logFile.toPath()), fileName);
        } catch (IOException e) {
            e.printStackTrace();
            logFile.delete();
            return false;
        }

        logFile.delete();
        return true;
    }

    private void buildLogFile(File logFile, ArrayList<LogEntry> logs) throws IOException {
        FileWriter fw = new FileWriter(logFile);

        for (LogEntry log : logs) {
            String userName = mom.getSession().findUserById(log.getUserID()).getUserName();

            if (log.isOriginal()) fw.write(String.format("%s: %s\n", userName, log.getMessage()));
            else fw.write(String.format("%s: %s (edited)\n", userName, log.getMessage()));
        }

        fw.close();
    }
}
