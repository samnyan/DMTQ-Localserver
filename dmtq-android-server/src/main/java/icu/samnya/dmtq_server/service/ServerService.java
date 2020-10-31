package icu.samnya.dmtq_server.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.io.File;
import java.io.IOException;

import icu.samnya.dmtq_server.proxy.ProxyServer;
import moe.msm.dmtqserver.GameServer;
import moe.msm.dmtqserver.model.ServerConfig;

public class ServerService extends Service {

    public class ServerBinder extends Binder {
        public ServerService getInstance() {
            return ServerService.this;
        }
    }

    private static GameServer gameServer;
    private static GameServer sslGameServer;

    private static DatabaseService dbService;
    private static AndroidStaticFileService staticFileService;

    private static ProxyServer proxyServer;

    private int port = 3456;
    private int sslPort = 3457;
    private int proxyPort = 3458;

    private Thread proxyServerThread;

    private Binder binder = new ServerBinder();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        // If the system recreate the service, run the server.
        if(intent == null) {
            runServer();
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        stopServer();
        super.onDestroy();
    }

    public void toggleServer() {
        if(gameServer == null) {
            runServer();
        } else {
            stopServer();
        }
    }

    public void runServer() {

        SharedPreferences sharedPref = getSharedPreferences("SERVER_PREFERENCES", Context.MODE_PRIVATE);
        String HOST = sharedPref.getString("HOST_ADDRESS", "localhost:3456");
        String SSL = sharedPref.getString("SSL_ADDRESS", "localhost:3457");
        String PROXY = sharedPref.getString("PROXY_ADDRESS", "localhost:3458");
        String ASSET = sharedPref.getString("ASSET_ADDRESS", "localhost:3456");

        int port = 3456;
        int sslPort = 3457;
        int proxyPort = 3458;
        try {
            String[] game = HOST.split(":");
            String[] ssl = SSL.split(":");
            String[] proxy = PROXY.split(":");
            port = Integer.parseInt(game[1]);
            sslPort = Integer.parseInt(ssl[1]);
            proxyPort = Integer.parseInt(proxy[1]);
        } catch (Exception e) {
        }

        this.port = port;
        this.sslPort = sslPort;
        this.proxyPort = proxyPort;

        ServerConfig config = new ServerConfig();
        config.setHostname("0.0.0.0");
        config.setPort(this.port);
        config.setHostAddress(HOST);
        config.setProxyAddress(PROXY);
        config.setAssetAddress(ASSET);

        if(proxyServerThread == null) {
            proxyServerThread = new Thread(() -> {
                proxyServer = new ProxyServer(this.proxyPort, this);
                proxyServer.start();
            });
            proxyServerThread.start();
        }

        try {
            if (!isExternalStorageWritable()) {
                throw new RuntimeException("NO PERMISSION TO WRITE FILE");
            }

            if (dbService == null) {
                File dir = getExternalFilesDir("");
                SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(new File(dir, "dmtq.db"), null);
                dbService = new DatabaseService(db);
            }

            if (staticFileService == null) {
                staticFileService = new AndroidStaticFileService(this);
            }

            if (gameServer == null) {
                gameServer = new GameServer(config, dbService, staticFileService);
            }
//            if (sslGameServer == null) {
//                char[] passwordArray = "password".toCharArray();
//                KeyStore keystore = KeyStore.getInstance("PKCS12");
//                File key = new File(dir, "keystore.jks");
//                if (key.exists()) {
//                    InputStream kis = new FileInputStream(key);
//                    keystore.load(kis, passwordArray);
//                    KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
//                    keyManagerFactory.init(keystore, "password".toCharArray());
//
//                    sslGameServer = new GameServer(sslPort, this, db);
//                    sslGameServer.makeSecure(NanoHTTPD.makeSSLSocketFactory(keystore, keyManagerFactory), null);
//                } else {
//                    throw new RuntimeException("Keystore file not found");
//                }
//            }
        } catch (Exception e) {
            Log.e("ServerService", e.getMessage(), e);
            Toast.makeText(this.getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
            gameServer = null;
        }

        if(gameServer != null) {
            if (!gameServer.isAlive()) {
                try {
                    gameServer.start();
                    Toast.makeText(this.getApplicationContext(), "Server start", Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    Log.e("Server", e.getMessage(), e);
                    Toast.makeText(this.getApplicationContext(), "Fail to start server", Toast.LENGTH_SHORT).show();
                }
            }
        }
        if(sslGameServer != null) {
            if (!sslGameServer.isAlive()) {
                try {
                    sslGameServer.start();
                    Toast.makeText(this.getApplicationContext(), "SSL Server start", Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    Log.e("Server", e.getMessage(), e);
                    Toast.makeText(this.getApplicationContext(), "Fail to start SSL server", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    public void stopServer()  {
        if(gameServer != null) {
            if(gameServer.isAlive()) {
                gameServer.stop();
                Toast.makeText(this.getApplicationContext(), "Server stop", Toast.LENGTH_SHORT).show();
            }
            gameServer = null;
        }
//        if(sslGameServer != null) {
//            if(sslGameServer.isAlive()) {
//                sslGameServer.stop();
//                Toast.makeText(this.getApplicationContext(), "Server stop", Toast.LENGTH_SHORT).show();
//            }
//            sslGameServer = null;
//        }
        if(proxyServerThread != null) {
            if(proxyServer.isRunning()) {
                proxyServer.stop();
            }
            try {
                proxyServerThread.join(0);
                proxyServerThread = null;
            } catch (InterruptedException e) {
                Toast.makeText(this.getApplicationContext(), "Fail to stop proxy thread", Toast.LENGTH_SHORT).show();
            }
        }

    }

    public boolean isAlive() {
        if (gameServer == null) {
            return false;
        } else {
            return gameServer.isAlive();
        }
    }

    private boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }
}
