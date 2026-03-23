package es.rhms.models;

import java.math.BigDecimal;

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
@Table(name = "Producto")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Producto {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "idproducto")
	private int idproducto;

	@Column(name = "name", nullable = false, length = 40)
	private String name;

	@Column(name = "description", nullable = false, length = 240)
	private String description;

	@Column(name = "photo", length = 240)
	private String photo;

	@Column(name = "precio", nullable = false, precision = 10, scale = 2)
	private BigDecimal precio;

	@Column(name = "stock", nullable = false)
	private int stock = 1;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "Club_idclub", nullable = false)
	private Club club;

}