package org.xcyms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.xcyms.common.Constant;
import org.xcyms.entity.File;
import org.xcyms.entity.SysLog;
import org.xcyms.entity.dto.DashboardStatsDTO;
import org.xcyms.service.IFileService;
import org.xcyms.service.IStatsService;
import org.xcyms.service.ISysLogService;
import org.xcyms.service.IUserService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatsServiceImpl implements IStatsService {

    private static final DateTimeFormatter LABEL_FORMATTER = DateTimeFormatter.ofPattern("MM-dd");

    private final IUserService userService;
    private final IFileService fileService;
    private final ISysLogService sysLogService;

    @Override
    @Cacheable(value = Constant.Cache.STATS, key = "'dashboard'", unless = "#result == null")
    public DashboardStatsDTO getDashboardStats() {
        DashboardStatsDTO stats = new DashboardStatsDTO();

        stats.setUserCount(userService.count());
        stats.setFileCount(fileService.count());
        stats.setLogCount(sysLogService.count());
        stats.setTotalStorageSize(getTotalStorageSize());

        fillTrendData(stats);
        stats.setFileTypeDistribution(buildFileTypeDistribution());
        return stats;
    }

    private long getTotalStorageSize() {
        QueryWrapper<File> fileWrapper = new QueryWrapper<>();
        fileWrapper.select("sum(size) as totalSize");
        Map<String, Object> sumMap = fileService.getMap(fileWrapper);
        Object totalSize = sumMap != null ? sumMap.get("totalSize") : 0;
        return totalSize != null ? Long.parseLong(totalSize.toString()) : 0L;
    }

    /**
     * 使用按天聚合查询替代循环 count，减少仪表盘请求的 SQL 次数。
     */
    private void fillTrendData(DashboardStatsDTO stats) {
        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusDays(6);
        LocalDateTime startTime = startDate.atStartOfDay();
        LocalDateTime endTime = today.plusDays(1).atStartOfDay();

        Map<LocalDate, Long> fileTrendMap = queryFileTrend(startTime, endTime);
        Map<LocalDate, Long> activeUserTrendMap = queryActiveUserTrend(startTime, endTime);

        List<String> dateLabels = new ArrayList<>();
        List<Long> fileUploadTrend = new ArrayList<>();
        List<Long> userActiveTrend = new ArrayList<>();

        for (int i = 0; i < 7; i++) {
            LocalDate date = startDate.plusDays(i);
            dateLabels.add(date.format(LABEL_FORMATTER));
            fileUploadTrend.add(fileTrendMap.getOrDefault(date, 0L));
            userActiveTrend.add(activeUserTrendMap.getOrDefault(date, 0L));
        }

        stats.setDateLabels(dateLabels);
        stats.setFileUploadTrend(fileUploadTrend);
        stats.setUserActiveTrend(userActiveTrend);
    }

    private Map<LocalDate, Long> queryFileTrend(LocalDateTime startTime, LocalDateTime endTime) {
        QueryWrapper<File> wrapper = new QueryWrapper<>();
        wrapper.select("DATE(create_time) as statDate", "COUNT(*) as total")
                .between("create_time", startTime, endTime)
                .groupBy("DATE(create_time)")
                .orderByAsc("DATE(create_time)");

        return fileService.listMaps(wrapper).stream()
                .collect(Collectors.toMap(
                        item -> LocalDate.parse(String.valueOf(item.get("statDate"))),
                        item -> Long.parseLong(String.valueOf(item.get("total"))),
                        (left, right) -> right,
                        LinkedHashMap::new
                ));
    }

    private Map<LocalDate, Long> queryActiveUserTrend(LocalDateTime startTime, LocalDateTime endTime) {
        QueryWrapper<SysLog> wrapper = new QueryWrapper<>();
        wrapper.select("DATE(create_time) as statDate", "COUNT(DISTINCT user_id) as total")
                .between("create_time", startTime, endTime)
                .isNotNull("user_id")
                .groupBy("DATE(create_time)")
                .orderByAsc("DATE(create_time)");

        return sysLogService.listMaps(wrapper).stream()
                .collect(Collectors.toMap(
                        item -> LocalDate.parse(String.valueOf(item.get("statDate"))),
                        item -> Long.parseLong(String.valueOf(item.get("total"))),
                        (left, right) -> right,
                        LinkedHashMap::new
                ));
    }

    private List<Map<String, Object>> buildFileTypeDistribution() {
        QueryWrapper<File> typeWrapper = new QueryWrapper<>();
        typeWrapper.select("type", "count(*) as count")
                .groupBy("type")
                .orderByDesc("count");

        List<Map<String, Object>> typeList = fileService.listMaps(typeWrapper);
        List<Map<String, Object>> distribution = new ArrayList<>();
        long otherCount = 0;

        for (int i = 0; i < typeList.size(); i++) {
            Map<String, Object> item = typeList.get(i);
            long count = Long.parseLong(String.valueOf(item.get("count")));
            String typeName = String.valueOf(item.get("type"));

            if (i < 5) {
                Map<String, Object> result = new HashMap<>();
                result.put("name", typeName);
                result.put("value", count);
                distribution.add(result);
            } else {
                otherCount += count;
            }
        }

        if (otherCount > 0) {
            Map<String, Object> other = new HashMap<>();
            other.put("name", "其他");
            other.put("value", otherCount);
            distribution.add(other);
        }

        return distribution;
    }
}
