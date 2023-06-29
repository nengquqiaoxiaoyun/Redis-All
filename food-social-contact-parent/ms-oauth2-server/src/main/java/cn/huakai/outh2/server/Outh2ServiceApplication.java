package cn.huakai.outh2.server;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author: huakaimay
 * @since: 2023-06-28
 */
@SpringBootApplication
@MapperScan("cn.huakai.outh2.server.mapper")
public class Outh2ServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(Outh2ServiceApplication.class, args);
    }
}
