package model;

import utils.Parser;

import java.util.Arrays;
import java.util.Base64;
import java.util.Objects;

public  class SendPacket {

    public static RREQ RREQ = new RREQ(PacketType.RREQ);
    public static RREP RREP = new RREP(PacketType.RREP);
    public static UD UD = new UD(PacketType.UD);

    protected PacketType type;
    protected byte[] packet;
    protected byte[] nextHop;


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

    public PacketType getType() {
        return type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SendPacket that)) return false;
        return type.equals(that.type) && Arrays.equals(packet, that.packet) && Arrays.equals(nextHop, that.nextHop);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(type);
        result = 31 * result + Arrays.hashCode(packet);
        result = 31 * result + Arrays.hashCode(nextHop);
        return result;
    }

    @Override
    public String toString() {
        return "SendPacket{" +
                type +
                ": packet=" + Base64.getEncoder().encodeToString(packet) +
                ", nextHop=" + Parser.parseBytesToAddr(nextHop) +
                '}';
    }


    public static class RREQ extends SendPacket {
        public RREQ(PacketType type) {
            this.type = type;
        }
    }

    public static class RREP extends SendPacket {
        public RREP(PacketType type) {
            this.type = type;
        }
    }

    public static class UD extends SendPacket {
        public UD(PacketType type) {
            this.type = type;
        }
    }

}
