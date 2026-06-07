package org.xcyms.utils;

import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;

/**
 * 全局唯一ID生成器
 * 封装 Hutool 的 ID 工具，提供比 UUID 更简洁、有序的 ID 生成方案
 */
public class IdGenerator {

    // 使用单例模式获取雪花算法对象，workerId 和 datacenterId 默认从配置或网卡读取
    private static final Snowflake snowflake = IdUtil.getSnowflake(1, 1);

    /**
     * 获取下一个雪花算法ID (数字型)
     * 优点：趋势递增、查询效率高、比UUID短
     * 示例：1748239485739212800
     */
    public static long nextId() {
        return snowflake.nextId();
    }

    /**
     * 获取下一个雪花算法ID的字符串形式
     */
    public static String nextIdStr() {
        return snowflake.nextIdStr();
    }

    /**
     * 获取 NanoID (紧凑型字符串)
     * 优点：比 UUID 短（默认21位），URL友好，完全随机
     * 示例：V1StG_c7pS60936tPpxId
     */
    public static String nanoId() {
        return IdUtil.nanoId();
    }

    /**
     * 获取自定义长度的 NanoID
     */
    public static String nanoId(int size) {
        return IdUtil.nanoId(size);
    }
}