import com.fazecast.jSerialComm.SerialPort;

import java.util.*;

import static java.lang.System.*;
import static java.lang.System.out;

public class Main {
    static Scanner scanner = new Scanner(in);
    public static App app = null;


    public static void main(String[] args) {

        byte firstNum = (byte) (18 >> 4);       // = 1
        byte secondNum = (byte) (18 & 0x0f);    // = 2
        byte thirdNum = (byte) (52 >> 4);       // = 3
        byte fourthNum = (byte) (52 & 0x0f);    // = 4
        out.println(firstNum);
        out.println(secondNum);
        out.println(thirdNum);
        out.println(fourthNum);

        if (args.length > 0) {
            return;
        }
        Connection connection = null;
        while (true) {
            if (connection == null) {
                connection = setConnection();
                app = new App(connection);
            } else {
                app.start();
            }
        }
    }


    private static Connection setConnection() {
        Connection connection;
        var port = SerialPort.getCommPort("/dev/ttys003");

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
