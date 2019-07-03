package com.github.stephengardner.mother.data;

import com.github.stephengardner.mother.Mother;

public enum Msg {
  ACTIVE_CONVS("*Active conversations:*\n"),
  ACTIVE_INFO(">%s (*<@%s>*)\n"),
  HELP_ACTIVE(">`active` - List active conversations\n"),
  HELP_CLOSE(">`close @user/thread_id` - End active conversation\n"),
  HELP_CONTACT(">`contact @user` - Start conversation with user\n"),
  HELP_HISTORY(">`history [@user] [page]` - List thread IDs of recent conversations\n"),
  HELP_LOGS(">`logs @user/thread_id` - Upload logs associated with user or thread ID\n"),
  HELP_RESUME(">`resume @user/thread_id` - Resume conversation under a new thread\n"),
  HELP_SHUTDOWN(">`shutdown` - Disconnect bot\n"),
  IN_CONV_CHANNEL(
      ">_*Users in `#%s` can not start conversations. Send `!help` for a list of available commands.*_"),
  LIST_COMMANDS("*Commands:*\n"),
  LIST_NONE(">_(None)_\n"),
  LIST_THREADS("*Recently expired threads _(page %d):_*\n"),
  LIST_THREADS_USER("*<@%s>'s recently expired threads _(page %d):_*\n"),
  LIST_THREADS_ELE(">%s (*<@%s>*) _%s_\n"),
  LIST_THREADS_ELE_USER(">%s _%s_\n"),
  LOG("%s %s: %s\n"),
  LOG_EDITED("%s %s: %s (edited)\n"),
  LOG_TIMESTAMP_FMT("[yyyy-MM-dd HH:mm:ss]"),
  LOG_TIMESTAMP_ZONE("PST"),
  MESSAGE_COPY_FMT("*<@%s>:* %s"),
  MESSAGE_LINK("<https://%s.slack.com/archives/%s/p%s|%s>"),
  NO_GROUPS(">_*This bot does not support group communications.*_"),
  REACT_FAILURE("x"),
  REACT_SUCCESS("white_check_mark"),
  REACT_UNKNOWN("question"),
  SESSION_CONTEXT_SWITCHED_FROM(">_*Session context switched from [%s].*_"),
  SESSION_CONTEXT_SWITCHED_TO(">_*Session context switched to [%s].*_"),
  SESSION_EXPIRED_CONV(
      ">_*Session [%s] has expired.*_\n>Edits/reactions to previous messages will no longer be reflected in communications."),
  SESSION_EXPIRED_DIRECT(
      ">_*Session has expired.*_\n>If your issue has not yet been resolved, an RA will be contacting you ASAP.\n>Edits/reactions to previous messages will no longer be reflected in communications."),
  SESSION_NOTICE("_*Conversation started with <@%s>* (converse in thread under this message)_"),
  SESSION_RESUME_CONV(">_*Session resumed.*_"),
  SESSION_RESUME_DIRECT(">_*An RA has resumed your session.*_"),
  SESSION_START(
      ">_*A dialogue has been started with the RA team. An RA will reach out to you shortly.*_");

  private String defaultMsg;

  Msg(String defaultMsg) {
    this.defaultMsg = defaultMsg;
  }

  public String get(Mother mom, Object... args) {
    MotherConfig mc = mom.getConfig();
    String msg = (mc.hasMsg(this.name())) ? mc.getMsg(this.name()) : defaultMsg;

    if (args == null) return msg;

    return String.format(msg, args);
  }

  public String getDefault() {
    return defaultMsg;
  }
}
