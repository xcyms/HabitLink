package org.xcyms.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.xcyms.common.ApiResult;
import org.xcyms.common.enums.HabitStatusEnum;
import org.xcyms.entity.CheckInRecord;
import org.xcyms.entity.DailyUserSummary;
import org.xcyms.entity.Habit;
import org.xcyms.entity.HabitScheduleRule;
import org.xcyms.entity.HabitStats;
import org.xcyms.entity.dto.HabitStatsDTO;
import org.xcyms.entity.dto.TodayHabitDTO;
import org.xcyms.entity.dto.TodayOverviewDTO;
import org.xcyms.mapper.CheckInRecordMapper;
import org.xcyms.mapper.DailyUserSummaryMapper;
import org.xcyms.mapper.HabitMapper;
import org.xcyms.mapper.HabitScheduleRuleMapper;
import org.xcyms.mapper.HabitStatsMapper;
import org.xcyms.service.IHabitStatsService;
import org.xcyms.utils.HabitRuleUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 首页总览与习惯统计服务实现类。
 */
@Service
@RequiredArgsConstructor
public class HabitStatsServiceImpl extends ServiceImpl<HabitStatsMapper, HabitStats> implements IHabitStatsService {

    private final HabitMapper habitMapper;
    private final HabitScheduleRuleMapper habitScheduleRuleMapper;
    private final CheckInRecordMapper checkInRecordMapper;
    private final DailyUserSummaryMapper dailyUserSummaryMapper;

    @Override
    public ApiResult<TodayOverviewDTO> getTodayOverview() {
        Long userId = StpUtil.getLoginIdAsLong();
        LocalDate today = LocalDate.now();

        List<Habit> habits = habitMapper.selectList(new LambdaQueryWrapper<Habit>()
                .eq(Habit::getUserId, userId)
                .eq(Habit::getStatus, HabitStatusEnum.ACTIVE.getCode())
                .orderByAsc(Habit::getSortOrder)
                .orderByDesc(Habit::getId));

        List<Long> habitIds = habits.stream().map(Habit::getId).toList();
        Map<Long, HabitScheduleRule> ruleMap = habitScheduleRuleMapper.selectList(new LambdaQueryWrapper<HabitScheduleRule>()
                        .in(!habitIds.isEmpty(), HabitScheduleRule::getHabitId, habitIds))
                .stream()
                .collect(Collectors.toMap(HabitScheduleRule::getHabitId, item -> item, (left, right) -> left));
        Map<Long, HabitStats> statsMap = this.list(new LambdaQueryWrapper<HabitStats>()
                        .in(!habitIds.isEmpty(), HabitStats::getHabitId, habitIds))
                .stream()
                .collect(Collectors.toMap(HabitStats::getHabitId, item -> item, (left, right) -> left));
        Set<Long> checkedHabitIds = checkInRecordMapper.selectList(new LambdaQueryWrapper<CheckInRecord>()
                        .eq(CheckInRecord::getUserId, userId)
                        .eq(CheckInRecord::getRecordDate, today)
                        .in(!habitIds.isEmpty(), CheckInRecord::getHabitId, habitIds))
                .stream()
                .map(CheckInRecord::getHabitId)
                .collect(Collectors.toSet());

        List<TodayHabitDTO> habitCards = habits.stream()
                .filter(habit -> HabitRuleUtils.isScheduledOnDate(habit, ruleMap.get(habit.getId()), today))
                .map(habit -> {
                    TodayHabitDTO dto = new TodayHabitDTO();
                    dto.setHabitId(habit.getId());
                    dto.setHabitName(habit.getName());
                    dto.setIcon(habit.getIcon());
                    dto.setColor(habit.getColor());
                    dto.setCategory(habit.getCategory());
                    dto.setReminderTime(habit.getReminderTime());
                    dto.setCurrentStreak(statsMap.get(habit.getId()) == null ? 0 : statsMap.get(habit.getId()).getCurrentStreak());
                    dto.setTodayChecked(checkedHabitIds.contains(habit.getId()) ? 1 : 0);
                    dto.setTodayDate(today);
                    dto.setRuleText(HabitRuleUtils.formatRuleText(ruleMap.get(habit.getId())));
                    return dto;
                })
                .toList();

        int plannedCount = habitCards.size();
        int completedCount = (int) habitCards.stream()
                .filter(item -> item.getTodayChecked() != null && item.getTodayChecked() == 1)
                .count();
        BigDecimal completionRate = plannedCount == 0
                ? BigDecimal.ZERO
                : BigDecimal.valueOf(completedCount * 100.0 / plannedCount).setScale(2, RoundingMode.HALF_UP);

        DailyUserSummary summary = dailyUserSummaryMapper.selectOne(new LambdaQueryWrapper<DailyUserSummary>()
                .eq(DailyUserSummary::getUserId, userId)
                .eq(DailyUserSummary::getSummaryDate, today));
        if (summary == null) {
            summary = new DailyUserSummary();
            summary.setUserId(userId);
            summary.setSummaryDate(today);
        }
        summary.setPlannedCount(plannedCount);
        summary.setCompletedCount(completedCount);
        summary.setCompletionRate(completionRate);
        if (summary.getId() == null) {
            dailyUserSummaryMapper.insert(summary);
        }
        else {
            dailyUserSummaryMapper.updateById(summary);
        }

        TodayOverviewDTO overviewDTO = new TodayOverviewDTO();
        overviewDTO.setDate(today);
        overviewDTO.setPlannedCount(summary.getPlannedCount());
        overviewDTO.setCompletedCount(summary.getCompletedCount());
        overviewDTO.setCompletionRate(summary.getCompletionRate());
        overviewDTO.setHabits(habitCards);
        return ApiResult.success(overviewDTO);
    }

    @Override
    public ApiResult<HabitStatsDTO> getHabitStats(Long habitId) {
        if (habitId == null) {
            return ApiResult.error("习惯ID不能为空");
        }

        Long userId = StpUtil.getLoginIdAsLong();
        Habit habit = habitMapper.selectById(habitId);
        if (habit == null || !habit.getUserId().equals(userId)) {
            return ApiResult.error("习惯不存在");
        }

        HabitScheduleRule rule = habitScheduleRuleMapper.selectOne(new LambdaQueryWrapper<HabitScheduleRule>()
                .eq(HabitScheduleRule::getHabitId, habitId));
        if (rule == null) {
            return ApiResult.error("习惯规则不存在");
        }

        List<CheckInRecord> records = checkInRecordMapper.selectList(new LambdaQueryWrapper<CheckInRecord>()
                .eq(CheckInRecord::getUserId, userId)
                .eq(CheckInRecord::getHabitId, habitId)
                .orderByAsc(CheckInRecord::getRecordDate));
        Set<LocalDate> completedDates = records.stream()
                .map(CheckInRecord::getRecordDate)
                .collect(Collectors.toSet());

        int currentStreak = HabitRuleUtils.calculateCurrentStreak(habit, rule, completedDates);
        int longestStreak = HabitRuleUtils.calculateLongestStreak(habit, rule, completedDates);
        int totalCheckInCount = records.size();
        LocalDate lastCheckInDate = records.isEmpty() ? null : records.get(records.size() - 1).getRecordDate();

        upsertStatsSnapshot(userId, habitId, currentStreak, longestStreak, totalCheckInCount, lastCheckInDate);

        HabitStatsDTO dto = new HabitStatsDTO();
        dto.setHabitId(habitId);
        dto.setHabitName(habit.getName());
        dto.setCurrentStreak(currentStreak);
        dto.setLongestStreak(longestStreak);
        dto.setTotalCheckInCount(totalCheckInCount);
        dto.setCompletionRate7d(calculateCompletionRate(habit, rule, completedDates, LocalDate.now().minusDays(6), LocalDate.now()));
        dto.setCompletionRate30d(calculateCompletionRate(habit, rule, completedDates, LocalDate.now().minusDays(29), LocalDate.now()));
        dto.setLastCheckInDate(lastCheckInDate);
        return ApiResult.success(dto);
    }

    /**
     * 计算时间区间内的完成率。
     */
    private BigDecimal calculateCompletionRate(Habit habit, HabitScheduleRule rule, Set<LocalDate> completedDates,
                                               LocalDate start, LocalDate end) {
        int plannedCount = HabitRuleUtils.countScheduledDays(habit, rule, start, end);
        if (plannedCount == 0) {
            return BigDecimal.ZERO;
        }

        long completedCount = HabitRuleUtils.listScheduledDates(habit, rule, start, end).stream()
                .filter(completedDates::contains)
                .count();
        return BigDecimal.valueOf(completedCount * 100.0 / plannedCount).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * 同步更新统计快照表。
     */
    private void upsertStatsSnapshot(Long userId, Long habitId, int currentStreak, int longestStreak,
                                     int totalCheckInCount, LocalDate lastCheckInDate) {
        HabitStats stats = this.getOne(new LambdaQueryWrapper<HabitStats>()
                .eq(HabitStats::getHabitId, habitId));
        if (stats == null) {
            stats = new HabitStats();
            stats.setUserId(userId);
            stats.setHabitId(habitId);
        }
        stats.setCurrentStreak(currentStreak);
        stats.setLongestStreak(longestStreak);
        stats.setTotalCheckInCount(totalCheckInCount);
        stats.setLastCheckInDate(lastCheckInDate);
        stats.setLastComputedAt(LocalDateTime.now());

        if (stats.getId() == null) {
            this.save(stats);
        }
        else {
            this.updateById(stats);
        }
    }
}
