package org.xcyms.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.xcyms.common.ApiResult;
import org.xcyms.common.annotation.ApiDoc;
import org.xcyms.common.annotation.Log;
import org.xcyms.entity.Habit;
import org.xcyms.entity.dto.HabitDTO;
import org.xcyms.entity.dto.HabitQueryDTO;
import org.xcyms.service.IHabitService;

import java.util.List;

/**
 * 习惯管理接口控制器。
 */
@ApiDoc("习惯管理")
@RestController
@RequiredArgsConstructor
@RequestMapping("/habit")
public class HabitController {

    private final IHabitService habitService;

    @Log("创建习惯")
    @ApiDoc(value = "创建习惯", notes = "为当前登录用户创建一个个人习惯", order = 1)
    @PostMapping("/create")
    public ApiResult<String> create(@RequestBody HabitDTO habitDTO) {
        return habitService.createHabit(habitDTO);
    }

    @Log("更新习惯")
    @ApiDoc(value = "更新习惯", notes = "更新当前登录用户的个人习惯", order = 2)
    @PostMapping("/update")
    public ApiResult<String> update(@RequestBody HabitDTO habitDTO) {
        return habitService.updateHabit(habitDTO);
    }

    @Log("删除习惯")
    @ApiDoc(value = "删除习惯", notes = "逻辑删除一个个人习惯", order = 3)
    @PostMapping("/delete")
    public ApiResult<String> delete(@RequestParam Long id) {
        return habitService.deleteHabit(id);
    }

    @Log("暂停习惯")
    @ApiDoc(value = "暂停习惯", notes = "暂停当前用户的一个习惯", order = 4)
    @PostMapping("/pause")
    public ApiResult<String> pause(@RequestParam Long id) {
        return habitService.pauseHabit(id);
    }

    @Log("恢复习惯")
    @ApiDoc(value = "恢复习惯", notes = "恢复当前用户已暂停的习惯", order = 5)
    @PostMapping("/resume")
    public ApiResult<String> resume(@RequestParam Long id) {
        return habitService.resumeHabit(id);
    }

    @ApiDoc(value = "获取习惯详情", notes = "获取单个习惯的详细信息", order = 6)
    @GetMapping("/detail")
    public ApiResult<HabitDTO> detail(@RequestParam Long id) {
        return habitService.getHabitDetail(id);
    }

    @ApiDoc(value = "获取我的习惯列表", notes = "获取当前登录用户的习惯列表", order = 7)
    @GetMapping("/list")
    public ApiResult<List<HabitDTO>> list() {
        return habitService.getMyHabitList();
    }

    @ApiDoc(value = "分页查询习惯", notes = "供后台管理端分页查询习惯数据", order = 8)
    @SaCheckRole("ADMIN")
    @PostMapping("/page")
    public ApiResult<Page<HabitDTO>> page(Page<Habit> page, @RequestBody HabitQueryDTO queryDTO) {
        page.addOrder(OrderItem.desc("id"));
        return habitService.getHabitPage(page, queryDTO);
    }
}
