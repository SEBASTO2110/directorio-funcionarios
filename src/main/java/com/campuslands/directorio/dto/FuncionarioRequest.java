package com.campuslands.directorio.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Cuerpo JSON que recibe la API al registrar o actualizar un funcionario.
 *
 * {@code @NotBlank} rechaza los valores nulos, vacios o con solo espacios.
 * El id se recibe solo para poder responder 400 si alguien lo envia al registrar.
 */
public record FuncionarioRequest(

		Long id,

		@NotBlank(message = "El nombre es obligatorio")
		@Size(max = 120, message = "El nombre admite máximo 120 caracteres")
		String nombre,

		@NotBlank(message = "El cargo es obligatorio")
		@Size(max = 120, message = "El cargo admite máximo 120 caracteres")
		String cargo,

		@NotBlank(message = "El departamento es obligatorio")
		@Size(max = 120, message = "El departamento admite máximo 120 caracteres")
		String departamento,

		@Size(max = 30, message = "El teléfono admite máximo 30 caracteres")
		String telefono) {
}
