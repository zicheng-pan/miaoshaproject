package com.example.miaoshaproject;

import com.example.miaoshaproject.dao.UserDOMapper;
import com.example.miaoshaproject.dataobject.UserDO;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@SpringBootApplication(scanBasePackages = {"com.example.miaoshaproject"})
@MapperScan("com.example.miaoshaproject.dao")
public class MiaoshaprojectApplication {

    public static void main(String[] args) {
        SpringApplication.run(MiaoshaprojectApplication.class, args);
    }

}
