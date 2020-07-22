package moe.msm.dmtqserver.model;

/**
 * @author sam_nya (privateamusement@protonmail.com)
 */
public class ServerConfig {

    private String hostname;

    private int port;

    private String hostAddress;

    private String proxyAddress;

    private String assetAddress;

    public ServerConfig() {
        this.hostname = "0.0.0.0";
        this.port = 3456;
        this.hostAddress = "localhost:3456";
        this.proxyAddress = "localhost:3458";
        this.assetAddress = "localhost:3456";
    }

    public ServerConfig(String hostname, int port, String hostAddress, String proxyAddress, String assetAddress) {
        this.hostname = hostname;
        this.port = port;
        this.hostAddress = hostAddress;
        this.proxyAddress = proxyAddress;
        this.assetAddress = assetAddress;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getHostAddress() {
        return hostAddress;
    }

    public void setHostAddress(String hostAddress) {
        this.hostAddress = hostAddress;
    }

    public String getProxyAddress() {
        return proxyAddress;
    }

    public void setProxyAddress(String proxyAddress) {
        this.proxyAddress = proxyAddress;
    }

    public String getAssetAddress() {
        return assetAddress;
    }

    public void setAssetAddress(String assetAddress) {
        this.assetAddress = assetAddress;
    }

    @Override
    public String toString() {
        return "ServerConfig{" +
                "hostname='" + hostname + '\'' +
                ", port=" + port +
                ", hostAddress='" + hostAddress + '\'' +
                ", proxyAddress='" + proxyAddress + '\'' +
                ", assetAddress='" + assetAddress + '\'' +
                '}';
    }
}
