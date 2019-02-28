package com.github.stephengardner.mother;

class BidiMessage {

  private final String directTimestamp;
  private final String chanTimestamp;

  BidiMessage(String directTimestamp, String chanTimestamp) {
    this.directTimestamp = directTimestamp;
    this.chanTimestamp = chanTimestamp;
  }

  String getChanTimestamp() {
    return chanTimestamp;
  }

  String getDirectTimestamp() {
    return directTimestamp;
  }
}
