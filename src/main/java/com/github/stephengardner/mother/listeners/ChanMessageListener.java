package com.github.stephengardner.mother.listeners;

import com.github.stephengardner.mother.Conversation;
import com.github.stephengardner.mother.Mother;
import com.github.stephengardner.mother.commands.CommandExecutor;
import com.github.stephengardner.mother.data.LogEntry;
import com.github.stephengardner.mother.data.Msg;
import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.events.SlackMessagePosted;
import com.ullink.slack.simpleslackapi.listeners.SlackMessagePostedListener;

import java.util.Arrays;
import java.util.HashMap;

public class ChanMessageListener implements SlackMessagePostedListener {

  private Mother mom;

  public ChanMessageListener(Mother mom) {
    this.mom = mom;
  }

  public void registerEvent() {
    mom.getSession().addMessagePostedListener(this);
  }

  @Override
  public void onEvent(SlackMessagePosted ev, SlackSession s) {
    String userID = ev.getUser().getId();
    String threadTimestamp = ev.getThreadTimestamp();

    if (!ev.getChannel().equals(mom.getConvChannelID())
        || s.sessionPersona().getId().equals(userID)
        || userID.equals("USLACKBOT")) return;

    if (threadTimestamp != null && converse(ev, userID, threadTimestamp)) return;

    if (!ev.getMessageContent().isEmpty() && ev.getMessageContent().startsWith("!"))
      runCommands(ev, s);
  }

  private boolean converse(SlackMessagePosted ev, String userID, String threadTimestamp) {
    Conversation conv = mom.findConversation(threadTimestamp, true);

    if (conv == null) return false;

    String content = String.format(Msg.MESSAGE_COPY_FMT.toString(), userID, ev.getMessageContent());
    String directTimestamp = conv.sendToUser(content).getTimestamp();
    LogEntry log = new LogEntry(userID, ev.getMessageContent(), ev.getTimestamp(), true);

    conv.addLog(directTimestamp, ev.getTimestamp(), log);
    return true;
  }

  private void runCommands(SlackMessagePosted ev, SlackSession s) {
    String[] args = ev.getMessageContent().trim().split("\\s+");
    HashMap<String, CommandExecutor> commands = mom.getCommands();

    for (String cmdName : commands.keySet()) {
      if (args[0].equalsIgnoreCase("!" + cmdName)) {
        String[] cmdArgs = Arrays.copyOfRange(args, 1, args.length);
        String timestamp =
            (ev.getThreadTimestamp() != null) ? ev.getThreadTimestamp() : ev.getTimestamp();
        CommandExecutor cmd = commands.get(cmdName);
        boolean success = cmd.onCommand(ev.getUser(), cmdArgs, timestamp);

        s.addReactionToMessage(
            ev.getChannel(),
            ev.getTimeStamp(),
            (success) ? Msg.REACT_SUCCESS.toString() : Msg.REACT_FAILURE.toString());
        return;
      }
    }

    s.addReactionToMessage(ev.getChannel(), ev.getTimeStamp(), Msg.REACT_UNKNOWN.toString());
  }
}
