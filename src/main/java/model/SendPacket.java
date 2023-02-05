package model;

import utils.Parser;

import java.util.Arrays;
import java.util.Base64;

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
                "packet=" + Base64.getEncoder().encodeToString(packet) +
                ", nextHop=" + Parser.parseBytesToAddr(nextHop) +
                '}';
    }
}
