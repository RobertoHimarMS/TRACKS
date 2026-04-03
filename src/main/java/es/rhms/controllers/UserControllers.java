/** CLASE CONTROLADORA DE USUARIO
 *  ===========================================================================
 *  Gestiona las acciones del usuario logueado relacionadas con su perfil
 *  - Inscribirse/desinscribirse de actividades
 *  - Comprar productos
 *  - etc.
 *
 */

package es.rhms.controllers;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import es.rhms.models.Actividad;
import es.rhms.models.Club;
import es.rhms.models.Producto;
import es.rhms.models.Usuario;
import es.rhms.services.ActividadService;
import es.rhms.services.CompraService;
import es.rhms.services.ProductoService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/user")
public class UserControllers {

	@Autowired
	private ActividadService actividadService;

	@Autowired
	private CompraService compraService;

	@Autowired
	private ProductoService productoService;

	/**
	 * Inscribe al usuario logueado en una actividad
	 * Solo permitido si el usuario pertenece al mismo club que la actividad
	 */
	@PostMapping("/activity/suscribe/{idact}")
	public ResponseEntity<Map<String, Object>> suscribirActividad(
			@PathVariable("idact") int idact,
			HttpServletRequest request) {

		Map<String, Object> response = new HashMap<>();

		// Verificar sesión
		HttpSession session = request.getSession(false);
		if (session == null || session.getAttribute("userlogged") == null) {
			response.put("success", false);
			response.put("error", "No autenticado");
			return ResponseEntity.status(401).body(response);
		}

		Usuario usuario = (Usuario) session.getAttribute("userlogged");
		Club clubLogged = (Club) session.getAttribute("clublogged");

		if (clubLogged == null) {
			response.put("success", false);
			response.put("error", "Sin club activo");
			return ResponseEntity.status(403).body(response);
		}

		// Obtener la actividad
		Actividad actividad = actividadService.findById(idact);
		if (actividad == null) {
			response.put("success", false);
			response.put("error", "Actividad no encontrada");
			return ResponseEntity.status(404).body(response);
		}

		// Verificar que la actividad pertenece al club del usuario
		if (actividad.getClub().getIdclub() != clubLogged.getIdclub()) {
			response.put("success", false);
			response.put("error", "Actividad no pertenece a tu club");
			return ResponseEntity.status(403).body(response);
		}

		// Inscribir al usuario
		boolean inscrito = actividadService.inscribirUsuario(usuario.getIduser(), idact);

		if (inscrito) {
			response.put("success", true);
			response.put("message", "Inscrito correctamente");
		} else {
			response.put("success", false);
			response.put("error", "Ya estabas inscrito o error al inscribir");
		}

		return ResponseEntity.ok(response);
	}

	/**
	 * Desinscribe al usuario logueado de una actividad
	 */
	@PostMapping("/activity/unsuscribe/{idact}")
	public ResponseEntity<Map<String, Object>> desinscribirActividad(
			@PathVariable("idact") int idact,
			HttpServletRequest request) {

		Map<String, Object> response = new HashMap<>();

		// Verificar sesión
		HttpSession session = request.getSession(false);
		if (session == null || session.getAttribute("userlogged") == null) {
			response.put("success", false);
			response.put("error", "No autenticado");
			return ResponseEntity.status(401).body(response);
		}

		Usuario usuario = (Usuario) session.getAttribute("userlogged");

		// Desinscribir al usuario
		boolean desinscrito = actividadService.desinscribirUsuario(usuario.getIduser(), idact);

		if (desinscrito) {
			response.put("success", true);
			response.put("message", "Desinscrito correctamente");
		} else {
			response.put("success", false);
			response.put("error", "No estabas inscrito o error al desinscribir");
		}

		return ResponseEntity.ok(response);
	}

	/**
	 * Reserva un producto para el usuario logueado (compra con estado reserved)
	 * Solo permitido si el usuario pertenece al mismo club que el producto y no es admin
	 */
	@PostMapping("/product/reserve/{idproducto}")
	public ResponseEntity<Map<String, Object>> reservarProducto(
			@PathVariable("idproducto") int idproducto,
			@RequestParam("cantidad") int cantidad,
			HttpServletRequest request) {

		Map<String, Object> response = new HashMap<>();

		// Verificar sesión
		HttpSession session = request.getSession(false);
		if (session == null || session.getAttribute("userlogged") == null) {
			response.put("success", false);
			response.put("error", "No autenticado");
			return ResponseEntity.status(401).body(response);
		}

		Usuario usuario = (Usuario) session.getAttribute("userlogged");
		Club clubLogged = (Club) session.getAttribute("clublogged");
		String rolLogged = (String) session.getAttribute("rollogged");

		if (clubLogged == null) {
			response.put("success", false);
			response.put("error", "Sin club activo");
			return ResponseEntity.status(403).body(response);
		}

		// Verificar que no es admin (admin no compra)
		if ("admin".equals(rolLogged)) {
			response.put("success", false);
			response.put("error", "El administrador no puede realizar compras");
			return ResponseEntity.status(403).body(response);
		}

		// Verificar cantidad válida
		if (cantidad <= 0) {
			response.put("success", false);
			response.put("error", "La cantidad debe ser mayor que 0");
			return ResponseEntity.status(400).body(response);
		}

		// Obtener el producto
		Producto producto = productoService.findById(idproducto);
		if (producto == null) {
			response.put("success", false);
			response.put("error", "Producto no encontrado");
			return ResponseEntity.status(404).body(response);
		}

		// Verificar que el producto pertenece al club del usuario
		if (producto.getClub().getIdclub() != clubLogged.getIdclub()) {
			response.put("success", false);
			response.put("error", "No eres socio de este club");
			return ResponseEntity.status(403).body(response);
		}

		// Intentar reservar el producto
		boolean reservado = compraService.reservarProducto(usuario.getIduser(), idproducto, cantidad);

		if (reservado) {
			response.put("success", true);
			response.put("message", "Reserva confirmada. Seguimiento en su sección de compras");
		} else {
			response.put("success", false);
			response.put("error", "Stock insuficiente");
			return ResponseEntity.status(400).body(response);
		}

		return ResponseEntity.ok(response);
	}

	/**
	 * Cancela una compra en estado reserved
	 * Solo permitido si la compra pertenece al usuario logueado
	 */
	@PostMapping("/product/cancel/{idcompra}")
	public ResponseEntity<Map<String, Object>> cancelarCompra(
			@PathVariable("idcompra") int idcompra,
			HttpServletRequest request) {

		Map<String, Object> response = new HashMap<>();

		// Verificar sesión
		HttpSession session = request.getSession(false);
		if (session == null || session.getAttribute("userlogged") == null) {
			response.put("success", false);
			response.put("error", "No autenticado");
			return ResponseEntity.status(401).body(response);
		}

		Usuario usuario = (Usuario) session.getAttribute("userlogged");

		// Intentar cancelar la compra
		boolean cancelado = compraService.cancelarCompra(usuario.getIduser(), idcompra);

		if (cancelado) {
			response.put("success", true);
			response.put("message", "Compra cancelada correctamente");
		} else {
			response.put("success", false);
			response.put("error", "No se puede cancelar esta compra");
			return ResponseEntity.status(400).body(response);
		}

		return ResponseEntity.ok(response);
	}

}