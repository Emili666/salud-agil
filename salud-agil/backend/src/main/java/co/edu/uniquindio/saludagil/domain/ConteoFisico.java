package co.edu.uniquindio.saludagil.domain;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/** Conteo físico de inventario. Compara lo que dice el sistema con lo que hay en estante. */
@Entity
@Table(name = "conteo_fisico")
public class ConteoFisico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "inventario_id")
    private Inventario inventario;

    private int cantidadSistema;

    private int cantidadFisica;

    @Column(nullable = false)
    private LocalDateTime fecha;

    protected ConteoFisico() {
    }

    public ConteoFisico(Inventario inventario, int cantidadSistema, int cantidadFisica, LocalDateTime fecha) {
        this.inventario = inventario;
        this.cantidadSistema = cantidadSistema;
        this.cantidadFisica = cantidadFisica;
        this.fecha = fecha;
    }

    public boolean coincide() {
        return cantidadSistema == cantidadFisica;
    }

    public Long getId() { return id; }
    public Inventario getInventario() { return inventario; }
    public int getCantidadSistema() { return cantidadSistema; }
    public int getCantidadFisica() { return cantidadFisica; }
    public LocalDateTime getFecha() { return fecha; }
}
