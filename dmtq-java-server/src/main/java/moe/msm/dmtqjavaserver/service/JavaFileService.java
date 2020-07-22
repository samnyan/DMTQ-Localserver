package moe.msm.dmtqjavaserver.service;

import moe.msm.dmtqserver.external.StaticFileService;

import java.io.File;

/**
 * @author sam_nya (privateamusement@protonmail.com)
 */
public class JavaFileService implements StaticFileService {

    private final String dir;
    public JavaFileService() {
        dir = System.getProperty("user.dir");
    }

    @Override
    public File getFileByPath(String path) {
        return new File(dir, path);
    }
}
