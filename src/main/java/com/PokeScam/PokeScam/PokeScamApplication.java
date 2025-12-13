package com.PokeScam.PokeScam;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;

@SpringBootApplication
public class PokeScamApplication implements WebMvcConfigurer {
	private final LocaleChangeInterceptor localeChangeInterceptor;

	public PokeScamApplication(LocaleChangeInterceptor localeChangeInterceptor) {
		this.localeChangeInterceptor = localeChangeInterceptor;
	}

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(localeChangeInterceptor);
	}

	public static void main(String[] args) {
		SpringApplication.run(PokeScamApplication.class, args);
	}
}