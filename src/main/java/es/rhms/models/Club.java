package es.rhms.models;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "Club")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Club {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "idclub")
	private int idclub;

	@Column(name = "name", nullable = false, length = 50)
	private String name;

	@Column(name = "description", nullable = false, length = 240)
	private String description;

	@Column(name = "sport", nullable = false, length = 20)
	private String sport;

	@Column(name = "email", nullable = false, length = 50)
	private String email;

	@Column(name = "cp", length = 8)
	private String cp;

	@Column(name = "city", length = 40)
	private String city;

	@Column(name = "photo", length = 240)
	private String photo;

	@Column(name = "active", nullable = false)
	private boolean active = true;

	@Column(name = "aud_created_at", insertable = false, updatable = false)
	private Date createdAt;

	@Column(name = "aud_updated_at", insertable = false, updatable = false)
	private Date updatedAt;

	@Column(name = "aud_updated_by")
	private Integer updatedBy;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "Request_idrequest")
	private Request request;											/* Se podría dejar sólo el integer idrequest?, es una opción */

	/**
	 * Devuelve la clase del icono Font Awesome según el deporte
	 * @return Clase CSS del icono (ej: "fa-person-running")
	 */
	public String getSportIcon() {
		if (sport == null) {
			return "fa-medal";
		}
		return switch (sport.toLowerCase()) {
			case "run", "running", "athletics"     -> "fa-person-running";
			case "basket", "basketball"            -> "fa-basketball";
			case "swim", "swimming"                -> "fa-swimmer";
			case "hike", "hiking"                  -> "fa-person-hiking";
			case "trail"                           -> "fa-mountain";
			case "cycling", "bicycle"              -> "fa-bicycle";
			case "futbol", "soccer"                -> "fa-futbol";
			case "volleyball"                      -> "fa-volleyball";
			case "baseball"                        -> "fa-baseball";
			case "tennis", "pingpong"              -> "fa-table-tennis-paddle-ball";
			case "hockey"                          -> "fa-hockey-puck";
			case "skating"                         -> "fa-person-skating";
			case "ski", "skiing"                   -> "fa-person-skiing";
			case "snowboard"                       -> "fa-person-snowboarding";
			case "handball", "martialarts"         -> "fa-hand-fist";
			case "gym", "fitness"                  -> "fa-dumbbell";
			case "cardio"                          -> "fa-heart-pulse";
			case "trophy", "trofeo"                -> "fa-trophy";
			default                                -> "fa-medal";
		};
	}

	/**
	 * Devuelve el nombre del deporte en español, capitalizado
	 * @return Nombre del deporte (ej: "Running", "Baloncesto")
	 */
	public String getSportName() {
		if (sport == null) {
			return "Deporte";
		}
		String name = switch (sport.toLowerCase()) {
			case "run", "running", "athletics"     -> "Running";
			case "basket", "basketball"            -> "Baloncesto";
			case "swim", "swimming"                -> "Natación";
			case "hike", "hiking"                  -> "Senderismo";
			case "trail"                           -> "Trail";
			case "cycling", "bicycle"              -> "Ciclismo";
			case "futbol", "soccer"                -> "Fútbol";
			case "volleyball"                      -> "Voleibol";
			case "baseball"                        -> "Béisbol";
			case "tennis"                          -> "Tenis";
			case "pingpong"                        -> "Ping-pong";
			case "hockey"                          -> "Hockey";
			case "skating"                         -> "Patinaje";
			case "ski", "skiing"                   -> "Esquí";
			case "snowboard"                       -> "Snowboard";
			case "handball"                        -> "Balonmano";
			case "martialarts"                     -> "Artes marciales";
			case "gym", "fitness"                  -> "Gimnasio";
			case "cardio"                          -> "Cardio";
			case "trophy", "trofeo"                -> "Trofeo";
			default                                -> sport;
		};
		return Character.toUpperCase(name.charAt(0)) + name.substring(1);
	}

}