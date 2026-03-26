package es.rhms.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import es.rhms.models.Publicacion;
import es.rhms.repositories.PublicacionRepository;

@Service
public class PublicacionService {

	@Autowired
	private PublicacionRepository publicacionRepository;

	/**
	 * Obtiene todas las publicaciones de un club ordenadas por fecha descendente
	 * @param idclub ID del club
	 * @return Lista de publicaciones del club
	 */
	public List<Publicacion> findByClubId(int idclub) {
		return publicacionRepository.findByClubId(idclub);
	}

	/**
	 * Guarda una nueva publicación
	 * @param publicacion Publicación a guardar
	 * @return Publicación guardada con ID generado
	 */
	public Publicacion save(Publicacion publicacion) {
		return publicacionRepository.save(publicacion);
	}

	/**
	 * Busca una publicación por su ID
	 * @param idpublicacion ID de la publicación
	 * @return La publicación o null si no existe
	 */
	public Publicacion findById(int idpublicacion) {
		return publicacionRepository.findById(idpublicacion).orElse(null);
	}

	/**
	 * Elimina una publicación por su ID
	 * @param idpublicacion ID de la publicación a eliminar
	 */
	public void deleteById(int idpublicacion) {
		publicacionRepository.deleteById(idpublicacion);
	}

}