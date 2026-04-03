package es.rhms.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import es.rhms.models.Request;
import es.rhms.models.Request.TipoRequest;
import es.rhms.models.Request.EstadoRequest;
import es.rhms.repositories.RequestRepository;

@Service
public class RequestService {

	@Autowired
	private RequestRepository requestRepository;

	/**
	 * Busca una petición por su ID
	 * @param id ID de la petición
	 * @return Optional con la petición si existe
	 */
	public Optional<Request> findById(int id) {
		return requestRepository.findById(id);
	}

	/**
	 * Guarda una nueva petición en la base de datos
	 * @param request Petición a guardar
	 * @return Petición guardada con ID generado
	 */
	public Request save(Request request) {
		return requestRepository.save(request);
	}

	/**
	 * Obtiene las peticiones de tipo CLUB ordenadas:
	 * - Primero las pending (más recientes primero)
	 * - Luego las accepted y rejected (más recientes primero)
	 * @return Lista de peticiones de club ordenada
	 */
	public List<Request> findClubRequestsOrdered() {
		List<Request> requests = new ArrayList<>();
		requests.addAll(requestRepository.findByTipoAndEstadoOrderByCreatedAtDesc(TipoRequest.club, EstadoRequest.pending));
		requests.addAll(requestRepository.findByTipoAndEstadoNotOrderByCreatedAtDesc(TipoRequest.club, EstadoRequest.pending));
		return requests;
	}

	/**
	 * Obtiene las peticiones de tipo PARTNER para un club específico ordenadas:
	 * - Primero las pending (más recientes primero)
	 * - Luego las accepted y rejected (más recientes primero)
	 * @param clubName Nombre del club para filtrar
	 * @return Lista de peticiones de socio ordenada
	 */
	public List<Request> findPartnerRequestsByClubOrdered(String clubName) {
		List<Request> requests = new ArrayList<>();
		requests.addAll(requestRepository.findByTipoAndClbTargetAndEstadoOrderByCreatedAtDesc(TipoRequest.partner, clubName, EstadoRequest.pending));
		requests.addAll(requestRepository.findByTipoAndClbTargetAndEstadoNotOrderByCreatedAtDesc(TipoRequest.partner, clubName, EstadoRequest.pending));
		return requests;
	}

	/**
	 * Actualiza el estado de una petición y registra quién la procesó
	 * @param id ID de la petición
	 * @param nuevoEstado Nuevo estado (accepted o rejected)
	 * @param updatedBy ID del usuario que procesa la petición
	 * @return true si se actualizó correctamente, false si no se encontró
	 */
	public boolean updateEstado(int id, EstadoRequest nuevoEstado, int updatedBy) {
		Optional<Request> optional = requestRepository.findById(id);
		if (optional.isPresent()) {
			Request request = optional.get();
			request.setEstado(nuevoEstado);
			request.setUpdatedBy(updatedBy);
			requestRepository.save(request);
			return true;
		}
		return false;
	}

	/**
	 * Busca una petición pendiente de un usuario para un club específico
	 * @param tipo Tipo de petición (club o partner)
	 * @param email Email del usuario
	 * @param clbTarget Nombre del club
	 * @param estado Estado de la petición
	 * @return La petición si existe, null si no
	 */
	public Request findByTipoAndUsrEmailAndClbTargetAndEstado(TipoRequest tipo, String email, String clbTarget, EstadoRequest estado) {
		return requestRepository.findByTipoAndUsrEmailAndClbTargetAndEstado(tipo, email, clbTarget, estado);
	}

}