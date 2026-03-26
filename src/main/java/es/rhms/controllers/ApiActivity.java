/** API REST PARA GESTIÓN DE ACTIVIDADES
 *  ===========================================================================
 *  POST /api/activity/create → Crea una nueva actividad para un club
 *
 */

package es.rhms.controllers;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import es.rhms.models.Actividad;
import es.rhms.models.Club;
import es.rhms.models.Usuario;
import es.rhms.services.ActividadService;
import es.rhms.services.ClubService;
import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api/activity")
public class ApiActivity {

	@Autowired
	private ActividadService actividadService;

	@Autowired
	private ClubService clubService;

	@PostMapping("/create")
	public RedirectView crearActividad(
			@RequestParam("idclub") int idclub,
			@RequestParam("title") String title,
			@RequestParam("description") String description,
			@RequestParam("sport") String sport,
			@RequestParam("fecha") String fechaStr,
			@RequestParam("place") String place,
			@RequestParam(value = "distancia", required = false) Integer distancia,
			RedirectAttributes redirectAttributes,
			HttpSession session) {

		// Verificar sesión y rol manager
		String rol = (String) session.getAttribute("rollogged");
		Usuario usuario = (Usuario) session.getAttribute("userlogged");
		Club clubLogged = (Club) session.getAttribute("clublogged");

		if (rol == null || !"manager".equals(rol) || clubLogged == null || clubLogged.getIdclub() != idclub) {
			redirectAttributes.addFlashAttribute("mensajeActivity", "No tienes permisos para realizar esta acción");
			return new RedirectView("/home");
		}

		try {
			// Obtener el club
			Club club = clubService.findById(idclub).orElse(null);
			if (club == null) {
				redirectAttributes.addFlashAttribute("mensajeActivity", "Club no encontrado");
				return new RedirectView("/home");
			}

			// Parsear la fecha
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
			Date fecha = sdf.parse(fechaStr);

			// Crear la actividad
			Actividad actividad = new Actividad();
			actividad.setTitle(title);
			actividad.setDescription(description);
			actividad.setSport(sport);
			actividad.setFecha(fecha);
			actividad.setPlace(place);
			actividad.setDistancia(distancia);
			actividad.setClub(club);

			// Guardar (aud_created_at y aud_updated_at se actualizan por trigger en BD)
			actividadService.save(actividad);

			redirectAttributes.addFlashAttribute("mensajeActivity", "Actividad creada con éxito");
			return new RedirectView("/club/" + idclub);

		} catch (ParseException e) {
			redirectAttributes.addFlashAttribute("mensajeActivity", "Error en el formato de fecha");
			return new RedirectView("/club/" + idclub);
		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("mensajeActivity", "Error al crear la actividad");
			return new RedirectView("/club/" + idclub);
		}
	}

	@PostMapping("/update/{idactividad}")
	public RedirectView actualizarActividad(
			@PathVariable("idactividad") int idactividad,
			@RequestParam("idclub") int idclub,
			@RequestParam("title") String title,
			@RequestParam("description") String description,
			@RequestParam("sport") String sport,
			@RequestParam("fecha") String fechaStr,
			@RequestParam("place") String place,
			@RequestParam(value = "distancia", required = false) Integer distancia,
			RedirectAttributes redirectAttributes,
			HttpSession session) {

		// Verificar sesión y rol manager
		String rol = (String) session.getAttribute("rollogged");
		Club clubLogged = (Club) session.getAttribute("clublogged");

		if (rol == null || !"manager".equals(rol) || clubLogged == null || clubLogged.getIdclub() != idclub) {
			redirectAttributes.addFlashAttribute("mensajeActivity", "No tienes permisos para realizar esta acción");
			return new RedirectView("/home");
		}

		try {
			// Buscar la actividad existente
			Actividad actividad = actividadService.findById(idactividad);
			if (actividad == null) {
				redirectAttributes.addFlashAttribute("mensajeActivity", "Error al guardar la actividad");
				return new RedirectView("/club/" + idclub);
			}

			// Verificar que la actividad pertenece al club del manager
			if (actividad.getClub().getIdclub() != idclub) {
				redirectAttributes.addFlashAttribute("mensajeActivity", "No tienes permisos para realizar esta acción");
				return new RedirectView("/home");
			}

			// Parsear la fecha
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
			Date fecha = sdf.parse(fechaStr);

			// Actualizar todos los campos
			actividad.setTitle(title);
			actividad.setDescription(description);
			actividad.setSport(sport);
			actividad.setFecha(fecha);
			actividad.setPlace(place);
			actividad.setDistancia(distancia);

			// Guardar (aud_updated_at se actualiza por trigger en BD)
			actividadService.save(actividad);

			redirectAttributes.addFlashAttribute("mensajeActivity", "La actividad se actualizó");
			return new RedirectView("/club/" + idclub);

		} catch (ParseException e) {
			redirectAttributes.addFlashAttribute("mensajeActivity", "Error en el formato de fecha");
			return new RedirectView("/club/" + idclub);
		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("mensajeActivity", "Error al guardar la actividad");
			return new RedirectView("/club/" + idclub);
		}
	}

	@PostMapping("/delete/{idactividad}")
	public RedirectView eliminarActividad(
			@PathVariable("idactividad") int idactividad,
			RedirectAttributes redirectAttributes,
			HttpSession session) {

		// Verificar sesión y rol manager
		String rol = (String) session.getAttribute("rollogged");
		Club clubLogged = (Club) session.getAttribute("clublogged");

		if (rol == null || !"manager".equals(rol) || clubLogged == null) {
			redirectAttributes.addFlashAttribute("mensajeActivity", "No tienes permisos para realizar esta acción");
			return new RedirectView("/home");
		}

		try {
			// Buscar la actividad
			Actividad actividad = actividadService.findById(idactividad);
			if (actividad == null) {
				redirectAttributes.addFlashAttribute("mensajeActivity", "Actividad no encontrada");
				return new RedirectView("/club/" + clubLogged.getIdclub());
			}

			// Verificar que la actividad pertenece al club del manager
			if (actividad.getClub().getIdclub() != clubLogged.getIdclub()) {
				redirectAttributes.addFlashAttribute("mensajeActivity", "No tienes permisos para realizar esta acción");
				return new RedirectView("/home");
			}

			int idclub = actividad.getClub().getIdclub();

			// Eliminar inscripciones primero (por la foreign key)
			actividadService.deleteInscripcionesByActividadId(idactividad);

			// Eliminar la actividad
			actividadService.deleteById(idactividad);

			redirectAttributes.addFlashAttribute("mensajeActivity", "La actividad ha sido eliminada");
			return new RedirectView("/club/" + idclub);

		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("mensajeActivity", "No se ha podido eliminar la actividad");
			return new RedirectView("/club/" + clubLogged.getIdclub());
		}
	}
}