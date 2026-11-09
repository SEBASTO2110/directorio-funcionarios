package com.campuslands.directorio.exception;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import com.campuslands.directorio.dto.ErrorRespuesta;

/**
 * Convierte las excepciones de la API en respuestas JSON con el codigo HTTP que pide el taller.
 */
@RestControllerAdvice
public class ManejadorErrores {

	@ExceptionHandler(FuncionarioNoEncontradoException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public ErrorRespuesta noEncontrado(FuncionarioNoEncontradoException ex) {
		return new ErrorRespuesta(404, ex.getMessage());
	}

	@ExceptionHandler(SolicitudInvalidaException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ErrorRespuesta solicitudInvalida(SolicitudInvalidaException ex) {
		return new ErrorRespuesta(400, ex.getMessage());
	}

	/** Falta un campo obligatorio, esta vacio, solo tiene espacios o supera la longitud permitida. */
	@ExceptionHandler(MethodArgumentNotValidException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ErrorRespuesta camposInvalidos(MethodArgumentNotValidException ex) {
		Map<String, String> errores = new LinkedHashMap<>();
		for (FieldError error : ex.getBindingResult().getFieldErrors()) {
			errores.putIfAbsent(error.getField(), error.getDefaultMessage());
		}
		return new ErrorRespuesta(400, "Hay campos obligatorios sin diligenciar o con valores no válidos", errores);
	}

	/** El cuerpo no llego, no es JSON valido o trae un tipo de dato incorrecto. */
	@ExceptionHandler(HttpMessageNotReadableException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ErrorRespuesta cuerpoIlegible(HttpMessageNotReadableException ex) {
		return new ErrorRespuesta(400, "El cuerpo de la solicitud debe ser un JSON válido");
	}

	/** El ID de la ruta no es un numero, por ejemplo /api/v1/funcionarios/abc. */
	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ErrorRespuesta parametroInvalido(MethodArgumentTypeMismatchException ex) {
		return new ErrorRespuesta(400, "El valor de '" + ex.getName() + "' debe ser un número entero");
	}

}
