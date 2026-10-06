package com.oplus.osense.eventinfo;

import java.util.HashSet;

public class EventConfig {

    private HashSet<Integer> mEventSet;
    private HashSet<OsenseConfig> mOsenseConfigSet;

    public EventConfig(HashSet<Integer> eventSet) {
        mEventSet = eventSet;
    }

    public void setOsenseConfigSet(HashSet<OsenseConfig> osenseConfigSet) {
        mOsenseConfigSet = osenseConfigSet;
    }
}
