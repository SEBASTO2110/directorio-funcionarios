package com.campuslands.directorio.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.campuslands.directorio.dto.FuncionarioRequest;
import com.campuslands.directorio.model.Funcionario;
import com.campuslands.directorio.service.FuncionarioService;

import jakarta.validation.Valid;

/**
 * API REST del directorio. Recibe la solicitud HTTP, delega en el servicio y arma la respuesta.
 */
@RestController
@RequestMapping("/api/v1/funcionarios")
public class FuncionarioController {

	private final FuncionarioService service;

	public FuncionarioController(FuncionarioService service) {
		this.service = service;
	}

	/** Listar, buscar por nombre o filtrar por departamento: 200 con resultados, 204 sin ellos. */
	@GetMapping
	public ResponseEntity<List<Funcionario>> listar(
			@RequestParam(required = false) String nombre,
			@RequestParam(required = false) String departamento) {
		List<Funcionario> funcionarios = service.buscar(nombre, departamento);
		if (funcionarios.isEmpty()) {
			return ResponseEntity.noContent().build();
		}
		return ResponseEntity.ok(funcionarios);
	}

	/** Consultar por ID: 200 con el funcionario, 404 si no existe. */
	@GetMapping("/{id}")
	public Funcionario consultar(@PathVariable Long id) {
		return service.obtenerPorId(id);
	}

	/** Registrar: 201 con el funcionario creado y su ID. */
	@PostMapping
	public ResponseEntity<Funcionario> registrar(@Valid @RequestBody FuncionarioRequest datos) {
		Funcionario creado = service.registrar(datos);
		URI ubicacion = ServletUriComponentsBuilder.fromCurrentRequest()
				.path("/{id}")
				.buildAndExpand(creado.getId())
				.toUri();
		return ResponseEntity.created(ubicacion).body(creado);
	}

	/** Actualizar: 200 con los datos actualizados, 404 si no existe. */
	@PutMapping("/{id}")
	public Funcionario actualizar(@PathVariable Long id, @Valid @RequestBody FuncionarioRequest datos) {
		return service.actualizar(id, datos);
	}

	/** Eliminar: 204 sin cuerpo, 404 si no existe. */
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> eliminar(@PathVariable Long id) {
		service.eliminar(id);
		return ResponseEntity.noContent().build();
	}

}
