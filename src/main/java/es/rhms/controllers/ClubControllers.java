/** CLASE CONTROLADORA DE CLUBS
 *  ===========================================================================
 *  Permite ver un determinado Club por su Id 										/club/{id}
 *  Permite administrar el Club (ROL Manager tramita latas de socios)				/club/admin/{idclub}
 *  Permite solicitar el alta de un nuevo club (cualquier visitante)				/club/newclub
 *  Permite solicitar el alta de socio en un club (cualquier visitante)			/club/newuser/{idclub}
 *  Permite ver el carné del socio (actividades y compras)							/club/partner/{iduser}
 *  Permite darse de baja del club actual											/club/unsuscribe
 *  TODO: Aún en este MVP no se ha implementado modificar datos de Club creado		/club/edit
 *
 * */

package es.rhms.controllers;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.view.RedirectView;

import es.rhms.models.Actividad;
import es.rhms.models.Club;
import es.rhms.models.Compra;
import es.rhms.models.Producto;
import es.rhms.models.Publicacion;
import es.rhms.models.Request;
import es.rhms.models.Socios;
import es.rhms.models.Usuario;
import es.rhms.repositories.ActividadRepository;
import es.rhms.repositories.CompraRepository;
import es.rhms.repositories.SociosRepository;
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

	@Autowired
	private ActividadRepository actividadRepository;

	@Autowired
	private CompraRepository compraRepository;

	@Autowired
	private SociosRepository sociosRepository;

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

		// Verificar si el usuario logueado ya es socio de este club
		HttpSession session = request.getSession(false);
		boolean esSocio = false;
		Set<Integer> actividadesInscritas = new HashSet<>();

		if (session != null && session.getAttribute("userlogged") != null) {
			Usuario usuario = (Usuario) session.getAttribute("userlogged");
			esSocio = sociosService.isUserInClub(usuario.getIduser(), id);

			// Obtener actividades inscritas solo si está viendo su club activo
			Club clubLogged = (Club) session.getAttribute("clublogged");
			if (clubLogged != null && clubLogged.getIdclub() == id) {
				actividadesInscritas = actividadRepository.findInscribedActivityIds(usuario.getIduser());
			}
		}
		model.addAttribute("esSocio", esSocio);
		model.addAttribute("actividadesInscritas", actividadesInscritas);

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

	@GetMapping("/unsuscribe")
	public String darseDeBaja(HttpServletRequest request) {
		// Obtener datos de sesión (NO de parámetros GET por seguridad)
		HttpSession session = request.getSession(false);
		if (session == null || session.getAttribute("userlogged") == null) {
			return "redirect:/home";
		}

		Usuario usuario = (Usuario) session.getAttribute("userlogged");
		Club club = (Club) session.getAttribute("clublogged");

		if (club == null) {
			return "redirect:/home";
		}

		// Ejecutar baja del socio/manager usando IDs de sesión
		sociosService.deletePartnerOrManager(usuario.getIduser(), club.getIdclub());

		// Invalidar sesión
		session.invalidate();

		return "redirect:/home";
	}

	/**
	 * Muestra el carné del socio con sus actividades y compras
	 * Solo accesible para usuarios logueados
	 */
	@GetMapping("/partner/{iduser}")
	public String verCarne(@PathVariable("iduser") int iduser, Model model, HttpServletRequest request) {
		// Verificar sesión
		HttpSession session = request.getSession(false);
		if (session == null || session.getAttribute("userlogged") == null) {
			return "redirect:/home";
		}

		// Verificar que el ID de la URL coincide con el usuario logueado
		Usuario usuarioLogueado = (Usuario) session.getAttribute("userlogged");
		if (usuarioLogueado.getIduser() != iduser) {
			return "redirect:/home";
		}

		// Obtener el club activo de la sesión
		Club club = (Club) session.getAttribute("clublogged");

		// Obtener fecha de alta en el club actual
		Date fechaAlta = null;
		if (club != null) {
			List<Socios> sociosList = sociosRepository.findByUsuario(usuarioLogueado);
			for (Socios socio : sociosList) {
				if (socio.getClub().getIdclub() == club.getIdclub() && socio.getUnsuscribedAt() == null) {
					fechaAlta = socio.getRegisteredAt();
					break;
				}
			}
		}

		// Obtener actividades inscritas del usuario (con JOIN FETCH para evitar LazyInitializationException)
		List<Actividad> misActividades = actividadRepository.findInscripcionesByUserId(iduser);

		// Obtener compras del usuario (con JOIN FETCH para evitar LazyInitializationException)
		List<Compra> compras = compraRepository.findComprasByUserId(iduser);

		// Pasar datos al modelo
		model.addAttribute("usuario", usuarioLogueado);
		model.addAttribute("club", club);
		model.addAttribute("fechaAlta", fechaAlta);
		model.addAttribute("misActividades", misActividades);
		model.addAttribute("compras", compras);

		return "carne";
	}

	/**
	 * Muestra el formulario para crear una nueva actividad
	 * Solo accesible para managers del club
	 */
	@GetMapping("/newactivity/{idclub}")
	public String nuevaActividad(@PathVariable("idclub") int idclub, Model model, HttpServletRequest request) {
		// Verificar sesión y rol manager
		HttpSession session = request.getSession(false);
		if (session == null || session.getAttribute("rollogged") == null) {
			return "redirect:/home";
		}

		String rol = (String) session.getAttribute("rollogged");
		if (!"manager".equals(rol)) {
			return "redirect:/home";
		}

		// Verificar que el club de la URL coincide con el club de la sesión
		Club clubLogged = (Club) session.getAttribute("clublogged");
		if (clubLogged == null || clubLogged.getIdclub() != idclub) {
			return "redirect:/home";
		}

		// Obtener el club y pasarlo al modelo
		Club club = clubService.findById(idclub).orElse(null);
		if (club == null) {
			return "redirect:/home";
		}

		model.addAttribute("club", club);
		model.addAttribute("actividad", null);  // null = modo creación
		return "formactivity";
	}

	/**
	 * Muestra el formulario para editar una actividad existente
	 * Solo accesible para managers del club propietario de la actividad
	 */
	@GetMapping("/editactivity/{idactividad}")
	public String editarActividad(@PathVariable("idactividad") int idactividad, Model model, HttpServletRequest request) {
		// Verificar sesión y rol manager
		HttpSession session = request.getSession(false);
		if (session == null || session.getAttribute("rollogged") == null) {
			return "redirect:/home";
		}

		String rol = (String) session.getAttribute("rollogged");
		if (!"manager".equals(rol)) {
			return "redirect:/home";
		}

		// Obtener la actividad
		Actividad actividad = actividadService.findById(idactividad);
		if (actividad == null) {
			return "redirect:/home";
		}

		// Verificar que la actividad pertenece al club del manager logueado
		Club clubLogged = (Club) session.getAttribute("clublogged");
		if (clubLogged == null || actividad.getClub().getIdclub() != clubLogged.getIdclub()) {
			return "redirect:/home";
		}

		model.addAttribute("club", actividad.getClub());
		model.addAttribute("actividad", actividad);  // no null = modo edición
		return "formactivity";
	}

	/**
	 * Muestra el formulario para crear una nueva publicación
	 * Solo accesible para managers del club
	 */
	@GetMapping("/newpublish/{idclub}")
	public String nuevaPublicacion(@PathVariable("idclub") int idclub, Model model, HttpServletRequest request) {
		// Verificar sesión y rol manager
		HttpSession session = request.getSession(false);
		if (session == null || session.getAttribute("rollogged") == null) {
			return "redirect:/home";
		}

		String rol = (String) session.getAttribute("rollogged");
		if (!"manager".equals(rol)) {
			return "redirect:/home";
		}

		// Verificar que el club de la URL coincide con el club de la sesión
		Club clubLogged = (Club) session.getAttribute("clublogged");
		if (clubLogged == null || clubLogged.getIdclub() != idclub) {
			return "redirect:/home";
		}

		// Obtener el club y pasarlo al modelo
		Club club = clubService.findById(idclub).orElse(null);
		if (club == null) {
			return "redirect:/home";
		}

		model.addAttribute("club", club);
		model.addAttribute("publicacion", null);  // null = modo creación
		return "formpublish";
	}

	/**
	 * Muestra el formulario para editar una publicación existente
	 * Solo accesible para managers del club propietario de la publicación
	 */
	@GetMapping("/editpublish/{idpublicacion}")
	public String editarPublicacion(@PathVariable("idpublicacion") int idpublicacion, Model model, HttpServletRequest request) {
		// Verificar sesión y rol manager
		HttpSession session = request.getSession(false);
		if (session == null || session.getAttribute("rollogged") == null) {
			return "redirect:/home";
		}

		String rol = (String) session.getAttribute("rollogged");
		if (!"manager".equals(rol)) {
			return "redirect:/home";
		}

		// Obtener la publicación
		Publicacion publicacion = publicacionService.findById(idpublicacion);
		if (publicacion == null) {
			return "redirect:/home";
		}

		// Verificar que la publicación pertenece al club del manager logueado
		Club clubLogged = (Club) session.getAttribute("clublogged");
		if (clubLogged == null || publicacion.getClub().getIdclub() != clubLogged.getIdclub()) {
			return "redirect:/home";
		}

		model.addAttribute("club", publicacion.getClub());
		model.addAttribute("publicacion", publicacion);  // no null = modo edición
		return "formpublish";
	}

	/**
	 * Muestra el formulario para crear un nuevo producto
	 * Solo accesible para managers del club
	 */
	@GetMapping("/newproduct/{idclub}")
	public String nuevoProducto(@PathVariable("idclub") int idclub, Model model, HttpServletRequest request) {
		// Verificar sesión y rol manager
		HttpSession session = request.getSession(false);
		if (session == null || session.getAttribute("rollogged") == null) {
			return "redirect:/home";
		}

		String rol = (String) session.getAttribute("rollogged");
		if (!"manager".equals(rol)) {
			return "redirect:/home";
		}

		// Verificar que el club de la URL coincide con el club de la sesión
		Club clubLogged = (Club) session.getAttribute("clublogged");
		if (clubLogged == null || clubLogged.getIdclub() != idclub) {
			return "redirect:/home";
		}

		// Obtener el club y pasarlo al modelo
		Club club = clubService.findById(idclub).orElse(null);
		if (club == null) {
			return "redirect:/home";
		}

		model.addAttribute("club", club);
		model.addAttribute("producto", null);  // null = modo creación
		return "formproduct";
	}

	/**
	 * Muestra el formulario para editar un producto existente
	 * Solo accesible para managers del club propietario del producto
	 */
	@GetMapping("/editproduct/{idproducto}")
	public String editarProducto(@PathVariable("idproducto") int idproducto, Model model, HttpServletRequest request) {
		// Verificar sesión y rol manager
		HttpSession session = request.getSession(false);
		if (session == null || session.getAttribute("rollogged") == null) {
			return "redirect:/home";
		}

		String rol = (String) session.getAttribute("rollogged");
		if (!"manager".equals(rol)) {
			return "redirect:/home";
		}

		// Obtener el producto
		Producto producto = productoService.findById(idproducto);
		if (producto == null) {
			return "redirect:/home";
		}

		// Verificar que el producto pertenece al club del manager logueado
		Club clubLogged = (Club) session.getAttribute("clublogged");
		if (clubLogged == null || producto.getClub().getIdclub() != clubLogged.getIdclub()) {
			return "redirect:/home";
		}

		model.addAttribute("club", producto.getClub());
		model.addAttribute("producto", producto);  // no null = modo edición
		return "formproduct";
	}

}