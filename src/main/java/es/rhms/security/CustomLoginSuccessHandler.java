package es.rhms.security;

import java.io.IOException;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

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

	private static final int SYSTEM_CLUB_ID = 1;												/* ID del club "System" donde está el admin del sistema */

	public CustomLoginSuccessHandler(UsuarioService usuarioService, ClubService clubService, SociosService sociosService) {
		this.usuarioService = usuarioService;
		this.clubService = clubService;
		this.sociosService = sociosService;
	}

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
																	Authentication authentication) throws IOException, ServletException {

		String email = authentication.getName();
		String clubIdParam = request.getParameter("clubId");

		Usuario usuario = usuarioService.findByEmail(email).orElse(null);							/* Obtener el usuario */

		if (usuario == null) {
			response.sendRedirect("/home?error=user");
			return;
		}

		// Si no se seleccionó club, redirigir a página por defecto (se debe evitar validar exitsamente sin Club)
		if (clubIdParam == null || clubIdParam.isEmpty()) {
			response.sendRedirect("/home?error=userWithoutClub");
			return;
		}

		int clubId;
		try {
			clubId = Integer.parseInt(clubIdParam);
		} catch (NumberFormatException e) {
			response.sendRedirect("/home?error=errorInClubId");
			return;
		}

		// Obtener el rol del usuario en el club seleccionado
		String rol = sociosService.findUserRoleInClub(usuario.getIduser(), clubId);

		if (rol == null) {
			// El usuario no pertenece a este club
			response.sendRedirect("/home?error=userWithoutRoles");
			return;
		}

		// ACTUALIZAR SPRING SECURITY CON EL ROL CORRECTO DEL CLUB SELECCIONADO
		SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + rol.toUpperCase());
		UsernamePasswordAuthenticationToken newAuth = new UsernamePasswordAuthenticationToken(
				authentication.getPrincipal(),
				authentication.getCredentials(),
				List.of(authority)
		);
		SecurityContextHolder.getContext().setAuthentication(newAuth);

		// IMPORTANTE: Momento en el que se guardan en sesión las variables de Club, Usuario y Rol
		Club club = clubService.findById(clubId).orElse(null);
		request.getSession().setAttribute("userlogged", usuario);
		request.getSession().setAttribute("clublogged", club);
		request.getSession().setAttribute("rollogged", rol);

		// Redirigir según el rol y el club
		String redirectUrl;

		if ("admin".equals(rol) && clubId == SYSTEM_CLUB_ID) {
			// Admin del sistema (rol admin en club System) → página de administración
			redirectUrl = "/admin";
		} else {
			// Partner → página del club (solo visualización)
			redirectUrl = "/club/" + clubId;
		}

		response.sendRedirect(redirectUrl);
	}

}