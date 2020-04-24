package icu.samnya.dmtq_server.server;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD;
import icu.samnya.dmtq_server.R;
import icu.samnya.dmtq_server.server.handler.DMQHandler;
import icu.samnya.dmtq_server.server.handler.NeonApiHandler;
import icu.samnya.dmtq_server.server.handler.StaticHandler;

public class GameServer extends NanoHTTPD {

    private final DMQHandler dmqHandler;
    private final NeonApiHandler neonApiHandler;
    private final StaticHandler staticHandler;

    private final DatabaseService dbService;

    private final SQLiteDatabase db;

    private Context ctx;

    public GameServer(int port, Context ctx, SQLiteDatabase db) {
        super("0.0.0.0", port);
        // Create database
        this.ctx = ctx;
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
            StringBuilder html = new StringBuilder();
            html.append("DMQ Server running<br>");
            html.append("Cert download here:<a href=\"ca.crt\">ca.crt</a> <br>");
            html.append("PAC(Proxy auto config) URL here:<a href=\"pac\">LONG PRESS TO COPY</a> <br>");
            html.append("<br>");
            html.append("<br>");
            html.append("After you click the ca.crt, press allow <br>");
            html.append("Then open setting app on iOS, you will see a downloaded profile on top, follow it to install <br>");
            html.append("iOS 10.3 and later need to manually trust certificate <br>");
            html.append("go to Settings > General > About > Certificate Trust Settings <br>");
            html.append("Then enable the Certificate just install <br>");
            html.append("For detailed info: <a href=\"https://support.apple.com/en-us/HT204477\">https://support.apple.com/en-us/HT204477</a> <br>");
            html.append("<br>");
            html.append("<br>");
            html.append("How to setup proxy: <br>");
            html.append("You must set the correct Proxy Server Address to use PAC mode. <br>");
            html.append("You must set the correct HTTP Server Address to the Server's IP Address:Port to use on iOS. <br>");
            html.append("<br>");
            html.append("Go to <br>");
            return newFixedLengthResponse(Response.Status.OK, "text/html", html.toString());
        }

        if(uri.equalsIgnoreCase("/pac") || uri.equalsIgnoreCase("/pac/")) {
            SharedPreferences sharedPref = ctx.getSharedPreferences("SERVER_PREFERENCES", Context.MODE_PRIVATE);
            String proxy = sharedPref.getString("PROXY_ADDRESS", "localhost:3457");
            StringBuilder pac = new StringBuilder();
            proxy = "\"PROXY " + proxy + "\"";
            pac.append("function FindProxyForURL(url, host) {\n");
            pac.append("if (shExpMatch(url,\"*pmang.com*\")) {return ").append(proxy).append(";}\n");
            pac.append("if (shExpMatch(url,\"*pmangplus.com*\")) {return ").append(proxy).append(";}\n");
            pac.append("if (shExpMatch(url,\"*neonapi.com*\")) {return ").append(proxy).append(";}\n");
            pac.append("return \"DIRECT\";\n");
            pac.append("}\n");

            return newFixedLengthResponse(Response.Status.OK, "application/x-ns-proxy-autoconfig", pac.toString());
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
