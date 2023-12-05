package com.cuit.gcsj.model.dto.file;

import lombok.Data;

import java.io.Serializable;

/**
 * 文件上传请求
 *
 * @author <a href="https://github.com/lihz">ithe,itz</a>
 * @from 
 */
@Data
public class UploadFileRequest implements Serializable {

    /**
     * 业务
     */
    private Integer biz;

    private static final long serialVersionUID = 1L;
}