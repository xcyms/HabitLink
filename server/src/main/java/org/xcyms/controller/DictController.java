package org.xcyms.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.xcyms.common.ApiResult;
import org.xcyms.common.annotation.ApiDoc;
import org.xcyms.common.annotation.Log;
import org.xcyms.entity.SysDict;
import org.xcyms.entity.SysDictData;
import org.xcyms.service.ISysDictDataService;
import org.xcyms.service.ISysDictService;

import java.util.List;

/**
 * <p>
 * 字典管理 前端控制器
 * </p>
 *
 * @author liu-xu
 * @since 2026-01-12
 */
@ApiDoc("字典管理")
@RestController
@RequestMapping("/dict")
@RequiredArgsConstructor
public class DictController {

    private final ISysDictService dictService;

    private final ISysDictDataService dictDataService;

    // ==================== 字典类型接口 ====================

    /**
     * 分页查询字典类型
     */
    @ApiDoc("分页查询字典类型")
    @PostMapping("/page")
    @SaCheckRole("ADMIN")
    public ApiResult<Page<SysDict>> page(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestBody(required = false) SysDict dict) {
        Page<SysDict> page = new Page<>(current, size);
        return ApiResult.success(dictService.getDictPage(page, dict));
    }

    /**
     * 获取所有字典类型列表
     */
    @ApiDoc("获取所有字典类型列表")
    @GetMapping("/list")
    @SaCheckRole("ADMIN")
    public ApiResult<List<SysDict>> list() {
        return ApiResult.success(dictService.list());
    }

    /**
     * 获取字典类型详情
     */
    @ApiDoc("获取字典类型详情")
    @GetMapping("/{id}")
    @SaCheckRole("ADMIN")
    public ApiResult<SysDict> getById(@PathVariable Long id) {
        return ApiResult.success(dictService.getById(id));
    }

    /**
     * 新增字典类型
     */
    @ApiDoc("新增字典类型")
    @PostMapping
    @SaCheckRole("ADMIN")
    @Log("新增字典类型")
    public ApiResult<String> add(@RequestBody SysDict dict) {
        dictService.addDict(dict);
        return ApiResult.success("新增成功");
    }

    /**
     * 更新字典类型
     */
    @PutMapping
    @SaCheckRole("ADMIN")
    @ApiDoc(value = "更新字典类型", notes = "更新字典类型", order = 2)
    @Log("更新字典类型")
    public ApiResult<String> update(@RequestBody SysDict dict) {
        dictService.updateDict(dict);
        return ApiResult.success("更新成功");
    }

    /**
     * 删除字典类型
     */
    @DeleteMapping("/{id}")
    @SaCheckRole("ADMIN")
    @ApiDoc(value = "删除字典类型", notes = "删除字典类型", order = 3)
    @Log("删除字典类型")
    public ApiResult<String> delete(@PathVariable Long id) {
        dictService.deleteDict(id);
        return ApiResult.success("删除成功");
    }

    // ==================== 字典数据接口 ====================

    /**
     * 分页查询字典数据
     */
    @PostMapping("/data/page")
    @SaCheckRole("ADMIN")
    @ApiDoc("分页查询字典数据")
    public ApiResult<Page<SysDictData>> dataPage(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestBody(required = false) SysDictData dictData) {
        Page<SysDictData> page = new Page<>(current, size);
        return ApiResult.success(dictDataService.getDictDataPage(page, dictData));
    }

    /**
     * 根据字典类型获取字典数据
     */
    @ApiDoc("根据字典类型获取字典数据")
    @GetMapping("/data/{dictType}")
    public ApiResult<List<SysDictData>> getDataByType(@PathVariable String dictType) {
        return ApiResult.success(dictDataService.getDictDataByType(dictType));
    }

    /**
     * 获取字典数据详情
     */
    @ApiDoc("获取字典数据详情")
    @GetMapping("/data/detail/{id}")
    @SaCheckRole("ADMIN")
    public ApiResult<SysDictData> getDataById(@PathVariable Long id) {
        return ApiResult.success(dictDataService.getById(id));
    }

    /**
     * 新增字典数据
     */
    @PostMapping("/data")
    @SaCheckRole("ADMIN")
    @ApiDoc(value = "新增字典数据", notes = "新增字典数据", order = 2)
    @Log("新增字典数据")
    public ApiResult<String> addData(@RequestBody SysDictData dictData) {
        dictDataService.addDictData(dictData);
        return ApiResult.success("新增成功");
    }

    /**
     * 更新字典数据
     */
    @PutMapping("/data")
    @SaCheckRole("ADMIN")
    @ApiDoc(value = "更新字典数据", notes = "更新字典数据", order = 2)
    @Log("更新字典数据")
    public ApiResult<String> updateData(@RequestBody SysDictData dictData) {
        dictDataService.updateDictData(dictData);
        return ApiResult.success("更新成功");
    }

    /**
     * 删除字典数据
     */
    @DeleteMapping("/data/{id}")
    @SaCheckRole("ADMIN")
    @ApiDoc(value = "删除字典数据", notes = "删除字典数据", order = 3)
    @Log("删除字典数据")
    public ApiResult<String> deleteData(@PathVariable Long id) {
        dictDataService.deleteDictData(id);
        return ApiResult.success("删除成功");
    }
}
