package com.github.stephengardner.mother.data;

public enum SQL {
  CREATE_MESSAGES_TABLE(
      "CREATE TABLE IF NOT EXISTS %s ("
          + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
          + " user_id TEXT,"
          + " thread_id TEXT,"
          + " content TEXT,"
          + " timestamp TEXT,"
          + " original BOOLEAN);"),
  CREATE_THREAD_INDEX_TABLE(
      "CREATE TABLE IF NOT EXISTS %s ("
          + "thread_id TEXT PRIMARY KEY,"
          + " user_id TEXT,"
          + " timestamp DATETIME DEFAULT CURRENT_TIMESTAMP);"),
  FIND_THREAD_INDEX("SELECT user_id FROM %s" + " WHERE thread_id = ?;"),
  INSERT_MESSAGE(
      "INSERT INTO %s"
          + " (user_id, thread_id, content, timestamp, original)"
          + " VALUES (?, ?, ?, ?, ?);"),
  INSERT_THREAD_INDEX("INSERT OR REPLACE INTO %s" + " (thread_id, user_id)" + " VALUES (?, ?);"),
  LOOKUP_LOGS_THREAD(
      "SELECT user_id, content, timestamp, original FROM %s"
          + " WHERE thread_id = ?"
          + " ORDER BY timestamp ASC, id DESC;"),
  LOOKUP_LOGS_USER(
      "SELECT user_id, content, timestamp, original FROM %s"
          + " WHERE thread_id IN (SELECT thread_id FROM %s WHERE user_id = ?)"
          + " ORDER BY timestamp ASC, id DESC;"),
  LOOKUP_THREADS("SELECT * FROM %s" + " ORDER BY timestamp DESC" + " LIMIT ?" + " OFFSET ?;"),
  LOOKUP_THREADS_USER(
      "SELECT * FROM %s"
          + " WHERE user_id = ?"
          + " ORDER BY timestamp DESC"
          + " LIMIT ?"
          + " OFFSET ?;");

  private final String sql;

  SQL(final String sql) {
    this.sql = sql;
  }

  public String get(Object... args) {
    if (args == null) return sql;

    return String.format(sql, args);
  }
}
