import java.io.IOException;
import java.util.*;

import com.fazecast.jSerialComm.SerialPort;

import static java.lang.System.*;
import static java.lang.System.out;
import static java.lang.Thread.sleep;

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
                var m = scanner.nextLine();
                if (m.equals("exit")) {
                    break;
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
        var port = SerialPort.getCommPort("/dev/ttys002");

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
        connection = new Connection(port);
        if (connection.connect()) {
            out.println("Opened port: " + connection.port().getDescriptivePortName());
            return connection;
        } else {
            return null;
        }
    }
}
