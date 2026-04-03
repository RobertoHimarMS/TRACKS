package es.rhms.repositories;

import java.util.List;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import es.rhms.models.Actividad;

@Repository
public interface ActividadRepository extends JpaRepository<Actividad, Integer> {

	@Query("SELECT a FROM Actividad a JOIN FETCH a.club c WHERE c.idclub = :idclub ORDER BY a.fecha DESC")
	List<Actividad> findByClubId(@Param("idclub") int idclub);

	@Query("SELECT a.idactividad FROM Actividad a WHERE a.club.idclub = :idclub")
	List<Integer> findIdsByClubId(@Param("idclub") int idclub);

	void deleteByClub_Idclub(int idclub);

	@Modifying
	@Query(value = "DELETE FROM se_inscribe WHERE Actividad_idactividad IN (SELECT idactividad FROM Actividad WHERE Club_idclub = :idclub)", nativeQuery = true)
	int deleteInscripcionesByClubId(@Param("idclub") int idclub);

	@Modifying
	@Query(value = "DELETE FROM se_inscribe WHERE Users_iduser = :iduser AND Actividad_idactividad IN (SELECT idactividad FROM Actividad WHERE Club_idclub = :idclub)", nativeQuery = true)
	int deleteInscripcionesByUserAndClub(@Param("iduser") int iduser, @Param("idclub") int idclub);

	@Query("SELECT a FROM Actividad a JOIN FETCH a.club c JOIN a.usuariosInscritos u WHERE u.iduser = :userId AND c.idclub = :clubId ORDER BY a.fecha DESC")
	List<Actividad> findInscripcionesByUserIdAndClubId(@Param("userId") int userId, @Param("clubId") int clubId);

	@Query("SELECT a.idactividad FROM Actividad a JOIN a.usuariosInscritos u WHERE u.iduser = :userId")
	Set<Integer> findInscribedActivityIds(@Param("userId") int userId);

	@Modifying
	@Query(value = "INSERT INTO se_inscribe (Users_iduser, Actividad_idactividad) VALUES (:userId, :actividadId)", nativeQuery = true)
	int inscribirUsuario(@Param("userId") int userId, @Param("actividadId") int actividadId);

	@Modifying
	@Query(value = "DELETE FROM se_inscribe WHERE Users_iduser = :userId AND Actividad_idactividad = :actividadId", nativeQuery = true)
	int desinscribirUsuario(@Param("userId") int userId, @Param("actividadId") int actividadId);

	@Modifying
	@Query(value = "DELETE FROM se_inscribe WHERE Actividad_idactividad = :actividadId", nativeQuery = true)
	int deleteInscripcionesByActividadId(@Param("actividadId") int actividadId);

}