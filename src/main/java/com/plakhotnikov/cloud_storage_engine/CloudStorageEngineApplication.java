package com.plakhotnikov.cloud_storage_engine;

import com.plakhotnikov.cloud_storage_engine.properties.JwtProperties;
import com.plakhotnikov.cloud_storage_engine.security.User;
import com.plakhotnikov.cloud_storage_engine.security.UserRepository;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootApplication
public class CloudStorageEngineApplication {

    public static void main(String[] args) {
        var context = SpringApplication.run(CloudStorageEngineApplication.class, args);
        System.out.println((new BCryptPasswordEncoder().encode("123")));
        var repo = context.getBean(UserRepository.class);
        var x = repo.findById(1L);

    }

}
