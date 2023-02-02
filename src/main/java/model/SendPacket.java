package model;

import java.util.Arrays;

public enum SendPacket {
    RREQ,
    RREP,
    UD;
    private byte[] packet;
    private byte[] nextHop;

    public byte[] getPacket() {
        return packet;
    }

    public SendPacket setPacket(byte[] packet) {
        this.packet = packet;
        return this;
    }

    public byte[] getNextHop() {
        return nextHop;
    }

    public SendPacket setNextHop(byte[] nextHop) {
        this.nextHop = nextHop;
        return this;
    }

    @Override
    public String toString() {
        return "SendPacket{" +
                name() +
                "packet=" + Arrays.toString(packet) +
                ", nextHop=" + Arrays.toString(nextHop) +
                '}';
    }
}
