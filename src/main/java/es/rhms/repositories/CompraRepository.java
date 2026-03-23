package es.rhms.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import es.rhms.models.Compra;
import es.rhms.models.Usuario;

@Repository
public interface CompraRepository extends JpaRepository<Compra, Integer> {

	List<Compra> findByUsuario(Usuario usuario);

}