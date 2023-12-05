package com.cuit.gcsj.model.dto.spark;

import com.cuit.gcsj.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * @author L
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class UserInput extends PageRequest implements Serializable {
    /**
     * 电影类型
     */
    private String movieType;
    /**
     * 电影名称
     */
    private String movieName;
    /**
     * 匹配方式
     */
    private Integer matchType;
}
