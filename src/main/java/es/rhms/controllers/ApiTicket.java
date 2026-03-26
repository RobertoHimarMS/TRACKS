package es.rhms.controllers;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import es.rhms.models.Ticket;
import es.rhms.models.Usuario;
import es.rhms.services.TicketService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api/ticket")
public class ApiTicket {

	@Autowired
	private TicketService ticketService;

	/********************************************************************************/
	/*  MAPEO PARA VER TICKETS: Se llama desde consola de Administración del ADMIN  */
	/********************************************************************************/
	@GetMapping("/ver/{id}")
	public ResponseEntity<Ticket> verTicket(@PathVariable int id) {
		return ticketService.findById(id)
				.map(ResponseEntity::ok)
				.orElse(ResponseEntity.notFound().build());
	}

	/************************************************************************************/
	/*  MAPEO PARA MANEJAR TICKETS: Se llama desde consola de Administración del ADMIN  */
	/************************************************************************************/
	@PostMapping("/edit/{id}")
	public ResponseEntity<String> resolverTicket(@PathVariable int id, HttpServletRequest request) {
		// Obtener usuario de la sesión
		HttpSession session = request.getSession(false);
		if (session == null || session.getAttribute("userlogged") == null) {
			return ResponseEntity.status(401).body("No autorizado");
		}

		Usuario usuario = (Usuario) session.getAttribute("userlogged");
		int userId = usuario.getIduser();

		boolean actualizado = ticketService.markAsResolved(id, userId);
		if (actualizado) {
			return ResponseEntity.ok("Ticket resuelto correctamente");
		} else {
			return ResponseEntity.notFound().build();
		}
	}

	/********************************************************************************/
	/*  MAPEO PARA CREAR TICKETS: Se llama desde el formulario de contacto del Home  */
	/********************************************************************************/
	@PostMapping("/new")
	public ResponseEntity<Map<String, String>> crearTicket(@RequestParam String subject,
														   @RequestParam String email,
														   @RequestParam String description) {
		try {
			Ticket ticket = new Ticket();
			ticket.setSubject(subject);
			ticket.setEmail(email);
			ticket.setDescription(description);
			ticket.setHandled(false);

			ticketService.save(ticket);

			return ResponseEntity.ok(Map.of("status", "ok",
											"message", "Ticket enviado correctamente"));
		} 
		catch (Exception e) {
			return ResponseEntity.badRequest().body(Map.of("status", "error",
														   "message", "Error al enviar el ticket"));
		}
	}

}