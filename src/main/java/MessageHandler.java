import model.Node;
import model.RoutTableEntry;
import org.apache.commons.lang3.StringUtils;
import packets.RREP;
import packets.RREQ;
import utils.Converter;
import utils.Parser;
import utils.Timer;

import java.util.Arrays;
import java.util.Base64;

public class MessageHandler {


    public static String handle(byte[] bytes) {
        var s = StringUtils.substringBefore(new String(bytes), "\r");
        if (s.contains("LR")) {
            var arr = s.split(",");
            var prevHop = Parser.parseAddrToByte(arr[1]);
            var data = arr[3];
            var decoded = Base64.getDecoder().decode(data.getBytes());
            decoded = Converter.convertDecoded(decoded);
            var res = switch (decoded[0]) {
                case 1 -> handleRREQ(decoded, prevHop);
                case 2 -> handleRREP(decoded, prevHop);
                default -> throw new IllegalStateException("Unexpected value: " + decoded[0]);
            };
            if (res == null) {
                return null;
            }
            res = Converter.prepareForEncoding(res);
            return Base64.getEncoder().encodeToString(res);
        } else if (s.contains("ADDR")) {
            var addr = StringUtils.substringAfter(s, "=");
            Node.setADDR(Parser.parseAddrToByte(addr));
        }
        return null;
    }

    private static byte[] handleRREQ(byte[] decodedPacket, byte[] prevHop) {
        var rreq = new RREQ(decodedPacket);
        rreq.increaseHopCount();
        var tableEntry = new RoutTableEntry(rreq.getOriAddr(), prevHop, rreq.getHopCount(), rreq.getOriSeqNum(), true);

        if (Node.addTableEntry(tableEntry)) {
            Node.updateRouteLifetimeRREQ(tableEntry.getDestAddr());
        }

        if (Node.RREQWasProcessed(rreq)) {
            return null;
        }

        var route = Node.validRouteExists(rreq);
        if (Arrays.equals(rreq.getDestAddr(), Node.getADDR())) { //todo ? Node.validRouteExists(rreq)
            if ((Node.getSeqNum() + 1) == rreq.getDestSeqNum()) {
                Node.incrementSeqNum();
            }
            return new RREP(Node.MY_ROUTE_TIMEOUT, rreq.getOriAddr(), Node.getSeqNum(), Node.getADDR(), (byte) 0).getBytes(); // todo life time
        } else if (route != null) {
            return new RREP((int) Math.abs((route.getLifetime() - Timer.getCurrentTimestamp()) % 0x3ffff), rreq.getOriAddr(), route.getSeq(), route.getDestAddr(), route.getHopCount()).getBytes(); //todo ? route.getDestAddr() or my addr?
        }

        return rreq.getBytes();
    }

    private static byte[] handleRREP(byte[] decodedPacket, byte[] prevHop) {
        var packet = new RREP(decodedPacket);
        packet.increaseHopCount();
        var tableEntry = new RoutTableEntry(packet.getOriAddr(), prevHop, packet.getHopCount(), packet.getDestSeqNum(), true);
        if (Node.addTableEntry(tableEntry)) {
            Node.updateRouteLifetimeRREP(tableEntry.getDestAddr(), tableEntry.getLifetime());
        }

        return null;
    }


}
