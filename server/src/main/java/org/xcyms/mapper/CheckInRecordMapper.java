package org.xcyms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.xcyms.entity.CheckInRecord;

/**
 * 习惯打卡记录表 Mapper。
 */
@Mapper
public interface CheckInRecordMapper extends BaseMapper<CheckInRecord> {

}
