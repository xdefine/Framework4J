package com.bonocomms.xdefine.spring;

import java.io.File;
import java.util.Properties;

import javax.servlet.ServletContext;

import org.apache.velocity.app.VelocityEngine;
import org.hibernate.SessionFactory;
import org.reflections.Reflections;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import com.bonocomms.xdefine.XFContext;
import com.bonocomms.xdefine.base.XFInterceptor;
import com.bonocomms.xdefine.common.interceptor.RequestInterceptor;
import com.bonocomms.xdefine.file.AttachFileContext;
import com.bonocomms.xdefine.mail.SMTPMailSender;

@Configuration
@EnableWebMvc
@PropertySource("classpath:xdefine.properties")
@ComponentScan(basePackages = "${webapp.package}")
@Import(value = { SpringVelocityConfig.class })
@EnableScheduling
public class MvcConfig extends WebMvcConfigurerAdapter implements ApplicationContextAware {

	private ApplicationContext context;
	
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.context = applicationContext;
	}
	
	@Bean
	public AttachFileContext fileContext() {
		return new AttachFileContext();
	}

	@Bean
	public CommonsMultipartResolver multipartResolver() {
		return new CommonsMultipartResolver();
	}

	@Bean
	public ThreadPoolTaskExecutor taskExecutor() {
		ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
		taskExecutor.setCorePoolSize(5);
		taskExecutor.setMaxPoolSize(10);
		taskExecutor.setQueueCapacity(25);
		return taskExecutor;
	}
	
	@Bean
	public JavaMailSender javaMailSender() {

		Properties properties = new Properties();
		for(Object key : XFContext.keySet()) {
			if (!key.toString().startsWith("webapp.mail.smtp.")) continue;
			String rkey = key.toString().substring(key.toString().indexOf(".") + 1);
			properties.put(rkey, XFContext.getProperty(key.toString()).trim());
		}
		
		JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
		mailSender.setUsername(XFContext.getProperty("webapp.mail.username"));
		mailSender.setPassword(XFContext.getProperty("webapp.mail.password"));
		mailSender.setJavaMailProperties(properties);
		
		return mailSender;
	}
	
	@Bean
	public SMTPMailSender mailSender() {
		SMTPMailSender sender = new SMTPMailSender();
		sender.setJavaMailSender((JavaMailSender) context.getBean("javaMailSender"));
		sender.setVelocityEngine((VelocityEngine) context.getBean("velocityEngine"));
		return sender;
	}
	
	@Override
	public void addInterceptors(InterceptorRegistry registry) {

		registry.addInterceptor(new RequestInterceptor());

		Reflections reflections = new Reflections(XFContext.getProperty("webapp.package"));
		for (Class<? extends XFInterceptor> subType : reflections.getSubTypesOf(XFInterceptor.class)) {
			try {
				XFInterceptor interceptor = subType.newInstance();
				interceptor.setSessionFactory(context.getBean(SessionFactory.class));
				if (interceptor != null) registry.addInterceptor(interceptor);
			}
			catch(Exception ex) {
				ex.printStackTrace();
			}
		}

	}
	
	@Override
	public void addResourceHandlers(final ResourceHandlerRegistry registry) {
		try {
			
			ServletContext servletContext = context.getBean(ServletContext.class);
			File dir = new File(servletContext.getRealPath("/"));
			
			for (File file : dir.listFiles()) {
				String name = file.getName();
				if (name.toLowerCase().endsWith("-inf") || name.isEmpty()) continue;
				
				if (file.isDirectory()) {
					registry.addResourceHandler("/" + name + "/**").addResourceLocations("/" + name + "/");
				}
				else {
					registry.addResourceHandler("/" + name).addResourceLocations("/" + name);
				}
			}
		}
		catch(Exception ex) {
			ex.printStackTrace();
		}
	}
	
	

	

}
