import { useEffect, useState } from 'react'
import Afiliado from './afiliado/Afiliado'
import Farmacia from './farmacia/Farmacia'
import Indicadores from './indicadores/Indicadores'

const VISTAS = [
  { id: 'afiliado', texto: 'Soy afiliado' },
  { id: 'farmacia', texto: 'Farmacia' },
  { id: 'eps', texto: 'Indicadores EPS' }
]

function vistaDesdeHash() {
  const id = window.location.hash.replace('#/', '')
  return VISTAS.some((v) => v.id === id) ? id : 'afiliado'
}

export default function App() {
  const [vista, setVista] = useState(vistaDesdeHash)
  const [textoGrande, setTextoGrande] = useState(false)

  useEffect(() => {
    const alCambiar = () => setVista(vistaDesdeHash())
    window.addEventListener('hashchange', alCambiar)
    return () => window.removeEventListener('hashchange', alCambiar)
  }, [])

  useEffect(() => {
    document.documentElement.classList.toggle('texto-grande', textoGrande)
  }, [textoGrande])

  return (
    <>
      <header className="cabecera">
        <div className="cabecera-interior">
          <a className="marca" href="#/afiliado">
            <span className="logo" aria-hidden="true">+</span>
            <span>Salud <strong>Ágil</strong></span>
          </a>
          <nav aria-label="Secciones">
            {VISTAS.map((v) => (
              <a key={v.id} href={`#/${v.id}`} aria-current={vista === v.id ? 'page' : undefined}>{v.texto}</a>
            ))}
          </nav>
          <button type="button" className="boton-texto" aria-pressed={textoGrande}
                  onClick={() => setTextoGrande(!textoGrande)}>
            {textoGrande ? 'A− Texto normal' : 'A+ Texto grande'}
          </button>
        </div>
      </header>
      <main className="contenedor">
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
