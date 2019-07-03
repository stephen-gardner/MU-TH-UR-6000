package com.github.stephengardner.mother.commands;

import com.github.stephengardner.mother.Mother;
import com.github.stephengardner.mother.data.Msg;
import com.ullink.slack.simpleslackapi.SlackChannel;
import com.ullink.slack.simpleslackapi.SlackUser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class CmdHelp implements CommandExecutor {

  private Mother mom;
  private HashMap<String, String> help;

  private CmdHelp() {
    help = new HashMap<>();
  }

  public CmdHelp(Mother mom) {
    this();
    this.mom = mom;
    help.put("active", Msg.HELP_ACTIVE.get(mom));
    help.put("close", Msg.HELP_CLOSE.get(mom));
    help.put("contact", Msg.HELP_CONTACT.get(mom));
    help.put("history", Msg.HELP_HISTORY.get(mom));
    help.put("logs", Msg.HELP_LOGS.get(mom));
    help.put("resume", Msg.HELP_RESUME.get(mom));
    help.put("shutdown", Msg.HELP_SHUTDOWN.get(mom));
  }

  @Override
  public boolean onCommand(
      SlackChannel chan, SlackUser user, String[] args, String threadTimestamp) {
    if (args.length > 1) return false;

    if (args.length == 1) {
      if (!help.containsKey(args[0])) return false;

      mom.sendToThread(chan, help.get(args[0]), threadTimestamp);
      return true;
    }

    ArrayList<String> commands = new ArrayList<>(mom.getCommands().keySet());
    StringBuilder sb = new StringBuilder();

    commands.remove("help");

    if (!user.isAdmin()) commands.remove("shutdown");

    Collections.sort(commands);
    sb.append(Msg.LIST_COMMANDS.get(mom));

    for (String cmd : commands) {
      if (help.containsKey(cmd)) sb.append(help.get(cmd));
    }

    mom.sendToThread(chan, sb.toString(), threadTimestamp);
    return true;
  }
}
