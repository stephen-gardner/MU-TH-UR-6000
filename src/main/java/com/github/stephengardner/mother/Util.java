package com.github.stephengardner.mother;

import com.github.stephengardner.mother.data.Msg;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Util {

  public static String getTaggedUserID(String tag) {
    Pattern p = Pattern.compile("<@(.*?)>");
    Matcher m = p.matcher(tag);

    if (m.find()) return m.group(1);

    return null;
  }

  public static String getThreadLink(Mother mom, String chanID, String timestamp) {
    String domain = mom.getSession().getTeam().getDomain();
    String parsed = timestamp.replace(".", "");

    return Msg.MESSAGE_LINK.get(mom, domain, chanID, parsed, timestamp);
  }
}
