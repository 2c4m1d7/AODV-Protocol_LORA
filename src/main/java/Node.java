import model.RoutTableEntry;
import packets.RREQ;

import java.util.HashMap;
import java.util.Map;

public class Node {

    private Node() {
    }

    private static final Map<byte[], RoutTableEntry> table = new HashMap<>();

    private static byte[] ADDR;

    public static void setADDR(byte[] addr) {
        ADDR = addr;
    }

    public static byte[] getADDR() {
        return ADDR;
    }

    public static boolean addTableEntry(RoutTableEntry control) {
        var entry = table.putIfAbsent(control.getDestAddr(), control.setValidRoute(false));
        if (entry != null) {  // https://github.com/2c4m1d7/AODV-Protocol_LORA#create-or-update-routes  3. ii.
            if (entry.getSeq() == 0 // todo ? the sequence number is unknown
                    || control.getSeq() > entry.getSeq()
                    || ((control.getSeq() == entry.getSeq()) && control.getHopCount() < entry.getHopCount())) {
                table.put(control.getDestAddr(), control);
                return true;
            } else return false;
        }
        return true;
    }

    public static void updateRouteLifetimeRREQ(byte[] destAddr) {
        table.get(destAddr).updateLifetimeRREQ();
    }

    public static void updateRouteLifetimeRREP(byte[] destAddr, long lifetime) {
        table.get(destAddr).updateLifetimeRREP(lifetime);
    }
}
