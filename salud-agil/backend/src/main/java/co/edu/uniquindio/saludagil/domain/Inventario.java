package co.edu.uniquindio.saludagil.domain;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/** Existencias de un medicamento en un punto de entrega. Una fila por (punto, medicamento). */
@Entity
@Table(name = "inventario",
        uniqueConstraints = @UniqueConstraint(columnNames = {"punto_id", "medicamento_id"}))
public class Inventario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "punto_id")
    private PuntoEntrega punto;

    @ManyToOne(optional = false)
    @JoinColumn(name = "medicamento_id")
    private Medicamento medicamento;

    private int cantidad;

    @Column(nullable = false)
    private LocalDateTime ultimaActualizacion;

    protected Inventario() {
    }

    public Inventario(PuntoEntrega punto, Medicamento medicamento, int cantidad, LocalDateTime ultimaActualizacion) {
        this.punto = punto;
        this.medicamento = medicamento;
        this.cantidad = cantidad;
        this.ultimaActualizacion = ultimaActualizacion;
    }

    public void actualizarCantidad(int nuevaCantidad) {
        if (nuevaCantidad < 0) {
            throw new IllegalArgumentException("La cantidad no puede ser negativa");
        }
        this.cantidad = nuevaCantidad;
        this.ultimaActualizacion = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public PuntoEntrega getPunto() { return punto; }
    public Medicamento getMedicamento() { return medicamento; }
    public int getCantidad() { return cantidad; }
    public LocalDateTime getUltimaActualizacion() { return ultimaActualizacion; }
}
