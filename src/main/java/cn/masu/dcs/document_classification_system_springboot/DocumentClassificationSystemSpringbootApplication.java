package cn.masu.dcs.document_classification_system_springboot;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * @author zyq
 */
@SpringBootApplication
@ComponentScan("cn.masu.dcs")
@MapperScan("cn.masu.dcs.mapper")
public class DocumentClassificationSystemSpringbootApplication {

    public static void main(String[] args) {
        SpringApplication.run(DocumentClassificationSystemSpringbootApplication.class, args);
    }

}
