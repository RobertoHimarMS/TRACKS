package es.rhms.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import es.rhms.models.Compra;
import es.rhms.models.Usuario;

@Repository
public interface CompraRepository extends JpaRepository<Compra, Integer> {

	List<Compra> findByUsuario(Usuario usuario);

	@Query("SELECT c FROM Compra c JOIN FETCH c.producto p WHERE c.usuario.iduser = :userId AND p.club.idclub = :clubId ORDER BY c.createdAt DESC")
	List<Compra> findComprasByUserIdAndClubId(@Param("userId") int userId, @Param("clubId") int clubId);

}