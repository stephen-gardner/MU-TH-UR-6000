package com.github.stephengardner.mother.commands;

import com.github.stephengardner.mother.Mother;
import com.github.stephengardner.mother.Util;
import com.github.stephengardner.mother.data.LogEntry;
import com.github.stephengardner.mother.data.Msg;
import com.ullink.slack.simpleslackapi.SlackChannel;
import com.ullink.slack.simpleslackapi.SlackUser;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;

public class CmdLogs implements CommandExecutor {

  private Mother mom;

  public CmdLogs(Mother mom) {
    this.mom = mom;
  }

  @Override
  public boolean onCommand(
      SlackChannel chan, SlackUser user, String[] args, String threadTimestamp) {
    ArrayList<LogEntry> logs;

    if (args.length != 1) return false;

    String id = Util.getTaggedUserID(args[0]);

    try {
      if (id != null) {
        logs = mom.getDatabase().lookupLogs(id, true);
        id = mom.getSession().findUserById(id).getRealName().replaceAll(" ", "_").toLowerCase();
      } else {
        logs = mom.getDatabase().lookupLogs(args[0], false);
        id = args[0];
      }
    } catch (SQLException e) {
      e.printStackTrace();
      return false;
    }

    if (logs.isEmpty()) return false;

    String fileName = String.format("log-%s.txt", id);
    File logFile = new File(fileName);

    try {
      buildLogFile(logFile, logs);
      mom.getSession().sendFile(chan, Files.readAllBytes(logFile.toPath()), fileName);
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
    DateFormat df = new SimpleDateFormat(Msg.LOG_TIMESTAMP_FMT.toString());

    df.setTimeZone(TimeZone.getTimeZone(Msg.LOG_TIMESTAMP_ZONE.toString()));

    for (LogEntry log : logs) {
      Msg fmt = (log.isOriginal()) ? Msg.LOG : Msg.LOG_EDITED;
      String name = mom.getSession().findUserById(log.getUserID()).getRealName();
      long epoch = Long.parseLong(log.getTimestamp().split("\\.")[0]);
      Date date = Date.from(Instant.ofEpochSecond(epoch));

      fw.write(String.format(fmt.toString(), df.format(date), name, log.getMessage()));
    }

    fw.close();
  }
}
