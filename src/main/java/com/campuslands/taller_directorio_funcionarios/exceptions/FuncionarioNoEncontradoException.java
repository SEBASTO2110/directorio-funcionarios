package com.campuslands.taller_directorio_funcionarios.exceptions;

/** Se lanza cuando no existe un funcionario con el ID solicitado. La API responde 404. */
public class FuncionarioNoEncontradoException extends RuntimeException {

	public FuncionarioNoEncontradoException(Long id) {
		super("No existe un funcionario con ID " + id);
	}

}
