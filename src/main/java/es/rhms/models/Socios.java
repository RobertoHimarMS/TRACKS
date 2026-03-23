package es.rhms.models;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "pertenece_a")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@IdClass(SociosId.class)
public class Socios {

	@Id
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "Users_iduser", nullable = false)
	private Usuario usuario;

	@Id
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "Club_idclub", nullable = false)
	private Club club;

	@Column(name = "rol", nullable = false)
	@Enumerated(EnumType.STRING)
	private RolSocio rol;

	@Column(name = "registered_at", nullable = false, insertable = false)
	private Date registeredAt;

	@Column(name = "unsuscribed_at")
	private Date unsuscribedAt;

	public enum RolSocio {
		admin, manager, partner
	}

}