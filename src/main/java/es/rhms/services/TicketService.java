package es.rhms.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import es.rhms.models.Ticket;
import es.rhms.repositories.TicketRepository;

@Service
public class TicketService {

	@Autowired
	private TicketRepository ticketRepository;

	/**
	 * Guarda un nuevo ticket en la base de datos
	 * @param ticket Ticket a guardar
	 * @return Ticket guardado con ID generado
	 */
	public Ticket save(Ticket ticket) {
		return ticketRepository.save(ticket);
	}

	/**
	 * Busca un ticket por su ID
	 * @param id ID del ticket
	 * @return Optional con el ticket si existe
	 */
	public Optional<Ticket> findById(int id) {
		return ticketRepository.findById(id);
	}

	/**
	 * Obtiene todos los tickets ordenados: primero no manejados (DESC), luego manejados (DESC)
	 * @return Lista de tickets ordenada
	 */
	public List<Ticket> findAllOrdered() {
		List<Ticket> tickets = new ArrayList<>();
		tickets.addAll(ticketRepository.findByHandledFalseOrderByCreatedAtDesc());
		tickets.addAll(ticketRepository.findByHandledTrueOrderByCreatedAtDesc());
		return tickets;
	}

	/**
	 * Marca un ticket como resuelto
	 * @param id ID del ticket
	 * @param userId ID del usuario que resuelve el ticket
	 * @return true si se actualizó correctamente, false si no se encontró
	 */
	public boolean markAsResolved(int id, int userId) {
		Optional<Ticket> optional = ticketRepository.findById(id);
		if (optional.isPresent()) {
			Ticket ticket = optional.get();
			ticket.setHandled(true);
			ticket.setUpdatedBy(userId);
			ticketRepository.save(ticket);
			return true;
		}
		return false;
	}

}