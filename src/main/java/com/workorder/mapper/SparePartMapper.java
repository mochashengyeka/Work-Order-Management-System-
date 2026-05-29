package com.workorder.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.workorder.entity.SparePart;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SparePartMapper extends BaseMapper<SparePart> {
    // 所有基础 CRUD 方法由 BaseMapper 自动提供
}