import java.util.*;

import static java.lang.System.*;
import static java.lang.System.out;

public class Main {
    static Scanner scanner = new Scanner(in);

    public static void main(String[] args) {


        Connection connection = null;
        App app = null;
        while (true) {
            if (connection == null) {
                connection = setConnection();
                app = new App(connection);
            } else {
                app.start();
            }
        }
//        connection.stop();
    }


    private static Connection setConnection() {
        Connection connection;
//        var port = SerialPort.getCommPort("/dev/ttys001");

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
//        connection = new Connection(port, null);
        if (connection.connect()) {
            out.println("Opened port: " + connection.port().getDescriptivePortName());
            return connection;
        } else {
            return null;
        }
    }
}
