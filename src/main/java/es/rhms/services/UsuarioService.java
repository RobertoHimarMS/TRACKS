package es.rhms.services;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import es.rhms.models.Usuario;
import es.rhms.repositories.UsuarioRepository;

@Service
public class UsuarioService {

	@Autowired
	private UsuarioRepository usuarioRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	/**
	 * Registra un usuario encriptando la contraseña
	 */
	public Usuario registrar(Usuario usuario) {
		usuario.setPasswd(passwordEncoder.encode(usuario.getPasswd()));
		return usuarioRepository.save(usuario);
	}

	/**
	 * Guarda un usuario SIN encriptar la contraseña
	 * Usar cuando la contraseña ya tiene el formato correcto (ej: {noop}password)
	 */
	public Usuario save(Usuario usuario) {
		return usuarioRepository.save(usuario);
	}

	public Optional<Usuario> findById(int id) {
		return usuarioRepository.findById(id);
	}

	public Optional<Usuario> findByEmail(String email) {
		return usuarioRepository.findByEmail(email);
	}

	public Optional<Usuario> findByDni(String dni) {
		return usuarioRepository.findByDni(dni);
	}

	/**
	 * Reactiva un usuario (pone active=true)
	 * @param usuario Usuario a reactivar
	 * @return Usuario actualizado
	 */
	public Usuario reactivar(Usuario usuario) {
		usuario.setActive(true);
		return usuarioRepository.save(usuario);
	}

}