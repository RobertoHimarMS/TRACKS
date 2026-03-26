package es.rhms.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import es.rhms.models.Actividad;
import es.rhms.repositories.ActividadRepository;

@Service
public class ActividadService {

	@Autowired
	private ActividadRepository actividadRepository;

	/**
	 * Obtiene todas las actividades de un club ordenadas por fecha descendente
	 * @param idclub ID del club
	 * @return Lista de actividades del club
	 */
	public List<Actividad> findByClubId(int idclub) {
		return actividadRepository.findByClubId(idclub);
	}

	/**
	 * Busca una actividad por su ID
	 * @param idactividad ID de la actividad
	 * @return La actividad o null si no existe
	 */
	public Actividad findById(int idactividad) {
		return actividadRepository.findById(idactividad).orElse(null);
	}

	/**
	 * Guarda una nueva actividad
	 * @param actividad Actividad a guardar
	 * @return Actividad guardada con ID generado
	 */
	public Actividad save(Actividad actividad) {
		return actividadRepository.save(actividad);
	}

	/**
	 * Elimina una actividad por su ID
	 * @param idactividad ID de la actividad a eliminar
	 */
	public void deleteById(int idactividad) {
		actividadRepository.deleteById(idactividad);
	}

	/**
	 * Inscribe un usuario en una actividad
	 * @param userId ID del usuario
	 * @param actividadId ID de la actividad
	 * @return true si se inscribió correctamente, false si ya estaba inscrito
	 */
	@Transactional
	public boolean inscribirUsuario(int userId, int actividadId) {
		try {
			int result = actividadRepository.inscribirUsuario(userId, actividadId);
			return result > 0;
		} catch (Exception e) {
			// Puede ser por duplicado (ya inscrito)
			return false;
		}
	}

	/**
	 * Desinscribe un usuario de una actividad
	 * @param userId ID del usuario
	 * @param actividadId ID de la actividad
	 * @return true si se desinscribió correctamente, false si no estaba inscrito
	 */
	@Transactional
	public boolean desinscribirUsuario(int userId, int actividadId) {
		try {
			int result = actividadRepository.desinscribirUsuario(userId, actividadId);
			return result > 0;
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * Elimina todas las inscripciones de una actividad
	 * @param actividadId ID de la actividad
	 */
	@Transactional
	public void deleteInscripcionesByActividadId(int actividadId) {
		actividadRepository.deleteInscripcionesByActividadId(actividadId);
	}

}