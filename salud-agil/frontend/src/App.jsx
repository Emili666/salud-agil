import { useEffect, useState } from 'react'
import Afiliado from './afiliado/Afiliado'
import Farmacia from './farmacia/Farmacia'
import Indicadores from './indicadores/Indicadores'
import Login from './Login'
import { api } from './api'
import { guardarSesion, leerSesion, VISTA_POR_ROL } from './sesion'

const VISTAS = [
  { id: 'afiliado', texto: 'Soy afiliado' },
  { id: 'farmacia', texto: 'Farmacia' },
  { id: 'eps', texto: 'Indicadores EPS' }
]

export default function App() {
  const [sesion, setSesion] = useState(leerSesion)
  const [textoGrande, setTextoGrande] = useState(false)

  useEffect(() => {
    const alCambiar = () => setSesion(leerSesion())
    window.addEventListener('sesion', alCambiar)
    return () => window.removeEventListener('sesion', alCambiar)
  }, [])

  useEffect(() => {
    document.documentElement.classList.toggle('texto-grande', textoGrande)
  }, [textoGrande])

  // Cada rol tiene una sola vista.
  const vista = sesion ? VISTA_POR_ROL[sesion.rol] : null
  const visible = VISTAS.find((v) => v.id === vista)

  async function salir() {
    try { await api.logout() } catch { /* la sesión ya no existía */ }
    guardarSesion(null)
  }

  return (
    <>
      <header className="cabecera">
        <div className="cabecera-interior">
          <a className="marca" href="#/">
            <span className="logo" aria-hidden="true">+</span>
            <span>Salud <strong>Ágil</strong></span>
          </a>
          {visible && (
            <nav aria-label="Secciones">
              <a href="#/" aria-current="page">{visible.texto}</a>
            </nav>
          )}
          <button type="button" className="boton-texto" aria-pressed={textoGrande}
                  onClick={() => setTextoGrande(!textoGrande)}>
            {textoGrande ? 'A− Texto normal' : 'A+ Texto grande'}
          </button>
          {sesion && (
            <div className="usuario-actual">
              <span>{sesion.nombre.split(' ')[0]}</span>
              <button type="button" className="boton-texto" onClick={salir}>Salir</button>
            </div>
          )}
        </div>
      </header>
      <main className="contenedor">
        {!sesion && <Login />}
        {vista === 'afiliado' && <Afiliado />}
        {vista === 'farmacia' && <Farmacia />}
        {vista === 'eps' && <Indicadores />}
      </main>
      <footer className="pie">
        Prototipo académico · Formulación de Proyectos · Universidad del Quindío · Datos de demostración ficticios
      </footer>
    </>
  )
}
