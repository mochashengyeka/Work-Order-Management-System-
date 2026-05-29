package com.workorder.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.workorder.dto.PageDTO;
import com.workorder.entity.SparePart;
import com.workorder.exception.BusinessException;
import com.workorder.mapper.SparePartMapper;
import com.workorder.service.SparePartService;
import com.workorder.vo.SparePartVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SparePartServiceImpl implements SparePartService {

    private final SparePartMapper partMapper;

    @Override
    public IPage<SparePartVO> getPartPage(PageDTO pageDTO, String keyword) {
        LambdaQueryWrapper<SparePart> wrapper = new LambdaQueryWrapper<SparePart>()
                .like(keyword != null, SparePart::getPartName, keyword)
                .or().like(keyword != null, SparePart::getPartCode, keyword)
                .orderByDesc(SparePart::getCreateTime);
        IPage<SparePart> page = partMapper.selectPage(
                new Page<>(pageDTO.getPage(), pageDTO.getSize()), wrapper);
        return page.convert(this::convertToVO);
    }

    @Override
    public SparePartVO getPartById(Long id) {
        SparePart part = partMapper.selectById(id);
        if (part == null) {
            throw new BusinessException("备件不存在");
        }
        return convertToVO(part);
    }

    @Override
    public void addPart(SparePart part) {
        part.setStatus(1);
        partMapper.insert(part);
    }

    @Override
    public void updatePart(SparePart part) {
        partMapper.updateById(part);
    }

    @Override
    public void updateStock(Long id, Integer quantity) {
        SparePart part = partMapper.selectById(id);
        if (part == null) {
            throw new BusinessException("备件不存在");
        }
        part.setStockQuantity(quantity);
        partMapper.updateById(part);
    }

    @Override
    public void deletePart(Long id) {
        SparePart part = partMapper.selectById(id);
        if (part == null) {
            throw new BusinessException("备件不存在");
        }
        partMapper.deleteById(id);
    }

    private SparePartVO convertToVO(SparePart part) {
        SparePartVO vo = new SparePartVO();
        BeanUtils.copyProperties(part, vo);
        return vo;
    }
}