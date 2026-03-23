package es.rhms.models;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "Actividad")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Actividad {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "idactividad")
	private int idactividad;

	@Column(name = "title", nullable = false, length = 40)
	private String title;

	@Column(name = "description", nullable = false, length = 240)
	private String description;

	@Column(name = "sport", nullable = false, length = 20)
	private String sport;

	@Column(name = "fecha", nullable = false)
	private Date fecha;

	@Column(name = "place", nullable = false, length = 40)
	private String place;

	@Column(name = "distancia")
	private Integer distancia;

	@Column(name = "photo", length = 240)
	private String photo;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "Club_idclub", nullable = false)
	private Club club;

	@ManyToMany(mappedBy = "actividadesInscritas")
	private Set<Usuario> usuariosInscritos;

	/**
	 * Devuelve el icono de Font Awesome correspondiente al deporte
	 */
	public String getSportIcon() {
		if (sport == null) return "fa-medal";
		return switch (sport.toLowerCase()) {
			case "run", "running", "athletics" -> "fa-person-running";
			case "basket", "basketball" -> "fa-basketball";
			case "swim", "swimming" -> "fa-swimmer";
			case "hike", "hiking" -> "fa-person-hiking";
			case "trail" -> "fa-mountain";
			case "cycling", "bicycle" -> "fa-bicycle";
			case "futbol", "soccer" -> "fa-futbol";
			case "volleyball" -> "fa-volleyball";
			case "baseball" -> "fa-baseball";
			case "tennis", "pingpong" -> "fa-table-tennis-paddle-ball";
			case "hockey" -> "fa-hockey-puck";
			case "skating" -> "fa-person-skating";
			case "ski", "skiing" -> "fa-person-skiing";
			case "snowboard" -> "fa-person-snowboarding";
			case "handball", "martialarts" -> "fa-hand-fist";
			case "gym", "fitness" -> "fa-dumbbell";
			case "cardio" -> "fa-heart-pulse";
			case "trophy", "trofeo" -> "fa-trophy";
			default -> "fa-medal";
		};
	}

	/**
	 * Devuelve la fecha formateada para visualización
	 */
	public String getFechaFormateada() {
		if (fecha == null) return "";
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
		return sdf.format(fecha);
	}

	/**
	 * Devuelve la fecha corta (sin hora) para visualización
	 */
	public String getFechaCorta() {
		if (fecha == null) return "";
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
		return sdf.format(fecha);
	}

}