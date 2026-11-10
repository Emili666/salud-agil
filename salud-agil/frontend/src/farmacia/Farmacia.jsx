import { useCallback, useEffect, useState } from 'react'
import { api } from '../api'
import { Aviso, Cargando, EstadoFormula, Nivel, Segmentos, useToast } from '../comun/Ui'
import { ESTADO_TURNO, fechaCorta, hace, hora12, isoLocal } from '../comun/formato'

const PESTANAS = [
  { valor: 'inventario', texto: 'Inventario' },
  { valor: 'formulas', texto: 'Fórmulas' },
  { valor: 'turnos', texto: 'Turnos' },
  { valor: 'radicar', texto: 'Radicar fórmula' }
]

export default function Farmacia() {
  const [puntos, setPuntos] = useState([])
  const [puntoId, setPuntoId] = useState(null)
  const [pestana, setPestana] = useState('inventario')
  const [toast, mostrar] = useToast()
  const [error, setError] = useState('')

  useEffect(() => {
    api.puntos().then((p) => {
      setPuntos(p)
      setPuntoId(p[0]?.id ?? null)
    }).catch((e) => setError(`No fue posible conectar con el servidor: ${e.message}`))
  }, [])

  return (
    <section>
      {toast}
      <h1>Panel de farmacia</h1>
      <Aviso tipo="error">{error}</Aviso>
      <div className="barra-filtros">
        <Segmentos etiqueta="Punto de entrega" valor={puntoId} onChange={setPuntoId}
                   opciones={puntos.map((p) => ({ valor: p.id, texto: p.nombre }))} />
        <Segmentos etiqueta="Sección" valor={pestana} onChange={setPestana} opciones={PESTANAS} />
      </div>
      {puntoId && pestana === 'inventario' && <Inventario puntoId={puntoId} mostrar={mostrar} />}
      {puntoId && pestana === 'formulas' && <Formulas puntoId={puntoId} mostrar={mostrar} />}
      {puntoId && pestana === 'turnos' && <Turnos puntoId={puntoId} mostrar={mostrar} />}
      {puntoId && pestana === 'radicar' && <Radicar puntoId={puntoId} mostrar={mostrar} />}
    </section>
  )
}

/* ------------------------------------------------------------------ inventario */

function Inventario({ puntoId, mostrar }) {
  const [filas, setFilas] = useState(null)
  const [filtro, setFiltro] = useState('')

  const cargar = useCallback(() => {
    api.inventario(puntoId).then(setFilas).catch((e) => mostrar(e.message, 'error'))
  }, [puntoId, mostrar])

  useEffect(() => { setFilas(null); cargar() }, [cargar])

  if (!filas) return <Cargando />
  const visibles = filas.filter((f) => f.medicamento.nombre.toLowerCase().includes(filtro.toLowerCase()))

  return (
    <>
      <p className="nota">
        Al guardar una cantidad, el sistema revisa las fórmulas que esperaban ese medicamento y avisa a los
        afiliados. El conteo físico corrige el sistema y alimenta el indicador de exactitud.
      </p>
      <label className="campo compacto">
        <span>Filtrar</span>
        <input type="search" value={filtro} onChange={(e) => setFiltro(e.target.value)} placeholder="Nombre del medicamento" />
      </label>
      <div className="tabla-envoltura">
        <table>
          <thead>
            <tr><th>Medicamento</th><th>Estado</th><th>Actualizado</th><th>Cantidad en sistema</th><th>Conteo físico</th></tr>
          </thead>
          <tbody>
            {visibles.map((f) => (
              <FilaInventario key={f.id} fila={f} puntoId={puntoId} mostrar={mostrar} recargar={cargar} />
            ))}
          </tbody>
        </table>
      </div>
    </>
  )
}

function FilaInventario({ fila, puntoId, mostrar, recargar }) {
  const [cantidad, setCantidad] = useState(fila.cantidad)
  const [conteo, setConteo] = useState('')
  useEffect(() => setCantidad(fila.cantidad), [fila.cantidad])

  async function guardar() {
    try {
      await api.actualizarInventario(puntoId, fila.medicamento.id, Number(cantidad))
      mostrar(`${fila.medicamento.nombre}: ${cantidad} unidades guardadas.`)
      recargar()
    } catch (e) {
      mostrar(e.message, 'error')
    }
  }

  async function contar() {
    try {
      const r = await api.registrarConteo(puntoId, fila.medicamento.id, Number(conteo))
      mostrar(r.diferencia === 0
        ? `Conteo correcto: coincide con el sistema (${r.cantidadFisica}).`
        : `Diferencia de ${r.diferencia > 0 ? '+' : ''}${r.diferencia} unidades. Inventario corregido.`,
      r.diferencia === 0 ? 'ok' : 'info')
      setConteo('')
      recargar()
    } catch (e) {
      mostrar(e.message, 'error')
    }
  }

  return (
    <tr>
      <td><strong>{fila.medicamento.nombre}</strong><br /><small>{fila.medicamento.presentacion}</small></td>
      <td><Nivel nivel={fila.nivel} /></td>
      <td><small>{hace(fila.ultimaActualizacion)}</small></td>
      <td>
        <div className="en-linea">
          <input type="number" min="0" className="numero" value={cantidad}
                 aria-label={`Cantidad de ${fila.medicamento.nombre}`}
                 onChange={(e) => setCantidad(e.target.value)} />
          <button type="button" className="secundario pequeno" disabled={Number(cantidad) === fila.cantidad || cantidad === ''}
                  onClick={guardar}>Guardar</button>
        </div>
      </td>
      <td>
        <div className="en-linea">
          <input type="number" min="0" className="numero" value={conteo} placeholder="—"
                 aria-label={`Conteo físico de ${fila.medicamento.nombre}`}
                 onChange={(e) => setConteo(e.target.value)} />
          <button type="button" className="secundario pequeno" disabled={conteo === ''} onClick={contar}>Registrar</button>
        </div>
      </td>
    </tr>
  )
}

/* ------------------------------------------------------------------ fórmulas */

const FILTROS_FORMULA = [
  { valor: 'ABIERTAS', texto: 'Abiertas' },
  { valor: 'DISPONIBLE', texto: 'Listas' },
  { valor: 'PENDIENTE', texto: 'Pendientes' },
  { valor: 'EN_TRANSITO', texto: 'En camino' },
  { valor: 'ENTREGADA', texto: 'Entregadas' }
]

function Formulas({ puntoId, mostrar }) {
  const [formulas, setFormulas] = useState(null)
  const [filtro, setFiltro] = useState('ABIERTAS')

  const cargar = useCallback(() => {
    api.formulasPunto(puntoId).then(setFormulas).catch((e) => mostrar(e.message, 'error'))
  }, [puntoId, mostrar])

  useEffect(() => { setFormulas(null); cargar() }, [cargar])

  async function accion(promesa, exito) {
    try {
      const r = await promesa
      mostrar(exito(r), r?.completa === false ? 'info' : 'ok')
      cargar()
    } catch (e) {
      mostrar(e.message, 'error')
    }
  }

  if (!formulas) return <Cargando />
  const visibles = formulas.filter((f) => filtro === 'ABIERTAS' ? f.estado !== 'ENTREGADA' : f.estado === filtro)

  return (
    <>
      <Segmentos etiqueta="Filtrar fórmulas" valor={filtro} onChange={setFiltro} opciones={FILTROS_FORMULA} />
      {visibles.length === 0 && <p className="vacio">No hay fórmulas en este estado.</p>}
      <div className="lista-formulas">
        {visibles.map((f) => (
          <article key={f.codigo} className="fila-formula">
            <div className="fila-formula-datos">
              <strong>{f.codigo}</strong> · {f.afiliadoNombre} <small>({f.afiliadoDocumento})</small>
              <ul>
                {f.items.map((it) => (
                  <li key={it.medicamento.id}>
                    {it.medicamento.nombre}: {it.cantidadEntregada}/{it.cantidadPrescrita}
                    {it.cantidadPendiente > 0 && <> · en estante {it.existenciasEnPunto}</>}
                  </li>
                ))}
              </ul>
              <small>
                Radicada {fechaCorta(f.fechaRadicacion)}
                {f.turnoVigente && <> · turno {f.turnoVigente.codigo} ({hora12(f.turnoVigente.hora)})</>}
              </small>
            </div>
            <EstadoFormula estado={f.estado} />
            <div className="fila-formula-acciones">
              {f.estado === 'PENDIENTE' && (
                <button type="button" className="secundario pequeno"
                        onClick={() => accion(api.enTransito(f.codigo), () => `${f.codigo} marcada en camino. Afiliado notificado.`)}>
                  Marcar en camino
                </button>
              )}
              {f.estado !== 'ENTREGADA' && (
                <button type="button" className="primario pequeno"
                        onClick={() => accion(api.dispensar(f.codigo),
                          (r) => r.completa ? `${f.codigo} entregada completa.` : `Entrega parcial. ${r.detalle}`)}>
                  Entregar
                </button>
              )}
            </div>
          </article>
        ))}
      </div>
    </>
  )
}

/* ------------------------------------------------------------------ turnos */

function Turnos({ puntoId, mostrar }) {
  const [fecha, setFecha] = useState(isoLocal())
  const [turnos, setTurnos] = useState(null)

  const cargar = useCallback(() => {
    api.turnosPunto(puntoId, fecha).then(setTurnos).catch((e) => mostrar(e.message, 'error'))
  }, [puntoId, fecha, mostrar])

  useEffect(() => { setTurnos(null); cargar() }, [cargar])

  async function entregar(t) {
    try {
      const r = await api.dispensar(t.formulaCodigo)
      mostrar(r.completa ? `Turno ${t.codigo} atendido: entrega completa.` : `Entrega parcial. ${r.detalle}`,
        r.completa ? 'ok' : 'info')
      cargar()
    } catch (e) {
      mostrar(e.message, 'error')
    }
  }

  return (
    <>
      <label className="campo compacto">
        <span>Fecha</span>
        <input type="date" value={fecha} onChange={(e) => setFecha(e.target.value)} />
      </label>
      {!turnos && <Cargando />}
      {turnos && turnos.length === 0 && <p className="vacio">No hay turnos para esta fecha.</p>}
      {turnos && turnos.length > 0 && (
        <div className="tabla-envoltura">
          <table>
            <thead><tr><th>Hora</th><th>Turno</th><th>Afiliado</th><th>Fórmula</th><th>Estado</th><th></th></tr></thead>
            <tbody>
              {turnos.map((t) => (
                <tr key={t.codigo}>
                  <td>{hora12(t.hora)}</td>
                  <td><strong>{t.codigo}</strong></td>
                  <td>{t.afiliadoNombre}<br /><small>{t.afiliadoDocumento}</small></td>
                  <td>{t.formulaCodigo}</td>
                  <td>{ESTADO_TURNO[t.estado]}</td>
                  <td>
                    {t.estado === 'PROGRAMADO' && (
                      <button type="button" className="primario pequeno" onClick={() => entregar(t)}>Entregar</button>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </>
  )
}

/* ------------------------------------------------------------------ radicar */

function Radicar({ puntoId, mostrar }) {
  const [medicamentos, setMedicamentos] = useState([])
  const [documento, setDocumento] = useState('')
  const [items, setItems] = useState([{ medicamentoId: '', cantidad: 30 }])
  const [error, setError] = useState('')

  useEffect(() => { api.medicamentos().then(setMedicamentos) }, [])

  const cambiar = (i, campo, valor) =>
    setItems(items.map((it, j) => (j === i ? { ...it, [campo]: valor } : it)))

  async function enviar(e) {
    e.preventDefault()
    setError('')
    try {
      const f = await api.radicar(documento, puntoId, items.map((it) => ({
        medicamentoId: Number(it.medicamentoId), cantidad: Number(it.cantidad)
      })))
      mostrar(`Fórmula ${f.codigo} radicada: ${f.estado === 'DISPONIBLE' ? 'lista para entregar' : 'queda pendiente'}. Afiliado notificado.`)
      setDocumento('')
      setItems([{ medicamentoId: '', cantidad: 30 }])
    } catch (err) {
      setError(err.message)
    }
  }

  const completo = documento && items.every((it) => it.medicamentoId && Number(it.cantidad) > 0)

  return (
    <form className="radicar" onSubmit={enviar}>
      <label className="campo">
        <span>Documento del afiliado</span>
        <input inputMode="numeric" value={documento} placeholder="Ej.: 1094000002"
               onChange={(e) => setDocumento(e.target.value.replace(/\D/g, ''))} />
      </label>
      <h3>Medicamentos</h3>
      {items.map((it, i) => (
        <div className="item-radicar" key={i}>
          <select value={it.medicamentoId} aria-label="Medicamento" onChange={(e) => cambiar(i, 'medicamentoId', e.target.value)}>
            <option value="">Elija un medicamento…</option>
            {medicamentos.map((m) => <option key={m.id} value={m.id}>{m.nombre} — {m.presentacion}</option>)}
          </select>
          <input type="number" min="1" className="numero" value={it.cantidad} aria-label="Cantidad"
                 onChange={(e) => cambiar(i, 'cantidad', e.target.value)} />
          {items.length > 1 && (
            <button type="button" className="secundario pequeno" onClick={() => setItems(items.filter((_, j) => j !== i))}>Quitar</button>
          )}
        </div>
      ))}
      <button type="button" className="enlace" onClick={() => setItems([...items, { medicamentoId: '', cantidad: 30 }])}>
        + Agregar otro medicamento
      </button>
      <Aviso tipo="error">{error}</Aviso>
      <div className="acciones">
        <button type="submit" className="primario" disabled={!completo}>Radicar fórmula</button>
      </div>
    </form>
  )
}
