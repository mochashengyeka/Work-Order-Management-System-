package com.workorder.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.workorder.entity.WorkOrder;
import com.workorder.entity.OrderFeedback;
import com.workorder.entity.OrderLog;
import com.workorder.entity.User;
import com.workorder.exception.Result;
import com.workorder.mapper.OrderFeedbackMapper;
import com.workorder.mapper.UserMapper;
import com.workorder.mapper.WorkOrderMapper;
import com.workorder.mapper.OrderLogMapper;
import com.workorder.security.SecurityUtils;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/order")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminOrderController {

    private final WorkOrderMapper orderMapper;
    private final OrderLogMapper logMapper;
    private final UserMapper userMapper;
    private final SecurityUtils securityUtils;
    private final OrderFeedbackMapper feedbackMapper;

    /**
     * 工单列表（全部）
     */
    @GetMapping("/list")
    public Result getOrderList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer priority,
            @RequestParam(required = false) String keyword) {

        LambdaQueryWrapper<WorkOrder> wrapper = new LambdaQueryWrapper<WorkOrder>()
                .eq(status != null, WorkOrder::getStatus, status)
                .eq(priority != null, WorkOrder::getPriority, priority)
                .like(keyword != null, WorkOrder::getTitle, keyword)
                .or().like(keyword != null, WorkOrder::getOrderNo, keyword)
                .orderByDesc(WorkOrder::getCreateTime);

        IPage<WorkOrder> result = orderMapper.selectPage(new Page<>(page, size), wrapper);
        return Result.success(result);
    }

    /**
     * 派单（指派工程师）
     */
    @PostMapping("/assign/{id}")
    public Result assignOrder(@PathVariable Long id, @RequestBody AssignRequest request) {
        WorkOrder order = orderMapper.selectById(id);
        if (order == null) {
            return Result.error("工单不存在");
        }

        if (order.getStatus() != 1) {
            return Result.error("只有待派单状态的工单才能指派");
        }

        // 验证工程师是否存在且角色正确
        User engineer = userMapper.selectById(request.getEngineerId());
        if (engineer == null || engineer.getRole() != 2 || engineer.getStatus() != 1) {
            return Result.error("工程师不存在或状态异常");
        }

        order.setEngineerId(request.getEngineerId());
        order.setStatus(2);  // 待接单
        order.setAssignTime(LocalDateTime.now());
        orderMapper.updateById(order);

        saveLog(order.getId(), 2, 1, 2, "管理员派单给工程师：" + engineer.getRealName());

        return Result.success("派单成功");
    }

    /**
     * 获取可派单工程师列表
     */
    @GetMapping("/engineers")
    public Result getAvailableEngineers() {
        List<User> engineers = userMapper.selectList(
                new LambdaQueryWrapper<User>()
                        .eq(User::getRole, 2)
                        .eq(User::getStatus, 1));
        return Result.success(engineers);
    }

    /**
     * 强制关闭工单（删除工单及相关日志）
     */
    @PostMapping("/force-close/{id}")
    public Result forceCloseOrder(@PathVariable Long id, @RequestParam String reason) {
        WorkOrder order = orderMapper.selectById(id);
        if (order == null) {
            return Result.error("工单不存在");
        }

        if (order.getStatus() == 5 || order.getStatus() == 6) {
            return Result.error("工单已结束，无法操作");
        }

        logMapper.delete(new LambdaQueryWrapper<OrderLog>().eq(OrderLog::getOrderId, id));
        feedbackMapper.delete(new LambdaQueryWrapper<OrderFeedback>()
                .eq(OrderFeedback::getOrderId, id));
        orderMapper.deleteById(id);

        return Result.success("工单已关闭并删除");
    }

    /**
     * 工单详情
     */
    @GetMapping("/detail/{id}")
    public Result getOrderDetail(@PathVariable Long id) {
        WorkOrder order = orderMapper.selectById(id);
        if (order == null) {
            return Result.error("工单不存在");
        }

        Map<String, Object> detail = new HashMap<>();
        detail.put("id", order.getId());
        detail.put("orderNo", order.getOrderNo());
        detail.put("title", order.getTitle());
        detail.put("description", order.getDescription());
        detail.put("priority", order.getPriority());
        detail.put("status", order.getStatus());
        detail.put("location", order.getLocation());
        detail.put("deviceInfo", order.getDeviceInfo());
        detail.put("images", order.getImages());
        detail.put("diagnosis", order.getDiagnosis());
        detail.put("repairAction", order.getRepairAction());
        detail.put("repairImages", order.getRepairImages());
        detail.put("partsUsed", order.getPartsUsed());
        detail.put("actualCost", order.getActualCost());
        detail.put("createTime", order.getCreateTime());
        detail.put("assignTime", order.getAssignTime());
        detail.put("acceptTime", order.getAcceptTime());
        detail.put("startTime", order.getStartTime());
        detail.put("finishTime", order.getFinishTime());
        detail.put("confirmTime", order.getConfirmTime());
        detail.put("slaDeadline", order.getSlaDeadline());

        // 查询报修人姓名
        if (order.getReporterId() != null) {
            User reporter = userMapper.selectById(order.getReporterId());
            detail.put("reporterName", reporter != null ? reporter.getRealName() : "未知");
        } else {
            detail.put("reporterName", "未知");
        }

        // 查询工程师姓名
        if (order.getEngineerId() != null) {
            User engineer = userMapper.selectById(order.getEngineerId());
            detail.put("engineerName", engineer != null ? engineer.getRealName() : "未指派");
        } else {
            detail.put("engineerName", "未指派");
        }

        return Result.success(detail);
    }

    /**
     * 工单操作日志
     */
    @GetMapping("/logs/{id}")
    public Result getOrderLogs(@PathVariable Long id) {
        List<OrderLog> logs = logMapper.selectList(
                new LambdaQueryWrapper<OrderLog>()
                        .eq(OrderLog::getOrderId, id)
                        .orderByAsc(OrderLog::getCreateTime));

        List<Map<String, Object>> logList = logs.stream().map(log -> {
            Map<String, Object> item = new HashMap<>();
            item.put("id", log.getId());
            item.put("orderId", log.getOrderId());
            item.put("actionType", log.getActionType());
            item.put("fromStatus", log.getFromStatus());
            item.put("toStatus", log.getToStatus());
            item.put("remark", log.getRemark());
            item.put("createTime", log.getCreateTime());

            // 查询操作人姓名
            if (log.getOperatorId() != null) {
                User operator = userMapper.selectById(log.getOperatorId());
                item.put("operatorName", operator != null ? operator.getRealName() : "系统");
            } else {
                item.put("operatorName", "系统");
            }
            return item;
        }).toList();

        return Result.success(logList);
    }

    @GetMapping("/detail/{id}/logs")
    public Result getOrderDetailAndLogs(@PathVariable Long id) {
        WorkOrder order = orderMapper.selectById(id);
        if (order == null) {
            return Result.error("工单不存在");
        }

        Map<String, Object> detail = new HashMap<>();
        detail.put("id", order.getId());
        detail.put("orderNo", order.getOrderNo());
        detail.put("title", order.getTitle());
        detail.put("description", order.getDescription());
        detail.put("priority", order.getPriority());
        detail.put("status", order.getStatus());
        detail.put("location", order.getLocation());
        detail.put("deviceInfo", order.getDeviceInfo());
        detail.put("images", order.getImages());
        detail.put("diagnosis", order.getDiagnosis());
        detail.put("repairAction", order.getRepairAction());
        detail.put("repairImages", order.getRepairImages());
        detail.put("partsUsed", order.getPartsUsed());
        detail.put("actualCost", order.getActualCost());
        detail.put("createTime", order.getCreateTime());
        detail.put("assignTime", order.getAssignTime());
        detail.put("acceptTime", order.getAcceptTime());
        detail.put("startTime", order.getStartTime());
        detail.put("finishTime", order.getFinishTime());
        detail.put("confirmTime", order.getConfirmTime());
        detail.put("slaDeadline", order.getSlaDeadline());

        if (order.getReporterId() != null) {
            User reporter = userMapper.selectById(order.getReporterId());
            detail.put("reporterName", reporter != null ? reporter.getRealName() : "未知");
        } else {
            detail.put("reporterName", "未知");
        }

        if (order.getEngineerId() != null) {
            User engineer = userMapper.selectById(order.getEngineerId());
            detail.put("engineerName", engineer != null ? engineer.getRealName() : "未指派");
        } else {
            detail.put("engineerName", "未指派");
        }

        List<OrderLog> logs = logMapper.selectList(
                new LambdaQueryWrapper<OrderLog>()
                        .eq(OrderLog::getOrderId, id)
                        .orderByAsc(OrderLog::getCreateTime));

        List<Map<String, Object>> logList = logs.stream().map(log -> {
            Map<String, Object> item = new HashMap<>();
            item.put("id", log.getId());
            item.put("orderId", log.getOrderId());
            item.put("actionType", log.getActionType());
            item.put("fromStatus", log.getFromStatus());
            item.put("toStatus", log.getToStatus());
            item.put("remark", log.getRemark());
            item.put("createTime", log.getCreateTime());

            if (log.getOperatorId() != null) {
                User operator = userMapper.selectById(log.getOperatorId());
                item.put("operatorName", operator != null ? operator.getRealName() : "系统");
            } else {
                item.put("operatorName", "系统");
            }
            return item;
        }).toList();

        detail.put("logs", logList);
        return Result.success(detail);
    }

    /**
     * 数据大屏统计
     */
    @GetMapping("/dashboard")
    public Result getDashboardData() {
        Map<String, Object> data = new HashMap<>();

        // 各状态工单数量
        data.put("totalOrders", orderMapper.selectCount(null));
        data.put("pendingAssign", orderMapper.selectCount(new LambdaQueryWrapper<WorkOrder>().eq(WorkOrder::getStatus, 1)));
        data.put("pendingAccept", orderMapper.selectCount(new LambdaQueryWrapper<WorkOrder>().eq(WorkOrder::getStatus, 2)));
        data.put("processing", orderMapper.selectCount(new LambdaQueryWrapper<WorkOrder>().eq(WorkOrder::getStatus, 3)));
        data.put("pendingConfirm", orderMapper.selectCount(new LambdaQueryWrapper<WorkOrder>().eq(WorkOrder::getStatus, 4)));
        data.put("completed", orderMapper.selectCount(new LambdaQueryWrapper<WorkOrder>().eq(WorkOrder::getStatus, 5)));

        // 今日新增
        LocalDateTime todayStart = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        data.put("todayNew", orderMapper.selectCount(new LambdaQueryWrapper<WorkOrder>().ge(WorkOrder::getCreateTime, todayStart)));

        // 超时工单
        data.put("overdue", orderMapper.selectCount(new LambdaQueryWrapper<WorkOrder>()
                .lt(WorkOrder::getSlaDeadline, LocalDateTime.now())
                .notIn(WorkOrder::getStatus, 5, 6)));

        return Result.success(data);
    }

    private void saveLog(Long orderId, Integer actionType, Integer fromStatus, Integer toStatus, String remark) {
        OrderLog log = new OrderLog();
        log.setOrderId(orderId);
        log.setOperatorId(securityUtils.getCurrentUserId());
        log.setOperatorRole(3);
        log.setActionType(actionType);
        log.setFromStatus(fromStatus);
        log.setToStatus(toStatus);
        log.setRemark(remark);
        logMapper.insert(log);
    }

    @Data
    public static class AssignRequest {
        private Long engineerId;
    }
}