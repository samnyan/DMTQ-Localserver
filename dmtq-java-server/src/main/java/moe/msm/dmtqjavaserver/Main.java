package moe.msm.dmtqjavaserver;

import fi.iki.elonen.NanoHTTPD;
import moe.msm.dmtqjavaserver.service.JavaDatabaseService;
import moe.msm.dmtqjavaserver.service.JavaFileService;
import moe.msm.dmtqserver.GameServer;
import moe.msm.dmtqserver.model.ServerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    private static GameServer gameServer;

    private static JavaDatabaseService databaseService;
    private static JavaFileService fileService;

    public static void main(String[] args) {
        ServerConfig config = new ServerConfig();

        if(databaseService == null) {
            databaseService = new JavaDatabaseService();
        }

        if(fileService == null) {
            fileService = new JavaFileService();
        }

        if(gameServer == null) {
            gameServer = new GameServer(config,databaseService,fileService);
        }

        try {
            gameServer.start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
            logger.info("Server Running with config {}", config);
        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}