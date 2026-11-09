package com.campuslands.taller_directorio_funcionarios.exceptions;

/** Se lanza cuando la solicitud incumple una regla del directorio. La API responde 400. */
public class SolicitudInvalidaException extends RuntimeException {

	public SolicitudInvalidaException(String mensaje) {
		super(mensaje);
	}

}
