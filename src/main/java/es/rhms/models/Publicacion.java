package es.rhms.models;

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
@Table(name = "Publicacion")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Publicacion {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "idpublicacion")
	private int idpublicacion;

	@Column(name = "subject", nullable = false, length = 40)
	private String subject;

	@Column(name = "text", nullable = false, length = 240)
	private String text;

	@Column(name = "photo", length = 240)
	private String photo;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "Club_idclub", nullable = false)
	private Club club;

}