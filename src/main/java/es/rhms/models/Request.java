package es.rhms.models;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "Request")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Request {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "idrequest")
	private int idrequest;

	@Enumerated(EnumType.STRING)
	@Column(name = "tipo", nullable = false)
	private TipoRequest tipo;

	@Enumerated(EnumType.STRING)
	@Column(name = "estado", nullable = false)
	private EstadoRequest estado;

	@Column(name = "clb_target", nullable = false, length = 50)
	private String clbTarget;

	@Column(name = "clb_description", nullable = false, length = 240)
	private String clbDescription;

	@Column(name = "clb_sport", nullable = false, length = 20)
	private String clbSport;

	@Column(name = "clb_email", nullable = false, length = 50)
	private String clbEmail;

	@Column(name = "clb_cp", length = 8)
	private String clbCp;

	@Column(name = "clb_city", length = 40)
	private String clbCity;

	@Column(name = "clb_photo", length = 240)
	private String clbPhoto;

	@Column(name = "usr_dni", nullable = false, length = 20)
	private String usrDni;

	@Column(name = "usr_name", nullable = false, length = 40)
	private String usrName;

	@Column(name = "usr_surname", nullable = false, length = 40)
	private String usrSurname;

	@Column(name = "usr_email", nullable = false, length = 50)
	private String usrEmail;

	@Column(name = "usr_passwd", nullable = false, length = 255)
	private String usrPasswd;

	@Column(name = "usr_cp", length = 8)
	private String usrCp;

	@Column(name = "usr_city", length = 40)
	private String usrCity;

	@Column(name = "usr_borned")
	private Date usrBorned;

	@Column(name = "usr_phone", length = 20)
	private String usrPhone;

	@Column(name = "usr_photo", length = 240)
	private String usrPhoto;

	@Column(name = "aud_created_at", insertable = false, updatable = false)
	private Date createdAt;

	@Column(name = "aud_updated_at", insertable = false, updatable = false)
	private Date updatedAt;

	@Column(name = "aud_updated_by")
	private Integer updatedBy;

	public enum TipoRequest {
		club, partner
	}

	public enum EstadoRequest {
		pending, accepted, rejected
	}

}