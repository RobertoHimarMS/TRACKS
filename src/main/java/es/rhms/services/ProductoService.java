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
	 * Elimina un producto por su ID
	 * @param idproducto ID del producto a eliminar
	 */
	public void deleteById(int idproducto) {
		productoRepository.deleteById(idproducto);
	}

}