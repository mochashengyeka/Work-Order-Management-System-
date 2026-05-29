package com.workorder.controller.admin;

import com.workorder.dto.PageDTO;
import com.workorder.entity.SparePart;
import com.workorder.exception.Result;
import com.workorder.service.SparePartService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/part")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminPartController {

    private final SparePartService partService;

    @GetMapping("/list")
    public Result getPartList(PageDTO pageDTO, @RequestParam(required = false) String keyword) {
        return Result.success(partService.getPartPage(pageDTO, keyword));
    }

    @PostMapping("/add")
    public Result addPart(@RequestBody SparePart part) {
        partService.addPart(part);
        return Result.success("添加成功");
    }

    @PutMapping("/update")
    public Result updatePart(@RequestBody SparePart part) {
        partService.updatePart(part);
        return Result.success("更新成功");
    }

    @PutMapping("/stock/{id}")
    public Result updateStock(@PathVariable Long id, @RequestParam Integer quantity) {
        partService.updateStock(id, quantity);
        return Result.success("库存更新成功");
    }

    @DeleteMapping("/delete/{id}")
    public Result deletePart(@PathVariable Long id) {
        partService.deletePart(id);
        return Result.success("删除成功");
    }
}