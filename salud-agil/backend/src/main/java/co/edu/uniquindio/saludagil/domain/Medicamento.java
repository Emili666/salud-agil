package co.edu.uniquindio.saludagil.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "medicamento")
public class Medicamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 30)
    private String codigo;

    @Column(nullable = false, length = 120)
    private String nombre;

    @Column(length = 120)
    private String presentacion;

    /** Medicamentos de alto costo: se priorizan en el tablero de indicadores. */
    private boolean altoCosto;

    protected Medicamento() {
    }

    public Medicamento(String codigo, String nombre, String presentacion, boolean altoCosto) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.presentacion = presentacion;
        this.altoCosto = altoCosto;
    }

    public Long getId() { return id; }
    public String getCodigo() { return codigo; }
    public String getNombre() { return nombre; }
    public String getPresentacion() { return presentacion; }
    public boolean isAltoCosto() { return altoCosto; }
}
