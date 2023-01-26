import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;
import model.Node;
import model.SendPacket;
import org.apache.commons.lang3.StringUtils;
import utils.Converter;
import utils.Parser;

import java.io.IOException;
import java.io.OutputStream;
import java.util.*;

//AT+DEST=FFFF
//AT+CFG=433920000,5,6,10,4,1,0,0,0,0,3000,8,4
//AT+RX
//AT+SEND=14
//BBACAAEAAAMI
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

    public boolean sendThreadInProcess(){
       return listener.myThread.inProcess();
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

                send("AT+CFG=433920000,5,6,10,4,1,0,0,0,0,3000,8,4");
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

    public boolean send(String m)  {
        if (outputStream == null) {
            return false;
        }
        try {
            outputStream.write((m + "\r\n").getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
//        outputStream.flush();
        return true;
    }

    public MyThread sendPacket(SendPacket answer) {
        return new MyThread(answer);
    }

    public void stop() {
        port.closePort();
    }

    public class Listener implements SerialPortDataListener {

        private final Set<SendPacket> sendPackets = new HashSet<>();

        private MyThread myThread = new MyThread(null);
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
                if (myThread != null) {
                    synchronized (myThread) {
                        myThread.notify();
                    }
                }
            }

            //test
//            System.out.println(new String(buffer));
//            printInfo();
//            System.out.println("-------------------------");

            var sendPacket = MessageHandler.handle(buffer);

            if (!connected) {
                return;
            }
            if (sendPacket != null) {
//                System.out.println("Antwort = " + Base64.getEncoder().encodeToString(sendPacket.getPacket()));
                sendPackets.add(sendPacket);
            }

            if (sendPackets.size() != 0 && !myThread.inProcess()) {
                var packet = (SendPacket) sendPackets.toArray()[0];
                sendPackets.remove(packet);
                myThread = sendPacket(packet);
            }
//            printInfo();
//            System.out.println("***************************");


        }

        private void printInfo() {
            System.out.println(sendPackets);
            System.out.println("Thread in process : " + myThread.inProcess());
            System.out.println("My address = " + Arrays.toString(Node.getADDR()));
            System.out.println(Node.getInfo());
        }

        public void setSendThread(MyThread sendThread) {
            myThread = sendThread;
        }
    }


    private class MyThread implements Runnable {
        private final Thread t;
        SendPacket sendPacket;

        public MyThread(SendPacket sendPacket) {
            this.sendPacket = sendPacket;
            t = new Thread(this);
            t.start();
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
                        packet = Base64.getEncoder().encode(packet);

                        send("AT+SEND=" + packet.length);
                        this.wait();
                        send(new String(packet));
                        if (!Arrays.equals(dest, Parser.parseAddrToBytes("FFFF"))) {
                            this.wait();
                            send("AT+DEST=FFFF");
                        }
                    }
                }
            } catch ( InterruptedException e) {
                System.err.println(e);
            }

        }

    }
}
