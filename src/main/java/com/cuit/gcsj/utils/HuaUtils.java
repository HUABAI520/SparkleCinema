package com.cuit.gcsj.utils;

import org.springframework.beans.BeanUtils;

import java.lang.reflect.Field;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author L
 * @description 花白定义工具类
 */
public class HuaUtils {


    /**
     * 转换list对象工具
     *
     * @param sourceList
     * @param targetClass
     * @param <T>
     * @param <R>
     * @return
     */
    public static <T, R> List<R> convertList(List<T> sourceList, Class<R> targetClass) {
        return sourceList.stream()
                .map(source -> {
                    try {
                        R target = targetClass.getDeclaredConstructor().newInstance();
                        BeanUtils.copyProperties(source, target);
                        return target;
                    } catch (Exception e) {
                        throw new RuntimeException("Error converting object: " + e.getMessage());
                    }
                })
                .collect(Collectors.toList());
    }

    /**
     * 判断是否有更新字段工具
     *
     * @param t 更新对象
     * @return
     */
    public static <T> boolean hasNonEmptyFields(T t) {
        int i = 0;
        for (Field field : t.getClass().getDeclaredFields()) {
            i++;// 跳过 id 字段
            field.setAccessible(true);
            try {
                if (i != 1 && !"serialVersionUID".equals(field.getName())) {
                    Object value = field.get(t);
                    if (value != null && !"".equals(value.toString())) {
                        // 如果至少一个字段不为空，表示有更新字段
                        return true;
                    }
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        // 如果所有字段（除了 id 外）都为空，表示没有更新字段
        return false;
    }




}
