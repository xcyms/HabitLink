package org.xcyms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.xcyms.entity.Habit;

/**
 * 习惯主表 Mapper。
 */
@Mapper
public interface HabitMapper extends BaseMapper<Habit> {

}
