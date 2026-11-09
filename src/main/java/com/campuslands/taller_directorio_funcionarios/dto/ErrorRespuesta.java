package com.campuslands.taller_directorio_funcionarios.dto;

import java.util.Map;

/**
 * Cuerpo JSON de las respuestas de error (400 y 404).
 *
 * @param estado  codigo HTTP de la respuesta
 * @param mensaje explicacion general del error
 * @param errores detalle por campo cuando falla la validacion; vacio en los demas casos
 */
public record ErrorRespuesta(int estado, String mensaje, Map<String, String> errores) {

	public ErrorRespuesta(int estado, String mensaje) {
		this(estado, mensaje, Map.of());
	}

}
