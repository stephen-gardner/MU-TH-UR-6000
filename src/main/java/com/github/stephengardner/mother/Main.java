package com.github.stephengardner.mother;

import com.github.stephengardner.mother.data.MotherConfig;
import com.github.stephengardner.mother.data.Msg;
import com.github.stephengardner.mother.listeners.*;
import com.google.gson.Gson;
import com.google.gson.JsonParser;
import com.ullink.slack.simpleslackapi.SlackChannel;
import com.ullink.slack.simpleslackapi.SlackUser;

import java.io.FileReader;

public class Main {

  private static MotherConfig mc;

  public static void main(String[] args) {
    loadConfig();

    final Mother mom = new Mother(mc.getAuthToken(), mc.getConvChanID(), mc.getDbPath());

    new DirectMessageListener(mom).registerEvent();
    new ChanMessageListener(mom).registerEvent();
    new DirectTypingListener(mom).registerEvent();
    new UpdatedListener(mom).registerEvent();
    new EmojiAddedListener(mom).registerEvent();
    new EmojiRemovedListener(mom).registerEvent();
    new JoinedChannelListener(mom).registerEvent();

    SlackUser user = mom.getSession().findUserById("USLACKBOT");
    SlackChannel chan =
        mom.getSession().openDirectMessageChannel(user).getReply().getSlackChannel();

    while (mom.isOnline()) {
      try {
        Thread.sleep(mc.getTimeoutCheckInterval());
        mom.reapConversations(mc.getSessionTimeout());
        mom.getSession().sendTyping(chan); // Fool Slack into thinking bot is always active
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  public static MotherConfig getConfig() {
    return mc;
  }

  public static void loadConfig() {
    String configFile = "config.json";

    try {
      FileReader reader = new FileReader(configFile);

      mc = new Gson().fromJson(new JsonParser().parse(reader), MotherConfig.class);
      reader.close();
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(1);
    }

    if (mc == null) {
      System.err.println(String.format(Msg.CONFIG_ERROR.toString(), configFile));
      System.exit(1);
    }
  }
}
