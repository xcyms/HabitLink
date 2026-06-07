package org.xcyms.common;

import lombok.Getter;

/**
 * <p>
 *     公共常量类
 * </p>
 * @author liu-xu
 * @date 2026年01月11日 15:31
 */
@Getter
public class Constant {

    public static final String SALT = "universal_starter_salt";

    public static final String UPLOAD_ROOT_PATH = "/uploads/";

    /**
     * 配置键常量
     */
    public interface ConfigKey {
        String MAX_FILE_SIZE = "max_file_size";
        String ALLOWED_EXTENSIONS = "allowed_extensions";
        String USER_UPLOAD_DIR = "user_upload_dir";
        String UPLOAD_PATH = "upload_path";
        String STORAGE_TYPE = "storage_type";
    }

    /**
     * 缓存常量
     */
    public interface Cache {
        String CONFIG = "config";
        String DICT = "dict";
        String DICT_DATA = "dict:data";
        String STATS = "stats";

        Long EXPIRE_7_DAYS = 60 * 60 * 24 * 7L;

        Long EXPIRE_1_HOUR = 60 * 60L;

        Long EXPIRE_30_MINUTES = 60 * 30L;

        Long EXPIRE_1_MINUTE = 60L;

        Long EXPIRE_5_SECONDS = 5L;

        Long EXPIRE_NONE = -1L;
    }

    /**
     * 角色常量
     */
    public interface Role {
        Long ADMIN = 1L;
        Long USER = 2L;
    }

    public interface File {
        String FILE = "file";
        String AVATAR = "avatar";
    }
}
