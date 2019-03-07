package com.github.stephengardner.mother.commands;

import com.github.stephengardner.mother.Conversation;
import com.github.stephengardner.mother.Mother;
import com.github.stephengardner.mother.Util;
import com.ullink.slack.simpleslackapi.SlackChannel;
import com.ullink.slack.simpleslackapi.SlackUser;

public class CmdContact implements CommandExecutor {

  private Mother mom;

  public CmdContact(Mother mom) {
    this.mom = mom;
  }

  @Override
  public boolean onCommand(
      SlackChannel chan, SlackUser user, String[] args, String threadTimestamp) {
    if (args.length != 1) return false;

    String dstUserID = Util.getTaggedUserID(args[0]);

    if (mom.inConvChannel(dstUserID)) return false;

    SlackUser dstUser = mom.getSession().findUserById(dstUserID);

    if (dstUser == null) return false;

    Conversation conv = mom.findConversationByUserID(dstUserID);

    if (conv != null) {
      mom.startConversation(dstUser, conv.getDirectChannelID(), false);
      return true;
    }

    mom.startConversation(dstUser, mom.getUserChannel(dstUser).getId(), true);
    return true;
  }
}
