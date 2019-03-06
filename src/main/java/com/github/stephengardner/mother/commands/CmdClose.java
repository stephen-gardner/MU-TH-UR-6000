package com.github.stephengardner.mother.commands;

import com.github.stephengardner.mother.Conversation;
import com.github.stephengardner.mother.Main;
import com.github.stephengardner.mother.Mother;
import com.github.stephengardner.mother.Util;
import com.ullink.slack.simpleslackapi.SlackChannel;
import com.ullink.slack.simpleslackapi.SlackUser;

import java.sql.SQLException;

public class CmdClose implements CommandExecutor {

  private Mother mom;

  public CmdClose(Mother mom) {
    this.mom = mom;
  }

  @Override
  public boolean onCommand(
      SlackChannel chan, SlackUser user, String[] args, String threadTimestamp) {
    Conversation conv;

    if (args.length != 1) return false;

    if (args[0].contains(".")) {
      conv = mom.findConversation(args[0], false);
    } else {
      String dstUserID = Util.getTaggedUserID(args[0]);
      SlackUser dstUser = mom.getSession().findUserById(dstUserID);

      if (dstUser == null) return false;

      conv = mom.findConversationByUserID(dstUserID);
    }

    if (conv == null) return false;

    conv.expire();

    try {
      mom.reapConversations(Main.getConfig().getSessionTimeout());
    } catch (SQLException e) {
      e.printStackTrace();
      return false;
    }

    return true;
  }
}
