/** API REST PARA GESTIÓN DE PUBLICACIONES
 *  ===========================================================================
 *  POST /api/publish/create → Crea una nueva publicación para un club
 *  POST /api/publish/update/{idpublicacion} → Actualiza una publicación existente
 *  POST /api/publish/delete/{idpublicacion} → Elimina una publicación
 */

package es.rhms.controllers;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.stereotype.Controller;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import es.rhms.models.Club;
import es.rhms.models.Publicacion;
import es.rhms.services.ClubService;
import es.rhms.services.PublicacionService;
import es.rhms.utilities.FileUploadUtility;
import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/api/publish")
public class ApiPublish {

	@Autowired
	private PublicacionService publicacionService;

	@Autowired
	private ClubService clubService;

	@Autowired
	private FileUploadUtility fileUploadUtility;

	@PostMapping("/create")
	public String crearPublicacion(
			@RequestParam("idclub") int idclub,
			@RequestParam("subject") String subject,
			@RequestParam("text") String text,
			@RequestParam(value = "photo", required = false) MultipartFile photo,
			RedirectAttributes redirectAttributes,
			HttpSession session) {

		// Verificar sesión y rol manager
		String rol = (String) session.getAttribute("rollogged");
		Club clubLogged = (Club) session.getAttribute("clublogged");

		if (rol == null || !"manager".equals(rol) || clubLogged == null || clubLogged.getIdclub() != idclub) {
			redirectAttributes.addFlashAttribute("mensajePublish", "No tienes permisos para realizar esta acción");
			return "redirect:/home";
		}

		try {
			// Obtener el club
			Club club = clubService.findById(idclub).orElse(null);
			if (club == null) {
				redirectAttributes.addFlashAttribute("mensajePublish", "Club no encontrado");
				return "redirect:/home";
			}

			// Crear la publicación
			Publicacion publicacion = new Publicacion();
			publicacion.setSubject(subject);
			publicacion.setText(text);
			publicacion.setClub(club);

			// Procesar imagen si se ha subido
			if (photo != null && !photo.isEmpty()) {
				try {
					String photoName = fileUploadUtility.saveImage(photo, "publishs");
					publicacion.setPhoto(photoName);
				} catch (IllegalArgumentException e) {
					redirectAttributes.addFlashAttribute("mensajePublish", e.getMessage());
					return "redirect:/club/" + idclub + "#publicaciones";
				}
			}

			// Guardar
			publicacionService.save(publicacion);

			redirectAttributes.addFlashAttribute("mensajePublish", "Publicación creada con éxito");
			return "redirect:/club/" + idclub + "#publicaciones";

		} catch (IOException e) {
			redirectAttributes.addFlashAttribute("mensajePublish", "Error al subir la imagen");
			return "redirect:/club/" + idclub + "#publicaciones";
		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("mensajePublish", "Error al crear la publicación");
			return "redirect:/club/" + idclub + "#publicaciones";
		}
	}

	@PostMapping("/update/{idpublicacion}")
	public String actualizarPublicacion(
			@PathVariable("idpublicacion") int idpublicacion,
			@RequestParam("idclub") int idclub,
			@RequestParam("subject") String subject,
			@RequestParam("text") String text,
			@RequestParam(value = "photo", required = false) MultipartFile photo,
			RedirectAttributes redirectAttributes,
			HttpSession session) {

		// Verificar sesión y rol manager
		String rol = (String) session.getAttribute("rollogged");
		Club clubLogged = (Club) session.getAttribute("clublogged");

		if (rol == null || !"manager".equals(rol) || clubLogged == null || clubLogged.getIdclub() != idclub) {
			redirectAttributes.addFlashAttribute("mensajePublish", "No tienes permisos para realizar esta acción");
			return "redirect:/home";
		}

		try {
			// Buscar la publicación existente
			Publicacion publicacion = publicacionService.findById(idpublicacion);
			if (publicacion == null) {
				redirectAttributes.addFlashAttribute("mensajePublish", "Error al guardar la publicación");
				return "redirect:/club/" + idclub + "#publicaciones";
			}

			// Verificar que la publicación pertenece al club del manager
			if (publicacion.getClub().getIdclub() != idclub) {
				redirectAttributes.addFlashAttribute("mensajePublish", "No tienes permisos para realizar esta acción");
				return "redirect:/home";
			}

			// Actualizar todos los campos
			publicacion.setSubject(subject);
			publicacion.setText(text);

			// Procesar imagen si se ha subido una nueva
			if (photo != null && !photo.isEmpty()) {
				try {
					String photoName = fileUploadUtility.saveImage(photo, "publishs");
					publicacion.setPhoto(photoName);
				} catch (IllegalArgumentException e) {
					redirectAttributes.addFlashAttribute("mensajePublish", e.getMessage());
					return "redirect:/club/" + idclub + "#publicaciones";
				}
			}

			// Guardar
			publicacionService.save(publicacion);

			redirectAttributes.addFlashAttribute("mensajePublish", "La publicación se actualizó");
			return "redirect:/club/" + idclub + "#publicaciones";

		} catch (IOException e) {
			redirectAttributes.addFlashAttribute("mensajePublish", "Error al subir la imagen");
			return "redirect:/club/" + idclub + "#publicaciones";
		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("mensajePublish", "Error al guardar la publicación: " + e.getMessage());
			return "redirect:/club/" + idclub + "#publicaciones";
		}
	}

	@PostMapping("/delete/{idpublicacion}")
	public String eliminarPublicacion(
			@PathVariable("idpublicacion") int idpublicacion,
			RedirectAttributes redirectAttributes,
			HttpSession session) {

		// Verificar sesión y rol manager
		String rol = (String) session.getAttribute("rollogged");
		Club clubLogged = (Club) session.getAttribute("clublogged");

		if (rol == null || !"manager".equals(rol) || clubLogged == null) {
			redirectAttributes.addFlashAttribute("mensajePublish", "No tienes permisos para realizar esta acción");
			return "redirect:/home";
		}

		try {
			// Buscar la publicación
			Publicacion publicacion = publicacionService.findById(idpublicacion);
			if (publicacion == null) {
				redirectAttributes.addFlashAttribute("mensajePublish", "Publicación no encontrada");
				return "redirect:/club/" + clubLogged.getIdclub() + "#publicaciones";
			}

			// Verificar que la publicación pertenece al club del manager
			if (publicacion.getClub().getIdclub() != clubLogged.getIdclub()) {
				redirectAttributes.addFlashAttribute("mensajePublish", "No tienes permisos para realizar esta acción");
				return "redirect:/home";
			}

			int idclub = publicacion.getClub().getIdclub();

			// Eliminar la publicación (no hay tablas relacionadas)
			publicacionService.deleteById(idpublicacion);

			redirectAttributes.addFlashAttribute("mensajePublish", "La publicación ha sido eliminada");
			return "redirect:/club/" + idclub + "#publicaciones";

		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("mensajePublish", "No se ha podido eliminar la publicación");
			return "redirect:/club/" + clubLogged.getIdclub() + "#publicaciones";
		}
	}
}