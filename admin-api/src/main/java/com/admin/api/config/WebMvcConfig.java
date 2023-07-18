package com.admin.api.config;

import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson.support.config.FastJsonConfig;
import com.alibaba.fastjson.support.spring.FastJsonHttpMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.filter.FormContentFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

import java.util.ArrayList;
import java.util.List;

/**
 * 修改web配置
 * @author wang_dgang
 * @since 2018-10-22 15:51:45
 */
@Configuration
public class WebMvcConfig extends WebMvcConfigurationSupport {
	
	/**
	 * 跨域配置
	 */
	@Override
	protected void addCorsMappings(CorsRegistry registry) {
		registry.addMapping("/**")
			// 允许跨域的源
			.allowedOriginPatterns("*")
			// 是否允许浏览器发送Cookie
			.allowCredentials(true)
			// 客户端所要访问的资源允许使用的方法或方法列表
			.allowedMethods("OPTIONS", "HEAD", "POST", "GET", "PUT", "DELETE")
			// 正式请求的首部信息
			// x-requested-with：ajax请求
			.allowedHeaders("X-Requested-With")
			// preflight request （预检请求）的返回结果
			//（即 Access-Control-Allow-Methods 和Access-Control-Allow-Headers 提供的信息）
			// 可以被缓存多久
			.maxAge(3600);
		super.addCorsMappings(registry);
	}
	/**
	 * 静态资源处理
	 * 如：Swagger页面
	 */
	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		super.addResourceHandlers(registry);
		registry.addResourceHandler("/**")
				.addResourceLocations("classpath:/static/");
	}
	/**
	 * FastJson对响应数据处理的相关配置
	 */
	@Override
	public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
		FastJsonHttpMessageConverter fastConverter = new FastJsonHttpMessageConverter();
		FastJsonConfig fastJsonConfig = new FastJsonConfig();
		// 序列化
		fastJsonConfig.setSerializerFeatures(
			SerializerFeature.WriteMapNullValue,
			SerializerFeature.WriteNullStringAsEmpty,
			// SerializerFeature.WriteNullNumberAsZero,
			SerializerFeature.WriteNullListAsEmpty
		);
		// 时间戳格式
		fastJsonConfig.setDateFormat("yyyy-MM-dd HH:mm:ss");
		// 处理中文乱码问题
		List<MediaType> fastMediaTypes = new ArrayList<>();
		fastMediaTypes.add(MediaType.APPLICATION_JSON);
		fastConverter.setSupportedMediaTypes(fastMediaTypes);
		// 设置
		fastConverter.setFastJsonConfig(fastJsonConfig);
		converters.add(fastConverter);
	}
	/**
	 * 解决put请求无法将参数转为对象
	 * @return
	 */
	@Bean
	public FormContentFilter httpPutFormContentFilter() {
		return new FormContentFilter();
	}
}
