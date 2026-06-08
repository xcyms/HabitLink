package org.xcyms.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.xcyms.common.ApiResult;
import org.xcyms.entity.Habit;
import org.xcyms.entity.dto.HabitDTO;
import org.xcyms.entity.dto.HabitQueryDTO;

import java.util.List;

/**
 * 习惯管理服务接口。
 */
public interface IHabitService extends IService<Habit> {

    /**
     * 为当前用户创建新的习惯。
     *
     * @param habitDTO 前端提交的习惯表单
     * @return 处理结果
     */
    ApiResult<String> createHabit(HabitDTO habitDTO);

    /**
     * 更新当前用户的习惯。
     *
     * @param habitDTO 前端提交的习惯表单
     * @return 处理结果
     */
    ApiResult<String> updateHabit(HabitDTO habitDTO);

    /**
     * 逻辑删除当前用户的习惯。
     *
     * @param id 习惯ID
     * @return 处理结果
     */
    ApiResult<String> deleteHabit(Long id);

    /**
     * 暂停当前用户的习惯。
     *
     * @param id 习惯ID
     * @return 处理结果
     */
    ApiResult<String> pauseHabit(Long id);

    /**
     * 恢复当前用户已暂停的习惯。
     *
     * @param id 习惯ID
     * @return 处理结果
     */
    ApiResult<String> resumeHabit(Long id);

    /**
     * 获取当前用户的习惯详情。
     *
     * @param id 习惯ID
     * @return 习惯详情
     */
    ApiResult<HabitDTO> getHabitDetail(Long id);

    /**
     * 获取当前用户的习惯列表。
     *
     * @return 习惯 DTO 列表
     */
    ApiResult<List<HabitDTO>> getMyHabitList();

    /**
     * 后台分页查询习惯。
     *
     * @param page 分页对象
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    ApiResult<Page<HabitDTO>> getHabitPage(Page<Habit> page, HabitQueryDTO queryDTO);
}
