/** API REST PARA GESTIÓN DE PRODUCTOS
 *  ===========================================================================
 *  POST /api/product/create → Crea un nuevo producto para un club
 *  POST /api/product/update/{idproducto} → Actualiza un producto existente
 *  POST /api/product/delete/{idproducto} → Marca producto como eliminado (stock=-1)
 */

package es.rhms.controllers;

import java.io.IOException;
import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import es.rhms.models.Club;
import es.rhms.models.Producto;
import es.rhms.services.ClubService;
import es.rhms.services.ProductoService;
import es.rhms.utilities.FileUploadUtility;
import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/api/product")
public class ApiProduct {

	@Autowired
	private ProductoService productoService;

	@Autowired
	private ClubService clubService;

	@Autowired
	private FileUploadUtility fileUploadUtility;

	@PostMapping("/create")
	public String crearProducto(
			@RequestParam("idclub") int idclub,
			@RequestParam("name") String name,
			@RequestParam("description") String description,
			@RequestParam("precio") BigDecimal precio,
			@RequestParam("stock") int stock,
			@RequestParam(value = "photo", required = false) MultipartFile photo,
			RedirectAttributes redirectAttributes,
			HttpSession session) {

		// Verificar sesión y rol manager
		String rol = (String) session.getAttribute("rollogged");
		Club clubLogged = (Club) session.getAttribute("clublogged");

		if (rol == null || !"manager".equals(rol) || clubLogged == null || clubLogged.getIdclub() != idclub) {
			redirectAttributes.addFlashAttribute("mensajeProduct", "No tienes permisos para realizar esta acción");
			return "redirect:/home";
		}

		try {
			Club club = clubService.findById(idclub).orElse(null);
			if (club == null) {
				redirectAttributes.addFlashAttribute("mensajeProduct", "Club no encontrado");
				return "redirect:/home";
			}

			Producto producto = new Producto();
			producto.setName(name);
			producto.setDescription(description);
			producto.setPrecio(precio);
			producto.setStock(stock);
			producto.setClub(club);

			if (photo != null && !photo.isEmpty()) {
				try {
					String photoName = fileUploadUtility.saveImage(photo, "products");
					producto.setPhoto(photoName);
				} catch (IllegalArgumentException e) {
					redirectAttributes.addFlashAttribute("mensajeProduct", e.getMessage());
					return "redirect:/club/" + idclub + "#tienda";
				}
			}

			productoService.save(producto);

			redirectAttributes.addFlashAttribute("mensajeProduct", "Producto creado con éxito");
			return "redirect:/club/" + idclub + "#tienda";

		} catch (IOException e) {
			redirectAttributes.addFlashAttribute("mensajeProduct", "Error al subir la imagen");
			return "redirect:/club/" + idclub + "#tienda";
		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("mensajeProduct", "Error al crear el producto");
			return "redirect:/club/" + idclub + "#tienda";
		}
	}

	@PostMapping("/update/{idproducto}")
	public String actualizarProducto(
			@PathVariable("idproducto") int idproducto,
			@RequestParam("idclub") int idclub,
			@RequestParam("name") String name,
			@RequestParam("description") String description,
			@RequestParam("precio") BigDecimal precio,
			@RequestParam("stock") int stock,
			@RequestParam(value = "photo", required = false) MultipartFile photo,
			RedirectAttributes redirectAttributes,
			HttpSession session) {

		// Verificar sesión y rol manager
		String rol = (String) session.getAttribute("rollogged");
		Club clubLogged = (Club) session.getAttribute("clublogged");

		if (rol == null || !"manager".equals(rol) || clubLogged == null || clubLogged.getIdclub() != idclub) {
			redirectAttributes.addFlashAttribute("mensajeProduct", "No tienes permisos para realizar esta acción");
			return "redirect:/home";
		}

		try {
			Producto producto = productoService.findById(idproducto);
			if (producto == null) {
				redirectAttributes.addFlashAttribute("mensajeProduct", "Error al guardar el producto");
				return "redirect:/club/" + idclub + "#tienda";
			}

			if (producto.getClub().getIdclub() != idclub) {
				redirectAttributes.addFlashAttribute("mensajeProduct", "No tienes permisos para realizar esta acción");
				return "redirect:/home";
			}

			producto.setName(name);
			producto.setDescription(description);
			producto.setPrecio(precio);
			producto.setStock(stock);

			if (photo != null && !photo.isEmpty()) {
				try {
					String photoName = fileUploadUtility.saveImage(photo, "products");
					producto.setPhoto(photoName);
				} catch (IllegalArgumentException e) {
					redirectAttributes.addFlashAttribute("mensajeProduct", e.getMessage());
					return "redirect:/club/" + idclub + "#tienda";
				}
			}

			productoService.save(producto);

			redirectAttributes.addFlashAttribute("mensajeProduct", "El producto se actualizó");
			return "redirect:/club/" + idclub + "#tienda";

		} catch (IOException e) {
			redirectAttributes.addFlashAttribute("mensajeProduct", "Error al subir la imagen");
			return "redirect:/club/" + idclub + "#tienda";
		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("mensajeProduct", "Error al guardar el producto");
			return "redirect:/club/" + idclub + "#tienda";
		}
	}

	@PostMapping("/delete/{idproducto}")
	public String eliminarProducto(
			@PathVariable("idproducto") int idproducto,
			RedirectAttributes redirectAttributes,
			HttpSession session) {

		// Verificar sesión y rol manager
		String rol = (String) session.getAttribute("rollogged");
		Club clubLogged = (Club) session.getAttribute("clublogged");

		if (rol == null || !"manager".equals(rol) || clubLogged == null) {
			redirectAttributes.addFlashAttribute("mensajeProduct", "No tienes permisos para realizar esta acción");
			return "redirect:/home";
		}

		try {
			Producto producto = productoService.findById(idproducto);
			if (producto == null) {
				redirectAttributes.addFlashAttribute("mensajeProduct", "Producto no encontrado");
				return "redirect:/club/" + clubLogged.getIdclub() + "#tienda";
			}

			if (producto.getClub().getIdclub() != clubLogged.getIdclub()) {
				redirectAttributes.addFlashAttribute("mensajeProduct", "No tienes permisos para realizar esta acción");
				return "redirect:/home";
			}

			int idclub = producto.getClub().getIdclub();
			productoService.softDeleteById(idproducto);

			redirectAttributes.addFlashAttribute("mensajeProduct", "El producto ha sido eliminado");
			return "redirect:/club/" + idclub + "#tienda";

		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("mensajeProduct", "No se ha podido eliminar el producto");
			return "redirect:/club/" + clubLogged.getIdclub() + "#tienda";
		}
	}

}