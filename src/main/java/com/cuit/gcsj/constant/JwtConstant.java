package com.cuit.gcsj.constant;

/**
 * @author L
 */


public interface JwtConstant {
    String TOKEN_HEADER = "Babai-Token";
    String TOKEN_KEY = "huabai";
    Long TOKEN_TIME = 3600*24*30*1000L; // 1个月 jwt令牌过期时间
}
