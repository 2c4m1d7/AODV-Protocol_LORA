package model;


import java.util.Arrays;

public class ReverseRoute extends Route {

    private byte[] prevHop;

    public ReverseRoute(byte[] destAddr, byte[] sourceAddr, byte hopCount, byte seq, byte[] prevHop) {
        super(destAddr, sourceAddr, hopCount, seq);
        this.prevHop = prevHop;
    }


    public byte[] getPrevHop() {
        return prevHop;
    }

    public void setPrevHop(byte[] prevHop) {
        this.prevHop = prevHop;
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
