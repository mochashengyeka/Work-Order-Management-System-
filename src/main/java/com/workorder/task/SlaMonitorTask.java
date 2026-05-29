package com.workorder.task;

import com.workorder.entity.WorkOrder;
import com.workorder.mapper.WorkOrderMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class SlaMonitorTask {

    private final WorkOrderMapper orderMapper;

    @Scheduled(fixedDelay = 60000) // 每分钟执行一次
    public void checkOverdueOrders() {
        List<WorkOrder> overdueOrders = orderMapper.findOverdueOrders(LocalDateTime.now());
        for (WorkOrder order : overdueOrders) {
            log.warn("工单 {} 已超时！当前状态：{}，SLA截止时间：{}",
                    order.getOrderNo(), order.getStatus(), order.getSlaDeadline());
            // TODO: 发送站内信/邮件/短信通知管理员和工程师
        }
    }
}