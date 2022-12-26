package model;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;

import static java.lang.Math.max;

public class RoutTableEntry {

    public static final int ACTIVE_ROUTE_TIMEOUT = 3000;
    public static final int MY_ROUTE_TIMEOUT = 2 * ACTIVE_ROUTE_TIMEOUT;
    public static final int NODE_TRAVERSAL_TIME = 40;
    public static final int RREQ_RETRIES = 2;
    public static final int NET_DIAMETER = 35;
    public static final int NET_TRAVERSAL_TIME = 2 * NODE_TRAVERSAL_TIME * NET_DIAMETER;
    public static final int PATH_DISCOVERY_TIME = 2 * NET_TRAVERSAL_TIME;

    public static final byte MAX_SEQ_NUM = 0x3f;
    private byte hopCount;
    private byte seq;
    private byte[] destAddr;
    private byte[] nextHop;
    private boolean validRoute;
    private byte active;
    private long lifetime;

    public RoutTableEntry(byte[] destAddr, byte[] nextHop, byte hopCount, byte seq, boolean validRoute) {
        this.hopCount = hopCount;
        this.seq = seq;
        this.destAddr = destAddr;
        this.nextHop = nextHop;
        this.validRoute = validRoute;
    }

    public void updateLifetimeRREQ() {
        if (isValidRoute()) {
            var currentTime = System.currentTimeMillis();
            long minimalLifetime = (currentTime + 2 * NET_TRAVERSAL_TIME - 2 * hopCount * NODE_TRAVERSAL_TIME);
            long existingLifeTime = lifetime;
            lifetime = max(existingLifeTime, minimalLifetime);
        }
    }

    public void updateLifetimeRREP(long packetLifetime) {
        lifetime = System.currentTimeMillis() + (packetLifetime <= 0 ? ACTIVE_ROUTE_TIMEOUT : packetLifetime);
    }

    public long getLifetime() {
        return lifetime;
    }

    public byte getHopCount() {
        return hopCount;
    }

    public RoutTableEntry setHopCount(byte hopCount) {
        this.hopCount = hopCount;
        return this;
    }

    public byte getSeq() {
        return seq;
    }

    public RoutTableEntry setSeq(byte seq) {
        this.seq = seq;
        return this;
    }

    public byte[] getDestAddr() {
        return destAddr;
    }

    public byte getActive() {
        return active;
    }

    public void setActive(byte active) {
        this.active = active;
    }

    public RoutTableEntry setDestAddr(byte[] destAddr) {
        this.destAddr = destAddr;
        return this;
    }

    public byte[] getNextHop() {
        return nextHop;
    }

    public RoutTableEntry setNextHop(byte[] nextHop) {
        this.nextHop = nextHop;
        return this;
    }

    public boolean isValidRoute() {
        return validRoute;
    }

    public RoutTableEntry setValidRoute(boolean validRoute) {
        this.validRoute = validRoute;
        return this;
    }
}
