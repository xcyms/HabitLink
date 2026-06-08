package org.xcyms.utils;

import org.xcyms.common.enums.HabitRuleTypeEnum;
import org.xcyms.entity.Habit;
import org.xcyms.entity.HabitScheduleRule;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 习惯规则工具类。
 * 统一处理规则解析、应执行日判断和连续天数计算。
 */
public final class HabitRuleUtils {

    private static final Pattern DAY_PATTERN = Pattern.compile("\\d+");

    private HabitRuleUtils() {
    }

    /**
     * 解析每周规则中的星期列表。
     * 约定数字范围为 1-7，分别表示周一到周日。
     */
    public static List<Integer> parseRuleDays(String ruleValue) {
        if (ruleValue == null || ruleValue.isBlank()) {
            return List.of();
        }

        Set<Integer> result = new LinkedHashSet<>();
        Matcher matcher = DAY_PATTERN.matcher(ruleValue);
        while (matcher.find()) {
            int day = Integer.parseInt(matcher.group());
            if (day >= 1 && day <= 7) {
                result.add(day);
            }
        }
        return result.stream().sorted().toList();
    }

    /**
     * 构建规则存储内容。
     * 当前版本采用简单 JSON 字符串格式，方便后续扩展。
     */
    public static String buildRuleValue(String ruleType, Collection<Integer> ruleDays) {
        if (HabitRuleTypeEnum.WEEKLY.getCode().equals(ruleType) && ruleDays != null) {
            List<Integer> normalized = ruleDays.stream()
                    .filter(day -> day != null && day >= 1 && day <= 7)
                    .distinct()
                    .sorted()
                    .toList();
            if (!normalized.isEmpty()) {
                String content = normalized.stream()
                        .map(String::valueOf)
                        .reduce((left, right) -> left + "," + right)
                        .orElse("");
                return "{\"days\":[" + content + "]}";
            }
        }
        return "{}";
    }

    /**
     * 判断指定日期是否在习惯有效范围内。
     */
    public static boolean isWithinActiveRange(Habit habit, LocalDate date) {
        if (habit == null || date == null || habit.getStartDate() == null) {
            return false;
        }
        if (date.isBefore(habit.getStartDate())) {
            return false;
        }
        return habit.getEndDate() == null || !date.isAfter(habit.getEndDate());
    }

    /**
     * 判断指定日期是否为习惯的应执行日。
     */
    public static boolean isScheduledOnDate(Habit habit, HabitScheduleRule rule, LocalDate date) {
        if (!isWithinActiveRange(habit, date) || rule == null || rule.getRuleType() == null) {
            return false;
        }

        if (HabitRuleTypeEnum.DAILY.getCode().equals(rule.getRuleType())) {
            return true;
        }

        if (HabitRuleTypeEnum.WEEKLY.getCode().equals(rule.getRuleType())) {
            return parseRuleDays(rule.getRuleValue()).contains(date.getDayOfWeek().getValue());
        }

        return false;
    }

    /**
     * 统计时间区间内的应执行天数。
     */
    public static int countScheduledDays(Habit habit, HabitScheduleRule rule, LocalDate start, LocalDate end) {
        if (habit == null || rule == null || start == null || end == null || start.isAfter(end)) {
            return 0;
        }

        int count = 0;
        LocalDate cursor = start;
        while (!cursor.isAfter(end)) {
            if (isScheduledOnDate(habit, rule, cursor)) {
                count++;
            }
            cursor = cursor.plusDays(1);
        }
        return count;
    }

    /**
     * 获取时间区间内所有应执行日期。
     */
    public static List<LocalDate> listScheduledDates(Habit habit, HabitScheduleRule rule, LocalDate start, LocalDate end) {
        List<LocalDate> result = new ArrayList<>();
        if (habit == null || rule == null || start == null || end == null || start.isAfter(end)) {
            return result;
        }

        LocalDate cursor = start;
        while (!cursor.isAfter(end)) {
            if (isScheduledOnDate(habit, rule, cursor)) {
                result.add(cursor);
            }
            cursor = cursor.plusDays(1);
        }
        return result;
    }

    /**
     * 获取规则的可读文案。
     */
    public static String formatRuleText(HabitScheduleRule rule) {
        if (rule == null || rule.getRuleType() == null) {
            return "未设置规则";
        }

        if (HabitRuleTypeEnum.DAILY.getCode().equals(rule.getRuleType())) {
            return "每天";
        }

        if (HabitRuleTypeEnum.WEEKLY.getCode().equals(rule.getRuleType())) {
            List<Integer> days = parseRuleDays(rule.getRuleValue());
            if (days.isEmpty()) {
                return "每周";
            }
            List<String> names = days.stream().map(HabitRuleUtils::toWeekName).toList();
            return "每周" + String.join("、", names);
        }

        return "未设置规则";
    }

    /**
     * 计算当前连续完成天数。
     * 连续逻辑只基于应执行日，不会因为非执行日中断。
     */
    public static int calculateCurrentStreak(Habit habit, HabitScheduleRule rule, Set<LocalDate> completedDates) {
        if (completedDates == null || completedDates.isEmpty()) {
            return 0;
        }

        LocalDate anchorDate = completedDates.stream().max(Comparator.naturalOrder()).orElse(null);
        if (anchorDate == null) {
            return 0;
        }

        int streak = 0;
        LocalDate cursor = anchorDate;
        while (cursor != null) {
            if (isScheduledOnDate(habit, rule, cursor) && completedDates.contains(cursor)) {
                streak++;
                cursor = getPreviousScheduledDate(habit, rule, cursor);
            }
            else {
                break;
            }
        }
        return streak;
    }

    /**
     * 计算历史最长连续完成天数。
     */
    public static int calculateLongestStreak(Habit habit, HabitScheduleRule rule, Set<LocalDate> completedDates) {
        if (habit == null || rule == null || completedDates == null || completedDates.isEmpty()) {
            return 0;
        }
        if (habit.getStartDate() == null) {
            return 0;
        }

        LocalDate end = completedDates.stream().max(Comparator.naturalOrder()).orElse(habit.getStartDate());
        int longest = 0;
        int current = 0;
        LocalDate cursor = habit.getStartDate();
        while (!cursor.isAfter(end)) {
            if (isScheduledOnDate(habit, rule, cursor)) {
                if (completedDates.contains(cursor)) {
                    current++;
                    longest = Math.max(longest, current);
                }
                else {
                    current = 0;
                }
            }
            cursor = cursor.plusDays(1);
        }
        return longest;
    }

    /**
     * 获取指定日期之前最近的应执行日。
     */
    public static LocalDate getPreviousScheduledDate(Habit habit, HabitScheduleRule rule, LocalDate currentDate) {
        if (habit == null || rule == null || currentDate == null || habit.getStartDate() == null) {
            return null;
        }

        LocalDate cursor = currentDate.minusDays(1);
        while (!cursor.isBefore(habit.getStartDate())) {
            if (isScheduledOnDate(habit, rule, cursor)) {
                return cursor;
            }
            cursor = cursor.minusDays(1);
        }
        return null;
    }

    /**
     * 将星期数字转为中文文案。
     */
    private static String toWeekName(Integer day) {
        if (day == null) {
            return "";
        }
        return switch (DayOfWeek.of(day)) {
            case MONDAY -> "一";
            case TUESDAY -> "二";
            case WEDNESDAY -> "三";
            case THURSDAY -> "四";
            case FRIDAY -> "五";
            case SATURDAY -> "六";
            case SUNDAY -> "日";
        };
    }
}
