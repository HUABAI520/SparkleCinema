package com.cuit.gcsj.controller;

import com.cuit.gcsj.common.BaseResponse;
import com.cuit.gcsj.common.ErrorCode;
import com.cuit.gcsj.common.ResultUtils;
import com.cuit.gcsj.exception.BusinessException;
import com.cuit.gcsj.model.dto.file.UploadFileRequest;
import com.cuit.gcsj.model.dto.spark.UserInput;
import com.cuit.gcsj.model.entity.Movie;
import com.cuit.gcsj.service.SparkService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author L
 */
@Slf4j
@RestController
@RequestMapping("/spark")
public class SparkController {

    @Resource(name = "sparkServiceNewImpl")
    private SparkService sparkService;

    @PostMapping("/filter")
    public BaseResponse<List<Movie>> filter(@RequestBody UserInput input) {
        log.info("json:{}", input);
        List<Movie> movies = sparkService.filter(input);
//        List<Movie> movies = new ArrayList<>();
//        Integer matchType = input.getMatchType();
//        for (int i = 0; i < 100; i++) {
//            movies.add(Movie.builder()
//                    .movieId(i)
//                    .movieName("电影" + i)
//                    .movieType("电影类型" + i)
//                    .filterType(matchType)
//                    .build());
//        }
        return ResultUtils.success(movies);
    }

    @PostMapping("/upload")
    public BaseResponse<Boolean> uploadFileExcel(@RequestPart("file") MultipartFile multipartFile, UploadFileRequest uploadFileRequest) {
        if (multipartFile.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件不能为空");
        }
        boolean rs = sparkService.uploadFile(multipartFile, uploadFileRequest);
        return ResultUtils.success(rs);
    }
}
