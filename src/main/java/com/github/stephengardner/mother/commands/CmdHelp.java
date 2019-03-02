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

  public CmdHelp() {
    help = new HashMap<>();
    help.put("active", Msg.HELP_ACTIVE.toString());
    help.put("close", Msg.HELP_CLOSE.toString());
    help.put("contact", Msg.HELP_CONTACT.toString());
    help.put("history", Msg.HELP_HISTORY.toString());
    help.put("logs", Msg.HELP_LOGS.toString());
    help.put("reload", Msg.HELP_RELOAD.toString());
    help.put("resume", Msg.HELP_RESUME.toString());
    help.put("shutdown", Msg.HELP_SHUTDOWN.toString());
  }

  public CmdHelp(Mother mom) {
    this();
    this.mom = mom;
  }

  @Override
  public boolean onCommand(
      SlackChannel chan, SlackUser user, String[] args, String threadTimestamp) {
    if (args.length != 0) return false;

    ArrayList<String> commands = new ArrayList<>(mom.getCommands().keySet());
    StringBuilder sb = new StringBuilder();

    commands.remove("help");

    if (!user.isAdmin() && !user.getId().equals("U24L3CM0R")) {
      commands.remove("reload");
      commands.remove("shutdown");
    }

    Collections.sort(commands);
    sb.append(Msg.LIST_COMMANDS.toString());

    for (String cmd : commands) {
      if (help.containsKey(cmd)) sb.append(help.get(cmd));
    }

    mom.sendToChannel(chan, sb.toString(), threadTimestamp);
    return true;
  }
}
