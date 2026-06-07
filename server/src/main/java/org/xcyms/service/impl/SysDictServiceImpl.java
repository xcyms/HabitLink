package org.xcyms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.xcyms.entity.SysDict;
import org.xcyms.entity.SysDictData;
import org.xcyms.mapper.SysDictMapper;
import org.xcyms.service.ISysDictDataService;
import org.xcyms.service.ISysDictService;

import java.util.List;

/**
 * <p>
 * 字典类型表 服务实现类
 * </p>
 *
 * @author liu-xu
 * @since 2026-01-12
 */
@Service
@RequiredArgsConstructor
public class SysDictServiceImpl extends ServiceImpl<SysDictMapper, SysDict> implements ISysDictService {

    private final ISysDictDataService dictDataService;

    @Override
    public Page<SysDict> getDictPage(Page<SysDict> page, SysDict dict) {
        LambdaQueryWrapper<SysDict> wrapper = new LambdaQueryWrapper<>();
        if (dict.getDictName() != null && !dict.getDictName().isEmpty()) {
            wrapper.like(SysDict::getDictName, dict.getDictName());
        }
        if (dict.getDictType() != null && !dict.getDictType().isEmpty()) {
            wrapper.like(SysDict::getDictType, dict.getDictType());
        }
        if (dict.getStatus() != null) {
            wrapper.eq(SysDict::getStatus, dict.getStatus());
        }
        wrapper.orderByDesc(SysDict::getCreateTime);
        return baseMapper.selectPage(page, wrapper);
    }

    @Override
    public List<SysDictData> getDictDataByType(String dictType) {
        return dictDataService.getDictDataByType(dictType);
    }

    @Override
    public boolean addDict(SysDict dict) {
        // 检查字典类型是否已存在
        LambdaQueryWrapper<SysDict> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysDict::getDictType, dict.getDictType());
        if (baseMapper.selectCount(wrapper) > 0) {
            throw new RuntimeException("字典类型已存在");
        }
        return save(dict);
    }

    @Override
    public boolean updateDict(SysDict dict) {
        // 检查字典类型是否被其他记录使用
        LambdaQueryWrapper<SysDict> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysDict::getDictType, dict.getDictType());
        wrapper.ne(SysDict::getId, dict.getId());
        if (baseMapper.selectCount(wrapper) > 0) {
            throw new RuntimeException("字典类型已存在");
        }
        return updateById(dict);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteDict(Long id) {
        SysDict dict = getById(id);
        if (dict == null) {
            return false;
        }
        // 删除关联的字典数据
        LambdaQueryWrapper<SysDictData> dataWrapper = new LambdaQueryWrapper<>();
        dataWrapper.eq(SysDictData::getDictType, dict.getDictType());
        dictDataService.remove(dataWrapper);
        // 删除字典类型
        return removeById(id);
    }
}
