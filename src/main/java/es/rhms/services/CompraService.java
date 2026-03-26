package es.rhms.services;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import es.rhms.models.Compra;
import es.rhms.models.Producto;
import es.rhms.models.Usuario;
import es.rhms.repositories.CompraRepository;
import es.rhms.repositories.ProductoRepository;
import es.rhms.repositories.UsuarioRepository;

@Service
public class CompraService {

	@Autowired
	private CompraRepository compraRepository;

	@Autowired
	private ProductoRepository productoRepository;

	@Autowired
	private UsuarioRepository usuarioRepository;

	/**
	 * Reserva un producto para un usuario (compra con estado reserved)
	 * @param userId ID del usuario que compra
	 * @param productoId ID del producto
	 * @param cantidad Cantidad a comprar
	 * @return true si la reserva fue exitosa, false si no hay stock suficiente
	 */
	@Transactional
	public boolean reservarProducto(int userId, int productoId, int cantidad) {
		// Verificar que la cantidad es válida
		if (cantidad <= 0) {
			return false;
		}

		// Intentar decrementar el stock de forma atómica
		int filasAfectadas = productoRepository.decrementarStock(productoId, cantidad);
		if (filasAfectadas == 0) {
			// No había stock suficiente
			return false;
		}

		// Obtener el producto actualizado para calcular el total
		Producto producto = productoRepository.findById(productoId).orElse(null);
		Usuario usuario = usuarioRepository.findById(userId).orElse(null);

		if (producto == null || usuario == null) {
			throw new RuntimeException("Producto o usuario no encontrado");
		}

		// Crear la compra
		Compra compra = new Compra();
		compra.setUsuario(usuario);
		compra.setProducto(producto);
		compra.setCantidad(cantidad);
		compra.setTotal(producto.getPrecio().multiply(new BigDecimal(cantidad)));
		compra.setEstado(Compra.EstadoCompra.reserved);
		compra.setUpdatedBy(userId); // Auditoría explícita (el trigger lo respetará)

		compraRepository.save(compra);
		return true;
	}

	/**
	 * Cancela una compra en estado reserved
	 * @param userId ID del usuario que cancela
	 * @param compraId ID de la compra a cancelar
	 * @return true si se canceló correctamente, false si no se pudo cancelar
	 */
	@Transactional
	public boolean cancelarCompra(int userId, int compraId) {
		// Obtener la compra
		Compra compra = compraRepository.findById(compraId).orElse(null);
		if (compra == null) {
			return false;
		}

		// Verificar que pertenece al usuario
		if (compra.getUsuario().getIduser() != userId) {
			return false;
		}

		// Verificar que está en estado reserved
		if (compra.getEstado() != Compra.EstadoCompra.reserved) {
			return false;
		}

		// Cambiar estado a cancelled
		compra.setEstado(Compra.EstadoCompra.cancelled);
		compra.setUpdatedBy(userId);

		// Restaurar stock del producto
		productoRepository.incrementarStock(compra.getProducto().getIdproducto(), compra.getCantidad());

		compraRepository.save(compra);
		return true;
	}

}