package com.github.stephengardner.mother.commands;

import com.github.stephengardner.mother.Mother;
import com.github.stephengardner.mother.Util;
import com.github.stephengardner.mother.data.Msg;
import com.ullink.slack.simpleslackapi.SlackChannel;
import com.ullink.slack.simpleslackapi.SlackUser;

import java.sql.SQLException;
import java.util.ArrayList;

public class CmdHistory implements CommandExecutor {

  private Mother mom;

  public CmdHistory(Mother mom) {
    this.mom = mom;
  }

  @Override
  public boolean onCommand(
      SlackChannel chan, SlackUser user, String[] args, String threadTimestamp) {
    ArrayList<String> threads;

    if (args.length < 1 || args.length > 2) return false;

    String dstUserID = Util.getTaggedUserID(args[0]);
    SlackUser dstUser = mom.getSession().findUserById(dstUserID);

    if (dstUser == null) return false;

    int page = 1;

    if (args.length == 2) {
      try {
        page = Integer.parseInt(args[1]);
      } catch (NumberFormatException e) {
        return false;
      }
    }

    if (page < 0) return false;

    try {
      threads = mom.getDatabase().lookupThreads(dstUserID, page);
    } catch (SQLException e) {
      e.printStackTrace();
      return false;
    }

    mom.sendToChannel(chan, buildOutputList(dstUserID, threads, page), threadTimestamp);
    return true;
  }

  private String buildOutputList(String userID, ArrayList<String> threads, int page) {
    StringBuilder sb = new StringBuilder();
    boolean empty = true;

    sb.append(String.format(Msg.LIST_THREADS.toString(), userID, page));

    for (String threadLink : threads) {
      if (empty) empty = false;

      sb.append(">").append(threadLink).append("\n");
    }

    if (empty) sb.append(Msg.LIST_NONE.toString());

    return sb.toString();
  }
}
