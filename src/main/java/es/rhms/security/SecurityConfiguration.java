package es.rhms.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.context.DelegatingSecurityContextRepository;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.RequestAttributeSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.web.multipart.support.MultipartFilter;

import jakarta.servlet.MultipartConfigElement;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http, AuthenticationSuccessHandler loginSuccessHandler, MultipartFilter multipartFilter) throws Exception {

		http
			// MultipartFilter se ejecuta ANTES de CsrfFilter para que el body esté parseado
			// y el token CSRF esté disponible para validación
			.addFilterBefore(multipartFilter, CsrfFilter.class)

			// CSRF habilitado para TODOS los endpoints
			.authorizeHttpRequests((authorize) -> authorize
				// === RECURSOS ESTÁTICOS (públicos) ===
				.requestMatchers("/", "/index", "/home").permitAll()
				.requestMatchers("/css/**", "/js/**", "/imgs/**", "/icons/**").permitAll()

				// === PÁGINAS PÚBLICAS ===
				.requestMatchers("/contacto").permitAll()
				.requestMatchers("/newclub").permitAll()
				.requestMatchers("/newpartner/**").permitAll()
				.requestMatchers("/club/{id}").permitAll()
				.requestMatchers("/avisosyusos", "/privacidad", "/cookies").permitAll()

				// === API PÚBLICA (sin autenticación) ===
				.requestMatchers("/api/misclubes").permitAll()
				.requestMatchers("/api/request/new").permitAll()
				.requestMatchers("/api/ticket/new").permitAll()

				// === API PROTEGIDA (requiere rol específico) ===
				.requestMatchers("/api/activity/create", "/api/activity/update/**", "/api/activity/delete/**").hasRole("MANAGER")
				.requestMatchers("/api/publish/create", "/api/publish/update/**", "/api/publish/delete/**").hasRole("MANAGER")
				.requestMatchers("/api/product/create", "/api/product/update/**", "/api/product/delete/**").hasRole("MANAGER")
				.requestMatchers("/api/request/ver/**", "/api/request/edit/**").hasAnyRole("MANAGER", "ADMIN")
				.requestMatchers("/api/ticket/ver/**", "/api/ticket/edit/**").hasRole("ADMIN")

				// === PÁGINAS PROTEGIDAS (requieren rol específico) ===
				.requestMatchers("/club/newactivity/**", "/club/editactivity/**").hasRole("MANAGER")
				.requestMatchers("/club/newpublish/**", "/club/editpublish/**").hasRole("MANAGER")
				.requestMatchers("/club/newproduct/**", "/club/editproduct/**").hasRole("MANAGER")
				.requestMatchers("/admin").hasRole("ADMIN")

				// === USUARIOS AUTENTICADOS ===
				.requestMatchers("/user/**").authenticated()

				// === CUALQUIER OTRA PETICIÓN ===
				.anyRequest().authenticated())
			.formLogin((form) -> form
				.loginPage("/home")
				.loginProcessingUrl("/logginprocess")
				.successHandler(loginSuccessHandler)
				.permitAll())
			.logout((logout) -> logout
				.logoutUrl("/logout")
				.logoutSuccessUrl("/home")
				.invalidateHttpSession(true)
				.clearAuthentication(true)
				.permitAll());

		return http.build();
	}

	@Bean
	PasswordEncoder passwordEncoder() {
		return PasswordEncoderFactories.createDelegatingPasswordEncoder();
	}

	@Bean
	SecurityContextRepository securityContextRepository() {
		return new DelegatingSecurityContextRepository(
			new RequestAttributeSecurityContextRepository(),
			new HttpSessionSecurityContextRepository()
		);
	}

	/**
	 * Configuración multipart para el dispatcher servlet.
	 * Necesario para que Tomcat embebido pueda procesar peticiones multipart.
	 * En Tomcat externo, ServletInitializer también aplica esta configuración.
	 */
	@Bean
	public MultipartConfigElement multipartConfigElement() {
		return new MultipartConfigElement(
			null,              // location - directorio temporal (null = default)
			10 * 1024 * 1024, // maxFileSize - 10MB
			10 * 1024 * 1024, // maxRequestSize - 10MB
			0                 // fileSizeThreshold - 0 bytes antes de escribir a disco
		);
	}

	@Bean
	MultipartFilter multipartFilter() {
		MultipartFilter filter = new MultipartFilter();
		filter.setMultipartResolverBeanName("multipartResolver");
		return filter;
	}

}