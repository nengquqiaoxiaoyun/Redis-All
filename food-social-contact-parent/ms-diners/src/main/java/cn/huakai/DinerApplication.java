package cn.huakai;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

/**
 * @author: huakaimay
 * @since: 2023-06-19
 */
@EnableEurekaClient
@SpringBootApplication
@MapperScan("cn.huakai.mapper")
public class DinerApplication {

    public static void main(String[] args) {
        SpringApplication.run(DinerApplication.class, args);
    }

}
