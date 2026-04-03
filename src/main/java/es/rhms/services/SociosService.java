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
import es.rhms.repositories.ActividadRepository;
import es.rhms.repositories.SociosRepository;
import es.rhms.repositories.UsuarioRepository;


@Service
public class SociosService {

	@Autowired
	private SociosRepository sociosRepository;

	@Autowired
	private UsuarioRepository usuarioRepository;

	@Autowired
	private ClubService clubService;

	@Autowired
	private ActividadRepository actividadRepository;

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

	/**
	 * Obtiene todas las relaciones de socio de un usuario
	 * @param usuario Usuario del que obtener los socios
	 * @return Lista de relaciones Socios del usuario
	 */
	@Transactional(readOnly = true)
	public List<Socios> findByUsuario(Usuario usuario) {
		return sociosRepository.findByUsuario(usuario);
	}

	/**
	 * Da de baja a un socio o manager de un club
	 * - Si es manager: elimina el club completo (incluye socios, actividades, publicaciones, productos)
	 * - Si es partner: da de baja al socio y elimina sus inscripciones en actividades del club
	 * - En ambos casos: si el usuario no tiene más clubes activos, se desactiva
	 *
	 * @param userId ID del usuario
	 * @param clubId ID del club
	 * @return true si la baja fue exitosa, false si el usuario no existe o no está activo
	 */
	@Transactional
	public boolean deletePartnerOrManager(int userId, int clubId) {
		// 1. Verificar usuario activo
		Usuario usuario = usuarioRepository.findById(userId).orElse(null);
		if (usuario == null || !usuario.isActive()) {
			return false;
		}

		// 2. Obtener rol del usuario en el club
		String rol = findUserRoleInClub(userId, clubId);
		if (rol == null) {
			return false;
		}

		// 3. Flujos separados según rol
		if ("manager".equals(rol)) {
			// Manager: eliminar club (ya incluye baja de socios e inscripciones)
			clubService.deleteClub(clubId, userId);

			// Verificar si tiene otros clubes activos y desactivar usuario si corresponde
			long clubesActivos = sociosRepository.countActiveByUserId(userId);
			if (clubesActivos == 0) {
				usuarioRepository.deactivateUser(userId);
			}
		} else {
			// Partner: dar de baja al socio en este club
			sociosRepository.unsubscribeUserFromClub(userId, clubId);

			// Verificar si tiene otros clubes activos y desactivar usuario si corresponde
			long clubesActivos = sociosRepository.countActiveByUserId(userId);
			if (clubesActivos == 0) {
				usuarioRepository.deactivateUser(userId);
			}

			// Eliminar inscripciones del usuario en actividades de este club
			actividadRepository.deleteInscripcionesByUserAndClub(userId, clubId);
		}

		return true;
	}

}