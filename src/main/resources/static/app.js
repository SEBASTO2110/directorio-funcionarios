'use strict';

// Ruta base de la API REST. Es relativa porque la pagina la sirve la misma aplicacion Spring Boot.
const API = '/api/v1/funcionarios';

const formulario = document.getElementById('formulario');
const tituloFormulario = document.getElementById('titulo-formulario');
const campoId = document.getElementById('campo-id');
const campoNombre = document.getElementById('campo-nombre');
const campoCargo = document.getElementById('campo-cargo');
const campoDepartamento = document.getElementById('campo-departamento');
const campoTelefono = document.getElementById('campo-telefono');
const botonGuardar = document.getElementById('boton-guardar');

const formId = document.getElementById('form-id');
const formNombre = document.getElementById('form-nombre');
const formDepartamento = document.getElementById('form-departamento');
const buscarId = document.getElementById('buscar-id');
const buscarNombre = document.getElementById('buscar-nombre');
const buscarDepartamento = document.getElementById('buscar-departamento');

const cuerpoTabla = document.getElementById('cuerpo-tabla');
const tablaVacia = document.getElementById('tabla-vacia');
const vistaActual = document.getElementById('vista-actual');
const mensaje = document.getElementById('mensaje');

const dialogoEliminar = document.getElementById('dialogo-eliminar');
const textoDialogo = document.getElementById('texto-dialogo');

// Funcionario que espera confirmacion en el dialogo de eliminar.
let pendienteEliminar = null;

/* ---------- Comunicacion con la API ---------- */

/**
 * Hace una solicitud a la API y devuelve el JSON de la respuesta.
 * Devuelve null cuando la API responde 204 (sin contenido).
 * Lanza un Error con un mensaje legible si la API responde 4xx o 5xx, o si no hay conexion.
 */
async function solicitar(url, opciones = {}) {
	let respuesta;
	try {
		respuesta = await fetch(url, {
			...opciones,
			headers: { 'Content-Type': 'application/json', Accept: 'application/json' },
		});
	} catch (error) {
		throw new Error('No se pudo conectar con el servidor. Verifica que la aplicación esté en ejecución.');
	}

	if (respuesta.status === 204) {
		return null;
	}

	const texto = await respuesta.text();
	let datos = null;
	try {
		datos = texto ? JSON.parse(texto) : null;
	} catch (error) {
		datos = null;
	}

	if (!respuesta.ok) {
		const fallo = new Error(mensajeDeError(respuesta.status, datos));
		fallo.estado = respuesta.status;
		throw fallo;
	}
	return datos;
}

/** Arma el texto de error a partir del cuerpo JSON que devuelve la API. */
function mensajeDeError(estado, datos) {
	const detalles = datos && datos.errores ? Object.values(datos.errores) : [];
	if (detalles.length > 0) {
		return detalles.join('. ') + '.';
	}
	if (datos && typeof datos.mensaje === 'string' && datos.mensaje !== '') {
		return datos.mensaje + '.';
	}
	return 'Se produjo un error inesperado (código ' + estado + ').';
}

/* ---------- Mensajes y tabla ---------- */

/** Muestra un mensaje de tipo "exito", "info" o "error". */
function mostrarMensaje(texto, tipo) {
	mensaje.textContent = texto;
	mensaje.className = 'mensaje mensaje--' + tipo;
	mensaje.hidden = false;
	// Si el mensaje quedo fuera de la pantalla, se desplaza la pagina hasta el.
	mensaje.scrollIntoView({ block: 'nearest' });
}

function ocultarMensaje() {
	mensaje.hidden = true;
	mensaje.textContent = '';
}

/** Reemplaza las filas de la tabla. Con una lista vacia la tabla queda sin filas de datos. */
function mostrarTabla(funcionarios, descripcionVista) {
	cuerpoTabla.replaceChildren(...funcionarios.map(crearFila));
	tablaVacia.hidden = funcionarios.length > 0;
	vistaActual.textContent = descripcionVista + ' · ' + contar(funcionarios.length);
}

function contar(cantidad) {
	return cantidad === 1 ? '1 funcionario' : cantidad + ' funcionarios';
}

function crearFila(funcionario) {
	const fila = document.createElement('tr');

	fila.append(
		crearCelda(funcionario.id, 'celda-id'),
		crearCelda(funcionario.nombre),
		crearCelda(funcionario.cargo),
		crearCelda(funcionario.departamento),
		crearCelda(funcionario.telefono || '—', funcionario.telefono ? '' : 'sin-dato'),
	);

	const acciones = document.createElement('td');
	acciones.className = 'celda-acciones';

	const botonEditar = document.createElement('button');
	botonEditar.type = 'button';
	botonEditar.className = 'boton boton--chico';
	botonEditar.textContent = 'Editar';
	botonEditar.setAttribute('aria-label', 'Editar a ' + funcionario.nombre);
	botonEditar.addEventListener('click', () => editar(funcionario.id));

	const botonEliminar = document.createElement('button');
	botonEliminar.type = 'button';
	botonEliminar.className = 'boton boton--chico boton--texto-peligro';
	botonEliminar.textContent = 'Eliminar';
	botonEliminar.setAttribute('aria-label', 'Eliminar a ' + funcionario.nombre);
	botonEliminar.addEventListener('click', () => pedirConfirmacion(funcionario));

	acciones.append(botonEditar, botonEliminar);
	fila.append(acciones);
	return fila;
}

/** Usa textContent para que los datos nunca se interpreten como HTML. */
function crearCelda(valor, clase) {
	const celda = document.createElement('td');
	celda.textContent = valor;
	if (clase) {
		celda.className = clase;
	}
	return celda;
}

/* ---------- Listado y busquedas ---------- */

/** Carga el listado completo. Devuelve true si la consulta funciono. */
async function cargarListado() {
	try {
		const funcionarios = (await solicitar(API)) || [];
		mostrarTabla(funcionarios, 'Listado completo');
		return true;
	} catch (error) {
		mostrarTabla([], 'Listado completo');
		mostrarMensaje(error.message, 'error');
		return false;
	}
}

async function verListadoCompleto() {
	reiniciarBusquedas();
	ocultarMensaje();
	const cargado = await cargarListado();
	if (cargado && cuerpoTabla.children.length === 0) {
		mostrarMensaje('El directorio está vacío. Registra el primer funcionario con el formulario.', 'info');
	}
}

async function consultarPorId(evento) {
	evento.preventDefault();
	const id = buscarId.value.trim();
	if (!/^\d+$/.test(id)) {
		mostrarMensaje('Escribe un ID numérico para consultar.', 'error');
		return;
	}
	formNombre.reset();
	formDepartamento.reset();
	const vista = 'Consulta por ID ' + id;
	try {
		const funcionario = await solicitar(API + '/' + id);
		mostrarTabla([funcionario], vista);
		mostrarMensaje('Se encontró el funcionario con ID ' + id + '.', 'exito');
	} catch (error) {
		mostrarTabla([], vista);
		if (error.estado === 404) {
			mostrarMensaje('No hay resultados: no existe un funcionario con ID ' + id + '.', 'info');
		} else {
			mostrarMensaje(error.message, 'error');
		}
	}
}

async function buscarPorNombre(evento) {
	evento.preventDefault();
	const fragmento = buscarNombre.value.trim();
	if (fragmento === '') {
		mostrarMensaje('Escribe parte del nombre para buscar.', 'error');
		return;
	}
	formId.reset();
	formDepartamento.reset();
	await filtrar(
		'nombre',
		fragmento,
		'Nombre que contiene «' + fragmento + '»',
		'No hay resultados: ningún funcionario tiene «' + fragmento + '» en el nombre.',
	);
}

async function filtrarPorDepartamento(evento) {
	evento.preventDefault();
	const departamento = buscarDepartamento.value.trim();
	if (departamento === '') {
		mostrarMensaje('Escribe el nombre completo del departamento para filtrar.', 'error');
		return;
	}
	formId.reset();
	formNombre.reset();
	await filtrar(
		'departamento',
		departamento,
		'Departamento «' + departamento + '»',
		'No hay resultados: ningún funcionario pertenece al departamento «' + departamento + '».',
	);
}

async function filtrar(parametro, valor, vista, mensajeSinResultados) {
	const consulta = new URLSearchParams({ [parametro]: valor });
	try {
		const funcionarios = (await solicitar(API + '?' + consulta)) || [];
		mostrarTabla(funcionarios, vista);
		if (funcionarios.length === 0) {
			mostrarMensaje(mensajeSinResultados, 'info');
		} else if (funcionarios.length === 1) {
			mostrarMensaje('Se encontró 1 funcionario.', 'exito');
		} else {
			mostrarMensaje('Se encontraron ' + funcionarios.length + ' funcionarios.', 'exito');
		}
	} catch (error) {
		mostrarTabla([], vista);
		mostrarMensaje(error.message, 'error');
	}
}

/* ---------- Formulario: registrar y editar ---------- */

function limpiarFormulario() {
	formulario.reset();
	campoId.value = '';
	tituloFormulario.textContent = 'Registrar funcionario';
	botonGuardar.textContent = 'Guardar';
}

/** Carga en el formulario los datos actuales del funcionario para corregirlos. */
async function editar(id) {
	try {
		const funcionario = await solicitar(API + '/' + id);
		campoId.value = funcionario.id;
		campoNombre.value = funcionario.nombre;
		campoCargo.value = funcionario.cargo;
		campoDepartamento.value = funcionario.departamento;
		campoTelefono.value = funcionario.telefono || '';
		tituloFormulario.textContent = 'Editar funcionario';
		botonGuardar.textContent = 'Guardar cambios';
		mostrarMensaje('Editando a ' + funcionario.nombre + ' (ID ' + funcionario.id + ').', 'info');
		campoNombre.focus();
	} catch (error) {
		if (error.estado === 404) {
			await cargarListado();
			mostrarMensaje('El funcionario con ID ' + id + ' ya no existe. Se actualizó el listado.', 'error');
		} else {
			mostrarMensaje(error.message, 'error');
		}
	}
}

async function guardar(evento) {
	evento.preventDefault();
	const id = campoId.value;
	const datos = {
		nombre: campoNombre.value,
		cargo: campoCargo.value,
		departamento: campoDepartamento.value,
		telefono: campoTelefono.value,
	};

	botonGuardar.disabled = true;
	try {
		let texto;
		if (id === '') {
			const creado = await solicitar(API, { method: 'POST', body: JSON.stringify(datos) });
			texto = 'Se registró a ' + creado.nombre + ' con ID ' + creado.id + '.';
		} else {
			const actualizado = await solicitar(API + '/' + id, { method: 'PUT', body: JSON.stringify(datos) });
			texto = 'Se actualizaron los datos de ' + actualizado.nombre + ' (ID ' + actualizado.id + ').';
		}
		limpiarFormulario();
		reiniciarBusquedas();
		if (await cargarListado()) {
			mostrarMensaje(texto, 'exito');
		}
	} catch (error) {
		mostrarMensaje(error.message, 'error');
	} finally {
		botonGuardar.disabled = false;
	}
}

function reiniciarBusquedas() {
	formId.reset();
	formNombre.reset();
	formDepartamento.reset();
}

/* ---------- Eliminar con confirmacion ---------- */

function pedirConfirmacion(funcionario) {
	pendienteEliminar = funcionario;
	textoDialogo.textContent =
		'¿Seguro que deseas eliminar a ' + funcionario.nombre + ' (ID ' + funcionario.id + ')? Esta acción no se puede deshacer.';
	dialogoEliminar.returnValue = '';
	dialogoEliminar.showModal();
}

/** Se ejecuta al cerrar el dialogo. Solo elimina si se pulso el boton "Eliminar". */
async function resolverConfirmacion() {
	const funcionario = pendienteEliminar;
	pendienteEliminar = null;
	if (!funcionario) {
		return;
	}
	if (dialogoEliminar.returnValue !== 'eliminar') {
		mostrarMensaje('Eliminación cancelada: ' + funcionario.nombre + ' se conserva en el directorio.', 'info');
		return;
	}

	try {
		await solicitar(API + '/' + funcionario.id, { method: 'DELETE' });
		if (campoId.value === String(funcionario.id)) {
			limpiarFormulario();
		}
		reiniciarBusquedas();
		if (await cargarListado()) {
			mostrarMensaje('Se eliminó a ' + funcionario.nombre + ' (ID ' + funcionario.id + ').', 'exito');
		}
	} catch (error) {
		if (error.estado === 404) {
			await cargarListado();
			mostrarMensaje('El funcionario con ID ' + funcionario.id + ' ya no existía. Se actualizó el listado.', 'error');
		} else {
			mostrarMensaje(error.message, 'error');
		}
	}
}

/* ---------- Inicio ---------- */

formulario.addEventListener('submit', guardar);
document.getElementById('boton-limpiar').addEventListener('click', () => {
	limpiarFormulario();
	ocultarMensaje();
	campoNombre.focus();
});
formId.addEventListener('submit', consultarPorId);
formNombre.addEventListener('submit', buscarPorNombre);
formDepartamento.addEventListener('submit', filtrarPorDepartamento);
document.getElementById('boton-ver-todos').addEventListener('click', verListadoCompleto);
dialogoEliminar.addEventListener('close', resolverConfirmacion);

verListadoCompleto();
