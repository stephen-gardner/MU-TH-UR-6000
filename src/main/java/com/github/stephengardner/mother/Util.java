package com.github.stephengardner.mother;

import com.github.stephengardner.mother.data.Msg;
import com.ullink.slack.simpleslackapi.SlackSession;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Util {

  public static String getTaggedUserID(String tag) {
    Pattern p = Pattern.compile("<@(.*?)>");
    Matcher m = p.matcher(tag);

    if (m.find()) return m.group(1);

    return null;
  }

  public static String getThreadLink(SlackSession s, String chanID, String timestamp) {
    return String.format(
        Msg.MESSAGE_LINK.toString(),
        s.getTeam().getDomain(),
        chanID,
        timestamp.replace(".", ""),
        timestamp);
  }
}
