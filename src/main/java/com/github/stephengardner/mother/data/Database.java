package com.github.stephengardner.mother.data;

import com.github.stephengardner.mother.Conversation;
import com.github.stephengardner.mother.Main;
import com.github.stephengardner.mother.Mother;
import com.ullink.slack.simpleslackapi.SlackUser;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;

public class Database {

  private Mother mom;
  private Connection conn;

  public Database(Mother mom, String dbPath) throws SQLException {
    this.mom = mom;
    conn = openConnection(new File(dbPath));
  }

  public Conversation loadConversation(String threadTimestamp) throws SQLException {
    PreparedStatement ps = conn.prepareStatement(SQL.FIND_THREAD_INDEX.toString());
    Conversation conv = null;

    ps.setString(1, threadTimestamp);

    ResultSet rs = ps.executeQuery();

    if (!rs.next()) {
      rs.close();
      ps.close();
      return null;
    }

    SlackUser user = mom.getSession().findUserById(rs.getString("user_id"));

    if (!mom.inConvChannel(user.getId())) {
      conv = new Conversation(mom, user.getId(), mom.getUserChannel(user).getId(), threadTimestamp);

      if (mom.addConversation(conv.getDirectChannelID(), conv) == null) {
        conv.sendToThread(Msg.SESSION_RESUME_CONV.toString());
        conv.sendToUser(Msg.SESSION_RESUME_DIRECT.toString());
      }
    }

    rs.close();
    ps.close();
    return conv;
  }

  public ArrayList<LogEntry> lookupLogs(String id, boolean isUser) throws SQLException {
    ArrayList<LogEntry> logs = new ArrayList<>();
    SQL sql = (isUser) ? SQL.LOOKUP_LOGS_USER : SQL.LOOKUP_LOGS_THREAD;
    PreparedStatement ps = conn.prepareStatement(sql.toString());

    ps.setString(1, id);

    ResultSet rs = ps.executeQuery();

    while (rs.next()) {
      logs.add(
          new LogEntry(
              rs.getString("user_id"),
              rs.getString("content"),
              rs.getString("timestamp"),
              rs.getBoolean("original")));
    }

    rs.close();
    ps.close();
    return logs;
  }

  public ArrayList<String> lookupThreads(String userID, int page) throws SQLException {
    ArrayList<String> threads = new ArrayList<>();
    PreparedStatement ps = conn.prepareStatement(SQL.LOOKUP_THREADS.toString());

    ps.setString(1, userID);
    ps.setInt(2, Main.getConfig().getThreadsPerPage());
    ps.setInt(3, (Main.getConfig().getThreadsPerPage() * (page - 1)));

    ResultSet rs = ps.executeQuery();

    while (rs.next()) threads.add(rs.getString("thread_id"));

    rs.close();
    ps.close();
    return threads;
  }

  public void saveMessages(Conversation conv) throws SQLException {
    PreparedStatement ps;

    ps = conn.prepareStatement(SQL.INSERT_THREAD_INDEX.toString());
    ps.setString(1, conv.getThreadTimestamp());
    ps.setString(2, conv.getUserID());
    ps.executeUpdate();
    ps = conn.prepareStatement(SQL.INSERT_MESSAGE.toString());

    for (LogEntry log : conv.getLogs()) {
      ps.setString(1, log.getUserID());
      ps.setString(2, conv.getThreadTimestamp());
      ps.setString(3, log.getMessage());
      ps.setString(4, log.getTimestamp());
      ps.setBoolean(5, log.isOriginal());
      ps.addBatch();
    }

    ps.executeBatch();
    ps.close();
  }

  private Connection openConnection(File dbFile) throws SQLException {
    Connection conn = DriverManager.getConnection("jdbc:sqlite:" + dbFile.getAbsolutePath());
    Statement stmt = conn.createStatement();

    stmt.execute(SQL.CREATE_MESSAGES_TABLE.toString());
    stmt.execute(SQL.CREATE_THREAD_INDEX_TABLE.toString());
    stmt.close();
    return conn;
  }

  public void close() throws SQLException {
    conn.close();
  }
}
