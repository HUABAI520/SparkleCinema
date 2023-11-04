package com.ithe.l2yh;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author L
 */
@MapperScan("com.ithe.l2yh.mapper")
@SpringBootApplication
public class L2yhApplication {

	public static void main(String[] args) {
		SpringApplication.run(L2yhApplication.class, args);
	}

}
