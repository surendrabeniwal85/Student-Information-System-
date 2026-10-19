package com.example.sis;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
public class StudentInformationWebApplication {

    public static void main(String[] args) {
        StudentInformationSystem2.createTable();

        SpringApplication.run(
                StudentInformationWebApplication.class,
                args
        );
    }
}
