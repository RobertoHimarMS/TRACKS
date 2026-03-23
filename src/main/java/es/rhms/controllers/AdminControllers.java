package es.rhms.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import es.rhms.models.Request;
import es.rhms.models.Ticket;
import es.rhms.services.RequestService;
import es.rhms.services.TicketService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/admin")
public class AdminControllers {

	@Autowired
	private TicketService ticketService;
	@Autowired
	private RequestService requestService;

	@GetMapping
	public String adminPage(Model model, HttpServletRequest request) {
		// Verificar que el usuario tiene rol admin del sistema
		HttpSession session = request.getSession(false);
		if (session == null || session.getAttribute("rollogged") == null) {
			return "redirect:/home";
		}
		String rol = (String) session.getAttribute("rollogged");
		if (!"admin".equals(rol)) {
			return "redirect:/home";
		}

		// Obtener tickets ordenados: primero no manejados, luego manejados
		List<Ticket> tickets = ticketService.findAllOrdered();
		model.addAttribute("tickets", tickets);

		// Obtener peticiones de club: primero pending, luego tramitadas
		List<Request> clubRequests = requestService.findClubRequestsOrdered();
		model.addAttribute("clubRequests", clubRequests);

		return "admin";
	}

}