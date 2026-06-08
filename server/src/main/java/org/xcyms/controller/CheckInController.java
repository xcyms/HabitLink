package org.xcyms.controller;

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
import org.xcyms.entity.dto.CheckInDTO;
import org.xcyms.entity.dto.CheckInRecordDTO;
import org.xcyms.service.ICheckInService;

import java.util.List;

/**
 * 习惯打卡接口控制器。
 */
@ApiDoc("习惯打卡")
@RestController
@RequiredArgsConstructor
@RequestMapping("/check-in")
public class CheckInController {

    private final ICheckInService checkInService;

    @Log("提交打卡")
    @ApiDoc(value = "提交打卡", notes = "提交一次普通打卡", order = 1)
    @PostMapping("/submit")
    public ApiResult<String> submit(@RequestBody CheckInDTO checkInDTO) {
        return checkInService.submit(checkInDTO);
    }

    @Log("提交补打卡")
    @ApiDoc(value = "提交补打卡", notes = "提交一次补打卡记录", order = 2)
    @PostMapping("/makeup")
    public ApiResult<String> makeup(@RequestBody CheckInDTO checkInDTO) {
        return checkInService.makeup(checkInDTO);
    }

    @ApiDoc(value = "获取打卡记录列表", notes = "获取当前用户某个习惯的打卡记录列表", order = 3)
    @GetMapping("/list")
    public ApiResult<List<CheckInRecordDTO>> list(@RequestParam Long habitId) {
        return checkInService.getRecordsByHabit(habitId);
    }
}
