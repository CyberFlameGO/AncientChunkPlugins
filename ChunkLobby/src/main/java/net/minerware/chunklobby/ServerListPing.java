package net.minerware.chunklobby;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.Charset;

final class ServerListPing {

    private String address = "localhost";
    private int port = 25565;

    private int timeout = 7000;

    private int pingVersion = -1;
    private int protocolVersion = -1;
    private String gameVersion = "1.3";
    private String motd = "";
    private int playersOnline = -1;
    private int maxPlayers = -1;

    public ServerListPing() {

    }

    private ServerListPing(String address) {
        this();

        this.setAddress(address);
    }

    private ServerListPing(String address, int port) {
        this(address);

        this.setPort(port);
    }

    public ServerListPing(String address, int port, int timeout) {
        this(address, port);

        this.setTimeout(timeout);
    }

    public void setAddress(String address) {
        this.address = address;
    }

    String getAddress() {
        return this.address;
    }

    public void setPort(int port) {
        this.port = port;
    }

    int getPort() {
        return this.port;
    }

    void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    int getTimeout() {
        return this.timeout;
    }

    private void setPingVersion(int pingVersion) {
        this.pingVersion = pingVersion;
    }

    public int getPingVersion() {
        return this.pingVersion;
    }

    private void setProtocolVersion(int protocolVersion) {
        this.protocolVersion = protocolVersion;
    }

    public int getProtocolVersion() {
        return this.protocolVersion;
    }

    private void setGameVersion(String gameVersion) {
        this.gameVersion = gameVersion;
    }

    public String getGameVersion() {
        return this.gameVersion;
    }

    private void setMotd(String motd) {
        this.motd = motd;
    }

    public String getMotd() {
        return this.motd;
    }

    private void setPlayersOnline(int playersOnline) {
        this.playersOnline = playersOnline;
    }

    public int getPlayersOnline() {
        return this.playersOnline;
    }

    private void setMaxPlayers(int maxPlayers) {
        this.maxPlayers = maxPlayers;
    }

    public int getMaxPlayers() {
        return this.maxPlayers;
    }

    public void fetchData() throws IOException {
        Socket socket = new Socket();
        OutputStream outputStream;
        DataOutputStream dataOutputStream;
        InputStream inputStream;
        InputStreamReader inputStreamReader;

        socket.setSoTimeout(this.timeout);

        socket.connect(new InetSocketAddress(
                this.getAddress(),
                this.getPort()
        ), this.getTimeout());

        outputStream = socket.getOutputStream();
        dataOutputStream = new DataOutputStream(outputStream);

        inputStream = socket.getInputStream();
        inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-16BE"));

        dataOutputStream.write(new byte[]{
                (byte) 0xFE,
                (byte) 0x01
        });

        int packetId = inputStream.read();

        if (packetId == -1) {
            throw new IOException("Premature end of stream.");
        }

        if (packetId != 0xFF) {
            throw new IOException("Invalid packet ID (" + packetId + ").");
        }

        int length = inputStreamReader.read();

        if (length == -1) {
            throw new IOException("Premature end of stream.");
        }

        if (length == 0) {
            throw new IOException("Invalid string length.");
        }

        char[] chars = new char[length];

        if (inputStreamReader.read(chars, 0, length) != length) {
            throw new IOException("Premature end of stream.");
        }

        String string = new String(chars);

        String[] data = string.split("\0");

        this.setPingVersion(Integer.parseInt(data[0].substring(1)));
        this.setProtocolVersion(Integer.parseInt(data[1]));
        this.setGameVersion(data[2]);
        this.setMotd(data[3]);
        this.setPlayersOnline(Integer.parseInt(data[4]));
        this.setMaxPlayers(Integer.parseInt(data[5]));

        dataOutputStream.close();
        outputStream.close();

        inputStreamReader.close();
        inputStream.close();

        socket.close();
    }

}