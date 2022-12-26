package model;

import utils.Timer;

import static java.lang.Math.max;
public class RoutTableEntry {


    public static final byte MAX_SEQ_NUM = 0x3f;
    private byte hopCount;
    private byte seq;
    private byte[] destAddr;
    private byte[] nextHop;
    private boolean validRoute;
    private boolean active;
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
            var currentTime = Timer.getCurrentTimestamp();
            long minimalLifetime = (currentTime + 2 *  Node.NET_TRAVERSAL_TIME - 2 * hopCount * Node.NODE_TRAVERSAL_TIME);
            long existingLifeTime = lifetime;
            lifetime = max(existingLifeTime, minimalLifetime);
        }
    }

    public void updateLifetimeRREP(long packetLifetime) {
        lifetime = Timer.getCurrentTimestamp() + (packetLifetime <= 0 ? Node.ACTIVE_ROUTE_TIMEOUT : packetLifetime);
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

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
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
