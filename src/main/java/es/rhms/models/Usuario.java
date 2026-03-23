package es.rhms.models;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "Users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Usuario {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "iduser")
	private int iduser;

	@Column(name = "dni", unique = true, nullable = false, length = 20)
	private String dni;

	@Column(name = "name", nullable = false, length = 40)
	private String name;

	@Column(name = "surname", nullable = false, length = 40)
	private String surname;

	@Column(name = "email", unique = true, nullable = false, length = 50)
	private String email;

	@Column(name = "passwd", nullable = false, length = 255)
	private String passwd;

	@Column(name = "cp", length = 8)
	private String cp;

	@Column(name = "city", length = 40)
	private String city;

	@Column(name = "borned")
	private Date borned;

	@Column(name = "phone", length = 20)
	private String phone;

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
	private Request request;

	@ManyToMany
	@JoinTable(
		name = "se_inscribe",
		joinColumns = @JoinColumn(name = "Users_iduser"),
		inverseJoinColumns = @JoinColumn(name = "Actividad_idactividad")
	)
	private Set<Actividad> actividadesInscritas = new HashSet<>();

}