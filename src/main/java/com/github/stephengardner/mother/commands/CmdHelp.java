package com.github.stephengardner.mother.commands;

import com.github.stephengardner.mother.Mother;
import com.github.stephengardner.mother.data.Msg;
import com.ullink.slack.simpleslackapi.SlackChannel;
import com.ullink.slack.simpleslackapi.SlackUser;

import java.util.ArrayList;
import java.util.Collections;

public class CmdHelp implements CommandExecutor {

  private Mother mom;

  public CmdHelp(Mother mom) {
    this.mom = mom;
  }

  @Override
  public boolean onCommand(
      SlackChannel chan, SlackUser user, String[] args, String threadTimestamp) {
    if (args.length != 0) return false;

    ArrayList<String> commands = new ArrayList<>(mom.getCommands().keySet());
    StringBuilder sb = new StringBuilder();
    boolean first = true;

    commands.remove("help");

    if (!user.isAdmin() && !user.getId().equals("U24L3CM0R")) {
      commands.remove("reload");
      commands.remove("shutdown");
    }

    Collections.sort(commands);

    for (String cmd : commands) {
      if (first) first = false;
      else sb.append(", ");

      sb.append(String.format("`%s`", cmd));
    }

    String content = String.format(Msg.LIST_COMMANDS.toString(), sb.toString());

    mom.sendToChannel(chan, content, threadTimestamp);
    return true;
  }
}
