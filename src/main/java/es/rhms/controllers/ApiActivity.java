/** API REST PARA GESTIÓN DE ACTIVIDADES
 *  ===========================================================================
 *  POST /api/activity/create → Crea una nueva actividad para un club
 *  POST /api/activity/update/{id} → Actualiza una actividad existente
 *  POST /api/activity/delete/{id} → Elimina una actividad
 */

package es.rhms.controllers;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.stereotype.Controller;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import es.rhms.models.Actividad;
import es.rhms.models.Club;
import es.rhms.services.ActividadService;
import es.rhms.services.ClubService;
import es.rhms.utilities.FileUploadUtility;
import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/api/activity")
public class ApiActivity {

	@Autowired
	private ActividadService actividadService;

	@Autowired
	private ClubService clubService;

	@Autowired
	private FileUploadUtility fileUploadUtility;

	@PostMapping("/create")
	public String crearActividad(
			@RequestParam int idclub,
			@RequestParam String title,
			@RequestParam String description,
			@RequestParam String sport,
			@RequestParam("fecha") String fechaStr,
			@RequestParam String place,
			@RequestParam(required = false) Integer distancia,
			@RequestParam(required = false) MultipartFile photo,
			RedirectAttributes redirectAttributes,
			HttpSession session) {

		// Verificar sesión y rol manager
		String rol = (String) session.getAttribute("rollogged");
		Club clubLogged = (Club) session.getAttribute("clublogged");

		if (rol == null || !"manager".equals(rol) || clubLogged == null || clubLogged.getIdclub() != idclub) {
			redirectAttributes.addFlashAttribute("mensajeActivity", "No tienes permisos para realizar esta acción");
			return "redirect:/home";
		}

		try {
			// Obtener el club
			Club club = clubService.findById(idclub).orElse(null);
			if (club == null) {
				redirectAttributes.addFlashAttribute("mensajeActivity", "Club no encontrado");
				return "redirect:/home";
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

			// Procesar imagen si se ha subido
			if (photo != null && !photo.isEmpty()) {
				try {
					String photoName = fileUploadUtility.saveImage(photo, "activities");
					actividad.setPhoto(photoName);
				} catch (IllegalArgumentException e) {
					redirectAttributes.addFlashAttribute("mensajeActivity", e.getMessage());
					return "redirect:/club/" + idclub;
				}
			}

			// Guardar
			actividadService.save(actividad);

			redirectAttributes.addFlashAttribute("mensajeActivity", "Actividad creada con éxito");
			return "redirect:/club/" + idclub;

		} catch (ParseException e) {
			redirectAttributes.addFlashAttribute("mensajeActivity", "Error en el formato de fecha");
			return "redirect:/club/" + idclub;
		} catch (IOException e) {
			redirectAttributes.addFlashAttribute("mensajeActivity", "Error al subir la imagen");
			return "redirect:/club/" + idclub;
		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("mensajeActivity", "Error al crear la actividad");
			return "redirect:/club/" + idclub;
		}
	}

	@PostMapping("/update/{idactividad}")
	public String actualizarActividad(
			@PathVariable int idactividad,
			@RequestParam int idclub,
			@RequestParam String title,
			@RequestParam String description,
			@RequestParam String sport,
			@RequestParam("fecha") String fechaStr,
			@RequestParam String place,
			@RequestParam(required = false) Integer distancia,
			@RequestParam(required = false) MultipartFile photo,
			RedirectAttributes redirectAttributes,
			HttpSession session) {

		// Verificar sesión y rol manager
		String rol = (String) session.getAttribute("rollogged");
		Club clubLogged = (Club) session.getAttribute("clublogged");

		if (rol == null || !"manager".equals(rol) || clubLogged == null || clubLogged.getIdclub() != idclub) {
			redirectAttributes.addFlashAttribute("mensajeActivity", "No tienes permisos para realizar esta acción");
			return "redirect:/home";
		}

		try {
			// Buscar la actividad existente
			Actividad actividad = actividadService.findById(idactividad);
			if (actividad == null) {
				redirectAttributes.addFlashAttribute("mensajeActivity", "Error al guardar la actividad");
				return "redirect:/club/" + idclub;
			}

			// Verificar que la actividad pertenece al club del manager
			if (actividad.getClub().getIdclub() != idclub) {
				redirectAttributes.addFlashAttribute("mensajeActivity", "No tienes permisos para realizar esta acción");
				return "redirect:/home";
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

			// Procesar imagen si se ha subido una nueva
			if (photo != null && !photo.isEmpty()) {
				try {
					String photoName = fileUploadUtility.saveImage(photo, "activities");
					actividad.setPhoto(photoName);
				} catch (IllegalArgumentException e) {
					redirectAttributes.addFlashAttribute("mensajeActivity", e.getMessage());
					return "redirect:/club/" + idclub;
				}
			}

			// Guardar
			actividadService.save(actividad);

			redirectAttributes.addFlashAttribute("mensajeActivity", "La actividad se actualizó");
			return "redirect:/club/" + idclub;

		} catch (ParseException e) {
			redirectAttributes.addFlashAttribute("mensajeActivity", "Error en el formato de fecha");
			return "redirect:/club/" + idclub;
		} catch (IOException e) {
			redirectAttributes.addFlashAttribute("mensajeActivity", "Error al subir la imagen");
			return "redirect:/club/" + idclub;
		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("mensajeActivity", "Error al guardar la actividad");
			return "redirect:/club/" + idclub;
		}
	}

	@PostMapping("/delete/{idactividad}")
	public String eliminarActividad(
			@PathVariable int idactividad,
			RedirectAttributes redirectAttributes,
			HttpSession session) {

		// Verificar sesión y rol manager
		String rol = (String) session.getAttribute("rollogged");
		Club clubLogged = (Club) session.getAttribute("clublogged");

		if (rol == null || !"manager".equals(rol) || clubLogged == null) {
			redirectAttributes.addFlashAttribute("mensajeActivity", "No tienes permisos para realizar esta acción");
			return "redirect:/home";
		}

		try {
			// Buscar la actividad
			Actividad actividad = actividadService.findById(idactividad);
			if (actividad == null) {
				redirectAttributes.addFlashAttribute("mensajeActivity", "Actividad no encontrada");
				return "redirect:/club/" + clubLogged.getIdclub();
			}

			// Verificar que la actividad pertenece al club del manager
			if (actividad.getClub().getIdclub() != clubLogged.getIdclub()) {
				redirectAttributes.addFlashAttribute("mensajeActivity", "No tienes permisos para realizar esta acción");
				return "redirect:/home";
			}

			int idclub = actividad.getClub().getIdclub();

			// Eliminar inscripciones primero (por la foreign key)
			actividadService.deleteInscripcionesByActividadId(idactividad);

			// Eliminar la actividad
			actividadService.deleteById(idactividad);

			redirectAttributes.addFlashAttribute("mensajeActivity", "La actividad ha sido eliminada");
			return "redirect:/club/" + idclub;

		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("mensajeActivity", "No se ha podido eliminar la actividad");
			return "redirect:/club/" + clubLogged.getIdclub();
		}
	}
}