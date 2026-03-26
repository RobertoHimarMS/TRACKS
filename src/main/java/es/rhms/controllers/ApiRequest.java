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
import es.rhms.repositories.RequestRepository;
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

	@Autowired
	private RequestRepository requestRepository;

	/**
	 * Procesa solicitudes de alta (club o socio) desde el formulario unificado
	 *
	 * TIPO CLUB:
	 * - Cualquier visitante puede solicitar
	 * - Redirige a /home con mensaje
	 *
	 * TIPO PARTNER:
	 * - Cualquier visitante puede solicitar alta en un club específico
	 * - Si usuario logueado, usa datos de sesión
	 * - Verifica duplicados (email + club)
	 * - Redirige a /club/{idclub} con mensaje
	 */
	@PostMapping("/new")
	public RedirectView nuevaSolicitud(
			@RequestParam String type,
			@RequestParam String clb_target,
			@RequestParam String clb_description,
			@RequestParam String clb_sport,
			@RequestParam String clb_email,
			@RequestParam(required = false) String clb_cp,
			@RequestParam(required = false) String clb_city,
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
			// Verificar si el usuario está logueado
			HttpSession session = httpRequest.getSession(false);
			Usuario usuarioLogueado = null;
			if (session != null && session.getAttribute("userlogged") != null) {
				usuarioLogueado = (Usuario) session.getAttribute("userlogged");
			}

			// Determinar tipo de solicitud
			TipoRequest tipoSolicitud = "club".equals(type) ? TipoRequest.club : TipoRequest.partner;
			String emailSolicitante = (usuarioLogueado != null) ? usuarioLogueado.getEmail() : usr_email;

			// Verificar duplicados según tipo (solo para solicitud de socio)
			if (tipoSolicitud == TipoRequest.partner) {
				Request existingRequest = requestRepository.findByTipoAndUsrEmailAndClbTargetAndEstado(
						TipoRequest.partner, emailSolicitante, clb_target, EstadoRequest.pending);

				if (existingRequest != null) {
					redirectAttributes.addAttribute("mensaje", "duplicate");
					return new RedirectView("/club/" + clubId, true);
				}
			}

			// Crear la solicitud
			Request request = new Request();
			request.setTipo(tipoSolicitud);
			request.setEstado(EstadoRequest.pending);

			// Datos del club
			request.setClbTarget(clb_target);
			request.setClbDescription(clb_description);
			request.setClbSport(clb_sport);
			request.setClbEmail(clb_email);
			request.setClbCp(clb_cp);
			request.setClbCity(clb_city);

			// Datos del solicitante
			if (usuarioLogueado != null) {
				// Usuario logueado: usar datos de sesión
				request.setUsrDni(usuarioLogueado.getDni());
				request.setUsrName(usuarioLogueado.getName());
				request.setUsrSurname(usuarioLogueado.getSurname());
				request.setUsrEmail(usuarioLogueado.getEmail());
				request.setUsrPasswd(usuarioLogueado.getPasswd());
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
				// Para tipo club, añadir prefijo {noop} a la contraseña
				request.setUsrPasswd(tipoSolicitud == TipoRequest.club ? "{noop}" + usr_passwd : usr_passwd);
				request.setUsrCp(usr_cp);
				request.setUsrCity(usr_city);
				request.setUsrPhone(usr_phone);

				// Fecha de nacimiento
				if (usr_borned != null && !usr_borned.isEmpty()) {
					try {
						request.setUsrBorned(new java.text.SimpleDateFormat("yyyy-MM-dd").parse(usr_borned));
					} catch (Exception e) {
						// Si hay error parseando, dejar null
					}
				}
			}

			// Auditoría
			int updatedBy = (usuarioLogueado != null) ? usuarioLogueado.getIduser() : 1;
			request.setUpdatedBy(updatedBy);

			// Guardar la solicitud
			requestService.save(request);

			// Redirección según tipo
			redirectAttributes.addAttribute("mensaje", "ok");
			if (tipoSolicitud == TipoRequest.club) {
				return new RedirectView("/home", true);
			} else {
				return new RedirectView("/club/" + clubId, true);
			}

		} catch (Exception e) {
			redirectAttributes.addAttribute("mensaje", "ko");
			if ("club".equals(type)) {
				return new RedirectView("/home", true);
			} else {
				return new RedirectView("/club/" + clubId, true);
			}
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
	 */
	@PostMapping("/edit/{id}")
	@Transactional
	public ResponseEntity<Map<String, String>> resolverRequest(
			@PathVariable int id,
			@RequestParam boolean accept,
			HttpServletRequest httpRequest) {

		HttpSession session = httpRequest.getSession(false);
		if (session == null || session.getAttribute("userlogged") == null) {
			return ResponseEntity.status(401).body(Map.of("error", "No autorizado"));
		}

		Usuario actualUser = (Usuario) session.getAttribute("userlogged");
		String rol = (String) session.getAttribute("rollogged");
		Club club = (Club) session.getAttribute("clublogged");
		int actualUserId = actualUser.getIduser();

		Request req = requestService.findById(id).orElse(null);
		if (req == null) {
			return ResponseEntity.notFound().build();
		}

		if (req.getEstado() != EstadoRequest.pending) {
			return ResponseEntity.badRequest().body(Map.of("error", "La petición ya ha sido procesada"));
		}

		if (req.getTipo() == TipoRequest.club) {
			if (!"admin".equals(rol)) {
				return ResponseEntity.status(403).body(Map.of("error", "No tiene permisos para procesar esta solicitud"));
			}
			return procesarPeticionClub(req, accept, actualUserId);
		} else if (req.getTipo() == TipoRequest.partner) {
			if (!"manager".equals(rol) || club == null) {
				return ResponseEntity.status(403).body(Map.of("error", "No tiene permisos para procesar esta solicitud"));
			}
			if (!club.getName().equals(req.getClbTarget())) {
				return ResponseEntity.status(403).body(Map.of("error", "No tiene permisos para procesar solicitudes de este club"));
			}
			return procesarPeticionPartner(req, accept, actualUserId);
		} else {
			return ResponseEntity.badRequest().body(Map.of("error", "Tipo de petición desconocido"));
		}
	}

	private ResponseEntity<Map<String, String>> procesarPeticionClub(Request req, boolean accept, int actualUserId) {
		if (accept) {
			// Verificar si el usuario ya existe (por email o DNI)
			Usuario usuario = usuarioService.findByEmail(req.getUsrEmail()).orElse(null);
			if (usuario == null) {
				usuario = usuarioService.findByDni(req.getUsrDni()).orElse(null);
			}

			if (usuario == null) {
				// Usuario nuevo: crear desde cero
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
				// Usuario existente inactivo: reactivar
				usuario.setActive(true);
				usuario.setName(req.getUsrName());
				usuario.setSurname(req.getUsrSurname());
				usuario.setPhone(req.getUsrPhone());
				usuario.setCp(req.getUsrCp());
				usuario.setCity(req.getUsrCity());
				if (req.getUsrBorned() != null) {
					usuario.setBorned(req.getUsrBorned());
				}
				usuario.setUpdatedBy(actualUserId);
				usuario = usuarioService.save(usuario);
			}
			// Si el usuario ya existe y está activo, se usa tal cual

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

			sociosService.createSocio(usuario, clubGuardado, RolSocio.manager);

			requestService.updateEstado(req.getIdrequest(), EstadoRequest.accepted, actualUserId);

			return ResponseEntity.ok(Map.of("status", "accepted", "message", "Club creado correctamente"));
		} else {
			requestService.updateEstado(req.getIdrequest(), EstadoRequest.rejected, actualUserId);
			return ResponseEntity.ok(Map.of("status", "rejected", "message", "Solicitud rechazada"));
		}
	}

	private ResponseEntity<Map<String, String>> procesarPeticionPartner(Request req, boolean accept, int actualUserId) {
		if (accept) {
			Club club = clubService.findByName(req.getClbTarget()).orElse(null);
			if (club == null) {
				return ResponseEntity.badRequest().body(Map.of("error", "El club especificado no existe"));
			}

			Usuario usuario = usuarioService.findByEmail(req.getUsrEmail()).orElse(null);
			if (usuario == null) {
				usuario = usuarioService.findByDni(req.getUsrDni()).orElse(null);
			}

			if (usuario == null) {
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
				usuario.setActive(true);
				usuario.setName(req.getUsrName());
				usuario.setSurname(req.getUsrSurname());
				usuario.setPhone(req.getUsrPhone());
				usuario.setCp(req.getUsrCp());
				usuario.setCity(req.getUsrCity());
				if (req.getUsrBorned() != null) {
					usuario.setBorned(req.getUsrBorned());
				}
				usuario.setUpdatedBy(actualUserId);
				usuario = usuarioService.save(usuario);
			}

			sociosService.createSocio(usuario, club, RolSocio.partner);

			requestService.updateEstado(req.getIdrequest(), EstadoRequest.accepted, actualUserId);

			return ResponseEntity.ok(Map.of("status", "accepted", "message", "Socio añadido correctamente al club"));
		} else {
			requestService.updateEstado(req.getIdrequest(), EstadoRequest.rejected, actualUserId);
			return ResponseEntity.ok(Map.of("status", "rejected", "message", "Solicitud rechazada"));
		}
	}

}