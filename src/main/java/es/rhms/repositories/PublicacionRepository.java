package es.rhms.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import es.rhms.models.Publicacion;

@Repository
public interface PublicacionRepository extends JpaRepository<Publicacion, Integer> {

	@Query("SELECT p FROM Publicacion p JOIN FETCH p.club c WHERE c.idclub = :idclub ORDER BY p.idpublicacion DESC")
	List<Publicacion> findByClubId(@Param("idclub") int idclub);

	void deleteByClub_Idclub(int idclub);

}