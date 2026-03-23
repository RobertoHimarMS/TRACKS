package es.rhms.services;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import es.rhms.connections.MySqlConnection;
import es.rhms.models.Club;
import es.rhms.repositories.ClubRepository;

@Service
public class ClubService {

	@Autowired
	private ClubRepository clubRepository;

	private static final int SYSTEM_CLUB_ID = 1;							/* ID del club "System" - reservado para administración */

	/**
	 * Obtiene todos los clubes activos (excepto System) ordenados por fecha de creación
	 * @return Lista de clubes ordenada por fecha descendente
	 */
	public List<Club> findClubesPorFechaDesc() {
		List<Club> clubes = new ArrayList<>();
		MySqlConnection db = new MySqlConnection();

		try {
			db.open();

			String sql = "SELECT * FROM Club WHERE active = TRUE AND idclub <> " + SYSTEM_CLUB_ID + " ORDER BY aud_created_at DESC";
			ResultSet rs = db.executeSelect(sql);

			if (!db.isError() && rs != null) {
				while (rs.next()) {
					clubes.add(mapResultSetToClub(rs));
				}
			}
		} catch (SQLException e) {
			System.err.println("Error al obtener clubes: " + e.getMessage());
		} finally {
			db.close();
		}

		return clubes;
	}

	/**
	 * Obtiene un club por su ID
	 * @param id ID del club
	 * @return Club encontrado o null si no existe
	 */
	public Optional<Club> findById(int id) {
		Club club = null;
		MySqlConnection db = new MySqlConnection();

		try {
			db.open();

			String sql = "SELECT * FROM Club WHERE idclub = " + id;
			ResultSet rs = db.executeSelect(sql);

			if (!db.isError() && rs != null && rs.next()) {
				club = mapResultSetToClub(rs);
			}
		} catch (SQLException e) {
			System.err.println("Error al obtener club por ID: " + e.getMessage());
		} finally {
			db.close();
		}

		return Optional.ofNullable(club);
	}

	/**
	 * Obtiene todos los clubes activos de un deporte específico
	 * @param sport Deporte a filtrar
	 * @return Lista de clubes del deporte especificado
	 */
	public List<Club> findBySport(String sport) {
		List<Club> clubes = new ArrayList<>();
		MySqlConnection db = new MySqlConnection();

		try {
			db.open();

			String sql = "SELECT * FROM Club WHERE active = TRUE AND sport = '" + sport + "' ORDER BY aud_created_at ASC";
			ResultSet rs = db.executeSelect(sql);

			if (!db.isError() && rs != null) {
				while (rs.next()) {
					clubes.add(mapResultSetToClub(rs));
				}
			}
		} catch (SQLException e) {
			System.err.println("Error al obtener clubes por deporte: " + e.getMessage());
		} finally {
			db.close();
		}

		return clubes;
	}

	/**
	 * Mapea un ResultSet a un objeto Club
	 * @param rs ResultSet de la consulta
	 * @return Objeto Club con los datos del ResultSet
	 * @throws SQLException Si hay error al acceder a los datos
	 */
	private Club mapResultSetToClub(ResultSet rs) throws SQLException {
		Club club = new Club();

		club.setIdclub(rs.getInt("idclub"));
		club.setName(rs.getString("name"));
		club.setDescription(rs.getString("description"));
		club.setSport(rs.getString("sport"));
		club.setEmail(rs.getString("email"));
		club.setCp(rs.getString("cp"));
		club.setCity(rs.getString("city"));
		club.setPhoto(rs.getString("photo"));
		club.setActive(rs.getBoolean("active"));
		club.setCreatedAt(rs.getDate("aud_created_at"));
		club.setUpdatedAt(rs.getDate("aud_updated_at"));

		// aud_updated_by puede ser NULL
		int updatedBy = rs.getInt("aud_updated_by");
		if (rs.wasNull()) {
			club.setUpdatedBy(null);
		} else {
			club.setUpdatedBy(updatedBy);
		}

		// Request_idrequest puede ser NULL - no se carga aquí (requeriría otra consulta o join)
		// Se deja null por defecto

		return club;
	}

	/**
	 * Guarda un nuevo club usando JPA
	 * @param club Club a guardar
	 * @return Club guardado con ID generado
	 */
	public Club save(Club club) {
		return clubRepository.save(club);
	}

	/**
	 * Busca un club por su nombre
	 * @param name Nombre del club
	 * @return Optional con el club si existe
	 */
	public Optional<Club> findByName(String name) {
		return clubRepository.findByName(name);
	}

}