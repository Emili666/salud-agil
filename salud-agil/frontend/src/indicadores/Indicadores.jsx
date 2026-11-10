import { useEffect, useState } from 'react'
import { api } from '../api'
import { Aviso, Cargando, Segmentos } from '../comun/Ui'
import { fechaHora } from '../comun/formato'

function formatoValor(ind) {
  if (ind.sinDatos) return '—'
  return ind.valor.toLocaleString('es-CO', { maximumFractionDigits: 1 })
}

function textoMeta(ind) {
  return `Meta ${ind.mayorEsMejor ? '≥' : '≤'} ${ind.meta.toLocaleString('es-CO')} ${ind.unidad}`
}

/** Estado frente a la meta: siempre ícono + texto, nunca solo color. */
function Cumplimiento({ ind, corto = false }) {
  if (ind.sinDatos) return <span className="cumple sin-datos">Sin datos</span>
  return ind.cumple
    ? <span className="cumple si"><span className="punto-estado" aria-hidden="true">✓</span>{corto ? 'Cumple' : 'Cumple la meta'}</span>
    : <span className="cumple no"><span className="punto-estado" aria-hidden="true">✕</span>{corto ? 'No cumple' : 'No cumple la meta'}</span>
}

export default function Indicadores() {
  const [panel, setPanel] = useState(null)
  const [alcance, setAlcance] = useState('red')
  const [error, setError] = useState('')

  const cargar = () => {
    setError('')
    api.indicadores().then(setPanel).catch((e) => setError(e.message))
  }
  useEffect(cargar, [])

  if (error) return <Aviso tipo="error">No fue posible cargar los indicadores: {error}</Aviso>
  if (!panel) return <Cargando texto="Calculando indicadores…" />

  const tablero = alcance === 'red' ? panel.general : panel.porPunto.find((t) => String(t.punto.id) === alcance)
  const op = tablero.operacion

  return (
    <section>
      <div className="titulo-con-accion">
        <div>
          <h1>Tablero de indicadores</h1>
          <p className="entrada">Piloto Armenia · calculado {fechaHora(panel.generadoEn)}</p>
        </div>
        <button type="button" className="secundario" onClick={cargar}>Actualizar</button>
      </div>

      <div className="barra-filtros">
        <Segmentos etiqueta="Alcance" valor={alcance} onChange={setAlcance}
                   opciones={[{ valor: 'red', texto: 'Toda la red' },
                     ...panel.porPunto.map((t) => ({ valor: String(t.punto.id), texto: t.punto.nombre }))]} />
      </div>

      <div className="kpis">
        {tablero.indicadores.map((ind) => (
          <article key={ind.clave} className="kpi">
            <h2>{ind.nombre}</h2>
            <p className="kpi-valor">{formatoValor(ind)}<span className="kpi-unidad"> {ind.unidad}</span></p>
            <p className="kpi-meta">{textoMeta(ind)}</p>
            <Cumplimiento ind={ind} />
          </article>
        ))}
      </div>

      <h2 className="subtitulo">Operación de hoy</h2>
      <div className="operacion">
        <div><span>{op.formulasListas}</span>fórmulas listas para reclamar</div>
        <div><span>{op.formulasEnEspera}</span>fórmulas esperando medicamento</div>
        <div><span>{op.turnosHoy}</span>turnos agendados para hoy</div>
        <div><span>{op.referenciasAgotadas}</span>referencias agotadas</div>
      </div>

      <h2 className="subtitulo">Comparación por punto</h2>
      <div className="tabla-envoltura">
        <table className="comparacion">
          <thead>
            <tr>
              <th>Indicador</th>
              {panel.porPunto.map((t) => <th key={t.punto.id}>{t.punto.nombre}</th>)}
              <th>Meta</th>
            </tr>
          </thead>
          <tbody>
            {panel.general.indicadores.map((ind, i) => (
              <tr key={ind.clave}>
                <th scope="row">{ind.nombre}</th>
                {panel.porPunto.map((t) => {
                  const v = t.indicadores[i]
                  return (
                    <td key={t.punto.id}>
                      <span className="valor-celda">{formatoValor(v)} <small>{v.unidad}</small></span>
                      <Cumplimiento ind={v} corto />
                    </td>
                  )
                })}
                <td><small>{textoMeta(ind).replace('Meta ', '')}</small></td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
      <p className="nota">
        Tiempo de dispensación: horas entre la radicación y la entrega completa. Entregas completas: porcentaje de
        entregas en ventanilla con las que la fórmula quedó completa. Exactitud: conteos físicos que coinciden con el
        sistema. Satisfacción: promedio de las encuestas (1 a 5).
      </p>
    </section>
  )
}
