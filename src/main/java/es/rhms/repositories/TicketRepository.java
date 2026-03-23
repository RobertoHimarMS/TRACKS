package es.rhms.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import es.rhms.models.Ticket;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Integer> {

	/**
	 * Obtiene todos los tickets no manejados ordenados por fecha de creación DESC
	 */
	List<Ticket> findByHandledFalseOrderByCreatedAtDesc();

	/**
	 * Obtiene todos los tickets manejados ordenados por fecha de creación DESC
	 */
	List<Ticket> findByHandledTrueOrderByCreatedAtDesc();

}