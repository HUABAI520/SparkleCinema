//package com.cuit.gcsj.service.impl;
//
//import com.cuit.gcsj.common.ErrorCode;
//import com.cuit.gcsj.exception.BusinessException;
//import com.cuit.gcsj.model.dto.spark.UserInput;
//import com.cuit.gcsj.model.entity.Movie;
//import com.cuit.gcsj.service.SparkService;
//import com.jcraft.jsch.JSchException;
//import com.jcraft.jsch.Session;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.spark.SparkConf;
//import org.apache.spark.api.java.JavaRDD;
//import org.apache.spark.api.java.JavaSparkContext;
//import org.springframework.stereotype.Component;
//
//import javax.annotation.Resource;
//import java.io.IOException;
//import java.util.List;
//import java.util.concurrent.CompletableFuture;
//
///**
// * @author L
// */
//
//@Component
//@Slf4j
//@Deprecated
//public class SparkServiceImpl implements SparkService {
//
//    @Resource
//    private RedisServiceImpl redisService;
//
//    @Override
//    public List<Movie> filter(UserInput input) {
//        if (input == null || (input.getMovieName().isEmpty() && input.getMovieType().isEmpty())
//                || input.getMatchType()==null) {
//            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户输入关键字不能为空");
//        }
//        List<String> moviesData;
//        JavaRDD<String> moviesRdd;
//        List<Movie> result;
//        // 从 Redis 获取数据
//        moviesData = redisService.getMoviesData("movies");
//        // 如果 Redis 中没有数据，则从 HDFS 读取 movies.csv 文件
//        SparkConf conf = new SparkConf()
//                .setAppName("DataFiltering")
//                .setMaster("spark://192.168.165.112:7077")
//                .set("spark.executor.memory", "4g");
//        try ( // 创建 JavaSparkContext
//              JavaSparkContext sc = new JavaSparkContext(conf)
//        ) {
//            if (moviesData == null || moviesData.isEmpty()) {
//                moviesRdd = sc.textFile("hdfs://192.168.165.112:9000/data/movies.csv");
//                moviesData = moviesRdd.collect(); // 将 RDD 转换为 List
//            } else {
//                moviesRdd = sc.parallelize(moviesData);
//            }
//            if (moviesRdd == null) {
//                throw new BusinessException(ErrorCode.OPERATION_ERROR, "操作失败，未读取到movies.csv");
//            }
////            result = doFilter(moviesRdd,input);
////            // 输出过滤后的数据到控制台
////            result.forEach(System.out::println);
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//        // 异步操作
//        // 保存数据到 Redis
//        List<String> finalMoviesData = moviesData;
//        CompletableFuture.runAsync(() -> {
//            try {
//                if (finalMoviesData != null && !finalMoviesData.isEmpty()) {
//                    // 异步存入 Redis
//                    redisService.saveMoviesData(finalMoviesData, "movies");
//                }
//            } catch (Exception e) {
//                log.error("异步存入 Redis 失败", e);
//                // 处理异常
//            }
//        });
//        return null;
//    }
//
//    @Override
//    public List<Movie> executeRemoteCommand(Session session, UserInput input) throws JSchException, IOException {
//        return null;
//    }
//
//    @Override
//    public Movie parseMovie(String line) {
//        return null;
//    }
////    @Override
////    public List<Movie> doFilter(JavaRDD<String> moviesRdd,UserInput input) {
////        // 根据用户输入的参数进行过滤
////        String userInputName = input.getMovieName();
////        String userInputType = input.getMovieType();
////        JavaRDD<String> filteredData = moviesRdd.filter(line ->
////                (userInputName.isEmpty() || line.contains(userInputName))
////                        && (userInputType.isEmpty() || line.contains(userInputType))
////        );
//////        List<String> filteredList = filteredData.collect();
////        filteredData.foreach(System.out::println);
////        return null;
////    }
//}
