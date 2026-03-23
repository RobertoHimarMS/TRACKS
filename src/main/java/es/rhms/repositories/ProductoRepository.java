package es.rhms.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import es.rhms.models.Producto;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Integer> {

	@Query("SELECT p FROM Producto p JOIN FETCH p.club c WHERE c.idclub = :idclub ORDER BY p.idproducto DESC")
	List<Producto> findByClubId(@Param("idclub") int idclub);

}