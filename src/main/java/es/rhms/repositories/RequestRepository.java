package es.rhms.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import es.rhms.models.Request;
import es.rhms.models.Request.TipoRequest;
import es.rhms.models.Request.EstadoRequest;

@Repository
public interface RequestRepository extends JpaRepository<Request, Integer> {

	/**
	 * Obtiene las peticiones de tipo CLUB con estado PENDING ordenadas por fecha DESC
	 */
	List<Request> findByTipoAndEstadoOrderByCreatedAtDesc(TipoRequest tipo, EstadoRequest estado);

	/**
	 * Obtiene las peticiones de tipo CLUB que NO están pending (accepted o rejected)
	 */
	List<Request> findByTipoAndEstadoNotOrderByCreatedAtDesc(TipoRequest tipo, EstadoRequest estado);

	/**
	 * Obtiene las peticiones de tipo PARTNER para un club específico con un estado determinado
	 */
	List<Request> findByTipoAndClbTargetAndEstadoOrderByCreatedAtDesc(TipoRequest tipo, String clbTarget, EstadoRequest estado);

	/**
	 * Obtiene las peticiones de tipo PARTNER para un club específico que NO tienen el estado indicado
	 */
	List<Request> findByTipoAndClbTargetAndEstadoNotOrderByCreatedAtDesc(TipoRequest tipo, String clbTarget, EstadoRequest estado);

}