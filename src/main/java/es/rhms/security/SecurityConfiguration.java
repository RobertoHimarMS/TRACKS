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
													.requestMatchers("/api/**").permitAll()											/* permite acceder /api/misclubs */
													.requestMatchers("/contacto").permitAll()										/* cualquier visitante o logueado puede enviar un ticket */
													.requestMatchers("/club/newclub").permitAll()									/* cualquier visitante puede solicitar registro de club */
													.requestMatchers("/club/newuser/{idclub}").permitAll()						/* cualquier visitante puede solicitar alta de socio */
													.requestMatchers("/club/{id}").permitAll()										/* cualquier visitante puede ver detalle de club */
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