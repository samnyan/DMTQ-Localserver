package moe.msm.dmtqserver.handler;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import fi.iki.elonen.NanoHTTPD;
import moe.msm.dmtqserver.BaseHandler;
import moe.msm.dmtqserver.external.StaticFileService;
import moe.msm.dmtqserver.model.ServerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static fi.iki.elonen.NanoHTTPD.newFixedLengthResponse;

public class StaticHandler implements BaseHandler {

    private static final Logger logger = LoggerFactory.getLogger(StaticHandler.class);

    private final ServerConfig config;
    private final StaticFileService fileService;

    public StaticHandler(ServerConfig config, StaticFileService fileService) {
        this.config = config;
        this.fileService = fileService;
    }

    @Override
    public NanoHTTPD.Response handle(NanoHTTPD.IHTTPSession session, NanoHTTPD.Method method, String uri, Map<String, String> headers, Map<String, List<String>> parms, Map<String, String> body) {
        if(!isExternalStorageWritable()) {
            throw new RuntimeException("NO PERMISSION TO READ FILE");
        }

        try {
            if (uri.startsWith("/Patterns")) {
                return patternReader(uri);
            }
            if (uri.startsWith("/score")) {
                return scoreHandler(uri);
            }

            // Read other file
            return readStaticFile(uri);

        } catch (IOException e) {
            return newFixedLengthResponse(NanoHTTPD.Response.Status.NOT_FOUND, "text/html", "NOT_FOUND");
        }
    }

    private NanoHTTPD.Response patternReader(String uri) throws IOException {
        String filename = uri.substring(uri.lastIndexOf("/") + 1);

        // try zip file
        try {
            ZipFile zip = new ZipFile(fileService.getFileByPath("Patterns.zip"));
            ZipEntry file = zip.getEntry(filename);
            if(file!=null) {
                logger.info("Read {} from pattern zip", filename);
                InputStream i = zip.getInputStream(file);
                return newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "application/octet-stream", i, i.available());
            } else {
                throw new FileNotFoundException("File " + filename + " not found in zip");
            }
        } catch (IOException e) {
            logger.error("Zip Read Error", e);
        }

        // try read file
        return readStaticFile(uri);
    }

    public NanoHTTPD.Response readStaticFile(String uri) throws IOException {
        File f = fileService.getFileByPath(uri);
        logger.info("Read static file {}", f.getAbsolutePath());
        if(f.exists()) {
            FileInputStream i = new FileInputStream(f);
            return newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "application/octet-stream", i, i.available());
        } else {
            throw new FileNotFoundException();
        }
    }

    public NanoHTTPD.Response scoreHandler(String uri) {
        return newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "text/html", "Server Working");
    }

    private boolean isExternalStorageWritable() {
        // TODO: general
        return true;
    }
}
