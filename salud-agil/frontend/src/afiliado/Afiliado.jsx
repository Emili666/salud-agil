import { useEffect, useState } from 'react'
import { api } from '../api'
import { Aviso, Cargando, EstadoFormula, Nivel, useToast } from '../comun/Ui'
import { CANAL, fechaCorta, fechaHora, fechaLarga, hace, hora12, proximosDias } from '../comun/formato'

const DOCUMENTOS_DEMO = [
  { doc: '1094000001', quien: 'María Elena (una lista, una pendiente)' },
  { doc: '1094000002', quien: 'José Arcángel (en camino y por calificar)' },
  { doc: '1094000003', quien: 'Luz Dary (entrega parcial)' },
  { doc: '1094000004', quien: 'Carlos Andrés (turno agendado)' }
]

export default function Afiliado() {
  const [seccion, setSeccion] = useState(null)

  if (seccion === 'consultar') return <ConsultarMedicamento volver={() => setSeccion(null)} />
  if (seccion === 'formulas') return <MisFormulas volver={() => setSeccion(null)} />

  return (
    <section>
      <h1>¿Qué necesita hacer hoy?</h1>
      <p className="entrada">Revise antes de salir de casa si su medicamento está en el punto de entrega.</p>
      <div className="opciones-grandes">
        <button type="button" className="opcion" onClick={() => setSeccion('consultar')}>
          <span className="opcion-icono" aria-hidden="true">🔍</span>
          <span className="opcion-titulo">¿Está mi medicamento?</span>
          <span className="opcion-texto">Busque por nombre y vea en qué punto hay existencias.</span>
        </button>
        <button type="button" className="opcion" onClick={() => setSeccion('formulas')}>
          <span className="opcion-icono" aria-hidden="true">📋</span>
          <span className="opcion-titulo">Mis fórmulas y turnos</span>
          <span className="opcion-texto">Vea el estado de su fórmula, pida un turno y revise sus avisos.</span>
        </button>
      </div>
    </section>
  )
}

/* ------------------------------------------------------------------ consulta */

function ConsultarMedicamento({ volver }) {
  const [texto, setTexto] = useState('')
  const [resultados, setResultados] = useState([])
  const [elegido, setElegido] = useState(null)
  const [disponibilidad, setDisponibilidad] = useState(null)
  const [error, setError] = useState('')

  useEffect(() => {
    const t = setTimeout(() => {
      api.medicamentos(texto).then(setResultados).catch((e) => setError(e.message))
    }, 250)
    return () => clearTimeout(t)
  }, [texto])

  async function elegir(medicamento) {
    setElegido(medicamento)
    setDisponibilidad(null)
    setError('')
    try {
      setDisponibilidad(await api.disponibilidad(medicamento.id))
    } catch (e) {
      setError(e.message)
    }
  }

  return (
    <section>
      <button type="button" className="volver" onClick={volver}>← Volver</button>
      <h1>¿Está mi medicamento?</h1>

      {!elegido && (
        <>
          <label className="campo">
            <span>Escriba el nombre del medicamento</span>
            <input type="search" value={texto} autoFocus placeholder="Ej.: losartán, insulina, metformina"
                   onChange={(e) => setTexto(e.target.value)} />
          </label>
          <Aviso tipo="error">{error}</Aviso>
          <ul className="lista-medicamentos">
            {resultados.map((m) => (
              <li key={m.id}>
                <button type="button" onClick={() => elegir(m)}>
                  <strong>{m.nombre}</strong>
                  <span>{m.presentacion}{m.altoCosto ? ' · alto costo' : ''}</span>
                </button>
              </li>
            ))}
            {resultados.length === 0 && texto && <li className="vacio">No encontramos “{texto}”.</li>}
          </ul>
        </>
      )}

      {elegido && (
        <>
          <div className="medicamento-elegido">
            <div>
              <h2>{elegido.nombre}</h2>
              <p>{elegido.presentacion}</p>
            </div>
            <button type="button" className="secundario" onClick={() => setElegido(null)}>Buscar otro</button>
          </div>
          <Aviso tipo="error">{error}</Aviso>
          {!disponibilidad && !error && <Cargando texto="Consultando los puntos de entrega…" />}
          {disponibilidad && (
            <>
              <ResumenDisponibilidad filas={disponibilidad} />
              <div className="tarjetas-puntos">
                {disponibilidad.map((d) => (
                  <article key={d.punto.id} className={`tarjeta-punto ${d.nivel.toLowerCase()}`}>
                    <header>
                      <h3>{d.punto.nombre}</h3>
                      <Nivel nivel={d.nivel} />
                    </header>
                    <p className="direccion">{d.punto.direccion}</p>
                    <p className="horario">{d.punto.horario}</p>
                    <p className="actualizado">Dato actualizado {hace(d.ultimaActualizacion)}</p>
                  </article>
                ))}
              </div>
            </>
          )}
        </>
      )}
    </section>
  )
}

function ResumenDisponibilidad({ filas }) {
  const con = filas.filter((f) => f.nivel !== 'AGOTADO')
  if (con.length === 0) {
    return <Aviso tipo="error">Hoy no hay existencias en ningún punto. Si tiene una fórmula pendiente, le avisaremos cuando llegue.</Aviso>
  }
  if (con.length === filas.length) {
    return <Aviso tipo="ok">Hay existencias en todos los puntos.</Aviso>
  }
  return <Aviso tipo="info">Hay existencias en: {con.map((f) => f.punto.nombre).join(', ')}.</Aviso>
}

/* ------------------------------------------------------------------ fórmulas */

function MisFormulas({ volver }) {
  const [documento, setDocumento] = useState('')
  const [resumen, setResumen] = useState(null)
  const [cargando, setCargando] = useState(false)
  const [error, setError] = useState('')
  const [toast, mostrar] = useToast()

  async function cargar(doc = documento) {
    if (!doc.trim()) return
    setCargando(true)
    setError('')
    try {
      setResumen(await api.afiliado(doc.trim()))
      setDocumento(doc.trim())
    } catch (e) {
      setResumen(null)
      setError(e.message)
    } finally {
      setCargando(false)
    }
  }

  const avisar = (texto, tipo) => {
    mostrar(texto, tipo)
    cargar()
  }

  if (!resumen) {
    return (
      <section>
        <button type="button" className="volver" onClick={volver}>← Volver</button>
        <h1>Mis fórmulas y turnos</h1>
        <form className="formulario-documento" onSubmit={(e) => { e.preventDefault(); cargar() }}>
          <label className="campo">
            <span>Número de documento del afiliado</span>
            <input inputMode="numeric" value={documento} autoFocus
                   onChange={(e) => setDocumento(e.target.value.replace(/\D/g, ''))} />
          </label>
          <button type="submit" className="primario" disabled={cargando}>{cargando ? 'Buscando…' : 'Consultar'}</button>
        </form>
        <Aviso tipo="error">{error}</Aviso>
        <details className="demo">
          <summary>Documentos de demostración</summary>
          <ul>
            {DOCUMENTOS_DEMO.map((d) => (
              <li key={d.doc}>
                <button type="button" className="enlace" onClick={() => cargar(d.doc)}>{d.doc}</button> — {d.quien}
              </li>
            ))}
          </ul>
        </details>
      </section>
    )
  }

  const abiertas = resumen.formulas.filter((f) => f.estado !== 'ENTREGADA' || !f.encuestaRespondida)
  const anteriores = resumen.formulas.filter((f) => f.estado === 'ENTREGADA' && f.encuestaRespondida)

  return (
    <section>
      {toast}
      <button type="button" className="volver" onClick={() => setResumen(null)}>← Cambiar de afiliado</button>
      <h1>Hola, {resumen.nombres.split(' ')[0]}</h1>
      <p className="entrada">
        Le avisamos por <strong>{CANAL[resumen.canalPreferido]}</strong> al {resumen.destinoEnmascarado} cuando su
        fórmula cambie de estado.
      </p>

      <div className="columnas">
        <div>
          <h2>Fórmulas activas</h2>
          {abiertas.length === 0 && <p className="vacio">No tiene fórmulas activas.</p>}
          {abiertas.map((f) => <TarjetaFormula key={f.codigo} formula={f} avisar={avisar} />)}
          {anteriores.length > 0 && (
            <details className="historial">
              <summary>Fórmulas entregadas ({anteriores.length})</summary>
              <ul>
                {anteriores.map((f) => (
                  <li key={f.codigo}>{f.codigo} · {f.punto.nombre} · entregada el {fechaCorta(f.fechaEntrega)}</li>
                ))}
              </ul>
            </details>
          )}
        </div>
        <aside>
          <h2>Mis avisos</h2>
          <ol className="avisos">
            {resumen.notificaciones.map((n) => (
              <li key={n.id}>
                <span className="avisos-meta">{CANAL[n.canal]} · {fechaHora(n.fechaEnvio)}</span>
                {n.mensaje}
              </li>
            ))}
          </ol>
        </aside>
      </div>
    </section>
  )
}

const PASOS = ['PENDIENTE', 'EN_TRANSITO', 'DISPONIBLE', 'ENTREGADA']
const NOMBRE_PASO = { PENDIENTE: 'Radicada', EN_TRANSITO: 'En camino', DISPONIBLE: 'Lista', ENTREGADA: 'Entregada' }

function TarjetaFormula({ formula, avisar }) {
  const [agendando, setAgendando] = useState(false)
  const actual = PASOS.indexOf(formula.estado)

  async function cancelar() {
    try {
      await api.cancelarTurno(formula.turnoVigente.codigo)
      avisar('Turno cancelado.', 'ok')
    } catch (e) {
      avisar(e.message, 'error')
    }
  }

  return (
    <article className="tarjeta-formula">
      <header>
        <div>
          <h3>Fórmula {formula.codigo}</h3>
          <p>{formula.punto.nombre} · radicada el {fechaCorta(formula.fechaRadicacion)}</p>
        </div>
        <EstadoFormula estado={formula.estado} />
      </header>

      <ol className="pasos" aria-label="Avance de la fórmula">
        {PASOS.map((p, i) => (
          <li key={p} className={i < actual ? 'hecho' : i === actual ? 'actual' : ''}
              aria-current={i === actual ? 'step' : undefined}>
            {NOMBRE_PASO[p]}
          </li>
        ))}
      </ol>

      <ul className="items">
        {formula.items.map((it) => (
          <li key={it.medicamento.id}>
            <span>{it.medicamento.nombre} <small>× {it.cantidadPrescrita}</small></span>
            {it.cantidadPendiente === 0
              ? <span className="item-ok">✓ Entregado</span>
              : it.cantidadEntregada > 0
                ? <span className="item-falta">Faltan {it.cantidadPendiente}</span>
                : <Nivel nivel={it.nivelEnPunto} />}
          </li>
        ))}
      </ul>

      {formula.turnoVigente && (
        <div className="turno">
          <div>
            <span className="turno-etiqueta">Su turno</span>
            <span className="turno-codigo">{formula.turnoVigente.codigo}</span>
          </div>
          <p>
            {fechaLarga(formula.turnoVigente.fecha)}, {hora12(formula.turnoVigente.hora)}<br />
            {formula.punto.direccion}
          </p>
          <button type="button" className="secundario" onClick={cancelar}>Cancelar turno</button>
        </div>
      )}

      {formula.estado === 'DISPONIBLE' && !formula.turnoVigente && (
        agendando
          ? <AgendarTurno formula={formula} cerrar={() => setAgendando(false)} avisar={avisar} />
          : <button type="button" className="primario ancho" onClick={() => setAgendando(true)}>Agendar turno para reclamar</button>
      )}

      {(formula.estado === 'PENDIENTE' || formula.estado === 'EN_TRANSITO') && (
        <p className="nota">Le avisaremos apenas lleguen los medicamentos. No necesita acercarse al punto.</p>
      )}

      {formula.estado === 'ENTREGADA' && !formula.encuestaRespondida && (
        <Calificar formula={formula} avisar={avisar} />
      )}
    </article>
  )
}

function AgendarTurno({ formula, cerrar, avisar }) {
  const dias = proximosDias(6)
  const [dia, setDia] = useState(dias[0])
  const [franjas, setFranjas] = useState(null)
  const [hora, setHora] = useState(null)
  const [error, setError] = useState('')

  useEffect(() => {
    setFranjas(null)
    setHora(null)
    api.franjas(formula.punto.id, dia).then(setFranjas).catch((e) => setError(e.message))
  }, [dia, formula.punto.id])

  async function confirmar() {
    try {
      const turno = await api.agendarTurno(formula.codigo, dia, hora)
      avisar(`Turno ${turno.codigo} confirmado.`, 'ok')
    } catch (e) {
      setError(e.message)
    }
  }

  return (
    <div className="agendar">
      <h4>1. Elija el día</h4>
      <div className="chips">
        {dias.map((d) => (
          <button key={d} type="button" aria-pressed={d === dia} className={d === dia ? 'activo' : ''}
                  onClick={() => setDia(d)}>
            {fechaLarga(d).replace(/ de .*/, '')}
          </button>
        ))}
      </div>
      <h4>2. Elija la hora</h4>
      {!franjas && !error && <Cargando />}
      {franjas && franjas.length === 0 && <p className="vacio">No quedan horas para este día.</p>}
      {franjas && (
        <div className="franjas">
          {franjas.map((f) => (
            <button key={f.hora} type="button" disabled={f.disponibles === 0} aria-pressed={hora === f.hora}
                    className={hora === f.hora ? 'activo' : ''} onClick={() => setHora(f.hora)}>
              <strong>{hora12(f.hora)}</strong>
              <small>{f.disponibles === 0 ? 'Lleno' : `${f.disponibles} cupo${f.disponibles > 1 ? 's' : ''}`}</small>
            </button>
          ))}
        </div>
      )}
      <Aviso tipo="error">{error}</Aviso>
      <div className="acciones">
        <button type="button" className="secundario" onClick={cerrar}>Cancelar</button>
        <button type="button" className="primario" disabled={!hora} onClick={confirmar}>
          {hora ? `Confirmar ${fechaLarga(dia).split(' ')[0]} ${hora12(hora)}` : 'Confirmar turno'}
        </button>
      </div>
    </div>
  )
}

const CARITAS = ['Muy mala', 'Mala', 'Regular', 'Buena', 'Excelente']

function Calificar({ formula, avisar }) {
  const [nota, setNota] = useState(null)
  const [comentario, setComentario] = useState('')

  async function enviar() {
    try {
      await api.calificar(formula.codigo, nota, comentario || null)
      avisar('¡Gracias por calificar la atención!', 'ok')
    } catch (e) {
      avisar(e.message, 'error')
    }
  }

  return (
    <div className="calificar">
      <h4>¿Cómo le pareció la atención?</h4>
      <div className="chips">
        {CARITAS.map((c, i) => (
          <button key={c} type="button" aria-pressed={nota === i + 1} className={nota === i + 1 ? 'activo' : ''}
                  onClick={() => setNota(i + 1)}>
            {i + 1} · {c}
          </button>
        ))}
      </div>
      {nota && (
        <>
          <label className="campo">
            <span>Comentario (opcional)</span>
            <input value={comentario} maxLength={500} onChange={(e) => setComentario(e.target.value)} />
          </label>
          <button type="button" className="primario" onClick={enviar}>Enviar calificación</button>
        </>
      )}
    </div>
  )
}
