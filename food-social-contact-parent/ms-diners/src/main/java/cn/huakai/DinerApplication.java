package cn.huakai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

/**
 * @author: huakaimay
 * @since: 2023-06-19
 */
@EnableEurekaClient
@SpringBootApplication
public class DinerApplication {

    public static void main(String[] args) {
        SpringApplication.run(DinerApplication.class, args);
    }

}
