package com.workorder.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.workorder.dto.PageDTO;
import com.workorder.entity.SparePart;
import com.workorder.vo.SparePartVO;

public interface SparePartService {
    IPage<SparePartVO> getPartPage(PageDTO pageDTO, String keyword);
    SparePartVO getPartById(Long id);
    void addPart(SparePart part);
    void updatePart(SparePart part);
    void updateStock(Long id, Integer quantity);
    void deletePart(Long id);
}