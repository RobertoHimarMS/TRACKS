package es.rhms.models;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "Ticket")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Ticket {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "idticket")
	private int idticket;

	@Column(name = "subject", nullable = false, length = 40)
	private String subject;

	@Column(name = "description", nullable = false, length = 240)
	private String description;

	@Column(name = "email", nullable = false, length = 50)
	private String email;

	@Column(name = "handled", nullable = false)
	private boolean handled = false;

	@Column(name = "aud_created_at", insertable = false, updatable = false)
	private Date createdAt;

	@Column(name = "aud_updated_at", insertable = false, updatable = false)
	private Date updatedAt;

	@Column(name = "aud_updated_by")
	private Integer updatedBy;

}