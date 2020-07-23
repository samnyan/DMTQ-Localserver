package moe.msm.dmtqjavaserver;

import com.github.chhsiaoninety.nitmproxy.NitmProxyConfig;
import fi.iki.elonen.NanoHTTPD;
import moe.msm.NettyHijackProxy;
import moe.msm.dmtqjavaserver.service.JavaDatabaseService;
import moe.msm.dmtqjavaserver.service.JavaFileService;
import moe.msm.dmtqserver.GameServer;
import moe.msm.dmtqserver.model.ServerConfig;
import moe.msm.dmtqserver.util.StreamUtil;
import org.apache.commons.cli.*;
import org.json.JSONArray;
import org.json.JSONObject;
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

        CommandLineParser parser = new DefaultParser();

        Options options = new Options();

        options.addOption(
                Option.builder("c")
                        .longOpt("config")
                        .hasArg()
                        .argName("CONFIG")
                        .desc("Config file, default: config.json")
                .build());

        CommandLine cmdLine = null;

        try {
            cmdLine = parser.parse(options, args);
        } catch (ParseException e) {
            new HelpFormatter().printHelp("DMTQ Localserver", options, true);
            System.exit(-1);
        }

        File configFile = new File(System.getProperty("user.dir"), "config.json");
        if (cmdLine.hasOption("c")) {
            configFile = new File(cmdLine.getOptionValue("c"));
        }

        if (!configFile.exists()) {
            logger.warn("Config file not exists: {}", configFile.getAbsolutePath());
            System.exit(-1);
        }
        InputStream configIs = new FileInputStream(configFile);
        JSONObject configJson = new JSONObject(StreamUtil.readAllLine(configIs));

        // Database config
        String databaseUrl = configJson.getJSONObject("database").getString("url");

        // Server config
        ServerConfig config = new ServerConfig();
        config.setHostname(configJson.getJSONObject("gameServer").getString("host"));
        config.setPort(configJson.getJSONObject("gameServer").getJSONObject("httpServer").getInt("port"));
        config.setHostAddress(configJson.getJSONObject("gameServer").getString("hostAddress"));
        config.setAssetAddress(configJson.getJSONObject("gameServer").getString("assetAddress"));
        config.setProxyAddress(configJson.getJSONObject("gameServer").getString("proxyAddress"));

        ServerConfig sslConfig = new ServerConfig();
        sslConfig.setHostname(configJson.getJSONObject("gameServer").getString("host"));
        sslConfig.setPort(configJson.getJSONObject("gameServer").getJSONObject("httpsServer").getInt("port"));
        sslConfig.setHostAddress(configJson.getJSONObject("gameServer").getString("hostAddress"));
        sslConfig.setAssetAddress(configJson.getJSONObject("gameServer").getString("assetAddress"));
        sslConfig.setProxyAddress(configJson.getJSONObject("gameServer").getString("proxyAddress"));
        String sslKeyStorePath = configJson.getJSONObject("gameServer").getJSONObject("httpsServer").getString("keyStorePath");
        String sslKeyStorePassword = configJson.getJSONObject("gameServer").getJSONObject("httpsServer").getString("keyStorePassword");

        // Proxy config
        List<String> redirectList = new ArrayList<>();
        JSONArray redirectJsonList = configJson.getJSONObject("proxyServer").getJSONArray("redirectList");
        for(int i = 0; i < redirectJsonList.length(); i++) {
            redirectList.add(redirectJsonList.getString(i));
        }
        NitmProxyConfig proxyConfig = new NitmProxyConfig();
        proxyConfig.setHost(configJson.getJSONObject("proxyServer").getString("host"));
        proxyConfig.setPort(configJson.getJSONObject("proxyServer").getInt("port"));
        proxyConfig.setCertFile(configJson.getJSONObject("proxyServer").getString("rootCertPath"));
        String proxyCaKeyPath = configJson.getJSONObject("proxyServer").getString("rootKeyPath");
        proxyConfig.setKeyFile(proxyCaKeyPath);
        proxyConfig.setRedirectDomains(redirectList);
        proxyConfig.setRedirectTargetHost(configJson.getJSONObject("proxyServer").getString("redirectTarget"));
        proxyConfig.setInsecure(configJson.getJSONObject("proxyServer").getBoolean("insecure"));
        proxyConfig.setRedirectTargetHttpPort(configJson.getJSONObject("proxyServer").getInt("redirectHttpPort"));
        proxyConfig.setRedirectTargetHttpsPort(configJson.getJSONObject("proxyServer").getInt("redirectHttpsPort"));

        if(!new File(proxyCaKeyPath).exists()) {
            throw new RuntimeException("Proxy cert key file not found");
        }

        if(databaseService == null) {
            databaseService = new JavaDatabaseService(databaseUrl);
        }

        if(fileService == null) {
            fileService = new JavaFileService();
        }

        if(gameServer == null) {
            gameServer = new GameServer(config,databaseService,fileService);
        }

        if (sslGameServer == null) {
            sslConfig.setPort(3457);
            char[] passwordArray = sslKeyStorePassword.toCharArray();
            KeyStore keystore = KeyStore.getInstance("PKCS12");
            File key = new File(sslKeyStorePath);
            if (key.exists()) {
                InputStream kis = new FileInputStream(key);
                keystore.load(kis, passwordArray);
                KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
                keyManagerFactory.init(keystore, passwordArray);

                sslGameServer = new GameServer(sslConfig, databaseService, fileService);
                sslGameServer.makeSecure(NanoHTTPD.makeSSLSocketFactory(keystore, keyManagerFactory), null);
            } else {
                throw new RuntimeException("Https game server Keystore file not found");
            }
        }

        if(proxy == null) {
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