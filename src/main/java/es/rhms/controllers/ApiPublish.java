/** API REST PARA GESTIÓN DE PUBLICACIONES
 *  ===========================================================================
 *  POST /api/publish/create → Crea una nueva publicación para un club
 *  POST /api/publish/update/{idpublicacion} → Actualiza una publicación existente
 *  POST /api/publish/delete/{idpublicacion} → Elimina una publicación
 *
 */

package es.rhms.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import es.rhms.models.Club;
import es.rhms.models.Publicacion;
import es.rhms.services.ClubService;
import es.rhms.services.PublicacionService;
import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api/publish")
public class ApiPublish {

	@Autowired
	private PublicacionService publicacionService;

	@Autowired
	private ClubService clubService;

	@PostMapping("/create")
	public RedirectView crearPublicacion(
			@RequestParam("idclub") int idclub,
			@RequestParam("subject") String subject,
			@RequestParam("text") String text,
			RedirectAttributes redirectAttributes,
			HttpSession session) {

		// Verificar sesión y rol manager
		String rol = (String) session.getAttribute("rollogged");
		Club clubLogged = (Club) session.getAttribute("clublogged");

		if (rol == null || !"manager".equals(rol) || clubLogged == null || clubLogged.getIdclub() != idclub) {
			redirectAttributes.addFlashAttribute("mensajePublish", "No tienes permisos para realizar esta acción");
			return new RedirectView("/home");
		}

		try {
			// Obtener el club
			Club club = clubService.findById(idclub).orElse(null);
			if (club == null) {
				redirectAttributes.addFlashAttribute("mensajePublish", "Club no encontrado");
				return new RedirectView("/home");
			}

			// Crear la publicación
			Publicacion publicacion = new Publicacion();
			publicacion.setSubject(subject);
			publicacion.setText(text);
			publicacion.setClub(club);

			// Guardar
			publicacionService.save(publicacion);

			redirectAttributes.addFlashAttribute("mensajePublish", "Publicación creada con éxito");
			return new RedirectView("/club/" + idclub + "#publicaciones");

		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("mensajePublish", "Error al crear la publicación");
			return new RedirectView("/club/" + idclub + "#publicaciones");
		}
	}

	@PostMapping("/update/{idpublicacion}")
	public RedirectView actualizarPublicacion(
			@PathVariable("idpublicacion") int idpublicacion,
			@RequestParam("idclub") int idclub,
			@RequestParam("subject") String subject,
			@RequestParam("text") String text,
			RedirectAttributes redirectAttributes,
			HttpSession session) {

		// Verificar sesión y rol manager
		String rol = (String) session.getAttribute("rollogged");
		Club clubLogged = (Club) session.getAttribute("clublogged");

		if (rol == null || !"manager".equals(rol) || clubLogged == null || clubLogged.getIdclub() != idclub) {
			redirectAttributes.addFlashAttribute("mensajePublish", "No tienes permisos para realizar esta acción");
			return new RedirectView("/home");
		}

		try {
			// Buscar la publicación existente
			Publicacion publicacion = publicacionService.findById(idpublicacion);
			if (publicacion == null) {
				redirectAttributes.addFlashAttribute("mensajePublish", "Error al guardar la publicación");
				return new RedirectView("/club/" + idclub + "#publicaciones");
			}

			// Verificar que la publicación pertenece al club del manager
			if (publicacion.getClub().getIdclub() != idclub) {
				redirectAttributes.addFlashAttribute("mensajePublish", "No tienes permisos para realizar esta acción");
				return new RedirectView("/home");
			}

			// Actualizar todos los campos
			publicacion.setSubject(subject);
			publicacion.setText(text);

			// Guardar
			publicacionService.save(publicacion);

			redirectAttributes.addFlashAttribute("mensajePublish", "La publicación se actualizó");
			return new RedirectView("/club/" + idclub + "#publicaciones");

		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("mensajePublish", "Error al guardar la publicación");
			return new RedirectView("/club/" + idclub + "#publicaciones");
		}
	}

	@PostMapping("/delete/{idpublicacion}")
	public RedirectView eliminarPublicacion(
			@PathVariable("idpublicacion") int idpublicacion,
			RedirectAttributes redirectAttributes,
			HttpSession session) {

		// Verificar sesión y rol manager
		String rol = (String) session.getAttribute("rollogged");
		Club clubLogged = (Club) session.getAttribute("clublogged");

		if (rol == null || !"manager".equals(rol) || clubLogged == null) {
			redirectAttributes.addFlashAttribute("mensajePublish", "No tienes permisos para realizar esta acción");
			return new RedirectView("/home");
		}

		try {
			// Buscar la publicación
			Publicacion publicacion = publicacionService.findById(idpublicacion);
			if (publicacion == null) {
				redirectAttributes.addFlashAttribute("mensajePublish", "Publicación no encontrada");
				return new RedirectView("/club/" + clubLogged.getIdclub() + "#publicaciones");
			}

			// Verificar que la publicación pertenece al club del manager
			if (publicacion.getClub().getIdclub() != clubLogged.getIdclub()) {
				redirectAttributes.addFlashAttribute("mensajePublish", "No tienes permisos para realizar esta acción");
				return new RedirectView("/home");
			}

			int idclub = publicacion.getClub().getIdclub();

			// Eliminar la publicación (no hay tablas relacionadas)
			publicacionService.deleteById(idpublicacion);

			redirectAttributes.addFlashAttribute("mensajePublish", "La publicación ha sido eliminada");
			return new RedirectView("/club/" + idclub + "#publicaciones");

		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("mensajePublish", "No se ha podido eliminar la publicación");
			return new RedirectView("/club/" + clubLogged.getIdclub() + "#publicaciones");
		}
	}
}