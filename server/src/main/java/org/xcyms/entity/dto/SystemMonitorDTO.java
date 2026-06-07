package org.xcyms.entity.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 系统监控数据传输对象
 */
@Data
public class SystemMonitorDTO {
    
    /**
     * 系统信息
     */
    private SystemInfo systemInfo;
    
    /**
     * JVM信息
     */
    private JvmInfo jvmInfo;
    
    /**
     * 内存信息
     */
    private MemoryInfo memoryInfo;
    
    /**
     * 磁盘信息
     */
    private List<DiskInfo> diskInfos;
    
    /**
     * 运行时信息
     */
    private RuntimeInfo runtimeInfo;
    
    /**
     * 系统信息
     */
    @Data
    public static class SystemInfo {
        private String osName;
        private String osVersion;
        private String osArch;
        private String userHome;
        private String userDir;
        private String javaVersion;
        private String javaVendor;
        private String javaHome;
        private String hostName;
        private String hostAddress;
    }
    
    /**
     * JVM信息
     */
    @Data
    public static class JvmInfo {
        private String jvmName;
        private String jvmVersion;
        private String jvmVendor;
        private String jvmSpecVersion;
        private Long totalMemory;
        private Long freeMemory;
        private Long maxMemory;
        private Integer availableProcessors;
        private String jvmArgs;
        private Map<String, String> systemProperties;
    }
    
    /**
     * 内存信息
     */
    @Data
    public static class MemoryInfo {
        private Long totalMemory;
        private Long freeMemory;
        private Long maxMemory;
        private Long usedMemory;
        private Double memoryUsageRate;
        private Long totalSwap;
        private Long freeSwap;
        private Long usedSwap;
        private Double swapUsageRate;
    }
    
    /**
     * 磁盘信息
     */
    @Data
    public static class DiskInfo {
        private String fileSystem;
        private String mountPoint;
        private Long totalSpace;
        private Long freeSpace;
        private Long usableSpace;
        private Long usedSpace;
        private Double usageRate;
    }
    
    /**
     * 运行时信息
     */
    @Data
    public static class RuntimeInfo {
        private Long uptime;
        private Long startTime;
        private Integer threadCount;
        private Integer peakThreadCount;
        private Long totalStartedThreadCount;
        private Long heapMemoryUsed;
        private Long heapMemoryCommitted;
        private Long heapMemoryMax;
        private Long nonHeapMemoryUsed;
        private Long nonHeapMemoryCommitted;
        private Long nonHeapMemoryMax;
    }
}