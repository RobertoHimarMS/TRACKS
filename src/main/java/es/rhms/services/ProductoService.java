package es.rhms.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import es.rhms.models.Producto;
import es.rhms.repositories.ProductoRepository;

@Service
public class ProductoService {

	@Autowired
	private ProductoRepository productoRepository;

	/**
	 * Obtiene todos los productos de un club
	 * @param idclub ID del club
	 * @return Lista de productos del club
	 */
	public List<Producto> findByClubId(int idclub) {
		return productoRepository.findByClubId(idclub);
	}

	/**
	 * Guarda un nuevo producto
	 * @param producto Producto a guardar
	 * @return Producto guardado con ID generado
	 */
	public Producto save(Producto producto) {
		return productoRepository.save(producto);
	}

	/**
	 * Busca un producto por su ID
	 * @param idproducto ID del producto
	 * @return El producto o null si no existe
	 */
	public Producto findById(int idproducto) {
		return productoRepository.findById(idproducto).orElse(null);
	}

	/**
	 * Marca un producto como eliminado poniendo stock = -1
	 * (soft delete para mantener integridad con tabla Compra)
	 * @param idproducto ID del producto a marcar como eliminado
	 */
	public void softDeleteById(int idproducto) {
		Producto producto = productoRepository.findById(idproducto).orElse(null);
		if (producto != null) {
			producto.setStock(-1);
			productoRepository.save(producto);
		}
	}

}