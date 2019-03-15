package com.github.stephengardner.mother.commands;

import com.github.stephengardner.mother.Mother;
import com.github.stephengardner.mother.Util;
import com.github.stephengardner.mother.data.Msg;
import com.github.stephengardner.mother.data.ThreadInfo;
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
    ArrayList<ThreadInfo> threads;

    if (args.length > 2) return false;

    String dstUserID = (args.length > 0) ? Util.getTaggedUserID(args[0]) : null;
    int page = 1;

    if ((args.length > 0 && dstUserID == null) || args.length > 1) {
      try {
        page = Integer.parseInt((dstUserID == null) ? args[0] : args[1]);
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

  private String buildOutputList(String userID, ArrayList<ThreadInfo> threads, int page) {
    StringBuilder sb = new StringBuilder();

    if (userID != null) sb.append(String.format(Msg.LIST_THREADS_USER.get(mom), userID, page));
    else sb.append(String.format(Msg.LIST_THREADS.get(mom), page));

    for (ThreadInfo thread : threads) {
      String threadLink =
          Util.getThreadLink(mom, mom.getConfig().getConvChannelID(), thread.getThreadID());

      if (userID != null) {
        sb.append(
            String.format(Msg.LIST_THREADS_ELE_USER.get(mom), threadLink, thread.getTimestamp()));
      } else {
        sb.append(
            String.format(
                Msg.LIST_THREADS_ELE.get(mom),
                threadLink,
                thread.getUserID(),
                thread.getTimestamp()));
      }
    }

    if (threads.isEmpty()) sb.append(Msg.LIST_NONE.get(mom));

    return sb.toString();
  }
}
