import java.io.IOException;
import java.util.*;

import org.apache.commons.lang3.ArrayUtils;
import utils.Converter;
import utils.MyArrayUtils;

import static java.lang.System.*;
import static java.lang.System.out;
import static java.lang.Thread.sleep;

// DAVID NODE BgAAABMAABEA  RREQ
public class Main {
    static Scanner scanner = new Scanner(in);

    public static void main(String[] args) {
//
        var decoded = Base64.getDecoder().decode("A//9odWk");//AABBIYWxsbw=
        out.println(Arrays.toString(decoded));
        var converted = Converter.userDataPacketDecode(decoded);
        out.println(new String(MyArrayUtils.getRangeArray(converted, 5, (converted.length - 1))));
        out.println(Arrays.toString(converted));

        out.println(Base64.getEncoder().encodeToString(Converter.userDataPacketEncode(converted)));

        var newUD = ArrayUtils.addAll(new byte[]{0,0,0,1,3}, "Hallo".getBytes());

        var encided = Converter.userDataPacketEncode(newUD);
        out.println(Base64.getEncoder().encodeToString(encided));


        if (args.length > 0) {
            return;
        }

        Connection connection = null;

        while (true) {
            if (connection == null) {
                connection = setConnection();
            } else {
                out.println("1. send packet");
                var m = scanner.nextLine();
                if (m.equals("exit")) {
                    break;
                } else {
                    try {
                        if (Integer.parseInt(m) == 1) {
                            out.print("enter encoded packet in Base64: ");
                            var packet = scanner.next();
                            connection.listener().setSendThread(connection.sendPacket(packet.getBytes())); // get listener -> override mythread
                        }
                    } catch (NumberFormatException e) {
                        err.println(e);
                    }
                }
                try {
                    if (m.length() > 0 && !connection.send(m)) {
                        connection = null;
                    }
                } catch (IOException e) {
                    err.println("Failed");
                    connection = null;
                }
            }
        }
        connection.stop();
    }

    private static byte[] ud(byte[] udPacket) {
        var a = MyArrayUtils.getRangeArray(udPacket, 0, 2);
        var type = (byte) (a[0] >> 2);
        var addr0 = (byte) (((a[0] & 0x03) << 2) | (a[1] >> 6));
        var addr1 = (byte) (a[1] & 0x3C);
        var addr2 = (byte) (((a[1] & 0x03) << 2) | (a[2] >> 6));
        var addr3 = (byte) (a[2] & 0x3C);
        var b = MyArrayUtils.getRangeArray(udPacket, 3, udPacket.length - 1);
        b = ArrayUtils.addAll(new byte[]{(byte) (a[2] & 0x3)}, b);
        var res = new byte[b.length - 1];
        for (int i = 0; i < res.length; i++) {
            if (i == 0) {
                res[i] = (byte) (((b[i] & 0x03) << 6) | ((b[i + 1] & 0xfc) >> 2));
                continue;
            }
            res[i] = (byte) (((b[i] & 0x03) << 6) | ((b[i + 1] & 0xfc) >> 2));
        }
        return ArrayUtils.addAll(new byte[]{type, addr0, addr1, addr2, addr3}, res);
    }

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
        connection = new Connection(ports[num], null);
//        connection = new Connection(port);
        if (connection.connect()) {
            out.println("Opened port: " + connection.port().getDescriptivePortName());
            return connection;
        } else {
            return null;
        }
    }
}
