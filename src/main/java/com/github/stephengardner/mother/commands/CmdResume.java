package com.github.stephengardner.mother.commands;

import com.github.stephengardner.mother.Conversation;
import com.github.stephengardner.mother.Mother;
import com.ullink.slack.simpleslackapi.SlackUser;

import java.sql.SQLException;

public class CmdResume implements CommandExecutor {

  private Mother mom;

  public CmdResume(Mother mom) {
    this.mom = mom;
  }

  @Override
  public boolean onCommand(SlackUser user, String[] args, String threadTimestamp) {
    if (args.length != 1) return false;

    Conversation conv = mom.findConversation(args[0], false);

    if (conv != null) return false;

    try {
      conv = mom.getDatabase().loadConversation(args[0]);
    } catch (SQLException e) {
      e.printStackTrace();
      return false;
    }

    if (conv == null) return false;

    mom.startConversation(conv.getUser(), conv.getDirectChannelID(), false);
    return true;
  }
}
