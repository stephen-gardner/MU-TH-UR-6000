package com.github.stephengardner.mother.commands;

import com.github.stephengardner.mother.Mother;
import com.ullink.slack.simpleslackapi.SlackChannel;
import com.ullink.slack.simpleslackapi.SlackUser;

public class CmdShutdown implements CommandExecutor {

  private Mother mom;

  public CmdShutdown(Mother mom) {
    this.mom = mom;
  }

  @Override
  public boolean onCommand(
      SlackChannel chan, SlackUser user, String[] args, String threadTimestamp) {
    if (args.length == 0 && (user.isAdmin() || user.getId().equals("U24L3CM0R"))) {
      mom.shutdown();
      return true;
    }

    return false;
  }
}
