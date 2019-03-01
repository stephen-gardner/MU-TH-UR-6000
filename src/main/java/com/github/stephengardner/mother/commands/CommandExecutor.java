package com.github.stephengardner.mother.commands;

import com.ullink.slack.simpleslackapi.SlackChannel;
import com.ullink.slack.simpleslackapi.SlackUser;

public interface CommandExecutor {

  boolean onCommand(SlackChannel chan, SlackUser user, String[] args, String threadTimestamp);
}
