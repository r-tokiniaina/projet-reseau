package p2pshare.model;

import java.net.InetAddress;

public class Peer {

    private final String name;
    private final InetAddress address;
    private final int tcpPort;

    public Peer(String name, InetAddress address, int tcpPort) {
        this.name = name;
        this.address = address;
        this.tcpPort = tcpPort;
    }

    public String getName() { return name; }
    public InetAddress getAddress() { return address; }
    public int getTcpPort() { return tcpPort; }

    @Override
    public String toString() {
        return name + " (" + address.getHostAddress() + ")";
    }
}