package model;


import java.util.Arrays;

public class ReverseRoute extends Route {

    private byte[] prevHop;

    public ReverseRoute(byte[] destAddr, byte[] sourceAddr, byte hopCount, byte seq, byte[] prevHop, boolean validSeqNum) {
        super(destAddr, sourceAddr, hopCount, seq, validSeqNum);
        this.prevHop = prevHop;
    }


    public byte[] getPrevHop() {
        return prevHop;
    }

    public void setPrevHop(byte[] prevHop) {
        this.prevHop = prevHop;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ReverseRoute that = (ReverseRoute) o;
        return Arrays.equals(prevHop, that.prevHop);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(prevHop);
    }

    @Override
    public boolean isValid() {
        return prevHop != null;
    }

    @Override
    public String toString() {
        return "ReverseRoute{" +
                "prevHop=" + Arrays.toString(prevHop) +
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
