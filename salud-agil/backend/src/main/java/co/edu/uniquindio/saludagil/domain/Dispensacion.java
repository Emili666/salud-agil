package co.edu.uniquindio.saludagil.domain;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/** Evento de entrega en ventanilla. Sirve para medir el % de entregas completas. */
@Entity
@Table(name = "dispensacion")
public class Dispensacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "formula_id")
    private Formula formula;

    @Column(nullable = false)
    private LocalDateTime fecha;

    /** true si con esta entrega la fórmula quedó completa. */
    private boolean completa;

    @Column(length = 500)
    private String detalle;

    protected Dispensacion() {
    }

    public Dispensacion(Formula formula, LocalDateTime fecha, boolean completa, String detalle) {
        this.formula = formula;
        this.fecha = fecha;
        this.completa = completa;
        this.detalle = detalle;
    }

    public Long getId() { return id; }
    public Formula getFormula() { return formula; }
    public LocalDateTime getFecha() { return fecha; }
    public boolean isCompleta() { return completa; }
    public String getDetalle() { return detalle; }
}
