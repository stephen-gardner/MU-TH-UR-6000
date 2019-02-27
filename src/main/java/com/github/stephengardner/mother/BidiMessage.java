package com.github.stephengardner.mother;

public class BidiMessage {

    private final String directTimestamp;
    private final String chanTimestamp;

    public BidiMessage(String directTimestamp, String chanTimestamp) {
        this.directTimestamp = directTimestamp;
        this.chanTimestamp = chanTimestamp;
    }

    public String getChanTimestamp() {
        return chanTimestamp;
    }

    public String getDirectTimestamp() {
        return directTimestamp;
    }
}
