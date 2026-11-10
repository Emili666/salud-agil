package co.edu.uniquindio.saludagil.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "afiliado")
public class Afiliado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String documento;

    @Column(nullable = false, length = 120)
    private String nombres;

    @Column(length = 20)
    private String telefono;

    @Column(length = 120)
    private String correo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Canal canalPreferido;

    protected Afiliado() {
    }

    public Afiliado(String documento, String nombres, String telefono, String correo, Canal canalPreferido) {
        this.documento = documento;
        this.nombres = nombres;
        this.telefono = telefono;
        this.correo = correo;
        this.canalPreferido = canalPreferido;
    }

    /** Dirección a la que se envía la notificación según el canal preferido. */
    public String destinoNotificacion() {
        return canalPreferido == Canal.EMAIL ? correo : telefono;
    }

    public Long getId() { return id; }
    public String getDocumento() { return documento; }
    public String getNombres() { return nombres; }
    public String getTelefono() { return telefono; }
    public String getCorreo() { return correo; }
    public Canal getCanalPreferido() { return canalPreferido; }
}
