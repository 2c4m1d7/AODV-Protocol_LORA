import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;
import model.Node;
import model.PacketType;
import model.SendPacket;
import org.apache.commons.lang3.StringUtils;
import packets.RREP;
import packets.RREQ;
import packets.UserData;
import utils.Converter;
import utils.MyArrayUtils;
import utils.MyLogger;
import utils.Parser;

import java.io.IOException;
import java.io.OutputStream;
import java.util.*;

public record Connection(SerialPort port, Listener listener) {
    private static boolean connected = false;

    private static OutputStream outputStream;

    public Connection {
        listener = new Listener(this);
    }

    public SerialPort port() {
        return port;
    }

    public static SerialPort[] getPorts() {
        return SerialPort.getCommPorts();
    }

    @Override
    public Listener listener() {
        return listener;
    }

    public boolean sendThreadInProcess() {
        return listener.sendThread.inProcess();
    }

    public boolean connect() {
        if (!port.isOpen() && !port.openPort(2000)) {
            return false;
        }
        outputStream = port.getOutputStream();
        port.addDataListener(listener);
        try {
            synchronized (this) {
                send("AT+ADDR?");
                this.wait();

                send("AT+CFG=433920000,5,7,7,4,1,0,0,0,0,3000,8,4");
                this.wait();

                send("AT+DEST=FFFF");
                this.wait();

                send("AT+RX");
                this.wait();
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        connected = true;
        return true;
    }

    public boolean send(String m) {
        if (outputStream == null) {
            return false;
        }
        try {
            outputStream.write((m + "\r\n").getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return true;
    }

    public void stop() {
        port.closePort();
    }

    public class Listener implements SerialPortDataListener {

        private final Set<SendPacket> sendPackets = new HashSet<>();

        private SendThread sendThread = new SendThread();
        private final Set<String> loraResponses = Set.of("AT,SENDED");
        private String tmp = "";
        private final Connection connection;

        public Listener(Connection connection) {
            this.connection = connection;
        }

        public boolean addSendPacket(SendPacket sendPacket) {
            return sendPackets.add(sendPacket);
        }

        @Override
        public int getListeningEvents() {
            return SerialPort.LISTENING_EVENT_DATA_AVAILABLE;
        }

        @Override
        public void serialEvent(SerialPortEvent event) {
            byte[] buffer = new byte[event.getSerialPort().bytesAvailable()];
            event.getSerialPort().readBytes(buffer, buffer.length);

            tmp += new String(buffer);
            if (!tmp.contains("\r\n")) {
                return;
            } else {
                buffer = tmp.getBytes();
                tmp = "";
            }

            var response = StringUtils.substringBefore(new String(buffer), "\r");

            if (loraResponses.contains(response) || response.contains("OK")) {
                synchronized (connection) {
                    connection.notify();
                }
                if (sendThread != null) {
                    synchronized (sendThread) {
                        sendThread.notify();
                    }
                }
            }

            var sendPacket = MessageHandler.handle(buffer);

            if (!connected) {
                return;
            }


            if (sendPacket != null) {
                sendPackets.add(sendPacket);
            }

            if (sendPackets.size() != 0 && !sendThread.inProcess()) {
                var packet = (SendPacket) sendPackets.toArray()[0];
                sendPackets.remove(packet);
                sendThread.stop();
                sendThread = new SendThread(packet);
            }
        }

        public boolean packetStillInQueue(SendPacket sendPacket) {
            return sendPackets.contains(sendPacket);
        }
    }



    private class SendThread implements Runnable {
        private final Thread t;
        SendPacket sendPacket;

        public SendThread(SendPacket sendPacket) {
            this.sendPacket = sendPacket;
            t = new Thread(this);
            t.start();
        }

        public SendThread() {
            t = new Thread(this);
            t.start();
        }

        public void stop() {
            try {
                t.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        public boolean inProcess() {
            return t.isAlive();
        }

        @Override
        public void run() {
            try {
                if (sendPacket != null) {
                    synchronized (this) {
                        byte[] dest = sendPacket.getNextHop();

                        send("AT+DEST=" + Parser.parseBytesToAddr(dest));
                        this.wait();

                        var packet = sendPacket.getPacket();
                        var packetEncoded = Base64.getEncoder().encodeToString(packet);

                        send("AT+SEND=" + packetEncoded.length());
                        this.wait();
                        send(packetEncoded);

                        if (!Arrays.equals(dest, Parser.parseAddrToBytes("FFFF"))) {
                            this.wait();
                            send("AT+DEST=FFFF");
                        }

                        switch (sendPacket.getType()) {
                            case UD -> {
                                var converted = Converter.userDataPacketDecode(packet);
                                var destAddr = MyArrayUtils.getRangeArray(converted, 1, 4);
                                var message = new String(MyArrayUtils.getRangeArray(converted, 5, converted.length - 1));
                                MyLogger.info("\tDEST: " + Parser.parseBytesToAddr(sendPacket.getNextHop()) + "\t->\t" + packetEncoded + "\t->\t" + new UserData(destAddr, message));
                            }
                            case RREQ ->
                                    MyLogger.info("\tDEST: " + Parser.parseBytesToAddr(sendPacket.getNextHop()) + "\t->\t" + packetEncoded + "\t->\t" + new RREQ(Converter.convertDecoded(packet)));
                            case RREP ->
                                    MyLogger.info("\tDEST: " + Parser.parseBytesToAddr(sendPacket.getNextHop()) + "\t->\t" + packetEncoded + "\t->\t" + new RREP(Converter.convertDecoded(packet)));
                        }

                        synchronized (Main.app.getSendUDThread()) {
                            Main.app.getSendUDThread().notify();
                        }
                        synchronized (Main.app) {
                            Main.app.notify();
                        }
                        Node.logInfo();
                    }
                }
            } catch (InterruptedException e) {
                System.err.println(e);
            }
        }

    }
}
