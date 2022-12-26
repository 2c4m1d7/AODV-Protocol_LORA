import model.RoutTableEntry;
import org.apache.commons.lang3.StringUtils;
import packets.RREP;
import packets.RREQ;
import utils.Converter;
import utils.Parser;

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
            res = Converter.prepareForEncoding(res);
            return Base64.getEncoder().encodeToString(res);
        } else if (s.contains("ADDR")) {
            var addr = StringUtils.substringAfter(s, "=");
            Node.setADDR(Parser.parseAddrToByte(addr));
        }
        return null;
    }

    private static byte[] handleRREQ(byte[] decodedPacket, byte[] prevHop) {
        var packet = new RREQ(decodedPacket);
        var tableEntry = new RoutTableEntry(packet.getOriAddr(), prevHop, packet.getHopCount(), packet.getOriSeqNum(), true);

        if (Node.addTableEntry(tableEntry)) {
            Node.updateRouteLifetimeRREQ(tableEntry.getDestAddr());
        }

        if (Arrays.equals(packet.getDestAddr(), Node.getADDR())) {
//            return new RREP((byte) 0, packet.getOriAddr(), (byte) 63, Node.getADDR(), (byte) 0).getAsArray(); // todo life time
        }
        packet.increaseHopCount();
        return packet.getBytes();
    }

    private static byte[] handleRREP(byte[] decodedPacket, byte[] prevHop) {
        var packet = new RREP(decodedPacket);
        var tableEntry = new RoutTableEntry(packet.getOriAddr(), prevHop, packet.getHopCount(), packet.getDestSeqNum(), true);
        if (Node.addTableEntry(tableEntry)) {
            Node.updateRouteLifetimeRREP(tableEntry.getDestAddr(), tableEntry.getLifetime());
        }

        packet.increaseHopCount();
        return packet.getBytes();
    }


}
