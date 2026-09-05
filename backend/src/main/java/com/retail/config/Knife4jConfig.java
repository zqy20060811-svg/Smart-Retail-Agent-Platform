package com.retail.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

/**
 * 接口文档配置，访问地址：http://localhost:8080/doc.html
 */
@Configuration
public class Knife4jConfig {

    @Bean
    public Docket docket() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(new ApiInfoBuilder()
                        .title("智能零售客服与订单协同平台 API")
                        .version("1.0.0")
                        .description("用户端 / 管理端 / Dify Agent Tools 接口文档")
                        .build())
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.retail.controller"))
                .build();
    }
}
