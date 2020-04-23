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

import icu.samnya.dmtq_server.server.GameServer;

public class ServerService extends Service {

    public class ServerBinder extends Binder {
        public ServerService getInstance() {
            return ServerService.this;
        }
    }

    private static GameServer gameServer;

    private int port = 3456;

    private Binder binder = new ServerBinder();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        SharedPreferences sharedPref = getSharedPreferences("SERVER_PREFERENCES", Context.MODE_PRIVATE);
        String HOST = sharedPref.getString("HOST_ADDRESS", "localhost:3456");
        int port = 3456;
        try {
            String[] address = HOST.split(":");
            port = Integer.parseInt(address[1]);
        } catch (Exception e) {
            port = 3456;
        }

        this.port = port;

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
        if (gameServer == null) {
            try {
                if (!isExternalStorageWritable()) {
                    throw new RuntimeException("NO PERMISSION TO WRITE FILE");
                }
                File dir = getExternalFilesDir("");
                SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(new File(dir, "dmtq.db"), null);

                gameServer = new GameServer("localhost", port, this, db);
            } catch (Exception e) {
                Toast.makeText(this.getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
        if(!gameServer.isAlive()) {
            try {
                gameServer.start();
                Toast.makeText(this.getApplicationContext(), "Server start", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                Log.e("Server", e.getMessage(), e);
                Toast.makeText(this.getApplicationContext(), "Fail to start server", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void stopServer() {
        if(gameServer != null) {
            if(gameServer.isAlive()) {
                gameServer.stop();
                Toast.makeText(this.getApplicationContext(), "Server stop", Toast.LENGTH_SHORT).show();
            }
            gameServer = null;
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
