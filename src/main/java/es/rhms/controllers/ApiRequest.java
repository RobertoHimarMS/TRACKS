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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import es.rhms.models.Club;
import es.rhms.models.Request;
import es.rhms.models.Request.EstadoRequest;
import es.rhms.models.Request.TipoRequest;
import es.rhms.models.Socios.RolSocio;
import es.rhms.models.Usuario;
import es.rhms.services.ClubService;
import es.rhms.services.RequestService;
import es.rhms.services.SociosService;
import es.rhms.services.UsuarioService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;

@RestController
@RequestMapping("/api/request")
public class ApiRequest {

	@Autowired
	private RequestService requestService;

	@Autowired
	private UsuarioService usuarioService;

	@Autowired
	private ClubService clubService;

	@Autowired
	private SociosService sociosService;

	/**
	 * Procesa la solicitud de alta de socio desde el formulario
	 * Guarda la petición en BD y redirige a la vista del club
	 *
	 * Si el usuario está logueado, usa los datos de sesión.
	 * Si no está logueado, usa los datos del formulario.
	 */
	@PostMapping("/new")
	public RedirectView nuevaSolicitud(
			@RequestParam String clb_target,
			@RequestParam String clb_description,
			@RequestParam String clb_sport,
			@RequestParam String clb_email,
			@RequestParam String clb_cp,
			@RequestParam String clb_city,
			@RequestParam(required = false) String usr_name,
			@RequestParam(required = false) String usr_surname,
			@RequestParam(required = false) String usr_email,
			@RequestParam(required = false) String usr_passwd,
			@RequestParam(required = false) String usr_cp,
			@RequestParam(required = false) String usr_city,
			@RequestParam(required = false) String usr_phone,
			@RequestParam(required = false) String usr_borned,
			@RequestParam(required = false) String usr_dni,
			@RequestParam int clubId,
			RedirectAttributes redirectAttributes,
			HttpServletRequest httpRequest) {

		try {
			// Crear la solicitud de tipo partner
			Request request = new Request();
			request.setTipo(TipoRequest.partner);
			request.setEstado(EstadoRequest.pending);

			// Datos del club (inmutables)
			request.setClbTarget(clb_target);
			request.setClbDescription(clb_description);
			request.setClbSport(clb_sport);
			request.setClbEmail(clb_email);
			request.setClbCp(clb_cp);
			request.setClbCity(clb_city);

			// Verificar si el usuario está logueado
			HttpSession session = httpRequest.getSession(false);
			Usuario usuarioLogueado = null;
			if (session != null && session.getAttribute("userlogged") != null) {
				usuarioLogueado = (Usuario) session.getAttribute("userlogged");
			}

			// Datos del solicitante
			if (usuarioLogueado != null) {
				// Usuario logueado: usar datos de sesión
				request.setUsrDni(usuarioLogueado.getDni());
				request.setUsrName(usuarioLogueado.getName());
				request.setUsrSurname(usuarioLogueado.getSurname());
				request.setUsrEmail(usuarioLogueado.getEmail());
				request.setUsrPasswd(usuarioLogueado.getPasswd());                            // Ya está codificada con {noop}
				request.setUsrCp(usuarioLogueado.getCp());
				request.setUsrCity(usuarioLogueado.getCity());
				request.setUsrPhone(usuarioLogueado.getPhone());
				request.setUsrBorned(usuarioLogueado.getBorned());
				request.setUsrPhoto(usuarioLogueado.getPhoto());
			} else {
				// Usuario no logueado: usar datos del formulario
				request.setUsrDni(usr_dni);
				request.setUsrName(usr_name);
				request.setUsrSurname(usr_surname);
				request.setUsrEmail(usr_email);
				request.setUsrPasswd(usr_passwd);
				request.setUsrCp(usr_cp);
				request.setUsrCity(usr_city);
				request.setUsrPhone(usr_phone);

				// Fecha de nacimiento (si se proporciona)
				if (usr_borned != null && !usr_borned.isEmpty()) {
					try {
						request.setUsrBorned(new java.text.SimpleDateFormat("yyyy-MM-dd").parse(usr_borned));
					} catch (Exception e) {
						// Si hay error parseando, dejar null
					}
				}
			}

			// Auditoría: usuario del sistema (ID 1 = System) o el usuario logueado
			int updatedBy = (usuarioLogueado != null) ? usuarioLogueado.getIduser() : 1;
			request.setUpdatedBy(updatedBy);

			// Guardar la solicitud
			requestService.save(request);

			redirectAttributes.addAttribute("mensaje", "ok");
			return new RedirectView("/club/" + clubId, true);

		} catch (Exception e) {
			redirectAttributes.addAttribute("mensaje", "ko");
			return new RedirectView("/club/" + clubId, true);
		}
	}

	@GetMapping("/ver/{id}")
	public ResponseEntity<Request> verRequest(@PathVariable int id) {
		return requestService.findById(id)
				.map(ResponseEntity::ok)
				.orElse(ResponseEntity.notFound().build());
	}

	/**
	 * Resuelve una petición (aceptar o rechazar)
	 *
	 * TIPO CLUB (admin del sistema):
	 * - accept=true: crea Usuario, Club y relación Socios (rol manager)
	 * - accept=false: actualiza estado a rejected
	 *
	 * TIPO PARTNER (manager del club):
	 * - accept=true:
	 *   - Si usuario existe: crea relación Socios (rol partner)
	 *   - Si usuario no existe: crea Usuario + relación Socios (rol partner)
	 * - accept=false: actualiza estado a rejected
	 *
	 * @param id ID de la petición
	 * @param accept true para aceptar, false para rechazar
	 * @param httpRequest HttpServletRequest para obtener sesión
	 * @return ResponseEntity con JSON {status, message}
	 */
	@PostMapping("/edit/{id}")
	@Transactional
	public ResponseEntity<Map<String, String>> resolverRequest(
			@PathVariable int id,
			@RequestParam boolean accept,
			HttpServletRequest httpRequest) {

		// Obtener usuario de la sesión
		HttpSession session = httpRequest.getSession(false);
		if (session == null || session.getAttribute("userlogged") == null) {
			return ResponseEntity.status(401).body(Map.of("error", "No autorizado"));
		}

		Usuario actualUser = (Usuario) session.getAttribute("userlogged");
		int actualUserId = actualUser.getIduser();

		// Buscar la petición
		Request req = requestService.findById(id).orElse(null);
		if (req == null) {
			return ResponseEntity.notFound().build();
		}

		// Solo se pueden procesar peticiones pendientes
		if (req.getEstado() != EstadoRequest.pending) {
			return ResponseEntity.badRequest().body(Map.of("error", "La petición ya ha sido procesada"));
		}

		// Procesar según el tipo de petición
		if (req.getTipo() == TipoRequest.club) {
			return procesarPeticionClub(req, accept, actualUserId);
		} else if (req.getTipo() == TipoRequest.partner) {
			return procesarPeticionPartner(req, accept, actualUserId);
		} else {
			return ResponseEntity.badRequest().body(Map.of("error", "Tipo de petición desconocido"));
		}
	}

	/**
	 * Procesa una petición de tipo CLUB (alta de nuevo club)
	 * Solo puede ser procesada por un admin del sistema
	 */
	private ResponseEntity<Map<String, String>> procesarPeticionClub(Request req, boolean accept, int actualUserId) {
		if (accept) {
			// Crear Usuario
			Usuario usuario = new Usuario();
			usuario.setDni(req.getUsrDni());
			usuario.setName(req.getUsrName());
			usuario.setSurname(req.getUsrSurname());
			usuario.setEmail(req.getUsrEmail());
			usuario.setPasswd("{noop}" + req.getUsrPasswd());
			usuario.setCp(req.getUsrCp());
			usuario.setCity(req.getUsrCity());
			usuario.setBorned(req.getUsrBorned());
			usuario.setPhone(req.getUsrPhone());
			usuario.setPhoto(req.getUsrPhoto());
			usuario.setUpdatedBy(actualUserId);
			usuario.setRequest(req);
			Usuario usuarioGuardado = usuarioService.save(usuario);

			// Crear Club
			Club club = new Club();
			club.setName(req.getClbTarget());
			club.setDescription(req.getClbDescription());
			club.setSport(req.getClbSport());
			club.setEmail(req.getClbEmail());
			club.setCp(req.getClbCp());
			club.setCity(req.getClbCity());
			club.setPhoto(req.getClbPhoto());
			club.setActive(true);
			club.setUpdatedBy(actualUserId);
			club.setRequest(req);
			Club clubGuardado = clubService.save(club);

			// Crear relación Socios con rol manager
			sociosService.createSocio(usuarioGuardado, clubGuardado, RolSocio.manager);

			// Actualizar estado de la petición
			requestService.updateEstado(req.getIdrequest(), EstadoRequest.accepted, actualUserId);

			return ResponseEntity.ok(Map.of("status", "accepted", "message", "Club creado correctamente"));
		} else {
			// Rechazar petición
			requestService.updateEstado(req.getIdrequest(), EstadoRequest.rejected, actualUserId);
			return ResponseEntity.ok(Map.of("status", "rejected", "message", "Solicitud rechazada"));
		}
	}

	/**
	 * Procesa una petición de tipo PARTNER (alta de socio en club existente)
	 * Puede ser procesada por un manager del club
	 *
	 * Casos:
	 * 1. Usuario existe y activo → crear relación Socios
	 * 2. Usuario existe pero inactivo (active=false) → reactivar y crear relación Socios
	 * 3. Usuario no existe → crear nuevo usuario y crear relación Socios
	 */
	private ResponseEntity<Map<String, String>> procesarPeticionPartner(Request req, boolean accept, int actualUserId) {
		if (accept) {
			// Buscar el club por nombre
			Club club = clubService.findByName(req.getClbTarget()).orElse(null);
			if (club == null) {
				return ResponseEntity.badRequest().body(Map.of("error", "El club especificado no existe"));
			}

			// Verificar si el usuario ya existe (por email o DNI)
			Usuario usuario = usuarioService.findByEmail(req.getUsrEmail()).orElse(null);
			if (usuario == null) {
				usuario = usuarioService.findByDni(req.getUsrDni()).orElse(null);
			}

			if (usuario == null) {
				// Caso 3: Usuario no existe → crear nuevo usuario
				usuario = new Usuario();
				usuario.setDni(req.getUsrDni());
				usuario.setName(req.getUsrName());
				usuario.setSurname(req.getUsrSurname());
				usuario.setEmail(req.getUsrEmail());
				usuario.setPasswd("{noop}" + req.getUsrPasswd());
				usuario.setCp(req.getUsrCp());
				usuario.setCity(req.getUsrCity());
				usuario.setBorned(req.getUsrBorned());
				usuario.setPhone(req.getUsrPhone());
				usuario.setPhoto(req.getUsrPhoto());
				usuario.setUpdatedBy(actualUserId);
				usuario.setRequest(req);
				usuario = usuarioService.save(usuario);
			} else if (!usuario.isActive()) {
				// Caso 2: Usuario existe pero inactivo → reactivar
				usuario.setUpdatedBy(actualUserId);
				usuario = usuarioService.reactivar(usuario);
			}
			// Caso 1: Usuario existe y activo → no hacer nada, usar usuario existente

			// Crear relación Socios con rol partner
			sociosService.createSocio(usuario, club, RolSocio.partner);

			// Actualizar estado de la petición
			requestService.updateEstado(req.getIdrequest(), EstadoRequest.accepted, actualUserId);

			return ResponseEntity.ok(Map.of("status", "accepted", "message", "Socio añadido correctamente al club"));
		} else {
			// Rechazar petición
			requestService.updateEstado(req.getIdrequest(), EstadoRequest.rejected, actualUserId);
			return ResponseEntity.ok(Map.of("status", "rejected", "message", "Solicitud rechazada"));
		}
	}

}