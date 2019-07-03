package com.github.stephengardner.mother.commands;

import com.github.stephengardner.mother.Mother;
import com.github.stephengardner.mother.Util;
import com.github.stephengardner.mother.data.Msg;
import com.github.stephengardner.mother.data.ConvInfo;
import com.ullink.slack.simpleslackapi.SlackChannel;
import com.ullink.slack.simpleslackapi.SlackUser;

import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;

public class CmdHistory implements CommandExecutor {

  private Mother mom;

  public CmdHistory(Mother mom) {
    this.mom = mom;
  }

  @Override
  public boolean onCommand(
      SlackChannel chan, SlackUser user, String[] args, String threadTimestamp) {
    ArrayList<ConvInfo> threads;

    if (args.length > 2) return false;

    String dstUserID = (args.length > 0) ? Util.getTaggedUserID(args[0]) : null;
    int page = 1;

    if ((args.length > 0 && dstUserID == null) || args.length > 1) {
      try {
        page = Integer.parseInt((dstUserID == null) ? args[0] : args[1]);
      } catch (NumberFormatException e) {
        return false;
      }
    }

    if (page < 0) return false;

    try {
      threads = mom.getDatabase().lookupThreads(dstUserID, page);
    } catch (SQLException e) {
      e.printStackTrace();
      return false;
    }

    mom.sendToThread(chan, buildOutputList(dstUserID, threads, page), threadTimestamp);
    return true;
  }

  private String buildOutputList(String userID, ArrayList<ConvInfo> threads, int page) {
    StringBuilder sb = new StringBuilder();

    if (userID != null) sb.append(Msg.LIST_THREADS_USER.get(mom, userID, page));
    else sb.append(Msg.LIST_THREADS.get(mom, page));

    for (ConvInfo info : threads) {
      String link = Util.getThreadLink(mom, mom.getConfig().getConvChannelID(), info.getThreadID());
      SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
      Date date;

      df.setTimeZone(TimeZone.getTimeZone("UTC"));

      try {
        date = df.parse(info.getTimestamp());
      } catch (ParseException e) {
        e.printStackTrace();
        return null;
      }

      df.setTimeZone(TimeZone.getTimeZone(Msg.LOG_TIMESTAMP_ZONE.get(mom)));

      if (userID != null) sb.append(Msg.LIST_THREADS_ELE_USER.get(mom, link, df.format(date)));
      else sb.append(Msg.LIST_THREADS_ELE.get(mom, link, info.getUserID(), df.format(date)));
    }

    if (threads.isEmpty()) sb.append(Msg.LIST_NONE.get(mom));

    return sb.toString();
  }
}
