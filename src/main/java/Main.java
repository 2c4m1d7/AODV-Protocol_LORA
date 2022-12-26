import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalField;
import java.util.*;

import model.RoutTableEntry;
import org.uncommons.maths.binary.BitString;
import packets.RREP;
import packets.RREQ;
import utils.Converter;

import static java.lang.System.*;
import static java.lang.System.out;
import static java.lang.Thread.sleep;
import static model.RoutTableEntry.NET_TRAVERSAL_TIME;
import static model.RoutTableEntry.NODE_TRAVERSAL_TIME;

public class Main {
    static Scanner scanner = new Scanner(in);

    public static void main(String[] args) throws InterruptedException {
//        var rreq = new RREQ((byte) 2, (byte) 0, (byte) 5, new byte[] {0,0,3}, (byte) 9, new byte[] {0,0,1}, (byte) 8).getBytes();
//        var rreq = new byte[]{1,1,1,1};
//        var converted = Converter.convertTo6Bit(rreq);
//        out.println(Arrays.toString(converted));
//        var encoded = Base64.getEncoder().encodeToString(converted);
//        out.println("Encoded: "+encoded);
//
//        out.println("-----");
//        var decoded = Base64.getDecoder().decode(encoded.getBytes());
//        out.println("Decoded: " +Arrays.toString(decoded));
//        var convertded = Converter.convertTo8Bit(decoded);
//        out.print("Converted: "+ Arrays.toString(convertded));
//        out.println("; Expected: "+ Arrays.toString(rreq));

        var chiffre = "BCAHAAE/AAIB";
        var packet = Base64.getDecoder().decode(chiffre); //BCAHAAEDAAIB -> [1, 2, 0, 7, 0, 0, 0, 1, 3, 0, 0, 0, 2, 1]  BCAHgkEDAAIB
        out.println(Arrays.toString(packet));
        var converted = Converter.convertDecoded(packet);
        out.println(Arrays.toString(converted));
        var rreq = new RREQ(converted);
        out.println(rreq);
//
//        out.println(Arrays.toString(rreq.getBytes()));
//        var reconvertedRreq = Converter.prepareForEncoding(rreq.getBytes());
//        out.println(Base64.getEncoder().encodeToString(reconvertedRreq));
//
//        out.println(chiffre.equals(Base64.getEncoder().encodeToString(reconvertedRreq)));
//        out.println(getBits(BitSet.valueOf(new byte[]{48})));

//        var Addrs = Converter.prepareAddrPlusSeqNumToSend(new byte[]{0, 0, 0, 1, 5, 0, 0, 0, 7, 10});
//        var rreq = new RREQ(new byte[]{1, 20, 3, 4, Addrs[0], Addrs[1], Addrs[2], Addrs[3], Addrs[4], Addrs[5], Addrs[6], Addrs[7]});
//        var converted = Converter.prepareForEncoding(rreq.getBytes());
//        out.println(Base64.getEncoder().encodeToString(converted));
//        var tpl = Base64.getDecoder().decode("A/bb");
//        out.println(Arrays.toString(tpl));
//        var tpsconverted = Converter.convertDecoded(tpl);
//        out.println(Arrays.toString(tpsconverted));
//        var lifetime = (((int) tpsconverted[3])  | (((int) tpsconverted[2]) << 6) | ((((int) tpsconverted[1]) << 12)));
//        out.println( lifetime);
//        var lifetimeback = new byte[]{(byte) (lifetime >> 12), (byte) ((lifetime >> 6) & 0x3f), (byte) (lifetime & 0x3f)};
//        out.println(Arrays.toString(lifetimeback));

//        var decodedRREP = Base64.getDecoder().decode("C/bbASMPASUA");
//        var converted = Converter.convertDecoded(decodedRREP);
//        out.println(Arrays.toString(converted));
//        var rrep = new RREP(converted);
//        out.println(rrep);
//
//        var reconv = Converter.prepareForEncoding(rrep.getBytes());
//        out.println(Base64.getEncoder().encodeToString(reconv));


//        Connection connection = null;
//        while (true) {
//            if (connection == null) {
//                connection = setConnection();
//            } else {
//                var m = scanner.nextLine();
//                try {
//                    if (m.length() > 0 && !connection.send(m)) {
//                        connection = null;
//                    }
//                } catch (IOException e) {
//                    err.println("Failed");
//                    connection = null;
//                }
//            }
//        }

    }


    private static byte[] convertThree(byte a, byte b, byte c) {

        byte value1 = (byte) ((a & 0xff) >> 2);
        byte value2 = (byte) (((a & 0x3) << 4) | ((b & 0xff) >> 4));
        byte value3 = (byte) (((b & 0xf) << 2) | ((c & 0xff) >> 6));
        byte value4 = (byte) (c & 0x3f);

        return new byte[]{value1, value2, value3, value4};
    }

    static byte[] convertFour(byte a, byte b, byte c, byte d) {
        byte p = (byte) (((a & 0xff) << 2) | (b & 0xf0) >>> 4);
        byte q = (byte) (((b & 0x0f) << 4) | (c & 0xff) >>> 2);
        byte r = (byte) (((c & 0x0f) << 6) | (d & 0xff));
//        int s = ((d & 0x03) << 8) | (e & 0xff) >>> 0
        return new byte[]{p, q, r};
    }

    private static String getBits(BitSet set) {
        var bits = new BitString(set.length());
        for (int i = set.length() - 1; i >= 0; i--) {
            bits.setBit(i, set.get(i));
        }
        return bits.toString();
    }

//        private static Connection setConnection() {
//
//            out.print("enter port path: ");
//            var path = "/dev/ttys001"; //scanner.nextLine();
//            var port = SerialPort.getCommPort(path);
//            Connection connection = new Connection(port);
//
//            if (connection.connect()) {
//                out.println("Opened port: " + connection.port().getDescriptivePortName());
//                return connection;
//            } else {
//                return null;
//            }
//        }

    private static Connection setConnection() {
        Connection connection;
//        var port = SerialPort.getCommPort("/dev/ttys002");

        var ports = Connection.getPorts();
        for (int i = 0; i < ports.length; i++) {
            out.println(i + ". " + ports[i].getDescriptivePortName());
        }
        out.print("Choose port: ");
        var num = scanner.nextInt();
        if (num < 0 || num >= ports.length) {
            return null;
        }
        connection = new Connection(ports[num]);
//        connection = new Connection(port);
        if (connection.connect()) {
            out.println("Opened port: " + connection.port().getDescriptivePortName());
            return connection;
        } else {
            return null;
        }
    }
}
