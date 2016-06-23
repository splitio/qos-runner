package io.split.qos.server.failcondition;

/**
 * Defines given the failed condition, what the broadcasters should do.
 */
public enum Broadcast {
    FIRST,
    REBROADCAST,
    RECOVERY,
    NO
}
