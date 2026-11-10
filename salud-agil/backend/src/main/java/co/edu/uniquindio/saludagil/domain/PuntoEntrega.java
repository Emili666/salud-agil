package co.edu.uniquindio.saludagil.domain;

import jakarta.persistence.*;

/** Punto de dispensación (farmacia) donde el afiliado reclama sus medicamentos. */
@Entity
@Table(name = "punto_entrega")
public class PuntoEntrega {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String nombre;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Zona zona;

    @Column(length = 200)
    private String direccion;

    @Column(length = 120)
    private String horario;

    protected PuntoEntrega() {
    }

    public PuntoEntrega(String nombre, Zona zona, String direccion, String horario) {
        this.nombre = nombre;
        this.zona = zona;
        this.direccion = direccion;
        this.horario = horario;
    }

    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public Zona getZona() { return zona; }
    public String getDireccion() { return direccion; }
    public String getHorario() { return horario; }
}
