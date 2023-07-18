package com.admin.api.controller;

import cn.hutool.core.util.*;
import com.admin.api.config.BusinessException;
import com.admin.api.config.ControllerExceptionHandler;
import com.admin.api.constant.Result;
import com.admin.api.constant.ResultEnums;
import com.admin.api.utils.FileUtil;
import com.admin.api.utils.ResultUtil;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * 上传相关
 * @author wang_dgang
 * @since 2019-07-26 10:51:00
 */
@RestController
@RequestMapping("/upload")
public class UploadController {

    /**
     * 检查文件数量
     * @param md5
     * @return
     */
    @PostMapping("/v1/file/check/{md5}")
    @ControllerExceptionHandler
    public Result<Integer> fileCheck(@PathVariable("md5") String md5) {
        if (StrUtil.isEmpty(md5)) {
            throw new BusinessException(ResultEnums.FAILED.getCode(), "md5值不能为空");
        }
        int count = FileUtil.fileCount(md5);
        if (count > 0) {
            // 未确保最后一个切片文件完整，直接重新上传最后一个切片
            count--;
        }
        return ResultUtil.success(count);
    }

    /**
     * 分片上传临时文件
     * @param part
     * @param file
     * @param md5
     * @return
     */
    @PostMapping("/v1/chunk/{part}")
    @ControllerExceptionHandler
    public Result<String> uploadChunk(
            @PathVariable("part") String part,
            @RequestParam("file") MultipartFile file,
            @RequestParam("md5") String md5) {
        try {
            // TODO test
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            // 检查文件是否存在，存在直接返回成功
            /*boolean exists = FileUtil.fileExists(md5, part);
            if (exists) {
                return ResultUtil.success();
            }*/
            // 将切片文件存放的系统临时目录，以md5值为临时文件夹名，切片索引为文件名
            FileUtil.multipartFile2File(file, md5, part);
        } catch (IOException e) {
            throw new BusinessException(ResultEnums.FAILED.getCode(), String.format("切片%s-%s上传失败", md5, part));
        }
        return ResultUtil.success();
    }

    /**
     * 文件合并
     * @param md5 文件md5，作为存放切片文件的文件夹
     * @param chunks 切片数量
     * @param suffix 文件后缀，如：.png
     * @return
     */
    @PostMapping("/v1/file/merge/{md5}")
    @ControllerExceptionHandler
    public Result<String> fileMerge(
            @PathVariable("md5") String md5,
            @RequestParam("chunks") int chunks,
            @RequestParam("suffix") String suffix) {
        if (StrUtil.isEmpty(md5)) {
            throw new BusinessException(ResultEnums.FAILED.getCode(), "md5值不能为空");
        }
        try {
            // 随机生成文件名
            String resultName = String.format("%s%s", RandomUtil.randomString(6), suffix.toLowerCase());
            // 文件合并
            FileUtil.fileMerge(md5, chunks, resultName);
            // 返回文件名
            return ResultUtil.success(ResultEnums.SUCC_UPLOAD, resultName);
        } catch (BusinessException e) {
            throw e;
        } catch (IOException e) {
            throw new BusinessException(ResultEnums.FAILED.getCode(), "文件合并失败");
        }
    }
}