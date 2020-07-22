package icu.samnya.dmtq_server.service;

import android.content.Context;
import moe.msm.dmtqserver.external.StaticFileService;

import java.io.File;

/**
 * @author sam_nya (privateamusement@protonmail.com)
 */
public class AndroidStaticFileService implements StaticFileService {

    private final File dir;

    public AndroidStaticFileService(Context ctx) {
        dir = ctx.getExternalFilesDir("");
    }

    @Override
    public File getFileByPath(String path) {
        return new File(dir, path);
    }
}
