package org.xcyms.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.xcyms.common.ApiResult;
import org.xcyms.common.enums.HabitRuleTypeEnum;
import org.xcyms.common.enums.HabitStatusEnum;
import org.xcyms.entity.Habit;
import org.xcyms.entity.HabitScheduleRule;
import org.xcyms.entity.dto.HabitDTO;
import org.xcyms.entity.dto.HabitQueryDTO;
import org.xcyms.mapper.HabitMapper;
import org.xcyms.mapper.HabitScheduleRuleMapper;
import org.xcyms.service.IHabitService;
import org.xcyms.utils.HabitRuleUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 习惯管理服务实现类。
 */
@Service
@RequiredArgsConstructor
public class HabitServiceImpl extends ServiceImpl<HabitMapper, Habit> implements IHabitService {

    private final ModelMapper modelMapper;
    private final HabitScheduleRuleMapper habitScheduleRuleMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResult<String> createHabit(HabitDTO habitDTO) {
        if (habitDTO == null || habitDTO.getName() == null || habitDTO.getName().isBlank()) {
            return ApiResult.error("习惯名称不能为空");
        }
        if (habitDTO.getStartDate() == null) {
            return ApiResult.error("开始日期不能为空");
        }
        if (HabitRuleTypeEnum.WEEKLY.getCode().equals(habitDTO.getRuleType())
                && (habitDTO.getRuleDays() == null || habitDTO.getRuleDays().isEmpty())) {
            return ApiResult.error("每周规则至少需要选择一个星期");
        }

        Habit habit = modelMapper.map(habitDTO, Habit.class);
        habit.setId(null);
        habit.setUserId(StpUtil.getLoginIdAsLong());
        habit.setStatus(HabitStatusEnum.ACTIVE.getCode());
        this.save(habit);

        HabitScheduleRule rule = buildScheduleRule(habit.getId(), habitDTO);
        habitScheduleRuleMapper.insert(rule);
        return ApiResult.success("创建习惯成功");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResult<String> updateHabit(HabitDTO habitDTO) {
        if (habitDTO == null || habitDTO.getId() == null) {
            return ApiResult.error("习惯ID不能为空");
        }
        if (HabitRuleTypeEnum.WEEKLY.getCode().equals(habitDTO.getRuleType())
                && (habitDTO.getRuleDays() == null || habitDTO.getRuleDays().isEmpty())) {
            return ApiResult.error("每周规则至少需要选择一个星期");
        }

        Habit habit = this.getById(habitDTO.getId());
        if (habit == null || !habit.getUserId().equals(StpUtil.getLoginIdAsLong())) {
            return ApiResult.error("习惯不存在");
        }

        habit.setName(habitDTO.getName());
        habit.setDescription(habitDTO.getDescription());
        habit.setCategory(habitDTO.getCategory());
        habit.setIcon(habitDTO.getIcon());
        habit.setColor(habitDTO.getColor());
        habit.setStartDate(habitDTO.getStartDate());
        habit.setEndDate(habitDTO.getEndDate());
        habit.setAllowMakeup(habitDTO.getAllowMakeup());
        habit.setMakeupLimitDays(habitDTO.getMakeupLimitDays());
        habit.setReminderEnabled(habitDTO.getReminderEnabled());
        habit.setReminderTime(habitDTO.getReminderTime());
        habit.setSortOrder(habitDTO.getSortOrder());
        this.updateById(habit);

        HabitScheduleRule rule = habitScheduleRuleMapper.selectOne(new LambdaQueryWrapper<HabitScheduleRule>()
                .eq(HabitScheduleRule::getHabitId, habit.getId()));
        if (rule != null) {
            rule.setRuleType(resolveRuleType(habitDTO));
            rule.setRuleValue(HabitRuleUtils.buildRuleValue(resolveRuleType(habitDTO), habitDTO.getRuleDays()));
            habitScheduleRuleMapper.updateById(rule);
        }
        return ApiResult.success("更新习惯成功");
    }

    @Override
    public ApiResult<String> deleteHabit(Long id) {
        if (id == null) {
            return ApiResult.error("习惯ID不能为空");
        }

        Habit habit = this.getById(id);
        if (habit == null || !habit.getUserId().equals(StpUtil.getLoginIdAsLong())) {
            return ApiResult.error("习惯不存在");
        }
        this.removeById(id);
        return ApiResult.success("删除习惯成功");
    }

    @Override
    public ApiResult<String> pauseHabit(Long id) {
        return updateHabitStatus(id, HabitStatusEnum.PAUSED.getCode(), "暂停习惯成功");
    }

    @Override
    public ApiResult<String> resumeHabit(Long id) {
        return updateHabitStatus(id, HabitStatusEnum.ACTIVE.getCode(), "恢复习惯成功");
    }

    @Override
    public ApiResult<HabitDTO> getHabitDetail(Long id) {
        if (id == null) {
            return ApiResult.error("习惯ID不能为空");
        }

        Habit habit = this.getById(id);
        if (habit == null || !habit.getUserId().equals(StpUtil.getLoginIdAsLong())) {
            return ApiResult.error("习惯不存在");
        }

        HabitDTO dto = modelMapper.map(habit, HabitDTO.class);
        HabitScheduleRule rule = habitScheduleRuleMapper.selectOne(new LambdaQueryWrapper<HabitScheduleRule>()
                .eq(HabitScheduleRule::getHabitId, habit.getId()));
        if (rule != null) {
            dto.setRuleType(rule.getRuleType());
            dto.setRuleDays(HabitRuleUtils.parseRuleDays(rule.getRuleValue()));
        }
        else {
            dto.setRuleType(HabitRuleTypeEnum.DAILY.getCode());
            dto.setRuleDays(List.of());
        }
        return ApiResult.success(dto);
    }

    @Override
    public ApiResult<List<HabitDTO>> getMyHabitList() {
        Long userId = StpUtil.getLoginIdAsLong();
        List<Habit> habits = this.list(new LambdaQueryWrapper<Habit>()
                .eq(Habit::getUserId, userId)
                .orderByAsc(Habit::getSortOrder)
                .orderByDesc(Habit::getId));

        List<Long> habitIds = habits.stream().map(Habit::getId).toList();
        List<HabitScheduleRule> rules = habitScheduleRuleMapper.selectList(new LambdaQueryWrapper<HabitScheduleRule>()
                .in(!habitIds.isEmpty(), HabitScheduleRule::getHabitId, habitIds));

        return ApiResult.success(habits.stream().map(habit -> {
            HabitDTO dto = modelMapper.map(habit, HabitDTO.class);
            HabitScheduleRule rule = rules.stream()
                    .filter(item -> item.getHabitId().equals(habit.getId()))
                    .findFirst()
                    .orElse(null);
            if (rule != null) {
                dto.setRuleType(rule.getRuleType());
                dto.setRuleDays(HabitRuleUtils.parseRuleDays(rule.getRuleValue()));
            }
            else {
                dto.setRuleType(HabitRuleTypeEnum.DAILY.getCode());
                dto.setRuleDays(List.of());
            }
            return dto;
        }).collect(Collectors.toList()));
    }

    @Override
    public ApiResult<Page<HabitDTO>> getHabitPage(Page<Habit> page, HabitQueryDTO queryDTO) {
        LambdaQueryWrapper<Habit> wrapper = new LambdaQueryWrapper<>();
        if (queryDTO != null) {
            wrapper.like(queryDTO.getName() != null && !queryDTO.getName().isBlank(), Habit::getName, queryDTO.getName())
                    .eq(queryDTO.getCategory() != null && !queryDTO.getCategory().isBlank(), Habit::getCategory, queryDTO.getCategory())
                    .eq(queryDTO.getStatus() != null, Habit::getStatus, queryDTO.getStatus())
                    .eq(queryDTO.getUserId() != null, Habit::getUserId, queryDTO.getUserId());
        }
        Page<Habit> habitPage = this.page(page, wrapper);
        List<HabitDTO> records = habitPage.getRecords().stream()
                .map(item -> modelMapper.map(item, HabitDTO.class))
                .collect(Collectors.toList());

        Page<HabitDTO> result = new Page<>(page.getCurrent(), page.getSize(), habitPage.getTotal());
        result.setRecords(records);
        return ApiResult.success(result);
    }

    /**
     * 根据习惯表单构建规则实体。
     */
    private HabitScheduleRule buildScheduleRule(Long habitId, HabitDTO habitDTO) {
        HabitScheduleRule rule = new HabitScheduleRule();
        rule.setHabitId(habitId);
        rule.setRuleType(resolveRuleType(habitDTO));
        rule.setRuleValue(HabitRuleUtils.buildRuleValue(resolveRuleType(habitDTO), habitDTO.getRuleDays()));
        rule.setTimezone("Asia/Shanghai");
        return rule;
    }

    /**
     * 解析表单里的规则类型，默认按每天处理。
     */
    private String resolveRuleType(HabitDTO habitDTO) {
        return habitDTO.getRuleType() == null ? HabitRuleTypeEnum.DAILY.getCode() : habitDTO.getRuleType();
    }

    /**
     * 统一更新习惯状态。
     */
    private ApiResult<String> updateHabitStatus(Long id, Integer status, String successMessage) {
        if (id == null) {
            return ApiResult.error("习惯ID不能为空");
        }

        Habit habit = this.getById(id);
        if (habit == null || !habit.getUserId().equals(StpUtil.getLoginIdAsLong())) {
            return ApiResult.error("习惯不存在");
        }
        habit.setStatus(status);
        this.updateById(habit);
        return ApiResult.success(successMessage);
    }
}
