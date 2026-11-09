package com.campuslands.taller_directorio_funcionarios.services;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.campuslands.taller_directorio_funcionarios.dto.FuncionarioRequest;
import com.campuslands.taller_directorio_funcionarios.exceptions.FuncionarioNoEncontradoException;
import com.campuslands.taller_directorio_funcionarios.exceptions.SolicitudInvalidaException;
import com.campuslands.taller_directorio_funcionarios.entities.Funcionario;
import com.campuslands.taller_directorio_funcionarios.repositories.FuncionarioRepository;

/**
 * Reglas del directorio: que filtro se aplica, que pasa si un ID no existe y como se guardan los datos.
 */
@Service
public class FuncionarioService {

	private final FuncionarioRepository repository;

	public FuncionarioService(FuncionarioRepository repository) {
		this.repository = repository;
	}

	/**
	 * Lista funcionarios. Un filtro nulo, vacio o con solo espacios se trata como ausente.
	 * Si llegan los dos filtros con valor, se aplica el de nombre.
	 */
	@Transactional(readOnly = true)
	public List<Funcionario> buscar(String nombre, String departamento) {
		if (tieneValor(nombre)) {
			return repository.findByNombreContainingIgnoreCaseOrderByIdAsc(nombre.trim());
		}
		if (tieneValor(departamento)) {
			return repository.findByDepartamentoIgnoreCaseOrderByIdAsc(departamento.trim());
		}
		return repository.findAllByOrderByIdAsc();
	}

	@Transactional(readOnly = true)
	public Funcionario obtenerPorId(Long id) {
		return repository.findById(id).orElseThrow(() -> new FuncionarioNoEncontradoException(id));
	}

	@Transactional
	public Funcionario registrar(FuncionarioRequest datos) {
		if (datos.id() != null) {
			throw new SolicitudInvalidaException("No se debe enviar un ID al registrar: el directorio lo genera");
		}
		Funcionario funcionario = new Funcionario();
		copiarDatos(datos, funcionario);
		return repository.save(funcionario);
	}

	/** El ID de la ruta identifica el registro; el ID que venga en el cuerpo se ignora. */
	@Transactional
	public Funcionario actualizar(Long id, FuncionarioRequest datos) {
		Funcionario funcionario = obtenerPorId(id);
		copiarDatos(datos, funcionario);
		return repository.save(funcionario);
	}

	@Transactional
	public void eliminar(Long id) {
		Funcionario funcionario = obtenerPorId(id);
		repository.delete(funcionario);
	}

	private void copiarDatos(FuncionarioRequest datos, Funcionario funcionario) {
		funcionario.setNombre(datos.nombre().trim());
		funcionario.setCargo(datos.cargo().trim());
		funcionario.setDepartamento(datos.departamento().trim());
		// El telefono es opcional: si llega vacio o con solo espacios se guarda como nulo.
		funcionario.setTelefono(tieneValor(datos.telefono()) ? datos.telefono().trim() : null);
	}

	private boolean tieneValor(String texto) {
		return texto != null && !texto.isBlank();
	}

}
