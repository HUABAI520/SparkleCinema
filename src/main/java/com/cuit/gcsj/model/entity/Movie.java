package com.cuit.gcsj.model.entity;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

/**
 * @author L
 */
@Builder
@Data
public class Movie implements Serializable {
    /**
     * 电影id
     */
    private Integer movieId;
    /**
     * 电影名称
     */
    private String movieName;
    /**
     * 电影类型
     */
    private String movieType;

    private Integer  filterType;

    private static final long serialVersionUID = 1L;

}
