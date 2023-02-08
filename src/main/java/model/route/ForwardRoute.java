package model.route;

import model.Node;

import java.util.Arrays;

public class ForwardRoute extends Route {

    private byte[] nextHop;


    public ForwardRoute(byte[] destAddr, byte[] sourceAddr, byte hopCount, int seq, byte[] nextHop, boolean validSeqNum) {
        super(destAddr, sourceAddr, hopCount, seq, validSeqNum);
        this.nextHop = nextHop;
    }

    public byte[] getNextHop() {
        return nextHop;
    }

    public void setNextHop(byte[] nextHop) {
        this.nextHop = nextHop;
    }

    @Override
    public boolean isValid() {
        return nextHop != null && ((lifetime + Node.DELETE_PERIOD) >= System.currentTimeMillis());
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
                ", active=" + active() +
                '}';
    }
}
