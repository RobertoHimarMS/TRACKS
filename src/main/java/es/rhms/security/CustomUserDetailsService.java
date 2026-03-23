package es.rhms.security;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import es.rhms.models.Usuario;
import es.rhms.repositories.UsuarioRepository;

@Service
public class CustomUserDetailsService implements UserDetailsService {

	@Autowired
	private UsuarioRepository usuarioRepository;

	@Override
	public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
		Usuario usuario = usuarioRepository.findByEmail(email)
				.orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + email));

		// Rol temporal PARTNER - El rol real se asigna en CustomLoginSuccessHandler
		// según el club seleccionado por el usuario en el login
		SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_PARTNER");

		return User.builder()
				.username(usuario.getEmail())
				.password(usuario.getPasswd())
				.authorities(List.of(authority))
				.accountExpired(false)
				.accountLocked(false)
				.credentialsExpired(false)
				.disabled(!usuario.isActive())
				.build();
	}

}