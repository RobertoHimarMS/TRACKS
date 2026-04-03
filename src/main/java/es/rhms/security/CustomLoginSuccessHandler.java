package es.rhms.security;

import java.io.IOException;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.context.SecurityContextRepository;

import es.rhms.models.Club;
import es.rhms.models.Usuario;
import es.rhms.services.ClubService;
import es.rhms.services.SociosService;
import es.rhms.services.UsuarioService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class CustomLoginSuccessHandler implements AuthenticationSuccessHandler {

	private final UsuarioService usuarioService;
	private final ClubService clubService;
	private final SociosService sociosService;
	private final SecurityContextRepository securityContextRepository;

	private static final int SYSTEM_CLUB_ID = 1;

	public CustomLoginSuccessHandler(UsuarioService usuarioService, ClubService clubService, SociosService sociosService, SecurityContextRepository securityContextRepository) {
		this.usuarioService = usuarioService;
		this.clubService = clubService;
		this.sociosService = sociosService;
		this.securityContextRepository = securityContextRepository;
	}

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
																			Authentication authentication) throws IOException, ServletException {

		String email = authentication.getName();
		String clubIdParam = request.getParameter("clubId");

		Usuario usuario = usuarioService.findByEmail(email).orElse(null);

		if (usuario == null) {
			response.sendRedirect(request.getContextPath() + "/home?error=user");
			return;
		}

		if (clubIdParam == null || clubIdParam.isEmpty()) {
			response.sendRedirect(request.getContextPath() + "/home?error=userWithoutClub");
			return;
		}

		int clubId;
		try {
			clubId = Integer.parseInt(clubIdParam);
		} catch (NumberFormatException e) {
			response.sendRedirect(request.getContextPath() + "/home?error=errorInClubId");
			return;
		}

		String rol = sociosService.findUserRoleInClub(usuario.getIduser(), clubId);

		if (rol == null) {
			response.sendRedirect(request.getContextPath() + "/home?error=userWithoutRoles");
			return;
		}

		// Crear nuevo UserDetails con el rol correcto del club seleccionado
		SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + rol.toUpperCase());
		UserDetails newUserDetails = User.builder()
				.username(email)
				.password(usuario.getPasswd())
				.authorities(List.of(authority))
				.accountExpired(false)
				.accountLocked(false)
				.credentialsExpired(false)
				.disabled(!usuario.isActive())
				.build();

		// Crear nueva autenticación con el rol correcto
		UsernamePasswordAuthenticationToken newAuth = new UsernamePasswordAuthenticationToken(
				newUserDetails,
				authentication.getCredentials(),
				List.of(authority)
			);

		// Crear un nuevo SecurityContext y establecer la autenticación
		SecurityContext context = SecurityContextHolder.createEmptyContext();
		context.setAuthentication(newAuth);
		SecurityContextHolder.setContext(context);

		// Guardar el contexto usando SecurityContextRepository (Spring Security 6)
		securityContextRepository.saveContext(context, request, response);

		// Guardar datos adicionales en sesión
		Club club = clubService.findById(clubId).orElse(null);
		request.getSession().setAttribute("userlogged", usuario);
		request.getSession().setAttribute("clublogged", club);
		request.getSession().setAttribute("rollogged", rol);

		String redirectUrl;
		if ("admin".equals(rol) && clubId == SYSTEM_CLUB_ID) {
			redirectUrl = request.getContextPath() + "/admin";
		} else {
			redirectUrl = request.getContextPath() + "/club/" + clubId;
		}

		response.sendRedirect(redirectUrl);
	}

}