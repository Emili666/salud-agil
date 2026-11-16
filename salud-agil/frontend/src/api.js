// Cliente HTTP del backend. En desarrollo Vite redirige /api a http://localhost:8080.
import { guardarSesion, leerSesion } from './sesion'

const BASE = import.meta.env.VITE_API_URL ?? '/api'

async function solicitar(ruta, { method = 'GET', body } = {}) {
  const headers = {}
  if (body) headers['Content-Type'] = 'application/json'
  const token = leerSesion()?.token
  if (token) headers.Authorization = `Bearer ${token}`

  const respuesta = await fetch(BASE + ruta, {
    method,
    headers,
    body: body ? JSON.stringify(body) : undefined
  })
  const texto = await respuesta.text()
  const datos = texto ? JSON.parse(texto) : null
  if (respuesta.status === 401 && token) {
    // La sesión venció o el backend se reinició: volver a la pantalla de ingreso.
    guardarSesion(null)
  }
  if (!respuesta.ok) {
    throw new Error(datos?.mensaje ?? `Error ${respuesta.status}`)
  }
  return datos
}

const q = encodeURIComponent

export const api = {
  // Autenticación
  login: (usuario, clave) => solicitar('/auth/login', { method: 'POST', body: { usuario, clave } }),
  registro: (documento, correo, clave) =>
    solicitar('/auth/registro', { method: 'POST', body: { documento, correo, clave } }),
  logout: () => solicitar('/auth/logout', { method: 'POST' }),
  // Afiliado
  puntos: () => solicitar('/puntos'),
  medicamentos: (texto = '') => solicitar(`/medicamentos?q=${q(texto)}`),
  disponibilidad: (medicamentoId) => solicitar(`/medicamentos/${medicamentoId}/disponibilidad`),
  afiliado: (documento) => solicitar(`/afiliados/${q(documento)}`),
  franjas: (puntoId, fecha) => solicitar(`/turnos/franjas?puntoId=${puntoId}&fecha=${fecha}`),
  agendarTurno: (formulaCodigo, fecha, hora) =>
    solicitar('/turnos', { method: 'POST', body: { formulaCodigo, fecha, hora } }),
  cancelarTurno: (codigo) => solicitar(`/turnos/${q(codigo)}`, { method: 'DELETE' }),
  calificar: (codigo, calificacion, comentario) =>
    solicitar(`/formulas/${q(codigo)}/encuesta`, { method: 'POST', body: { calificacion, comentario } }),

  // Farmacia
  inventario: (puntoId) => solicitar(`/farmacia/puntos/${puntoId}/inventario`),
  actualizarInventario: (puntoId, medicamentoId, cantidad) =>
    solicitar('/farmacia/inventario', { method: 'PUT', body: { puntoId, medicamentoId, cantidad } }),
  registrarConteo: (puntoId, medicamentoId, cantidadFisica) =>
    solicitar('/farmacia/conteos', { method: 'POST', body: { puntoId, medicamentoId, cantidadFisica } }),
  formulasPunto: (puntoId) => solicitar(`/farmacia/puntos/${puntoId}/formulas`),
  radicar: (documento, puntoId, items) =>
    solicitar('/farmacia/formulas', { method: 'POST', body: { documento, puntoId, items } }),
  enTransito: (codigo) => solicitar(`/farmacia/formulas/${q(codigo)}/en-transito`, { method: 'POST' }),
  dispensar: (codigo) => solicitar(`/farmacia/formulas/${q(codigo)}/dispensar`, { method: 'POST' }),
  turnosPunto: (puntoId, fecha) => solicitar(`/farmacia/puntos/${puntoId}/turnos?fecha=${fecha}`),

  // EPS
  indicadores: () => solicitar('/indicadores')
}
