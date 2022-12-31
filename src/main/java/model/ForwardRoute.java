package model;

import java.util.Arrays;

public class ForwardRoute extends Route{

    private byte[] nextHop;

    public ForwardRoute(byte[] destAddr, byte[] sourceAddr, byte hopCount, byte seq, byte[] nextHop) {
        super(destAddr, sourceAddr, hopCount, seq);
        this.nextHop = nextHop;
    }

    public byte[] getNextHop() {
        return nextHop;
    }

    public void setNextHop(byte[] nextHop) {
        this.nextHop = nextHop;
    }

    @Override
    public String toString() {
        return "ForwardRoute{" +
                "nextHop=" + Arrays.toString(nextHop) +
                ", destAddr=" + Arrays.toString(destAddr) +
                ", sourceAddr=" + Arrays.toString(sourceAddr) +
                ", hopCount=" + hopCount +
                ", seq=" + seq +
                ", lifetime=" + lifetime +
                ", valid=" + isValid() +
                ", active=" + active +
                '}';
    }
}
