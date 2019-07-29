package com.sunkaisens.ibss;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;



/***
 * 指定API文档页的标题和描述信息等内容。
 */
@Configuration
@EnableSwagger2
//@ConditionalOnExpression("${swagger.enable}") //开启访问接口文档的权限
public class SpringFoxConfig {

	@Bean
    public Docket createRestApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.sunkaisens.ibss.system.controller"))//这里是controller所处的包名
                .paths(PathSelectors.any())
                .build();
    }
    //构建api文档的详细信息函数
    private ApiInfo apiInfo() {
        		 return new ApiInfoBuilder()
        	                .title("xxx项目开发接口文档")    //接口文档标题
        	                .description("此文档仅供开发技术组领导、开发人员使用")   //描述
        	                .termsOfServiceUrl("http://www.xxx.com/")   //相关的网址
        	                .contact(new Contact("后端开发","http://www.xxx.com/","8xxxxx67@qq.com"))    //作者  邮箱等
        	                .version("1.0")  //版本号
        	                .build();
    }

	
}
