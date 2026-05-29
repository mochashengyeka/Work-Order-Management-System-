package com.workorder.controller.common;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import com.workorder.exception.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Slf4j
@RestController
@RequestMapping("/api/common/file")
public class FileController {

    @Value("${upload.path:./uploads}")
    private String uploadPath;

    @PostMapping("/upload")
    public Result upload(@RequestParam("file") MultipartFile file) {
        try {
            // 生成存储目录（按日期）
            String dateDir = LocalDate.now().format(DateTimeFormatter.ISO_DATE);
            File dir = new File(uploadPath, dateDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            // 生成文件名
            String originalName = file.getOriginalFilename();
            String ext = originalName != null ?
                    originalName.substring(originalName.lastIndexOf(".")) : "";
            String newName = IdUtil.fastSimpleUUID() + ext;

            // 保存文件
            File dest = new File(dir, newName);
            file.transferTo(dest);

            // 返回访问 URL
            String url = "/uploads/" + dateDir + "/" + newName;
            return Result.success("上传成功", url);
        } catch (IOException e) {
            log.error("文件上传失败", e);
            return Result.error("文件上传失败");
        }
    }
}