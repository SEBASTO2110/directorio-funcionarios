package com.campuslands.taller_directorio_funcionarios.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Unica entidad del directorio. Cada objeto corresponde a una fila de la tabla "funcionarios".
 * Lombok genera los getters, los setters y los constructores.
 */
@Entity
@Table(name = "funcionarios")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Funcionario {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 120)
	private String nombre;

	@Column(nullable = false, length = 120)
	private String cargo;

	@Column(nullable = false, length = 120)
	private String departamento;

	@Column(length = 30)
	private String telefono;

}
