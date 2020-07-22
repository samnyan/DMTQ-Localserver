package moe.msm.dmtqjavaserver;

import com.github.chhsiaoninety.nitmproxy.NitmProxyConfig;
import fi.iki.elonen.NanoHTTPD;
import moe.msm.NettyHijackProxy;
import moe.msm.dmtqjavaserver.service.JavaDatabaseService;
import moe.msm.dmtqjavaserver.service.JavaFileService;
import moe.msm.dmtqserver.GameServer;
import moe.msm.dmtqserver.model.ServerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.KeyManagerFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.List;

public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    private static GameServer gameServer;
    private static GameServer sslGameServer;

    private static JavaDatabaseService databaseService;
    private static JavaFileService fileService;
    private static NettyHijackProxy proxy;

    private static Thread proxyThread;

    public static void main(String[] args) throws Exception {
        ServerConfig config = new ServerConfig();
        ServerConfig sslConfig = new ServerConfig();

        if(databaseService == null) {
            databaseService = new JavaDatabaseService();
        }

        if(fileService == null) {
            fileService = new JavaFileService();
        }

        if(gameServer == null) {
            gameServer = new GameServer(config,databaseService,fileService);
        }

        if (sslGameServer == null) {
            sslConfig.setPort(3457);
            char[] passwordArray = "password".toCharArray();
            KeyStore keystore = KeyStore.getInstance("PKCS12");
            File key = fileService.getFileByPath("files/keystore.jks");
            if (key.exists()) {
                InputStream kis = new FileInputStream(key);
                keystore.load(kis, passwordArray);
                KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
                keyManagerFactory.init(keystore, "password".toCharArray());

                sslGameServer = new GameServer(sslConfig, databaseService, fileService);
                sslGameServer.makeSecure(NanoHTTPD.makeSSLSocketFactory(keystore, keyManagerFactory), null);
            } else {
                throw new RuntimeException("Keystore file not found");
            }
        }

        if(proxy == null) {
            List<String> redirectList = new ArrayList<>();
            redirectList.add("pmang.com");
            redirectList.add("pmangplus.com");
            redirectList.add("neonapi.com");
            NitmProxyConfig proxyConfig = new NitmProxyConfig();
            proxyConfig.setHost("0.0.0.0");
            proxyConfig.setPort(3458);
            proxyConfig.setKeyFile(fileService.getFileByPath("key.pem").getAbsolutePath());
            proxyConfig.setCertFile(fileService.getFileByPath("server.pem").getAbsolutePath());
            proxyConfig.setRedirectDomains(redirectList);
            proxyConfig.setRedirectTargetHost("127.0.0.1");
            proxyConfig.setInsecure(true);
            proxyConfig.setRedirectTargetHttpPort(3456);
            proxyConfig.setRedirectTargetHttpsPort(3457);
            proxy = new NettyHijackProxy(proxyConfig);
        }

        try {
            logger.info("Server Running with config {}", config);
            proxyThread = new Thread(() -> {
                try {
                    logger.info("Proxy Running");
                    proxy.start();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            proxyThread.start();
            gameServer.start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
            logger.info("Http Server Running");
            sslGameServer.start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
            logger.info("Https Server Running");
        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}