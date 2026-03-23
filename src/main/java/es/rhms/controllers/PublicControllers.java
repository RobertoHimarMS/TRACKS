package es.rhms.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import es.rhms.models.Club;
import es.rhms.models.Ticket;
import es.rhms.services.ClubService;
import es.rhms.services.SociosService;
import es.rhms.services.TicketService;

@Controller
public class PublicControllers {

	@Autowired
	private ClubService clubService;

	@Autowired
	private TicketService ticketService;

	@Autowired
	private SociosService sociosService;

	@GetMapping(value = { "/", "/home", "/index" })
	public String homePage(Model model) {
		model.addAttribute("clubes", clubService.findClubesPorFechaDesc());
		return "home";
	}

	@PostMapping("/contacto")
	public ResponseEntity<String> enviarTicket(
			@RequestParam String subject,
			@RequestParam String email,
			@RequestParam String description) {

		Ticket ticket = new Ticket();
		ticket.setSubject(subject);
		ticket.setEmail(email);
		ticket.setDescription(description);
		ticket.setHandled(false);

		ticketService.save(ticket);

		return ResponseEntity.ok("Ticket enviado correctamente");
	}


	/* Record para exponer solo los datos necesarios del club en la API */
	public record ClubInfo(int idclub, String name, String sport) {}

	/**
	 * Obtiene los clubes donde un usuario es socio activo (necesario en Login)
	 * @param email Email del usuario
	 * @return Lista de clubes en formato JSON (solo id, nombre y deporte)
	 */
	@GetMapping("/api/misclubes")
	@ResponseBody
	public ResponseEntity<List<ClubInfo>> getUserClubs(@RequestParam String email) {
		List<Club> clubs = sociosService.findClubsByUserEmail(email);

		List<ClubInfo> response = clubs.stream()
				.map(c -> new ClubInfo(c.getIdclub(), c.getName(), c.getSportName()))
				.toList();

		return ResponseEntity.ok(response);
	}

}