package es.rhms.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import es.rhms.models.Club;
import es.rhms.models.Socios;
import es.rhms.models.SociosId;
import es.rhms.models.Usuario;

@Repository
public interface SociosRepository extends JpaRepository<Socios, SociosId> {

	List<Socios> findByClub(Club club);

	List<Socios> findByUsuario(Usuario usuario);

	Optional<Socios> findByUsuarioAndClub(Usuario usuario, Club club);

	boolean existsByUsuarioIduserAndClubIdclubAndUnsuscribedAtIsNull(int userId, int clubId);

}