package com.github.stephengardner.mother.data;

public enum SQL {
  CREATE_MESSAGES_TABLE(
      "CREATE TABLE IF NOT EXISTS messages ("
          + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
          + " user_id TEXT,"
          + " thread_id TEXT,"
          + " content TEXT,"
          + " timestamp TEXT,"
          + " original BOOLEAN);"),
  CREATE_THREAD_INDEX_TABLE(
      "CREATE TABLE IF NOT EXISTS thread_index ("
          + "thread_id TEXT PRIMARY KEY,"
          + " user_id TEXT);"),
  FIND_THREAD_INDEX("SELECT user_id FROM thread_index WHERE thread_id = ?;"),
  INSERT_MESSAGE(
      "INSERT INTO messages (user_id, thread_id, content, timestamp, original) VALUES (?, ?, ?, ?, ?);"),
  INSERT_THREAD_INDEX("INSERT OR REPLACE INTO thread_index (thread_id, user_id) VALUES (?, ?);"),
  LOOKUP_LOGS_THREAD(
      "SELECT user_id, content, timestamp, original FROM messages"
          + " WHERE thread_id = ?"
          + " ORDER BY timestamp ASC, id DESC;"),
  LOOKUP_LOGS_USER(
      "SELECT user_id, content, timestamp, original FROM messages"
          + " WHERE thread_id IN (SELECT thread_id FROM thread_index WHERE user_id = ?)"
          + " ORDER BY timestamp ASC, id DESC;"),
  LOOKUP_THREADS(
      "SELECT thread_id FROM thread_index WHERE user_id = ?"
          + " ORDER BY thread_id DESC"
          + " LIMIT ?"
          + " OFFSET ?;");

  private final String sql;

  SQL(final String sql) {
    this.sql = sql;
  }

  @Override
  public String toString() {
    return sql;
  }
}
