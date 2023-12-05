package com.cuit.gcsj.service;

import com.cuit.gcsj.model.dto.file.UploadFileRequest;
import com.cuit.gcsj.model.dto.spark.UserInput;
import com.cuit.gcsj.model.entity.Movie;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * @author L
 * @description 针对表【user(用户表)】的数据库操作Service
 * @createDate 2023-10-30 15:37:58
 */
@Component
public interface SparkService  {
    List<Movie> filter(UserInput input);


    List<Movie> executeRemoteCommand(Session session, UserInput input) throws JSchException, IOException;

    Movie parseMovie(String line,Integer matchType);

    boolean uploadFile(MultipartFile multipartFile, UploadFileRequest uploadFileRequest);
}
