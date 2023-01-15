import java.util.*;

import com.fazecast.jSerialComm.SerialPort;
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
        var decoded = Base64.getDecoder().decode("AAAVIYWxsbw=");//AABBIYWxsbw=
        out.println(Arrays.toString(decoded));
        var converted = Converter.userDataPacketDecode(decoded);
        out.println(new String(MyArrayUtils.getRangeArray(converted, 5, (converted.length - 1))));
        out.println(Arrays.toString(converted));

        out.println(Base64.getEncoder().encodeToString(Converter.userDataPacketEncode(converted)));

        var newUD = ArrayUtils.addAll(new byte[]{0,0,0,0,5}, "Hallo".getBytes());

        var encided = Converter.userDataPacketEncode(newUD);
        out.println(Base64.getEncoder().encodeToString(encided));


        if (args.length > 0) {
            return;
        }

        Connection connection = null;
        App app = null;
        while (true) {
            if (connection == null) {
                connection = setConnection();
                app = new App(connection);
            } else {
//                var m = scanner.nextLine();
//                if (m.equals("exit")) {
//                    break;
//                } else {
                    app.start();
//                try {
//                    if (m.length() > 0 && !connection.send(m)) {
//                        connection = null;
//                    }
//                } catch (RuntimeException e) {
//                    err.println("Failed to send");
//                    connection = null;
//                }
            }
        }
//        connection.stop();
    }


    private static Connection setConnection() {
        Connection connection;
        var port = SerialPort.getCommPort("/dev/ttys001");

        var ports = Connection.getPorts();
        for (int i = 0; i < ports.length; i++) {
            out.println(i + ". " + ports[i].getDescriptivePortName());
        }
        out.print("Choose port: ");
        var num = scanner.nextInt();
        if (num < 0 || num >= ports.length) {
            return null;
        }
//        connection = new Connection(ports[num], null);
        connection = new Connection(port, null);
        if (connection.connect()) {
            out.println("Opened port: " + connection.port().getDescriptivePortName());
            return connection;
        } else {
            return null;
        }
    }
}
