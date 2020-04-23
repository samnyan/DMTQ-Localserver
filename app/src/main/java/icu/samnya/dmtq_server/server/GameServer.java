package icu.samnya.dmtq_server.server;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD;
import icu.samnya.dmtq_server.server.handler.DMQHandler;
import icu.samnya.dmtq_server.server.handler.NeonApiHandler;
import icu.samnya.dmtq_server.server.handler.StaticHandler;

public class GameServer extends NanoHTTPD {

    private final DMQHandler dmqHandler;
    private final NeonApiHandler neonApiHandler;
    private final StaticHandler staticHandler;

    private final DatabaseService dbService;

    private final SQLiteDatabase db;

    public GameServer(String hostname, int port, Context ctx, SQLiteDatabase db) {
        super("0.0.0.0", port);
        // Create database
        this.db = db;
        this.dbService = new DatabaseService(db);
        this.dmqHandler = new DMQHandler(ctx, this.dbService);
        this.neonApiHandler = new NeonApiHandler(ctx, this.dbService);
        this.staticHandler = new StaticHandler(ctx);
    }

    @Override
    protected boolean useGzipWhenAccepted(Response r) {
        return false;
    }

    @Override
    public void stop() {
        dbService.close();
        super.stop();
    }

    @Override
    public Response serve(IHTTPSession session) {
        Method method = session.getMethod();
        String uri = session.getUri();
        Map<String, String> headers = session.getHeaders();

        // Get parameters
        // Including application/x-www-form-urlencoded
        Map<String, List<String>> parms = session.getParameters();

        // Read http post body
        Map<String, String> files = new HashMap<String, String>();
        if (Method.PUT.equals(method) || Method.POST.equals(method)) {
            try {
                session.parseBody(files);
            } catch (IOException ioe) {
                return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, NanoHTTPD.MIME_PLAINTEXT, "SERVER INTERNAL ERROR: IOException: " + ioe.getMessage());
            } catch (ResponseException re) {
                return newFixedLengthResponse(re.getStatus(), NanoHTTPD.MIME_PLAINTEXT, re.getMessage());
            }
        }

        System.out.print("Request: " + method + " " + uri);
        System.out.print(" Parms: " + parms.toString());
        System.out.println(" Body: " + files.toString());

        // Controller
        if(uri.equals("/")) {
            return newFixedLengthResponse(Response.Status.OK, "text/plain", "DMQ Server running");
        }

        if(uri.startsWith("/DMQ/")) {
            return dmqHandler.handle(session, method, uri, headers, parms, files);
        }

        if(uri.startsWith("/api/")) {
            return neonApiHandler.handle(session, method, uri, headers, parms, files);
        }

        return staticHandler.handle(session, method, uri, headers, parms, files);
    }

}
