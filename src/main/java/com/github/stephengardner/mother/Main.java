package com.github.stephengardner.mother;

import com.github.stephengardner.mother.data.MotherConfig;
import com.github.stephengardner.mother.listeners.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;

public class Main {

  private static HashMap<Mother, MotherConfig> bots;

  public static void main(String[] args) {
    bots = new HashMap<>();

    try {
      File configFile = new File("config.json");

      if (!configFile.exists()) {
        makeConfig(configFile);
        System.out.println(String.format("%s generated", configFile.getName()));
        System.exit(0);
      }

      if (!loadConfig(configFile)) {
        System.err.println(String.format("Error: %s is missing or invalid", configFile.getPath()));
        System.exit(1);
      }
    } catch (IOException e) {
      e.printStackTrace();
      System.exit(1);
    }

    boolean run = true;

    while (run) {
      try {
        Long sleepTime = scrubBots();

        if (sleepTime != null) Thread.sleep(sleepTime);
        else run = false;
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
  }

  private static Long scrubBots() {
    Iterator<Mother> it = bots.keySet().iterator();
    Long sleepTime = null;
    long currentTime = System.currentTimeMillis();

    while (it.hasNext()) {
      Mother mom = it.next();
      MotherConfig mc = mom.getConfig();

      if (!mom.isOnline()) {
        it.remove();

        try {
          mom.reapConversations(0L);
          mom.getDatabase().close();
        } catch (SQLException e) {
          e.printStackTrace();
        }

        continue;
      }

      long timeElapsed = currentTime - mom.getLastUpdate();
      long nextUpdate = mc.getTimeoutCheckInterval() - timeElapsed;

      if (timeElapsed >= mc.getTimeoutCheckInterval()) {
        mom.update();

        if (sleepTime == null) sleepTime = mc.getTimeoutCheckInterval();
      } else if (sleepTime == null || nextUpdate < sleepTime) sleepTime = nextUpdate;
    }

    return sleepTime;
  }

  private static void makeConfig(File configFile) throws IOException {
    Gson gson = new GsonBuilder().setPrettyPrinting().create();
    FileWriter writer = new FileWriter(configFile);
    MotherConfig mc = new MotherConfig();

    mc.initDefaults();
    bots.put(getMotherInstance(mc), mc);
    writer.write(gson.toJson(bots.values().toArray(new MotherConfig[1])));
    writer.close();
  }

  private static boolean loadConfig(File configFile) throws IOException {
    Gson gson = new Gson();
    FileReader reader = new FileReader(configFile);
    MotherConfig[] loaded = gson.fromJson(new JsonParser().parse(reader), MotherConfig[].class);

    if (loaded == null) return false;

    for (MotherConfig mc : loaded) {
      if (!mc.isValid()) {
        reader.close();
        return false;
      }

      bots.put(getMotherInstance(mc), mc);
    }

    reader.close();
    return true;
  }

  private static Mother getMotherInstance(MotherConfig mc) {
    Mother mom = new Mother(mc);

    new DirectMessageListener(mom).registerEvent();
    new ChanMessageListener(mom).registerEvent();
    new DirectTypingListener(mom).registerEvent();
    new UpdatedListener(mom).registerEvent();
    new EmojiAddedListener(mom).registerEvent();
    new EmojiRemovedListener(mom).registerEvent();
    new JoinedChannelListener(mom).registerEvent();
    return mom;
  }
}
