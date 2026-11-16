import { useState } from 'react'
import { api } from './api'
import { guardarSesion } from './sesion'
import { Aviso, Segmentos } from './comun/Ui'

const CUENTAS_DEMO = [
  { usuario: '1094000001', clave: 'salud123', quien: 'Afiliada · María Elena (una lista, una pendiente)' },
  { usuario: '1094000002', clave: 'salud123', quien: 'Afiliado · José Arcángel (en camino y por calificar)' },
  { usuario: '1094000003', clave: 'salud123', quien: 'Afiliada · Luz Dary (entrega parcial)' },
  { usuario: '1094000004', clave: 'salud123', quien: 'Afiliado · Carlos Andrés (turno agendado)' },
  { usuario: 'farmacia', clave: 'farmacia123', quien: 'Personal de farmacia' },
  { usuario: 'eps', clave: 'eps123', quien: 'Gestor de la EPS (indicadores)' }
]

export default function Login() {
  const [modo, setModo] = useState('entrar')

  return (
    <section className="ingreso">
      <h1>{modo === 'entrar' ? 'Ingrese a Salud Ágil' : 'Cree su cuenta de afiliado'}</h1>
      <Segmentos etiqueta="Tipo de ingreso" valor={modo} onChange={setModo}
                 opciones={[{ valor: 'entrar', texto: 'Iniciar sesión' }, { valor: 'registro', texto: 'Crear cuenta' }]} />
      {modo === 'entrar' ? <FormularioIngreso /> : <FormularioRegistro />}
    </section>
  )
}

function FormularioIngreso() {
  const [usuario, setUsuario] = useState('')
  const [clave, setClave] = useState('')
  const [error, setError] = useState('')
  const [enviando, setEnviando] = useState(false)

  async function entrar(u = usuario, c = clave) {
    setEnviando(true)
    setError('')
    try {
      guardarSesion(await api.login(u.trim(), c))
    } catch (e) {
      setError(e.message)
      setEnviando(false)
    }
  }

  return (
    <>
      <form className="tarjeta-ingreso" onSubmit={(e) => { e.preventDefault(); entrar() }}>
        <label className="campo">
          <span>Documento (afiliados) o usuario (personal)</span>
          <input value={usuario} autoFocus autoComplete="username" required
                 onChange={(e) => setUsuario(e.target.value)} />
        </label>
        <label className="campo">
          <span>Contraseña</span>
          <input type="password" value={clave} autoComplete="current-password" required
                 onChange={(e) => setClave(e.target.value)} />
        </label>
        <Aviso tipo="error">{error}</Aviso>
        <button type="submit" className="primario" disabled={enviando}>{enviando ? 'Ingresando…' : 'Ingresar'}</button>
      </form>
      <details className="demo">
        <summary>Cuentas de demostración</summary>
        <ul>
          {CUENTAS_DEMO.map((c) => (
            <li key={c.usuario}>
              <button type="button" className="enlace" onClick={() => entrar(c.usuario, c.clave)}>{c.usuario}</button>
              {' '}/ {c.clave} — {c.quien}
            </li>
          ))}
        </ul>
      </details>
    </>
  )
}

function FormularioRegistro() {
  const [documento, setDocumento] = useState('')
  const [correo, setCorreo] = useState('')
  const [clave, setClave] = useState('')
  const [confirmar, setConfirmar] = useState('')
  const [error, setError] = useState('')
  const [enviando, setEnviando] = useState(false)

  async function registrar(e) {
    e.preventDefault()
    if (clave !== confirmar) {
      setError('Las contraseñas no coinciden.')
      return
    }
    setEnviando(true)
    setError('')
    try {
      guardarSesion(await api.registro(documento, correo.trim(), clave))
    } catch (err) {
      setError(err.message)
      setEnviando(false)
    }
  }

  return (
    <form className="tarjeta-ingreso" onSubmit={registrar}>
      <p className="nota">
        Debe estar afiliado a la EPS. Le enviaremos al correo que registre los avisos de sus fórmulas y turnos.
        (Para probar, use un documento de la demo sin cuenta, por ejemplo 1094100000.)
      </p>
      <label className="campo">
        <span>Número de documento</span>
        <input inputMode="numeric" value={documento} autoFocus required
               onChange={(e) => setDocumento(e.target.value.replace(/\D/g, ''))} />
      </label>
      <label className="campo">
        <span>Correo electrónico</span>
        <input type="email" value={correo} autoComplete="email" required onChange={(e) => setCorreo(e.target.value)} />
      </label>
      <label className="campo">
        <span>Contraseña (mínimo 6 caracteres)</span>
        <input type="password" value={clave} minLength={6} autoComplete="new-password" required
               onChange={(e) => setClave(e.target.value)} />
      </label>
      <label className="campo">
        <span>Repita la contraseña</span>
        <input type="password" value={confirmar} minLength={6} autoComplete="new-password" required
               onChange={(e) => setConfirmar(e.target.value)} />
      </label>
      <Aviso tipo="error">{error}</Aviso>
      <button type="submit" className="primario" disabled={enviando}>{enviando ? 'Creando…' : 'Crear cuenta'}</button>
    </form>
  )
}
