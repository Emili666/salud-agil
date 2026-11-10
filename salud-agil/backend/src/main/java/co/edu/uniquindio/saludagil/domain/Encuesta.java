package co.edu.uniquindio.saludagil.domain;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/** Encuesta de satisfacción (1 a 5) que responde el afiliado después de la entrega. */
@Entity
@Table(name = "encuesta")
public class Encuesta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "formula_id")
    private Formula formula;

    private int calificacion;

    @Column(length = 500)
    private String comentario;

    @Column(nullable = false)
    private LocalDateTime fecha;

    protected Encuesta() {
    }

    public Encuesta(Formula formula, int calificacion, String comentario, LocalDateTime fecha) {
        this.formula = formula;
        this.calificacion = calificacion;
        this.comentario = comentario;
        this.fecha = fecha;
    }

    public Long getId() { return id; }
    public Formula getFormula() { return formula; }
    public int getCalificacion() { return calificacion; }
    public String getComentario() { return comentario; }
    public LocalDateTime getFecha() { return fecha; }
}
