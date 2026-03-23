package es.rhms.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import es.rhms.models.Club;

@Repository
public interface ClubRepository extends JpaRepository<Club, Integer> {

	Optional<Club> findByEmail(String email);

	Optional<Club> findByName(String name);

}