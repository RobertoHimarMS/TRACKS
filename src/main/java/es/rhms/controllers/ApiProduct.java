/** API REST PARA GESTIÓN DE PRODUCTOS
 *  ===========================================================================
 *  POST /api/product/create → Crea un nuevo producto para un club
 *  POST /api/product/update/{idproducto} → Actualiza un producto existente
 *  POST /api/product/delete/{idproducto} → Marca producto como eliminado (stock=-1)
 *
 */

package es.rhms.controllers;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import es.rhms.models.Club;
import es.rhms.models.Producto;
import es.rhms.services.ClubService;
import es.rhms.services.ProductoService;
import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api/product")
public class ApiProduct {

	@Autowired
	private ProductoService productoService;

	@Autowired
	private ClubService clubService;

	@PostMapping("/create")
	public RedirectView crearProducto(
			@RequestParam("idclub") int idclub,
			@RequestParam("name") String name,
			@RequestParam("description") String description,
			@RequestParam("precio") BigDecimal precio,
			@RequestParam("stock") int stock,
			RedirectAttributes redirectAttributes,
			HttpSession session) {

		// Verificar sesión y rol manager
		String rol = (String) session.getAttribute("rollogged");
		Club clubLogged = (Club) session.getAttribute("clublogged");

		if (rol == null || !"manager".equals(rol) || clubLogged == null || clubLogged.getIdclub() != idclub) {
			redirectAttributes.addFlashAttribute("mensajeProduct", "No tienes permisos para realizar esta acción");
			return new RedirectView("/home");
		}

		try {
			// Obtener el club
			Club club = clubService.findById(idclub).orElse(null);
			if (club == null) {
				redirectAttributes.addFlashAttribute("mensajeProduct", "Club no encontrado");
				return new RedirectView("/home");
			}

			// Crear el producto
			Producto producto = new Producto();
			producto.setName(name);
			producto.setDescription(description);
			producto.setPrecio(precio);
			producto.setStock(stock);
			producto.setClub(club);

			// Guardar
			productoService.save(producto);

			redirectAttributes.addFlashAttribute("mensajeProduct", "Producto creado con éxito");
			return new RedirectView("/club/" + idclub + "#tienda");

		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("mensajeProduct", "Error al crear el producto");
			return new RedirectView("/club/" + idclub + "#tienda");
		}
	}

	@PostMapping("/update/{idproducto}")
	public RedirectView actualizarProducto(
			@PathVariable("idproducto") int idproducto,
			@RequestParam("idclub") int idclub,
			@RequestParam("name") String name,
			@RequestParam("description") String description,
			@RequestParam("precio") BigDecimal precio,
			@RequestParam("stock") int stock,
			RedirectAttributes redirectAttributes,
			HttpSession session) {

		// Verificar sesión y rol manager
		String rol = (String) session.getAttribute("rollogged");
		Club clubLogged = (Club) session.getAttribute("clublogged");

		if (rol == null || !"manager".equals(rol) || clubLogged == null || clubLogged.getIdclub() != idclub) {
			redirectAttributes.addFlashAttribute("mensajeProduct", "No tienes permisos para realizar esta acción");
			return new RedirectView("/home");
		}

		try {
			// Buscar el producto existente
			Producto producto = productoService.findById(idproducto);
			if (producto == null) {
				redirectAttributes.addFlashAttribute("mensajeProduct", "Error al guardar el producto");
				return new RedirectView("/club/" + idclub + "#tienda");
			}

			// Verificar que el producto pertenece al club del manager
			if (producto.getClub().getIdclub() != idclub) {
				redirectAttributes.addFlashAttribute("mensajeProduct", "No tienes permisos para realizar esta acción");
				return new RedirectView("/home");
			}

			// Actualizar todos los campos
			producto.setName(name);
			producto.setDescription(description);
			producto.setPrecio(precio);
			producto.setStock(stock);

			// Guardar
			productoService.save(producto);

			redirectAttributes.addFlashAttribute("mensajeProduct", "El producto se actualizó");
			return new RedirectView("/club/" + idclub + "#tienda");

		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("mensajeProduct", "Error al guardar el producto");
			return new RedirectView("/club/" + idclub + "#tienda");
		}
	}

	@PostMapping("/delete/{idproducto}")
	public RedirectView eliminarProducto(
			@PathVariable("idproducto") int idproducto,
			RedirectAttributes redirectAttributes,
			HttpSession session) {

		// Verificar sesión y rol manager
		String rol = (String) session.getAttribute("rollogged");
		Club clubLogged = (Club) session.getAttribute("clublogged");

		if (rol == null || !"manager".equals(rol) || clubLogged == null) {
			redirectAttributes.addFlashAttribute("mensajeProduct", "No tienes permisos para realizar esta acción");
			return new RedirectView("/home");
		}

		try {
			// Buscar el producto
			Producto producto = productoService.findById(idproducto);
			if (producto == null) {
				redirectAttributes.addFlashAttribute("mensajeProduct", "Producto no encontrado");
				return new RedirectView("/club/" + clubLogged.getIdclub() + "#tienda");
			}

			// Verificar que el producto pertenece al club del manager
			if (producto.getClub().getIdclub() != clubLogged.getIdclub()) {
				redirectAttributes.addFlashAttribute("mensajeProduct", "No tienes permisos para realizar esta acción");
				return new RedirectView("/home");
			}

			int idclub = producto.getClub().getIdclub();

			// Soft delete: marcar como eliminado (stock=-1)
			productoService.softDeleteById(idproducto);

			redirectAttributes.addFlashAttribute("mensajeProduct", "El producto ha sido eliminado");
			return new RedirectView("/club/" + idclub + "#tienda");

		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("mensajeProduct", "No se ha podido eliminar el producto");
			return new RedirectView("/club/" + clubLogged.getIdclub() + "#tienda");
		}
	}
}