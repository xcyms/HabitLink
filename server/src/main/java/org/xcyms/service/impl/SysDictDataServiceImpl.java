package org.xcyms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.xcyms.common.Constant;
import org.xcyms.entity.SysDictData;
import org.xcyms.mapper.SysDictDataMapper;
import org.xcyms.service.ISysDictDataService;

import java.util.List;

/**
 * <p>
 * 字典数据表 服务实现类
 * </p>
 *
 * @author liu-xu
 * @since 2026-01-12
 */
@Service
@RequiredArgsConstructor
public class SysDictDataServiceImpl extends ServiceImpl<SysDictDataMapper, SysDictData> implements ISysDictDataService {

    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public Page<SysDictData> getDictDataPage(Page<SysDictData> page, SysDictData dictData) {
        LambdaQueryWrapper<SysDictData> wrapper = new LambdaQueryWrapper<>();
        if (dictData.getDictId() != null) {
            wrapper.eq(SysDictData::getDictId, dictData.getDictId());
        }
        if (dictData.getDictType() != null && !dictData.getDictType().isEmpty()) {
            wrapper.eq(SysDictData::getDictType, dictData.getDictType());
        }
        if (dictData.getDictLabel() != null && !dictData.getDictLabel().isEmpty()) {
            wrapper.like(SysDictData::getDictLabel, dictData.getDictLabel());
        }
        if (dictData.getStatus() != null) {
            wrapper.eq(SysDictData::getStatus, dictData.getStatus());
        }
        wrapper.orderByAsc(SysDictData::getDictSort);
        return baseMapper.selectPage(page, wrapper);
    }

    @Override
    @Cacheable(value = Constant.Cache.DICT_DATA, key = "#dictType", unless = "#result == null || #result.isEmpty()")
    public List<SysDictData> getDictDataByType(String dictType) {
        LambdaQueryWrapper<SysDictData> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysDictData::getDictType, dictType);
        return baseMapper.selectList(wrapper);
    }

    @Override
    @CacheEvict(value = Constant.Cache.DICT_DATA, key = "#dictData.dictType")
    public boolean addDictData(SysDictData dictData) {
        // 检查同一字典类型下是否有重复的字典键值
        LambdaQueryWrapper<SysDictData> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysDictData::getDictType, dictData.getDictType());
        wrapper.eq(SysDictData::getDictValue, dictData.getDictValue());
        if (baseMapper.selectCount(wrapper) > 0) {
            throw new RuntimeException("字典键值已存在");
        }
        return save(dictData);
    }

    @Override
    @CacheEvict(value = Constant.Cache.DICT_DATA, key = "#dictData.dictType")
    public boolean updateDictData(SysDictData dictData) {
        // 获取原字典类型，用于清除缓存
        SysDictData oldData = getById(dictData.getId());
        if (oldData != null && !oldData.getDictType().equals(dictData.getDictType())) {
            // 如果字典类型变更，清除新旧两个类型的缓存
            redisTemplate.delete(Constant.Cache.DICT_DATA + "::" + oldData.getDictType());
        }
        // 检查同一字典类型下是否有重复的字典键值（排除当前记录）
        LambdaQueryWrapper<SysDictData> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysDictData::getDictType, dictData.getDictType());
        wrapper.eq(SysDictData::getDictValue, dictData.getDictValue());
        wrapper.ne(SysDictData::getId, dictData.getId());
        if (baseMapper.selectCount(wrapper) > 0) {
            throw new RuntimeException("字典键值已存在");
        }
        return updateById(dictData);
    }

    @Override
    @CacheEvict(value = Constant.Cache.DICT_DATA, allEntries = true)
    public boolean deleteDictData(Long id) {
        // 获取字典类型，用于清除缓存
        SysDictData dictData = getById(id);
        if (dictData != null) {
            redisTemplate.delete(Constant.Cache.DICT_DATA + "::" + dictData.getDictType());
        }
        return removeById(id);
    }
}
