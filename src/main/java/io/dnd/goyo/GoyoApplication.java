package io.dnd.goyo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class GoyoApplication {

    public static void main(String[] args) {
        SpringApplication.run(GoyoApplication.class, args);
    }

}
