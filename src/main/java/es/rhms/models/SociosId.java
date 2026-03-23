package es.rhms.models;

import java.io.Serializable;
import java.util.Objects;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SociosId implements Serializable {

	private static final long serialVersionUID = 1L;

	private int usuario;
	private int club;

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		SociosId that = (SociosId) o;
		return usuario == that.usuario && club == that.club;
	}

	@Override
	public int hashCode() {
		return Objects.hash(usuario, club);
	}

}