package es.rhms.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.context.SecurityContextRepository;

import es.rhms.services.ClubService;
import es.rhms.services.SociosService;
import es.rhms.services.UsuarioService;

@Configuration
public class HandlerConfiguration {

	@Bean
	AuthenticationSuccessHandler customLoginSuccessHandler(
			UsuarioService usuarioService,
			ClubService clubService,
			SociosService sociosService,
			SecurityContextRepository securityContextRepository) {

		return new CustomLoginSuccessHandler(usuarioService, clubService, sociosService, securityContextRepository);
	}
}