package org.xcyms.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.xcyms.common.ApiResult;
import org.xcyms.common.enums.CheckInSourceEnum;
import org.xcyms.common.enums.CheckInStatusEnum;
import org.xcyms.common.enums.HabitStatusEnum;
import org.xcyms.entity.CheckInRecord;
import org.xcyms.entity.DailyUserSummary;
import org.xcyms.entity.Habit;
import org.xcyms.entity.HabitScheduleRule;
import org.xcyms.entity.HabitStats;
import org.xcyms.entity.dto.CheckInDTO;
import org.xcyms.entity.dto.CheckInRecordDTO;
import org.xcyms.mapper.CheckInRecordMapper;
import org.xcyms.mapper.DailyUserSummaryMapper;
import org.xcyms.mapper.HabitMapper;
import org.xcyms.mapper.HabitScheduleRuleMapper;
import org.xcyms.mapper.HabitStatsMapper;
import org.xcyms.service.ICheckInService;
import org.xcyms.utils.HabitRuleUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 习惯打卡服务实现类。
 */
@Service
@RequiredArgsConstructor
public class CheckInServiceImpl extends ServiceImpl<CheckInRecordMapper, CheckInRecord> implements ICheckInService {

    private final HabitMapper habitMapper;
    private final HabitScheduleRuleMapper habitScheduleRuleMapper;
    private final HabitStatsMapper habitStatsMapper;
    private final DailyUserSummaryMapper dailyUserSummaryMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResult<String> submit(CheckInDTO checkInDTO) {
        return createRecord(checkInDTO, false);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResult<String> makeup(CheckInDTO checkInDTO) {
        return createRecord(checkInDTO, true);
    }

    @Override
    public ApiResult<List<CheckInRecordDTO>> getRecordsByHabit(Long habitId) {
        if (habitId == null) {
            return ApiResult.error("习惯ID不能为空");
        }

        Long userId = StpUtil.getLoginIdAsLong();
        List<CheckInRecord> records = this.list(new LambdaQueryWrapper<CheckInRecord>()
                .eq(CheckInRecord::getUserId, userId)
                .eq(CheckInRecord::getHabitId, habitId)
                .orderByDesc(CheckInRecord::getRecordDate)
                .orderByDesc(CheckInRecord::getId));

        Habit habit = habitMapper.selectById(habitId);
        String habitName = habit == null ? null : habit.getName();

        return ApiResult.success(records.stream().map(record -> {
            CheckInRecordDTO dto = new CheckInRecordDTO();
            dto.setId(record.getId());
            dto.setHabitId(record.getHabitId());
            dto.setHabitName(habitName);
            dto.setRecordDate(record.getRecordDate());
            dto.setCheckInTime(record.getCheckInTime());
            dto.setStatus(record.getStatus());
            dto.setNote(record.getNote());
            dto.setIsMakeup(record.getIsMakeup());
            dto.setSource(record.getSource());
            return dto;
        }).collect(Collectors.toList()));
    }

    /**
     * 普通打卡和补打卡共用的记录创建逻辑。
     */
    private ApiResult<String> createRecord(CheckInDTO checkInDTO, boolean forceMakeup) {
        if (checkInDTO == null || checkInDTO.getHabitId() == null) {
            return ApiResult.error("习惯ID不能为空");
        }
        if (checkInDTO.getRecordDate() == null) {
            return ApiResult.error("打卡日期不能为空");
        }

        Long userId = StpUtil.getLoginIdAsLong();
        LocalDate today = LocalDate.now();
        Habit habit = habitMapper.selectById(checkInDTO.getHabitId());
        if (habit == null || !habit.getUserId().equals(userId)) {
            return ApiResult.error("习惯不存在");
        }
        if (checkInDTO.getRecordDate().isAfter(today)) {
            return ApiResult.error("不能对未来日期进行打卡");
        }

        HabitScheduleRule rule = habitScheduleRuleMapper.selectOne(new LambdaQueryWrapper<HabitScheduleRule>()
                .eq(HabitScheduleRule::getHabitId, checkInDTO.getHabitId()));
        if (rule == null) {
            return ApiResult.error("习惯规则不存在");
        }
        if (!HabitRuleUtils.isWithinActiveRange(habit, checkInDTO.getRecordDate())) {
            return ApiResult.error("打卡日期不在习惯有效期内");
        }
        if (!HabitRuleUtils.isScheduledOnDate(habit, rule, checkInDTO.getRecordDate())) {
            return ApiResult.error("该日期不是习惯的应执行日");
        }

        boolean isMakeup = forceMakeup || checkInDTO.getRecordDate().isBefore(today);
        if (isMakeup) {
            if (habit.getAllowMakeup() == null || habit.getAllowMakeup() != 1) {
                return ApiResult.error("该习惯未开启补打卡");
            }
            long daysBetween = ChronoUnit.DAYS.between(checkInDTO.getRecordDate(), today);
            int limitDays = habit.getMakeupLimitDays() == null ? 0 : habit.getMakeupLimitDays();
            if (daysBetween > limitDays) {
                return ApiResult.error("已超过允许补打卡的天数范围");
            }
        }

        CheckInRecord existing = this.getOne(new LambdaQueryWrapper<CheckInRecord>()
                .eq(CheckInRecord::getUserId, userId)
                .eq(CheckInRecord::getHabitId, checkInDTO.getHabitId())
                .eq(CheckInRecord::getRecordDate, checkInDTO.getRecordDate()));
        if (existing != null) {
            return ApiResult.error("该习惯在目标日期已完成打卡");
        }

        CheckInRecord record = new CheckInRecord();
        record.setUserId(userId);
        record.setHabitId(checkInDTO.getHabitId());
        record.setRecordDate(checkInDTO.getRecordDate());
        record.setCheckInTime(LocalDateTime.now());
        record.setStatus(CheckInStatusEnum.COMPLETED.getCode());
        record.setNote(checkInDTO.getNote());
        record.setSource(CheckInSourceEnum.MINI_PROGRAM.getCode());
        record.setIsMakeup(isMakeup ? 1 : 0);
        this.save(record);

        if (habit.getLastCheckInDate() == null || checkInDTO.getRecordDate().isAfter(habit.getLastCheckInDate())) {
            habit.setLastCheckInDate(checkInDTO.getRecordDate());
        }
        habitMapper.updateById(habit);

        refreshHabitStats(userId, habit, rule);
        refreshDailySummary(userId, checkInDTO.getRecordDate());
        return ApiResult.success(isMakeup ? "补打卡成功" : "打卡成功");
    }

    /**
     * 刷新某个习惯的统计快照。
     */
    private void refreshHabitStats(Long userId, Habit habit, HabitScheduleRule rule) {
        List<CheckInRecord> records = this.list(new LambdaQueryWrapper<CheckInRecord>()
                .eq(CheckInRecord::getUserId, userId)
                .eq(CheckInRecord::getHabitId, habit.getId())
                .orderByAsc(CheckInRecord::getRecordDate));
        Set<LocalDate> completedDates = records.stream()
                .map(CheckInRecord::getRecordDate)
                .collect(Collectors.toSet());

        HabitStats stats = habitStatsMapper.selectOne(new LambdaQueryWrapper<HabitStats>()
                .eq(HabitStats::getHabitId, habit.getId()));
        if (stats == null) {
            stats = new HabitStats();
            stats.setUserId(userId);
            stats.setHabitId(habit.getId());
        }

        stats.setCurrentStreak(HabitRuleUtils.calculateCurrentStreak(habit, rule, completedDates));
        stats.setLongestStreak(HabitRuleUtils.calculateLongestStreak(habit, rule, completedDates));
        stats.setTotalCheckInCount(records.size());
        stats.setLastCheckInDate(records.isEmpty() ? null : records.get(records.size() - 1).getRecordDate());
        stats.setLastComputedAt(LocalDateTime.now());

        if (stats.getId() == null) {
            habitStatsMapper.insert(stats);
        }
        else {
            habitStatsMapper.updateById(stats);
        }
    }

    /**
     * 刷新某一天的用户汇总快照。
     */
    private void refreshDailySummary(Long userId, LocalDate summaryDate) {
        List<Habit> habits = habitMapper.selectList(new LambdaQueryWrapper<Habit>()
                .eq(Habit::getUserId, userId)
                .eq(summaryDate.equals(LocalDate.now()), Habit::getStatus, HabitStatusEnum.ACTIVE.getCode()));
        if (habits.isEmpty()) {
            return;
        }

        List<Long> habitIds = habits.stream().map(Habit::getId).toList();
        Map<Long, HabitScheduleRule> ruleMap = habitScheduleRuleMapper.selectList(new LambdaQueryWrapper<HabitScheduleRule>()
                        .in(!habitIds.isEmpty(), HabitScheduleRule::getHabitId, habitIds))
                .stream()
                .collect(Collectors.toMap(HabitScheduleRule::getHabitId, item -> item, (left, right) -> left));

        int plannedCount = (int) habits.stream()
                .filter(habit -> HabitRuleUtils.isScheduledOnDate(habit, ruleMap.get(habit.getId()), summaryDate))
                .count();
        int completedCount = (int) this.list(new LambdaQueryWrapper<CheckInRecord>()
                        .eq(CheckInRecord::getUserId, userId)
                        .eq(CheckInRecord::getRecordDate, summaryDate))
                .stream()
                .map(CheckInRecord::getHabitId)
                .distinct()
                .count();

        BigDecimal completionRate = plannedCount == 0
                ? BigDecimal.ZERO
                : BigDecimal.valueOf(completedCount * 100.0 / plannedCount).setScale(2, RoundingMode.HALF_UP);

        DailyUserSummary summary = dailyUserSummaryMapper.selectOne(new LambdaQueryWrapper<DailyUserSummary>()
                .eq(DailyUserSummary::getUserId, userId)
                .eq(DailyUserSummary::getSummaryDate, summaryDate));
        if (summary == null) {
            summary = new DailyUserSummary();
            summary.setUserId(userId);
            summary.setSummaryDate(summaryDate);
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
    }
}
