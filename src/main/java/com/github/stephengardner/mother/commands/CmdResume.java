package com.github.stephengardner.mother.commands;

import com.github.stephengardner.mother.Conversation;
import com.github.stephengardner.mother.Mother;
import com.github.stephengardner.mother.Util;
import com.ullink.slack.simpleslackapi.SlackChannel;
import com.ullink.slack.simpleslackapi.SlackUser;

import java.sql.SQLException;
import java.util.ArrayList;

public class CmdResume implements CommandExecutor {

  private Mother mom;

  public CmdResume(Mother mom) {
    this.mom = mom;
  }

  @Override
  public boolean onCommand(
      SlackChannel chan, SlackUser user, String[] args, String threadTimestamp) {
    Conversation conv;

    if (args.length != 1) return false;

    try {
      if (args[0].contains(".")) {
        if (mom.findConversation(args[0], false) != null) return false;

        conv = mom.getDatabase().loadConversation(args[0]);
      } else {
        String dstUserID = Util.getTaggedUserID(args[0]);
        SlackUser dstUser = mom.getSession().findUserById(dstUserID);

        if (dstUser == null
            || mom.getConvChannel().getMembers().contains(dstUser)
            || mom.findConversationByUserID(dstUserID) != null) return false;

        ArrayList<String> threads = mom.getDatabase().lookupThreads(dstUserID, 1);

        if (threads.isEmpty()) return false;

        conv = mom.getDatabase().loadConversation(threads.get(0));
      }
    } catch (SQLException e) {
      e.printStackTrace();
      return false;
    }

    if (conv == null) return false;

    mom.startConversation(conv.getUser(), conv.getDirectChannelID(), false);
    return true;
  }
}
