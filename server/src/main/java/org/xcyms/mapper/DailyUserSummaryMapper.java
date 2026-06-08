package org.xcyms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.xcyms.entity.DailyUserSummary;

/**
 * 用户每日汇总表 Mapper。
 */
@Mapper
public interface DailyUserSummaryMapper extends BaseMapper<DailyUserSummary> {

}
