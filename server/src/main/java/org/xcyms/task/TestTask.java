package org.xcyms.task;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * <p>
 *     定时任务
 * </p>
 *
 * @author liu-xu
 * @date 2026年01月21日 16:44
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TestTask {

    /**
     * 每天凌晨2点执行
     * cron: 秒 分 时 日 月 周
     */
//    @Scheduled(cron = "0 0 2 * * ?")
    public void execute() {
        log.info("开始执行定时任务...");
    }
}
