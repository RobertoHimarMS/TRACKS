package es.rhms.models;

import java.math.BigDecimal;
import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "compra")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Compra {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "idcompra")
	private int idcompra;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "Users_iduser", nullable = false)
	private Usuario usuario;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "Producto_idproducto", nullable = false)
	private Producto producto;

	@Column(name = "cantidad", nullable = false)
	private int cantidad;

	@Column(name = "total", nullable = false, precision = 10, scale = 2)
	private BigDecimal total;

	@Enumerated(EnumType.STRING)
	@Column(name = "estado", nullable = false)
	private EstadoCompra estado = EstadoCompra.reserved;

	@Column(name = "aud_created_at", insertable = false, updatable = false)
	private Date createdAt;

	@Column(name = "aud_updated_at", insertable = false, updatable = false)
	private Date updatedAt;

	@Column(name = "aud_updated_by")
	private Integer updatedBy;

	public enum EstadoCompra {
		reserved, paid, collected, cancelled
	}

}