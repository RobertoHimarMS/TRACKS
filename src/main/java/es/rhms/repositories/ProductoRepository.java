package es.rhms.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import es.rhms.models.Producto;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Integer> {

	@Query("SELECT p FROM Producto p JOIN FETCH p.club c WHERE c.idclub = :idclub ORDER BY p.idproducto DESC")
	List<Producto> findByClubId(@Param("idclub") int idclub);

	@Modifying
	@Query("UPDATE Producto p SET p.stock = -1 WHERE p.club.idclub = :idclub")
	int markAsDeletedByClubId(@Param("idclub") int idclub);

	@Modifying
	@Query("UPDATE Producto p SET p.stock = p.stock - :cantidad WHERE p.idproducto = :idproducto AND p.stock >= :cantidad")
	int decrementarStock(@Param("idproducto") int idproducto, @Param("cantidad") int cantidad);

	@Modifying
	@Query("UPDATE Producto p SET p.stock = p.stock + :cantidad WHERE p.idproducto = :idproducto")
	int incrementarStock(@Param("idproducto") int idproducto, @Param("cantidad") int cantidad);

}