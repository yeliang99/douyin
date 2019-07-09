package com.jxw;


import com.jxw.controller.interceptor.MiniInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@Configuration
public class WebMvcConfig extends WebMvcConfigurerAdapter {

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		registry.addResourceHandler("/**")
		.addResourceLocations("classpath:/META-INF/resources/")
				.addResourceLocations("file:E:/video_dev/");


	}
	@Bean(initMethod = "init")
	public ZKCuratorClient zkCuratorClient(){

		return new ZKCuratorClient();
	}
	//1.0将拦截器作为一个bean的形式注册
	@Bean
	public MiniInterceptor miniInterceptor(){
		return new MiniInterceptor();
	}

	//2.0重写添加拦截器方法
	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		//将自定义的拦截器添加进去,同时添加需要拦截的URL
		registry.addInterceptor(miniInterceptor()).addPathPatterns("/user/**");
		super.addInterceptors(registry);
	}
}
