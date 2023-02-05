package model;

import utils.Parser;

import java.util.Arrays;
import java.util.Base64;
import java.util.Objects;

public abstract class SendPacketT {

    public static RREQ RREQ = new RREQ();
    public static RREP RREP = new RREP();
    public static UD UD = new UD();

    protected byte[] packet;
    protected byte[] nextHop;

    public static class RREQ extends SendPacketT {

        public final String name = "RREQ";

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof RREQ rreq)) return false;
            if (!super.equals(o)) return false;
            return name.equals(rreq.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(super.hashCode(), name);
        }

        @Override
        public String toString() {
            return "SendPacket{" +
                    "RREQ: " +
                    "packet=" + Base64.getEncoder().encodeToString(packet) +
                    ", nextHop=" + Parser.parseBytesToAddr(nextHop) +
                    '}';
        }
    }

    public static class RREP extends SendPacketT {
        public final String name = "RREP";

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof RREP rrep)) return false;
            if (!super.equals(o)) return false;
            return name.equals(rrep.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(super.hashCode(), name);
        }

        @Override
        public String toString() {
            return "SendPacket{" +
                    "RREP: " +
                    "packet=" + Base64.getEncoder().encodeToString(packet) +
                    ", nextHop=" + Parser.parseBytesToAddr(nextHop) +
                    '}';
        }
    }

    public static class UD extends SendPacketT {
        public final String name = "UD";


        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof UD ud)) return false;
            if (!super.equals(o)) return false;
            return name.equals(ud.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(super.hashCode(), name);
        }

        @Override
        public String toString() {
            return "SendPacket{" +
                    "UD: " +
                    "packet=" + Base64.getEncoder().encodeToString(packet) +
                    ", nextHop=" + Parser.parseBytesToAddr(nextHop) +
                    '}';
        }
    }

    public byte[] getPacket() {
        return packet;
    }

    public SendPacketT setPacket(byte[] packet) {
        this.packet = packet;
        return this;
    }

    public byte[] getNextHop() {
        return nextHop;
    }

    public SendPacketT setNextHop(byte[] nextHop) {
        this.nextHop = nextHop;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SendPacketT that)) return false;
        return Arrays.equals(packet, that.packet) && Arrays.equals(nextHop, that.nextHop);
    }

    @Override
    public int hashCode() {
        int result = Arrays.hashCode(packet);
        result = 31 * result + Arrays.hashCode(nextHop);
        return result;
    }
}
