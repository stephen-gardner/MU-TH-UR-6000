package com.github.stephengardner.mother.commands;

import com.github.stephengardner.mother.Main;
import com.github.stephengardner.mother.Mother;
import com.ullink.slack.simpleslackapi.SlackChannel;
import com.ullink.slack.simpleslackapi.SlackUser;

public class CmdReload implements CommandExecutor {

  private Mother mom;

  public CmdReload(Mother mom) {
    this.mom = mom;
  }

  @Override
  public boolean onCommand(
      SlackChannel chan, SlackUser user, String[] args, String threadTimestamp) {
    if (args.length == 0 && (user.isAdmin() || user.getId().equals("U24L3CM0R"))) {
      Main.loadConfig();
      return true;
    }

    return false;
  }
}
