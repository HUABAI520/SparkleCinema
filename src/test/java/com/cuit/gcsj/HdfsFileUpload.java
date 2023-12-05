package com.cuit.gcsj;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.security.UserGroupInformation;

import java.security.PrivilegedExceptionAction;
@Deprecated
public class HdfsFileUpload {

    @Deprecated
    public static void main(String[] args) {
        // HDFS地址
        String hdfsUri = "hdfs://192.168.189.112:9000";
        // 本地文件路径
        String localFilePath = "E:\\code\\javaee\\SparkleCinema\\src\\main\\resources\\banner.txt";
        // HDFS目标路径
        String hdfsFilePath = "/test/";

        Configuration conf = new Configuration();
        // 设置HDFS地址
        conf.set("fs.defaultFS", hdfsUri);
        // 设置HDFS的用户为root
        System.setProperty("HADOOP_USER_NAME", "root");
        conf.set("hadoop.job.ugi", "root");
        try {
            // 设置用户信息为root
            UserGroupInformation.setConfiguration(conf);
            UserGroupInformation ugi = UserGroupInformation.createRemoteUser("root");
            ugi.doAs((PrivilegedExceptionAction<Void>) () -> {
                FileSystem fs = FileSystem.get(conf);
                // 创建HDFS文件路径
                Path hdfsPath = new Path(hdfsFilePath);
                // 创建本地文件路径
                Path localPath = new Path(localFilePath);

                // 将本地文件上传到HDFS
                fs.copyFromLocalFile(localPath, hdfsPath);
                System.out.println("文件上传成功到: " + hdfsPath);

                // 关闭FileSystem
                fs.close();
                return null;
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
