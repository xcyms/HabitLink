package org.xcyms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.xcyms.common.ApiResult;
import org.xcyms.entity.CheckInRecord;
import org.xcyms.entity.dto.CheckInDTO;
import org.xcyms.entity.dto.CheckInRecordDTO;

import java.util.List;

/**
 * 习惯打卡服务接口。
 */
public interface ICheckInService extends IService<CheckInRecord> {

    /**
     * 提交普通打卡。
     *
     * @param checkInDTO 打卡请求参数
     * @return 处理结果
     */
    ApiResult<String> submit(CheckInDTO checkInDTO);

    /**
     * 提交补打卡。
     *
     * @param checkInDTO 打卡请求参数
     * @return 处理结果
     */
    ApiResult<String> makeup(CheckInDTO checkInDTO);

    /**
     * 查询当前用户指定习惯的打卡记录。
     *
     * @param habitId 习惯ID
     * @return 打卡记录列表
     */
    ApiResult<List<CheckInRecordDTO>> getRecordsByHabit(Long habitId);
}
