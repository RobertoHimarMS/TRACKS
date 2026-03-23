/** CLASE CONTROLADORA DE CLUBS
 *  ===========================================================================
 *  Permite ver un determinado Club por su Id 										/club/{id}
 *  Permite administrar el Club (ROL Manager tramita latas de socios)				/club/admin/{idclub}
 *  Permite solicitar el alta de un nuevo club (cualquier visitante)				/club/newclub
 *  Permite solicitar el alta de socio en un club (cualquier visitante)			/club/newuser/{idclub}
 *  TODO: Aún en este MVP no se ha implementado modificar datos de Club creado		/club/edit
 *
 * */

package es.rhms.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import es.rhms.models.Actividad;
import es.rhms.models.Club;
import es.rhms.models.Producto;
import es.rhms.models.Publicacion;
import es.rhms.models.Request;
import es.rhms.models.Usuario;
import es.rhms.services.ActividadService;
import es.rhms.services.ClubService;
import es.rhms.services.ProductoService;
import es.rhms.services.PublicacionService;
import es.rhms.services.RequestService;
import es.rhms.services.SociosService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/club")
public class ClubControllers {

	@Autowired
	private ClubService clubService;

	@Autowired
	private RequestService requestService;

	@Autowired
	private ActividadService actividadService;

	@Autowired
	private PublicacionService publicacionService;

	@Autowired
	private ProductoService productoService;
	
	@Autowired
	private SociosService sociosService;

	@GetMapping("/{id}")
	public String verClub(@PathVariable("id") int id, Model model, HttpServletRequest request) {

		Club club = clubService.findById(id).orElse(null);
		if (club == null) {
			return "redirect:/home";
		}
		model.addAttribute("club", club);

		List<Actividad> actividades = actividadService.findByClubId(id);
		List<Publicacion> publicaciones = publicacionService.findByClubId(id);
		List<Producto> productos = productoService.findByClubId(id);

		model.addAttribute("actividades", actividades);
		model.addAttribute("publicaciones", publicaciones);
		model.addAttribute("productos", productos);

		HttpSession session = request.getSession(false);
		boolean yaEsSocio = false;
		if (session != null && session.getAttribute("userlogged") != null) {
			Usuario usuario = (Usuario) session.getAttribute("userlogged");
			yaEsSocio = sociosService.isUserInClub(usuario.getIduser(), id);
		}
		model.addAttribute("yaEsSocio", yaEsSocio);

		return "club";
	}

	@GetMapping("/admin/{idclub}")
	public String administrarClub(@PathVariable("idclub") int idclub, Model model, HttpServletRequest request) {
		// Verificar sesión y rol manager
		HttpSession session = request.getSession(false);
		if (session == null || session.getAttribute("rollogged") == null) {
			return "redirect:/home";
		}

		String rol = (String) session.getAttribute("rollogged");
		if (!"manager".equals(rol)) {
			return "redirect:/home";
		}

		// Obtener el club activo de la sesión
		Club club = (Club) session.getAttribute("clublogged");
		if (club == null) {
			return "redirect:/home";
		}

		// Obtener solicitudes de socio para este club
		List<Request> partnerRequests = requestService.findPartnerRequestsByClubOrdered(club.getName());
		model.addAttribute("partnerRequests", partnerRequests);

		return "admin";
	}

	@GetMapping("/newclub")
	public String nuevoClub() {
		return "formclub";
	}

	@GetMapping("/newuser/{idclub}")
	public String nuevoSocio(@PathVariable("idclub") int idclub, Model model, HttpServletRequest request) {
		// Obtener datos del club
		Club club = clubService.findById(idclub).orElse(null);
		if (club == null) {
			return "redirect:/home";
		}
		model.addAttribute("club", club);

		// Verificar si el usuario está logueado
		HttpSession session = request.getSession(false);
		if (session != null && session.getAttribute("usuario") != null) {
			// Usuario logueado: prellenar datos del usuario
			model.addAttribute("usuario", session.getAttribute("usuario"));
		}

		return "formsocio";
	}

}