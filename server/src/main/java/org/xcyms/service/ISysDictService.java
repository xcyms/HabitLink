package org.xcyms.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.xcyms.entity.SysDict;
import org.xcyms.entity.SysDictData;

import java.util.List;

/**
 * <p>
 * 字典类型表 服务类
 * </p>
 *
 * @author liu-xu
 * @since 2026-01-12
 */
public interface ISysDictService extends IService<SysDict> {

    /**
     * 分页查询字典类型
     */
    Page<SysDict> getDictPage(Page<SysDict> page, SysDict dict);

    /**
     * 根据字典类型获取字典数据列表
     */
    List<SysDictData> getDictDataByType(String dictType);

    /**
     * 新增字典类型
     */
    boolean addDict(SysDict dict);

    /**
     * 更新字典类型
     */
    boolean updateDict(SysDict dict);

    /**
     * 删除字典类型（同时删除关联的字典数据）
     */
    boolean deleteDict(Long id);
}
