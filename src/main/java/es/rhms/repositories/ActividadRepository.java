package es.rhms.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import es.rhms.models.Actividad;

@Repository
public interface ActividadRepository extends JpaRepository<Actividad, Integer> { 

	@Query("SELECT a FROM Actividad a JOIN FETCH a.club c WHERE c.idclub = :idclub ORDER BY a.fecha DESC")
	List<Actividad> findByClubId(@Param("idclub") int idclub);

}