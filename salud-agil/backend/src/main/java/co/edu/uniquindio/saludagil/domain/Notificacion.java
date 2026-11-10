package co.edu.uniquindio.saludagil.domain;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "notificacion")
public class Notificacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "afiliado_id")
    private Afiliado afiliado;

    @ManyToOne
    @JoinColumn(name = "formula_id")
    private Formula formula;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Canal canal;

    @Column(length = 120)
    private String destino;

    @Column(nullable = false, length = 500)
    private String mensaje;

    @Column(nullable = false)
    private LocalDateTime fechaEnvio;

    protected Notificacion() {
    }

    public Notificacion(Afiliado afiliado, Formula formula, Canal canal, String destino,
                        String mensaje, LocalDateTime fechaEnvio) {
        this.afiliado = afiliado;
        this.formula = formula;
        this.canal = canal;
        this.destino = destino;
        this.mensaje = mensaje;
        this.fechaEnvio = fechaEnvio;
    }

    public Long getId() { return id; }
    public Afiliado getAfiliado() { return afiliado; }
    public Formula getFormula() { return formula; }
    public Canal getCanal() { return canal; }
    public String getDestino() { return destino; }
    public String getMensaje() { return mensaje; }
    public LocalDateTime getFechaEnvio() { return fechaEnvio; }
}
