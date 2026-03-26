package es.rhms.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http, AuthenticationSuccessHandler loginSuccessHandler) throws Exception {

		http.authorizeHttpRequests((authorize) -> authorize
													.requestMatchers("/", "/index", "/home").permitAll()
													.requestMatchers("/css/**", "/js/**", "/imgs/**", "/icons/**").permitAll()
													.requestMatchers("/api/**").permitAll()											/* permite acceder /api/misclubs y /api/request/new */
													.requestMatchers("/contacto").permitAll()										/* cualquier visitante o logueado puede enviar un ticket */
													.requestMatchers("/newclub").permitAll()										/* cualquier visitante puede solicitar registro de club */
													.requestMatchers("/newpartner/**").permitAll()								/* cualquier visitante puede solicitar alta de socio */
													.requestMatchers("/club/{id}").permitAll()										/* cualquier visitante puede ver detalle de club */
													.requestMatchers("/club/newactivity/**", "/club/editactivity/**", "/api/activity/create", "/api/activity/update/**", "/api/activity/delete/**").hasRole("MANAGER")	/* solo managers pueden gestionar actividades */
													.requestMatchers("/club/newpublish/**", "/club/editpublish/**", "/api/publish/create", "/api/publish/update/**", "/api/publish/delete/**").hasRole("MANAGER")	/* solo managers pueden gestionar publicaciones */
													.requestMatchers("/club/newproduct/**", "/club/editproduct/**", "/api/product/create", "/api/product/update/**", "/api/product/delete/**").hasRole("MANAGER")	/* solo managers pueden gestionar productos */
													.requestMatchers("/user/**").authenticated()									/* solo usuarios logueados pueden acceder a /user/** */
													.anyRequest().authenticated())
			.formLogin((form) -> form
									.loginPage("/home")
									.loginProcessingUrl("/logginprocess")
									.successHandler(loginSuccessHandler)															/* Handler personalizado según rol */
									.permitAll())
			.logout((logout) -> logout
									.logoutUrl("/logout")
									.logoutSuccessUrl("/home")
									.invalidateHttpSession(true)
									.clearAuthentication(true)
									.permitAll())
			.sessionManagement(session -> session
											.sessionCreationPolicy(SessionCreationPolicy.ALWAYS));

		return http.build();
	}

	@Bean
	PasswordEncoder passwordEncoder() {																		/* en desarrollo, contraseñas en texto plano */
		return PasswordEncoderFactories.createDelegatingPasswordEncoder();
	}

}