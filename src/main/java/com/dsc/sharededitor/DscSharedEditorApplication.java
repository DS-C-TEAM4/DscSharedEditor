package com.dsc.sharededitor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
public class DscSharedEditorApplication {

	public static void main(String[] args) {
		SpringApplication.run(DscSharedEditorApplication.class, args);
	}
}