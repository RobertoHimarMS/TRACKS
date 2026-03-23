package es.rhms.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

}