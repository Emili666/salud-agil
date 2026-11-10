import { useCallback, useEffect, useState } from 'react'
import { ESTADO_FORMULA, NIVEL } from './formato'

export function EstadoFormula({ estado }) {
  const e = ESTADO_FORMULA[estado]
  return <span className={`insignia ${e.clase}`}><span aria-hidden="true">{e.icono}</span> {e.texto}</span>
}

export function Nivel({ nivel }) {
  const n = NIVEL[nivel]
  return <span className={`insignia ${n.clase}`}><span aria-hidden="true">{n.icono}</span> {n.texto}</span>
}

export function Aviso({ tipo = 'info', children }) {
  if (!children) return null
  return <div className={`aviso ${tipo}`} role={tipo === 'error' ? 'alert' : 'status'}>{children}</div>
}

export function Cargando({ texto = 'Cargando…' }) {
  return <p className="cargando" role="status">{texto}</p>
}

/** Mensaje flotante que desaparece solo. */
export function useToast() {
  const [toast, setToast] = useState(null)
  useEffect(() => {
    if (!toast) return
    const t = setTimeout(() => setToast(null), 4500)
    return () => clearTimeout(t)
  }, [toast])
  const elemento = toast && (
    <div className={`toast ${toast.tipo}`} role="status">{toast.texto}</div>
  )
  const mostrar = useCallback((texto, tipo = 'ok') => setToast({ texto, tipo }), [])
  return [elemento, mostrar]
}

/** Grupo de botones tipo pestaña (filtros en una fila). */
export function Segmentos({ opciones, valor, onChange, etiqueta }) {
  return (
    <div className="segmentos" role="group" aria-label={etiqueta}>
      {opciones.map((o) => (
        <button key={o.valor} type="button" aria-pressed={valor === o.valor}
                className={valor === o.valor ? 'activo' : ''} onClick={() => onChange(o.valor)}>
          {o.texto}
        </button>
      ))}
    </div>
  )
}
