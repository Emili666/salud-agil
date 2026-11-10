package co.edu.uniquindio.saludagil.domain;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/** Turno para reclamar una fórmula en una franja horaria. */
@Entity
@Table(name = "turno")
public class Turno {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 30)
    private String codigo;

    @ManyToOne(optional = false)
    @JoinColumn(name = "formula_id")
    private Formula formula;

    @ManyToOne(optional = false)
    @JoinColumn(name = "punto_id")
    private PuntoEntrega punto;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(nullable = false)
    private LocalTime hora;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoTurno estado;

    @Column(nullable = false)
    private LocalDateTime creadoEn;

    protected Turno() {
    }

    public Turno(String codigo, Formula formula, LocalDate fecha, LocalTime hora) {
        this.codigo = codigo;
        this.formula = formula;
        this.punto = formula.getPunto();
        this.fecha = fecha;
        this.hora = hora;
        this.estado = EstadoTurno.PROGRAMADO;
        this.creadoEn = LocalDateTime.now();
    }

    public void setEstado(EstadoTurno estado) { this.estado = estado; }

    public Long getId() { return id; }
    public String getCodigo() { return codigo; }
    public Formula getFormula() { return formula; }
    public PuntoEntrega getPunto() { return punto; }
    public LocalDate getFecha() { return fecha; }
    public LocalTime getHora() { return hora; }
    public EstadoTurno getEstado() { return estado; }
    public LocalDateTime getCreadoEn() { return creadoEn; }
}
