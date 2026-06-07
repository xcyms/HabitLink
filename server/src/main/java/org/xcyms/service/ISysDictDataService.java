package org.xcyms.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.xcyms.entity.SysDictData;

import java.util.List;

/**
 * <p>
 * 字典数据表 服务类
 * </p>
 *
 * @author liu-xu
 * @since 2026-01-12
 */
public interface ISysDictDataService extends IService<SysDictData> {

    /**
     * 分页查询字典数据
     */
    Page<SysDictData> getDictDataPage(Page<SysDictData> page, SysDictData dictData);

    /**
     * 根据字典类型查询字典数据
     */
    List<SysDictData> getDictDataByType(String dictType);

    /**
     * 新增字典数据
     */
    boolean addDictData(SysDictData dictData);

    /**
     * 更新字典数据
     */
    boolean updateDictData(SysDictData dictData);

    /**
     * 删除字典数据
     */
    boolean deleteDictData(Long id);
}
