package com.cuit.gcsj.service.impl;

import com.cuit.gcsj.common.ErrorCode;
import com.cuit.gcsj.exception.BusinessException;
import com.cuit.gcsj.initializer.SparkInitializer;
import com.cuit.gcsj.model.dto.file.UploadFileRequest;
import com.cuit.gcsj.model.dto.spark.UserInput;
import com.cuit.gcsj.model.entity.Movie;
import com.cuit.gcsj.model.enums.HdfsUploadEnum;
import com.cuit.gcsj.model.enums.MatchTypeEnum;
import com.cuit.gcsj.model.prefix.SparkProperties;
import com.cuit.gcsj.service.SparkService;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import lombok.extern.slf4j.Slf4j;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.security.UserGroupInformation;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.PrivilegedExceptionAction;
import java.util.ArrayList;
import java.util.List;

/**
 * @author L
 */
@Slf4j
@Service
public class SparkServiceNewImpl implements SparkService {

    @Resource
    private SparkProperties sparkProperties;

    @Resource
    private SparkInitializer sparkInitializer; // ssh 连接启动就初始化

    @Override
    public List<Movie> filter(UserInput input) {
        if (input == null || (input.getMovieName().isEmpty() && input.getMovieType().isEmpty())
                || input.getMatchType() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户输入关键字不能为空");
        }
        Session session = null;
        List<Movie> movies = null;
        try {
            session = sparkInitializer.getSession();
            movies = executeRemoteCommand(session, input);
        } catch (JSchException | IOException e) {
            handleException(e);
        }
//         finally {
//            if (session != null && session.isConnected()) {
//                session.disconnect();
//            }
//        }
        return movies;
    }

    @Override
    public List<Movie> executeRemoteCommand(Session session, UserInput input) throws JSchException, IOException {
        String userInputName = input.getMovieName();
        String userInputType = input.getMovieType();
        Integer matchType = input.getMatchType(); //  根据用户的输入，选择不同的匹配方式
        MatchTypeEnum enumByValue = MatchTypeEnum.getEnumByValue(matchType);
        if (enumByValue == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户选择上传的类型不能为空");
        }
        String type = enumByValue.getText();
        ChannelExec channelExec = (ChannelExec) session.openChannel("exec");
        List<Movie> movies = new ArrayList<>();
        String command = sparkProperties.getCommand() + type;
        if (userInputName != null && !userInputName.isEmpty()) {
            command += " title:" + userInputName;
        }
        if (userInputType != null && !userInputType.isEmpty()) {
            command += " genres:" + userInputType.replace("|", "\\|");
        }
        log.info("command: " + command);
        channelExec.setCommand(command);
        channelExec.setInputStream(null);
        channelExec.setErrStream(System.err);

        InputStream in = channelExec.getInputStream();
        channelExec.connect();

        boolean startRecording = false;

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (startRecording) {
                    if ("======".equals(line)) {
                        break;
                    }
                    movies.add(parseMovie(line, matchType));
                } else if ("======".equals(line)) {
                    startRecording = true;
                }
                log.info(line);
            }
        }

        if (channelExec.isClosed()) {
            log.info("exit-status: " + channelExec.getExitStatus());
        }
        channelExec.disconnect();
        return movies;
    }


    @Override
    public Movie parseMovie(String line, Integer matchType) {
        String[] split = line.split(",");
        if (split.length >= 3) {
            return Movie.builder()
                    .movieId(Integer.valueOf(split[0]))
                    .movieName(split[1])
                    .movieType(split[2])
                    .filterType(matchType)
                    .build();
        }
        return null;
    }

    @Override
    public boolean uploadFile(MultipartFile multipartFile, UploadFileRequest uploadFileRequest) {
        Integer biz = uploadFileRequest.getBiz();
        HdfsUploadEnum enumByValue = HdfsUploadEnum.getEnumByValue(biz);
        String hdfsFilePath = "/data/";
        if (enumByValue == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户选择上传的类型不能为空");
        }
        String text = enumByValue.getText();
        hdfsFilePath += text;
        if(!text.equals(multipartFile.getOriginalFilename())){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户文件名必须与类型一样！此次应该为："+text);
        }
        String hdfsUri = sparkProperties.getHdfsUri();
        String hdfsUser = sparkProperties.getUser();
        Configuration conf = new Configuration();
        conf.set("fs.defaultFS", hdfsUri);
        // 设置HDFS的用户为root
        System.setProperty("HADOOP_user", hdfsUser);
        conf.set("hadoop.job.ugi", hdfsUser);
        try {
            // 设置用户信息为root
            UserGroupInformation.setConfiguration(conf);
            UserGroupInformation ugi = UserGroupInformation.createRemoteUser(hdfsUser);
            String finalHdfsFilePath = hdfsFilePath;
            ugi.doAs((PrivilegedExceptionAction<Void>) () -> {
                FileSystem fs = FileSystem.get(conf);
                // 创建HDFS文件路径
                Path hdfsPath = new Path(finalHdfsFilePath);
                // 检查文件是否存在，如果存在则删除
                if (fs.exists(hdfsPath)) {
                    fs.delete(hdfsPath, true); // true表示递归删除
                    log.info("已存在的文件已删除: " + hdfsPath);
                }
                // 获取输入流
                InputStream inputStream = multipartFile.getInputStream();
                // 创建HDFS上的文件并写入数据
                fs.create(hdfsPath).write(inputStream.readAllBytes());
                log.info("文件上传成功到: " + hdfsPath);
                // 关闭输入流和FileSystem
                inputStream.close();
                fs.close();
                return null;
            });
            return true;
        } catch (IOException | InterruptedException e) {
            log.error("上传文件到HDFS失败，详细信息：", e);
            throw new BusinessException(ErrorCode.OPERATION_ERROR,"上传文件到hdfs中失败！");
        }
    }
    private void handleException(Exception e) {
        log.error("An error occurred during Spark job execution.", e);
        throw new BusinessException(ErrorCode.OPERATION_ERROR, "执行Spark任务失败！");
    }

}

