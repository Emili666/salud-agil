const LOCALE = 'es-CO'

export const ESTADO_FORMULA = {
  PENDIENTE: { texto: 'Pendiente', ayuda: 'Algún medicamento aún no está en el punto.', clase: 'pendiente', icono: '⏳' },
  EN_TRANSITO: { texto: 'En camino', ayuda: 'Los medicamentos van hacia el punto.', clase: 'transito', icono: '🚚' },
  DISPONIBLE: { texto: 'Lista para reclamar', ayuda: 'Puede agendar su turno.', clase: 'disponible', icono: '✓' },
  ENTREGADA: { texto: 'Entregada', ayuda: 'Fórmula entregada completa.', clase: 'entregada', icono: '✓' }
}

export const NIVEL = {
  DISPONIBLE: { texto: 'Hay existencias', clase: 'disponible', icono: '✓' },
  POCAS_UNIDADES: { texto: 'Quedan pocas', clase: 'pocas', icono: '!' },
  AGOTADO: { texto: 'Agotado', clase: 'agotado', icono: '✕' }
}

export const CANAL = { SMS: 'SMS', WHATSAPP: 'WhatsApp', EMAIL: 'correo' }

export const ESTADO_TURNO = { PROGRAMADO: 'Programado', ATENDIDO: 'Atendido', CANCELADO: 'Cancelado' }

/** "hace 5 minutos", "hace 2 horas", o la fecha si es más antiguo. */
export function hace(iso) {
  if (!iso) return 'sin registro'
  const minutos = Math.round((Date.now() - new Date(iso).getTime()) / 60000)
  if (minutos < 1) return 'hace un momento'
  if (minutos < 60) return `hace ${minutos} min`
  const horas = Math.round(minutos / 60)
  if (horas < 24) return `hace ${horas} h`
  return fechaCorta(iso)
}

export function fechaCorta(iso) {
  return new Date(iso).toLocaleDateString(LOCALE, { day: 'numeric', month: 'short' })
}

export function fechaHora(iso) {
  return new Date(iso).toLocaleString(LOCALE, { day: 'numeric', month: 'short', hour: 'numeric', minute: '2-digit' })
}

/** "2026-11-10" -> "martes 10 de noviembre" */
export function fechaLarga(isoFecha) {
  const [a, m, d] = isoFecha.split('-').map(Number)
  return new Date(a, m - 1, d).toLocaleDateString(LOCALE, { weekday: 'long', day: 'numeric', month: 'long' })
}

/** "07:30:00" -> "7:30 a. m." */
export function hora12(hora) {
  const [h, m] = hora.split(':').map(Number)
  return new Date(2000, 0, 1, h, m).toLocaleTimeString(LOCALE, { hour: 'numeric', minute: '2-digit' })
}

/** Fecha local en formato ISO (yyyy-mm-dd), sin el desfase de toISOString(). */
export function isoLocal(fecha = new Date()) {
  const p = (n) => String(n).padStart(2, '0')
  return `${fecha.getFullYear()}-${p(fecha.getMonth() + 1)}-${p(fecha.getDate())}`
}

/** Próximos días de atención (lunes a sábado). */
export function proximosDias(cantidad = 6) {
  const dias = []
  const d = new Date()
  while (dias.length < cantidad) {
    if (d.getDay() !== 0) dias.push(isoLocal(d))
    d.setDate(d.getDate() + 1)
  }
  return dias
}
