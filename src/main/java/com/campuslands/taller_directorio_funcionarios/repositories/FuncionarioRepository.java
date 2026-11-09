package com.campuslands.taller_directorio_funcionarios.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.campuslands.taller_directorio_funcionarios.entities.Funcionario;

/**
 * Acceso a la tabla "funcionarios". Spring Data genera las consultas a partir del nombre de cada metodo.
 */
public interface FuncionarioRepository extends JpaRepository<Funcionario, Long> {

	List<Funcionario> findAllByOrderByIdAsc();

	/** Coincidencia parcial sobre el nombre, sin distinguir mayusculas de minusculas. */
	List<Funcionario> findByNombreContainingIgnoreCaseOrderByIdAsc(String fragmento);

	/** Coincidencia con el nombre completo del departamento, sin distinguir mayusculas de minusculas. */
	List<Funcionario> findByDepartamentoIgnoreCaseOrderByIdAsc(String departamento);

}
