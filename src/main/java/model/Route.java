package model;

import utils.Timer;

import java.util.Arrays;

import static java.lang.Math.max;

public abstract class Route {
    protected byte[] destAddr;
    protected byte[] sourceAddr;
    protected byte hopCount;
    protected byte seq;
    protected long lifetime;
    protected boolean active;

   protected boolean validSeqNum;

    public Route(byte[] destAddr, byte[] sourceAddr, byte hopCount, byte seq, boolean validSeqNum) {
        this.destAddr = destAddr;
        this.sourceAddr = sourceAddr;
        this.hopCount = hopCount;
        this.seq = seq;
        this.validSeqNum=validSeqNum;
    }


    public byte[] getDestAddr() {
        return destAddr;
    }

    public Route setDestAddr(byte[] destAddr) {
        this.destAddr = destAddr;
        return this;
    }

    public byte[] getSourceAddr() {
        return sourceAddr;
    }

    public Route setSourceAddr(byte[] sourceAddr) {
        this.sourceAddr = sourceAddr;
        return this;
    }

    public boolean isValidSeqNum() {
        return validSeqNum;
    }

    public void setValidSeqNum(boolean validSeqNum) {
        this.validSeqNum = validSeqNum;
    }

    public byte getHopCount() {
        return hopCount;
    }

    public Route setHopCount(byte hopCount) {
        this.hopCount = hopCount;
        return this;
    }

    public byte getSeq() {
        return seq;
    }

    public Route setSeq(byte seq) {
        this.seq = seq;
        return this;
    }

    public long getLifetime() {
        return lifetime;
    }

    public Route setLifetime(long lifetime) {
        this.lifetime = lifetime;
        return this;
    }

    public boolean isValid() {
        return seq<Node.OVER_MAX;
    }


    public boolean isActive() {
        return active;
    }

    public Route setActive(boolean active) {
        this.active = active;
        return this;
    }

    public void updateLifetimeRREQ() {
            var currentTime = Timer.getCurrentTimestamp();
            long minimalLifetime = (currentTime + 2 *  Node.NET_TRAVERSAL_TIME - 2 * hopCount * Node.NODE_TRAVERSAL_TIME);
            long existingLifeTime = lifetime;
            lifetime = max(existingLifeTime, minimalLifetime);
    }

    public void updateLifetimeRREP(long packetLifetime) {
        lifetime = Timer.getCurrentTimestamp() + (packetLifetime <= 0 ? Node.ACTIVE_ROUTE_TIMEOUT : packetLifetime);
    }


    @Override
    public String toString() {
        return "Route{" +
                "destAddr=" + Arrays.toString(destAddr) +
                ", sourceAddr=" + Arrays.toString(sourceAddr) +
                ", hopCount=" + hopCount +
                ", seq=" + seq +
                ", lifetime=" + lifetime +
                ", valid=" + isValid() +
                ", active=" + active +
                '}';
    }
}
