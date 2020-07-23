package moe.msm.dmtqserver;

import fi.iki.elonen.NanoHTTPD;
import moe.msm.dmtqserver.external.GameDatabaseService;
import moe.msm.dmtqserver.external.StaticFileService;
import moe.msm.dmtqserver.handler.DMQHandler;
import moe.msm.dmtqserver.handler.NeonApiHandler;
import moe.msm.dmtqserver.handler.StaticHandler;
import moe.msm.dmtqserver.model.ServerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameServer extends NanoHTTPD {

    private static final Logger logger = LoggerFactory.getLogger(GameServer.class);

    public static void main(String[] args) {
        System.out.println("OK");
    }

    private final DMQHandler dmqHandler;
    private final NeonApiHandler neonApiHandler;
    private final StaticHandler staticHandler;

    private final ServerConfig config;
    private final GameDatabaseService dbService;
    private final StaticFileService fileService;

    public GameServer(ServerConfig config, GameDatabaseService dbService, StaticFileService fileService) {
        super(config.getHostname(), config.getPort());
        this.config = config;
        // Create database
        this.dbService = dbService;
        this.fileService = fileService;
        this.dmqHandler = new DMQHandler(config, this.dbService, this.fileService);
        this.neonApiHandler = new NeonApiHandler(config, this.dbService);
        this.staticHandler = new StaticHandler(config, this.fileService);
    }

    @Override
    protected boolean useGzipWhenAccepted(NanoHTTPD.Response r) {
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
        Map<String, List<String>> params = session.getParameters();

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

        logger.info("Request: {} {} Params: {} Body: {}", method, uri, params, files);

        // Controller
        if(uri.equals("/")) {
            StringBuilder html = new StringBuilder();
            html.append("DMQ Server running<br>");
            html.append("Cert download here:<a href=\"rootCa.crt\">rootCa.crt</a> <br>");
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
            String proxy = config.getProxyAddress();
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
            return dmqHandler.handle(session, method, uri, headers, params, files);
        }

        if(uri.startsWith("/api/")) {
            return neonApiHandler.handle(session, method, uri, headers, params, files);
        }

        return staticHandler.handle(session, method, uri, headers, params, files);
    }
}