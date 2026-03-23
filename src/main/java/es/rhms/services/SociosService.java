package es.rhms.services;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import es.rhms.models.Club;
import es.rhms.models.Socios;
import es.rhms.models.Socios.RolSocio;
import es.rhms.models.Usuario;
import es.rhms.repositories.SociosRepository;
import es.rhms.repositories.UsuarioRepository;


@Service
public class SociosService {

	@Autowired
	private SociosRepository sociosRepository;

	@Autowired
	private UsuarioRepository usuarioRepository;

	/**
	 * Obtiene los clubes donde un usuario es socio activo (no dado de baja)
	 * @param userEmail Email del usuario
	 * @return Lista de clubes donde el usuario es socio
	 */
	@Transactional(readOnly = true)
	public List<Club> findClubsByUserEmail(String userEmail) {
		List<Club> clubes = new ArrayList<>();

		Usuario usuario = usuarioRepository.findByEmail(userEmail).orElse(null);
		if (usuario == null) {
			return clubes;
		}

		List<Socios> sociosList = sociosRepository.findByUsuario(usuario);
		for (Socios socio : sociosList) {
			// Solo incluir clubes activos y donde el usuario no esté dado de baja
			if (socio.getClub().isActive() && socio.getUnsuscribedAt() == null) {
				clubes.add(socio.getClub());
			}
		}

		return clubes;
	}

	/**
	 * Obtiene el rol de un usuario en un club específico
	 * @param userId ID del usuario
	 * @param clubId ID del club
	 * @return Rol del usuario (admin, manager, partner) o null si no pertenece
	 */
	@Transactional(readOnly = true)
	public String findUserRoleInClub(int userId, int clubId) {
		Usuario usuario = usuarioRepository.findById(userId).orElse(null);
		if (usuario == null) {
			return null;
		}

		// Buscar en todos los clubes del usuario
		List<Socios> sociosList = sociosRepository.findByUsuario(usuario);
		for (Socios socio : sociosList) {
			if (socio.getClub().getIdclub() == clubId && socio.getUnsuscribedAt() == null) {
				return socio.getRol().name();
			}
		}

		return null;
	}

	/**
	 * Obtiene el ID de usuario por su email
	 * @param userEmail Email del usuario
	 * @return ID del usuario o -1 si no existe
	 */
	public int findUserIdByEmail(String userEmail) {
		return usuarioRepository.findByEmail(userEmail)
				.map(Usuario::getIduser)
				.orElse(-1);
	}

	/**
	 * Crea una relación socio entre un usuario y un club
	 * @param usuario Usuario (entidad gestionada)
	 * @param club Club (entidad gestionada)
	 * @param rol Rol del socio en el club
	 * @return Socios creado
	 */
	public Socios createSocio(Usuario usuario, Club club, RolSocio rol) {
		Socios socio = new Socios();
		socio.setUsuario(usuario);
		socio.setClub(club);
		socio.setRol(rol);
		// registered_at usa DEFAULT CURRENT_TIMESTAMP en BD (ver @Column insertable=false)
		return sociosRepository.save(socio);
	}

	/**
	 * Verifica si un usuario pertenece a un club (como socio activo, no dado de baja)
	 * @param userId ID del usuario
	 * @param clubId ID del club
	 * @return true si el usuario es socio activo del club
	 */
	@Transactional(readOnly = true)
	public boolean isUserInClub(int userId, int clubId) {
		return sociosRepository.existsByUsuarioIduserAndClubIdclubAndUnsuscribedAtIsNull(userId, clubId);
	}

}