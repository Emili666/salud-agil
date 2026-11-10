package co.edu.uniquindio.saludagil.domain;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Fórmula médica radicada por un afiliado en un punto de entrega. */
@Entity
@Table(name = "formula")
public class Formula {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String codigo;

    @ManyToOne(optional = false)
    @JoinColumn(name = "afiliado_id")
    private Afiliado afiliado;

    @ManyToOne(optional = false)
    @JoinColumn(name = "punto_id")
    private PuntoEntrega punto;

    @Column(nullable = false)
    private LocalDateTime fechaRadicacion;

    private LocalDateTime fechaEntrega;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoFormula estado;

    @OneToMany(mappedBy = "formula", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FormulaItem> items = new ArrayList<>();

    protected Formula() {
    }

    public Formula(String codigo, Afiliado afiliado, PuntoEntrega punto, LocalDateTime fechaRadicacion) {
        this.codigo = codigo;
        this.afiliado = afiliado;
        this.punto = punto;
        this.fechaRadicacion = fechaRadicacion;
        this.estado = EstadoFormula.PENDIENTE;
    }

    public FormulaItem agregarItem(Medicamento medicamento, int cantidadPrescrita) {
        FormulaItem item = new FormulaItem(this, medicamento, cantidadPrescrita);
        items.add(item);
        return item;
    }

    public boolean todoEntregado() {
        return items.stream().allMatch(i -> i.getCantidadPendiente() == 0);
    }

    public void marcarEntregada(LocalDateTime fecha) {
        this.estado = EstadoFormula.ENTREGADA;
        this.fechaEntrega = fecha;
    }

    public void setEstado(EstadoFormula estado) { this.estado = estado; }

    public Long getId() { return id; }
    public String getCodigo() { return codigo; }
    public Afiliado getAfiliado() { return afiliado; }
    public PuntoEntrega getPunto() { return punto; }
    public LocalDateTime getFechaRadicacion() { return fechaRadicacion; }
    public LocalDateTime getFechaEntrega() { return fechaEntrega; }
    public EstadoFormula getEstado() { return estado; }
    public List<FormulaItem> getItems() { return items; }
}
