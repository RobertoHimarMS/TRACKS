package es.rhms.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import es.rhms.models.Club;
import es.rhms.services.ClubService;
import es.rhms.services.SociosService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Controller
public class PublicControllers {

	@Autowired
	private ClubService clubService;
	@Autowired
	private SociosService sociosService;

	@GetMapping(value = { "/", "/home", "/index" })
	public String homePage(Model model) {
		model.addAttribute("clubes", clubService.findClubesPorFechaDesc());
		return "home";
	}

	/**
	 * Muestra el formulario para solicitar alta de nuevo club
	 * Cualquier visitante puede acceder
	 */
	@GetMapping("/newclub")
	public String nuevoClub(Model model, HttpServletRequest request) {
		model.addAttribute("type", "club");
		model.addAttribute("clubId", 0);

		// Si hay usuario logueado, pasar sus datos para prefilled
		HttpSession session = request.getSession(false);
		if (session != null && session.getAttribute("userlogged") != null) {
			// El usuario logueado será el gestor del nuevo club
			// Los datos se toman de sesión en la vista
		}

		return "form";
	}

	/**
	 * Muestra el formulario para solicitar alta de socio en un club específico
	 * Cualquier visitante puede acceder
	 */
	@GetMapping("/newpartner/{idclub}")
	public String nuevoSocio(@PathVariable("idclub") int idclub, Model model, HttpServletRequest request) {
		// Obtener datos del club destino
		Club club = clubService.findById(idclub).orElse(null);
		if (club == null) {
			return "redirect:/home";
		}

		model.addAttribute("type", "partner");
		model.addAttribute("club", club);
		model.addAttribute("clubId", idclub);

		// Si hay usuario logueado, sus datos se tomarán de sesión en la vista

		return "form";
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

	/**
	 * Páginas legales públicas
	 */
	@GetMapping("/avisosyusos")
	public String avisoLegal() {
		return "avisosyusos";
	}

	@GetMapping("/privacidad")
	public String privacidad() {
		return "privacidad";
	}

	@GetMapping("/cookies")
	public String cookies() {
		return "cookies";
	}

}