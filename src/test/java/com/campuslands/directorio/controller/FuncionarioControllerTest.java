package com.campuslands.directorio.controller;

import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.campuslands.directorio.model.Funcionario;
import com.campuslands.directorio.repository.FuncionarioRepository;
import com.campuslands.directorio.service.FuncionarioService;

/**
 * Comprueba los codigos HTTP y las reglas de la API sin necesitar MySQL:
 * el controlador y el servicio son los reales y el repositorio es simulado.
 */
@WebMvcTest(FuncionarioController.class)
@Import(FuncionarioService.class)
class FuncionarioControllerTest {

	private static final String RUTA = "/api/v1/funcionarios";

	@Autowired
	private MockMvc mvc;

	@MockitoBean
	private FuncionarioRepository repository;

	@BeforeEach
	void simularGuardado() {
		// Al guardar un funcionario nuevo, el repositorio simulado le asigna el ID 1.
		when(repository.save(any(Funcionario.class))).thenAnswer(invocacion -> {
			Funcionario funcionario = invocacion.getArgument(0);
			if (funcionario.getId() == null) {
				funcionario.setId(1L);
			}
			return funcionario;
		});
	}

	private static Funcionario funcionario(Long id, String nombre, String cargo, String departamento, String telefono) {
		Funcionario funcionario = new Funcionario();
		funcionario.setId(id);
		funcionario.setNombre(nombre);
		funcionario.setCargo(cargo);
		funcionario.setDepartamento(departamento);
		funcionario.setTelefono(telefono);
		return funcionario;
	}

	private static Funcionario ana() {
		return funcionario(7L, "Ana Torres", "Contadora", "Contabilidad", "3001234567");
	}

	// ---------- Listar ----------

	@Test
	void listarResponde200ConLaLista() throws Exception {
		when(repository.findAllByOrderByIdAsc()).thenReturn(List.of(ana()));

		mvc.perform(get(RUTA))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(1)))
				.andExpect(jsonPath("$[0].id").value(7))
				.andExpect(jsonPath("$[0].nombre").value("Ana Torres"));
	}

	@Test
	void listarResponde204SinCuerpoSiElDirectorioEstaVacio() throws Exception {
		when(repository.findAllByOrderByIdAsc()).thenReturn(List.of());

		mvc.perform(get(RUTA))
				.andExpect(status().isNoContent())
				.andExpect(content().string(""));
	}

	// ---------- Consultar por ID ----------

	@Test
	void consultarResponde200ConElFuncionario() throws Exception {
		when(repository.findById(7L)).thenReturn(Optional.of(ana()));

		mvc.perform(get(RUTA + "/7"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.departamento").value("Contabilidad"));
	}

	@Test
	void consultarResponde404SiElIdNoExiste() throws Exception {
		when(repository.findById(99L)).thenReturn(Optional.empty());

		mvc.perform(get(RUTA + "/99"))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.mensaje").value("No existe un funcionario con ID 99"));
	}

	@Test
	void consultarResponde400SiElIdNoEsNumerico() throws Exception {
		mvc.perform(get(RUTA + "/abc")).andExpect(status().isBadRequest());
	}

	// ---------- Registrar ----------

	@Test
	void registrarResponde201ConElIdGenerado() throws Exception {
		String cuerpo = """
				{"nombre": "  Ana Torres ", "cargo": "Contadora", "departamento": "Contabilidad", "telefono": "3001234567"}
				""";

		mvc.perform(post(RUTA).contentType(MediaType.APPLICATION_JSON).content(cuerpo))
				.andExpect(status().isCreated())
				.andExpect(header().string("Location", endsWith(RUTA + "/1")))
				.andExpect(jsonPath("$.id").value(1))
				.andExpect(jsonPath("$.nombre").value("Ana Torres"));
	}

	@Test
	void registrarAceptaTelefonoOmitidoEIdNulo() throws Exception {
		String cuerpo = """
				{"id": null, "nombre": "Luis Rojas", "cargo": "Analista", "departamento": "Sistemas"}
				""";

		mvc.perform(post(RUTA).contentType(MediaType.APPLICATION_JSON).content(cuerpo))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").value(1))
				.andExpect(jsonPath("$.telefono").doesNotExist());
	}

	@Test
	void registrarResponde400SiFaltaUnCampoObligatorio() throws Exception {
		String cuerpo = """
				{"cargo": "Analista", "departamento": "Sistemas"}
				""";

		mvc.perform(post(RUTA).contentType(MediaType.APPLICATION_JSON).content(cuerpo))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.errores.nombre").value("El nombre es obligatorio"));
		verify(repository, never()).save(any());
	}

	@Test
	void registrarResponde400SiUnCampoEstaVacioOSoloTieneEspacios() throws Exception {
		String cuerpo = """
				{"nombre": "Luis Rojas", "cargo": "", "departamento": "   "}
				""";

		mvc.perform(post(RUTA).contentType(MediaType.APPLICATION_JSON).content(cuerpo))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.errores.cargo").exists())
				.andExpect(jsonPath("$.errores.departamento").exists());
		verify(repository, never()).save(any());
	}

	@Test
	void registrarResponde400SiSeEnviaUnIdNoNulo() throws Exception {
		String cuerpo = """
				{"id": 5, "nombre": "Luis Rojas", "cargo": "Analista", "departamento": "Sistemas"}
				""";

		mvc.perform(post(RUTA).contentType(MediaType.APPLICATION_JSON).content(cuerpo))
				.andExpect(status().isBadRequest());
		verify(repository, never()).save(any());
	}

	@Test
	void registrarResponde400SiElCuerpoNoEsJsonValido() throws Exception {
		mvc.perform(post(RUTA).contentType(MediaType.APPLICATION_JSON).content("{nombre"))
				.andExpect(status().isBadRequest());
	}

	// ---------- Actualizar ----------

	@Test
	void actualizarResponde200YConservaElIdDeLaRuta() throws Exception {
		when(repository.findById(7L)).thenReturn(Optional.of(ana()));
		String cuerpo = """
				{"id": 123, "nombre": "Ana Torres", "cargo": "Jefe de área", "departamento": "Tesorería", "telefono": ""}
				""";

		mvc.perform(put(RUTA + "/7").contentType(MediaType.APPLICATION_JSON).content(cuerpo))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(7))
				.andExpect(jsonPath("$.cargo").value("Jefe de área"))
				.andExpect(jsonPath("$.departamento").value("Tesorería"))
				.andExpect(jsonPath("$.telefono").doesNotExist());
	}

	@Test
	void actualizarResponde404SiElIdNoExiste() throws Exception {
		when(repository.findById(99L)).thenReturn(Optional.empty());
		String cuerpo = """
				{"nombre": "Ana Torres", "cargo": "Contadora", "departamento": "Contabilidad"}
				""";

		mvc.perform(put(RUTA + "/99").contentType(MediaType.APPLICATION_JSON).content(cuerpo))
				.andExpect(status().isNotFound());
	}

	@Test
	void actualizarResponde400SiFaltaUnCampoObligatorio() throws Exception {
		String cuerpo = """
				{"nombre": "Ana Torres", "cargo": " "}
				""";

		mvc.perform(put(RUTA + "/7").contentType(MediaType.APPLICATION_JSON).content(cuerpo))
				.andExpect(status().isBadRequest());
		verify(repository, never()).save(any());
	}

	// ---------- Eliminar ----------

	@Test
	void eliminarResponde204SinCuerpo() throws Exception {
		Funcionario ana = ana();
		when(repository.findById(7L)).thenReturn(Optional.of(ana));

		mvc.perform(delete(RUTA + "/7"))
				.andExpect(status().isNoContent())
				.andExpect(content().string(""));
		verify(repository).delete(ana);
	}

	@Test
	void eliminarResponde404SiElIdNoExiste() throws Exception {
		when(repository.findById(99L)).thenReturn(Optional.empty());

		mvc.perform(delete(RUTA + "/99")).andExpect(status().isNotFound());
		verify(repository, never()).delete(any());
	}

	// ---------- Busquedas ----------

	@Test
	void buscarPorNombreResponde200ConLasCoincidencias() throws Exception {
		when(repository.findByNombreContainingIgnoreCaseOrderByIdAsc("ana")).thenReturn(List.of(ana()));

		mvc.perform(get(RUTA).param("nombre", " ana "))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(1)));
	}

	@Test
	void buscarPorNombreResponde204SiNoHayResultados() throws Exception {
		when(repository.findByNombreContainingIgnoreCaseOrderByIdAsc("zzz")).thenReturn(List.of());

		mvc.perform(get(RUTA).param("nombre", "zzz")).andExpect(status().isNoContent());
	}

	@Test
	void filtrarPorDepartamentoResponde200ConLasCoincidencias() throws Exception {
		when(repository.findByDepartamentoIgnoreCaseOrderByIdAsc("contabilidad")).thenReturn(List.of(ana()));

		mvc.perform(get(RUTA).param("departamento", "contabilidad"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].departamento").value("Contabilidad"));
	}

	@Test
	void siLleganLosDosFiltrosSeAplicaElDeNombre() throws Exception {
		when(repository.findByNombreContainingIgnoreCaseOrderByIdAsc("ana")).thenReturn(List.of(ana()));

		mvc.perform(get(RUTA).param("nombre", "ana").param("departamento", "Sistemas"))
				.andExpect(status().isOk());
		verify(repository, never()).findByDepartamentoIgnoreCaseOrderByIdAsc(any());
	}

	@Test
	void unFiltroDeNombreVacioSeTrataComoAusente() throws Exception {
		when(repository.findByDepartamentoIgnoreCaseOrderByIdAsc("Sistemas")).thenReturn(List.of());

		mvc.perform(get(RUTA).param("nombre", "   ").param("departamento", "Sistemas"))
				.andExpect(status().isNoContent());
		verify(repository).findByDepartamentoIgnoreCaseOrderByIdAsc("Sistemas");
		verify(repository, never()).findByNombreContainingIgnoreCaseOrderByIdAsc(any());
	}

	@Test
	void losDosFiltrosVaciosDevuelvenElListadoCompleto() throws Exception {
		when(repository.findAllByOrderByIdAsc()).thenReturn(List.of(ana()));

		mvc.perform(get(RUTA).param("nombre", "").param("departamento", "  "))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(1)));
	}

}
