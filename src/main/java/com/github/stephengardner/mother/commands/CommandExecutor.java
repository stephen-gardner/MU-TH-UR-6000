package com.github.stephengardner.mother.commands;

import com.ullink.slack.simpleslackapi.SlackUser;

public interface CommandExecutor {
    boolean onCommand(SlackUser user, String[] args, String threadTimestamp);
}
