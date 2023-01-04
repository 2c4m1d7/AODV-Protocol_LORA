package connector;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;

import java.io.IOException;
import java.io.OutputStream;

public record TestConnection(SerialPort port) {

    private static OutputStream outputStream;

    public SerialPort port() {
        return port;
    }
    public static SerialPort[] getPorts() {
        return SerialPort.getCommPorts();
    }

    public boolean connect(SerialPortDataListener listener) {
        if (!port.isOpen() && !port.openPort(2000)) {
                return false;
        }
//        port.setBaudRate(115200);   scheint nicht noetig zu sein
        outputStream = port.getOutputStream();
        port.addDataListener(listener);
        return true;
    }

    public boolean send(String m) throws IOException {
        if (outputStream == null) {
            return false;
        }
        outputStream.write((m + "\r\n").getBytes());
        outputStream.flush();
        return true;
    }
}
