package es.rhms.repositories;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import es.rhms.models.Socios;
import es.rhms.models.SociosId;
import es.rhms.models.Usuario;

@Repository
public interface SociosRepository extends JpaRepository<Socios, SociosId> {

	List<Socios> findByUsuario(Usuario usuario);

	boolean existsByUsuarioIduserAndClubIdclubAndUnsuscribedAtIsNull(int userId, int clubId);

	@Modifying
	@Query("UPDATE Socios s SET s.unsuscribedAt = CURRENT_TIMESTAMP WHERE s.club.idclub = :idclub AND s.unsuscribedAt IS NULL")
	int unsubscribeAllByClubId(@Param("idclub") int idclub);

	@Modifying
	@Query("UPDATE Socios s SET s.unsuscribedAt = CURRENT_TIMESTAMP WHERE s.usuario.iduser = :iduser AND s.club.idclub = :idclub AND s.unsuscribedAt IS NULL")
	int unsubscribeUserFromClub(@Param("iduser") int iduser, @Param("idclub") int idclub);

	@Query("SELECT COUNT(s) FROM Socios s WHERE s.usuario.iduser = :iduser AND s.unsuscribedAt IS NULL")
	long countActiveByUserId(@Param("iduser") int iduser);

}