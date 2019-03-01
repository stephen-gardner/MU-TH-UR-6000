package com.github.stephengardner.mother.data;

import com.github.stephengardner.mother.Main;

public enum Msg {
  ACTIVE_CONVS,
  ACTIVE_INFO,
  CONFIG_ERROR,
  IN_CONV_CHANNEL,
  LIST_COMMANDS,
  LIST_NONE,
  LIST_THREADS,
  MESSAGE_COPY_FMT,
  MESSAGE_LINK,
  NO_GROUPS,
  REACT_FAILURE,
  REACT_SUCCESS,
  REACT_UNKNOWN,
  SESSION_CONTEXT_SWITCHED_FROM,
  SESSION_CONTEXT_SWITCHED_TO,
  SESSION_EXPIRED_CHAN,
  SESSION_EXPIRED_DIRECT,
  SESSION_NOTICE,
  SESSION_RESUME_CHAN,
  SESSION_RESUME_DIRECT,
  SESSION_START;

  @Override
  public String toString() {
    return Main.getConfig().getMsg(this.name());
  }
}
