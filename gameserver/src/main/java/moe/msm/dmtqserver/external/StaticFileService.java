package moe.msm.dmtqserver.external;

import java.io.File;

/**
 * @author sam_nya (privateamusement@protonmail.com)
 */
public interface StaticFileService {
    File getFileByPath(String path);
}
