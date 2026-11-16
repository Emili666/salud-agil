// Sesión guardada en el navegador (token + datos básicos del usuario).
const CLAVE = 'saludagil.sesion'

export function leerSesion() {
  try {
    return JSON.parse(localStorage.getItem(CLAVE))
  } catch {
    return null
  }
}

export function guardarSesion(sesion) {
  if (sesion) localStorage.setItem(CLAVE, JSON.stringify(sesion))
  else localStorage.removeItem(CLAVE)
  window.dispatchEvent(new Event('sesion'))
}

/** Vista que le corresponde a cada rol. */
export const VISTA_POR_ROL = { AFILIADO: 'afiliado', FARMACIA: 'farmacia', EPS: 'eps' }
