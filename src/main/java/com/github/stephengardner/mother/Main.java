package com.github.stephengardner.mother;

import com.github.stephengardner.mother.data.MotherConfig;
import com.github.stephengardner.mother.data.Msg;
import com.github.stephengardner.mother.listeners.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import com.ullink.slack.simpleslackapi.SlackChannel;
import com.ullink.slack.simpleslackapi.SlackUser;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class Main {

  private static MotherConfig mc;

  public static void main(String[] args) {
    loadConfig();

    final Mother mom = new Mother(mc.getAuthToken(), mc.getConvChannelID(), mc.getDbPath());

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
        mom.getSession().sendTyping(chan); // Fool Slack into thinking bot is always active
        mom.reapConversations(mc.getSessionTimeout());
        Thread.sleep(mc.getTimeoutCheckInterval());
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  public static MotherConfig getConfig() {
    return mc;
  }

  private static void loadConfig() {
    Gson gson = new GsonBuilder().setPrettyPrinting().create();
    File config = new File("config.json");

    try {
      if (!config.exists()) {
        FileWriter writer = new FileWriter(config);

        mc = new MotherConfig();
        mc.initDefaults();
        writer.write(gson.toJson(mc));
        writer.close();
        System.out.println(String.format("%s generated", config.getName()));
        System.exit(0);
      }

      FileReader reader = new FileReader(config);

      mc = new Gson().fromJson(new JsonParser().parse(reader), MotherConfig.class);
      reader.close();
    } catch (IOException e) {
      e.printStackTrace();
      System.exit(1);
    }

    if (mc == null) {
      System.err.println(String.format(Msg.CONFIG_ERROR.toString(), config));
      System.exit(1);
    }
  }
}
