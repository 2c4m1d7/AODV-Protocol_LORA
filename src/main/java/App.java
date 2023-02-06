import model.Node;
import model.PacketType;
import model.SendPacket;
import packets.UserData;
import utils.Parser;

import java.util.*;

public class App {

    private SendUDThread sendUDThread;
    private final Connection connection;

    public App(Connection connection) {
        this.connection = connection;
        sendUDThread=new SendUDThread(null);
    }

    public SendUDThread getSendUDThread() {
        return sendUDThread;
    }

    public void start() {
        var userData = new UserData();
        Scanner scanner = new Scanner(System.in);
        System.out.println("Send data");
        System.out.println("Select an address or write another:");
        var addrs = Node.getValidDestAddrs();
        for (int i = 0; i < addrs.size(); i++) {
            System.out.println(i + ": " + Arrays.toString(addrs.get(i)));
        }
        var input = scanner.nextLine();
        byte[] addr;
        try {
            var num = Integer.parseInt(input);
            if (num >= 0 && num < addrs.size()) {
                addr = addrs.get(num);
            } else {
                addr = Parser.parseAddrToBytes(input);
            }
        } catch (IllegalArgumentException e) {
            try {
                addr = Parser.parseAddrToBytes(input);
            } catch (IllegalArgumentException e1) {
                System.err.println(e);
                System.err.println(e1);
                return;
            }
        }

        userData.setDestAddr(addr);

        System.out.print("Write message:");
        var message = scanner.next();
        userData.setMessage(message);

        send(SendPacket.UD.setPacket(userData.getBytes()));
//        SendPacket sendPacket;
//        var i = 0;
//        do {
//            sendPacket = MessageHandler.handleUD(userData.getBytes(), null);
//            i++;
//            try {
//                sendPacket(sendPacket);
//                if (sendPacket.getType() == PacketType.RREQ) {
//                    synchronized (this) {
//                        do {
//                            wait();
//                        } while (connection.listener().packetStillInQueue(sendPacket));
//                    }
//                    Thread.sleep(((long) i * i * Node.NET_TRAVERSAL_TIME));
//                } else if (sendPacket.getType() == PacketType.UD) {
//                    break;
//                }
//            } catch (InterruptedException e) {
//                throw new RuntimeException(e);
//            }
//        } while (Node.RREQ_RETRIES != i);
//        if (sendPacket.getType() != PacketType.UD) {
//            sendPacket = MessageHandler.handleUD(userData.getBytes(), null);
//            if (sendPacket.getType() == PacketType.UD) {
//                sendPacket(sendPacket);
//            } else {
//                System.err.println("Not sent");
//            }
//        }

    }

    public void sendUD(SendPacket sendPacket) {
        if (sendUDThread.isInProcess()) {
            sendUDThread.add(sendPacket);
            return;
        }
        sendUDThread.stop();
        sendUDThread = new SendUDThread(sendPacket);
    }

    class SendUDThread implements Runnable {
        Thread t;

        List<SendPacket> sendPackets = new LinkedList<>();

        public SendUDThread(SendPacket sendPacket) {
            add(sendPacket);
//            this.sendPacket = sendPacket;
            this.t = new Thread(this);
            t.start();
        }

        public void stop() {
            try {
                t.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void run() {
            do {
                SendPacket sendPacket = sendPackets.get(0);
                sendPackets.remove(0);
                send(sendPacket);
            } while (sendPackets.size() != 0);

//            UserData userData = new UserData(sendPacket.getPacket());
//            var i = 0;
//            do {
//                sendPacket = MessageHandler.handleUD(userData.getBytes(), sendPacket.getNextHop());
//                if (sendPacket==null){
//                    break;
//                }
//                i++;
//                try {
//                    sendPacket(sendPacket);
//                    if (sendPacket.getType() == PacketType.RREQ) {
//                        synchronized (this) {
//                            do {
//                                wait();
//                            } while (connection.listener().packetStillInQueue(sendPacket));
//                        }
//                        Thread.sleep(((long) i * i * Node.NET_TRAVERSAL_TIME));
//                    } else if (sendPacket.getType() == PacketType.UD) {
//                        break;
//                    }
//                } catch (InterruptedException e) {
//                    throw new RuntimeException(e);
//                }
//            } while (Node.RREQ_RETRIES != i);
//            if (sendPacket.getType() != PacketType.UD) {
//                sendPacket = MessageHandler.handleUD(userData.getBytes(), null);
//                if (sendPacket.getType() == PacketType.UD) {
//                    sendPacket(sendPacket);
//                } else {
//                    System.err.println("Not sent");
//                }
//            }
        }

        public boolean isInProcess() {
            return t.isAlive();
        }

        public void add(SendPacket sendPacket) {
            sendPackets.add(sendPacket);
        }
    }

    public void send(SendPacket sendPacket) {
        if (sendPacket == null){
            return;
        }
        UserData userData = new UserData(sendPacket.getPacket());
        var i = 0;
        do {
            sendPacket = MessageHandler.handleUD(userData.getBytes(), sendPacket.getNextHop());
            if (sendPacket == null) {
                break;
            }
            i++;
            try {
                sendPacket(sendPacket);
                if (sendPacket.getType() == PacketType.RREQ) {
                    synchronized (sendUDThread) {
                        do {
                            sendUDThread.wait();
                        } while (connection.listener().packetStillInQueue(sendPacket));
                    }
                    Thread.sleep(((long) i * i * Node.NET_TRAVERSAL_TIME));
                } else if (sendPacket.getType() == PacketType.UD) {
                    break;
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        } while (Node.RREQ_RETRIES != i);

        if (sendPacket.getType() != PacketType.UD) {
            sendPacket = MessageHandler.handleUD(userData.getBytes(), null);
            if (sendPacket.getType() == PacketType.UD) {
                sendPacket(sendPacket);
            } else {
                System.err.println("Not sent");
            }
        }
    }

    private void sendPacket(SendPacket sendPacket) {
        connection.listener().addSendPacket(sendPacket);
        if (!connection.sendThreadInProcess()) {
            connection.send("AT");
        }
    }
}

