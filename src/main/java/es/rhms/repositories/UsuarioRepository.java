package es.rhms.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import es.rhms.models.Usuario;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {

	Optional<Usuario> findByEmail(String email);

	Optional<Usuario> findByDni(String dni);

	@Modifying
	@Query("UPDATE Usuario u SET u.active = false, u.updatedBy = :iduser WHERE u.iduser = :iduser")
	int deactivateUser(@Param("iduser") int iduser);

}