package com.workorder.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.workorder.entity.WorkOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface WorkOrderMapper extends BaseMapper<WorkOrder> {

    /** 统计各状态工单数量 */
    @Select("SELECT status, COUNT(*) as count FROM work_order GROUP BY status")
    List<Map<String, Object>> countByStatus();

    /** 查询超时工单 */
    @Select("SELECT * FROM work_order WHERE status IN (1,2,3) AND sla_deadline < #{now}")
    List<WorkOrder> findOverdueOrders(@Param("now") LocalDateTime now);
}