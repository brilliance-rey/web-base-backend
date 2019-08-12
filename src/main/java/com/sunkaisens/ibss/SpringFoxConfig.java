package com.sunkaisens.ibss;

import java.util.ArrayList;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.ApiKey;
import springfox.documentation.service.AuthorizationScope;
import springfox.documentation.service.Contact;
import springfox.documentation.service.SecurityReference;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;


/***
 * 指定API文档页的标题和描述信息等内容。
 * @param <Parameter>
 */
/*关键是在securitySchemes()方法配置里增加需要token的配置。
    配置完成后，swagger-ui.html里右上角会有一个Authorize的按钮，录入该token即能成功调用相关接口*/
@Configuration
@EnableSwagger2
public class SpringFoxConfig {
	@Bean
    public Docket createRestApi() {
	return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.sunkaisens.ibss"))//这里是controller所处的包名
                .paths(PathSelectors.any())
                .build()
                .securitySchemes(securitySchemes())
                .securityContexts(securityContexts());
               
    }
	/**
	 * 通过Swagger2的securitySchemes配置全局参数：如下列代码所示，securitySchemes的ApiKey中增加一个名为“Authentication”，type为“header”的参数。
	 *   
	 */
	private List<ApiKey> securitySchemes() {
		 List<ApiKey> apiKeyList= new ArrayList();
		 //注意下面的名字Authentication要和token请求的名字一直。 
		 apiKeyList.add(new ApiKey("Authentication", "Authentication", "header"));
		 return apiKeyList; 
	 }  
	//设置完成后进入SwaggerUI，右上角出现“Authorization”按钮，点击即可输入我们配置的参数。
	//对于不需要输入参数的接口（上文所述的包含auth的接口），在未输入Authorization参数就可以访问。
	//其他接口则将返回401错误。点击右上角“Authorization”按钮，输入配置的参数后即可访问。参数输入后全局有效，无需每个接口单独输入。
	//至此，完成Swagger2 非全局、无需重复输入的Head参数配置。
	//Swagger2的相关完整代码如下（工程基于Springboot）：
	private List<SecurityContext> securityContexts() {
		List<SecurityContext> securityContexts=new ArrayList<>();
		securityContexts.add(
                SecurityContext.builder()
                        .securityReferences(defaultAuth())
                        .forPaths(PathSelectors.regex("^(?!auth).*$"))
                        .build());
		return securityContexts;
    }

	List<SecurityReference> defaultAuth() {
        AuthorizationScope authorizationScope = new AuthorizationScope("global", "accessEverything");
        AuthorizationScope[] authorizationScopes = new AuthorizationScope[1];
        authorizationScopes[0] = authorizationScope;
        List<SecurityReference> securityReferences=new ArrayList<>();
        securityReferences.add(new SecurityReference("Authentication", authorizationScopes));
        return securityReferences;
    }
	
	//构建api文档的详细信息函数
    private ApiInfo apiInfo() {
        		 return new ApiInfoBuilder()
        	                .title("1808项目开发接口文档")    //接口文档标题
        	                .description("此文档仅供开发技术组人员使用")   //描述
        	                .termsOfServiceUrl("http://www.sunkaisens.com/")   //相关的网址
        	                .contact(new Contact("后端开发","http://www.sunkaisens.com/","sales@sunkaisens.com/"))    //作者  邮箱等
        	                .version("1.0")  //版本号
        	                .build();
    }
}
