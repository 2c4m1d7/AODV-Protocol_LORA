import java.io.IOException;
import java.util.*;

import com.fazecast.jSerialComm.SerialPort;

import static java.lang.System.*;
import static java.lang.System.out;
import static java.lang.Thread.sleep;

// DAVID NODE BgAAABMAABEA  RREQ
public class Main {
    static Scanner scanner = new Scanner(in);

    public static void main(String[] args) {

//        var decoded = Base64.getDecoder().decode("BBACAAEAAAMI");
//        out.println(Arrays.toString(decoded));
//        byte[] converted = Converter.convertDecoded(decoded);
//        out.println(Arrays.toString(converted));
//        var rreq = new RREQ(converted);
//        out.println(Arrays.toString(rreq.getBytes()));
//
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
