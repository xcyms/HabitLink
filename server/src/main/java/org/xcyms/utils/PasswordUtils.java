package org.xcyms.utils;

import java.util.regex.Pattern;

/**
 * 密码工具类
 */
public class PasswordUtils {

    /**
     * 密码复杂度校验正则：
     * 1. 至少8个字符
     * 2. 至少1个大写字母
     * 3. 至少1个小写字母
     * 4. 至少1个数字
     * 5. 至少1个特殊字符 (@$!%*?&._-)
     */
    private static final String PASSWORD_PATTERN = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&._-])[A-Za-z\\d@$!%*?&._-]{8,}$";
    private static final Pattern PATTERN = Pattern.compile(PASSWORD_PATTERN);

    /**
     * 校验密码复杂度
     * @param password 原始密码
     * @return true-符合要求，false-不符合要求
     */
    public static boolean checkComplexity(String password) {
        if (password == null) {
            return true;
        }
        return !PATTERN.matcher(password).matches();
    }
}