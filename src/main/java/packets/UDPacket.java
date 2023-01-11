package packets;

public class UDPacket {

    private static final byte type = 0;
    private byte[] destAddr;

    private String message;

    public UDPacket(byte[] destAddr, String message) {
        this.destAddr = destAddr;
        this.message = message;
    }

    public byte[] getDestAddr() {
        return destAddr;
    }

    public void setDestAddr(byte[] destAddr) {
        this.destAddr = destAddr;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    private byte getType(){
        return type;
    }
}
