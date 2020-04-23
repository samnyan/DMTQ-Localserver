package icu.samnya.dmtq_server.server.handler;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

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
import icu.samnya.dmtq_server.server.BaseHandler;

import static fi.iki.elonen.NanoHTTPD.newFixedLengthResponse;

public class StaticHandler implements BaseHandler {

    Context ctx;

    public StaticHandler(Context ctx) {
        this.ctx = ctx;
    }

    @Override
    public NanoHTTPD.Response handle(NanoHTTPD.IHTTPSession session, NanoHTTPD.Method method, String uri, Map<String, String> headers, Map<String, List<String>> parms, Map<String, String> body) {
        if(!isExternalStorageWritable()) {
            throw new RuntimeException("NO PERMISSION TO READ FILE");
        }
        File dir = ctx.getExternalFilesDir("");

        try {
            if (uri.startsWith("/Patterns")) {
                return patternReader(dir, uri);
            }
            if (uri.startsWith("/score")) {
                return scoreHandler(dir, uri);
            }

            // Read other file
            return readStaticFile(dir, uri);

        } catch (IOException e) {
            return newFixedLengthResponse(NanoHTTPD.Response.Status.NOT_FOUND, "text/html", "NOT_FOUND");
        }
    }

    private NanoHTTPD.Response patternReader(File dir, String uri) throws IOException {
        String filename = uri.substring(uri.lastIndexOf("/") + 1);

        // try zip file
        try {
            ZipFile zip = new ZipFile(new File(dir, "Patterns.zip"));
            ZipEntry file = zip.getEntry(filename);
            if(file!=null) {
                Log.i("PatternReader", "Read " + filename + " from pattern zip");
                InputStream i = zip.getInputStream(file);
                return newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "application/octet-stream", i, i.available());
            } else {
                throw new FileNotFoundException("File " + filename + " not found in zip");
            }
        } catch (IOException e) {
            Log.e("PatternReader", "Zip Read Error", e);
        }

        // try read file
        return readStaticFile(dir, uri);
    }

    public NanoHTTPD.Response readStaticFile(File dir, String uri) throws IOException {
        File f = new File(dir, uri);
        Log.i("StaticFileReader", f.getAbsolutePath());
        if(f.exists()) {
            FileInputStream i = new FileInputStream(f);
            return newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "application/octet-stream", i, i.available());
        } else {
            throw new FileNotFoundException();
        }
    }

    public NanoHTTPD.Response scoreHandler(File dir, String uri) {
        return newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "text/html", "Server Working");
    }

    private boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }
}
