package co.edu.uniquindio.saludagil.domain;

import jakarta.persistence.*;

/**
 * Cuenta para iniciar sesión. Un afiliado entra con su número de documento;
 * el personal de farmacia y la EPS con un usuario fijo.
 */
@Entity
@Table(name = "usuario")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Nombre de usuario para entrar (documento en el caso del afiliado). */
    @Column(nullable = false, unique = true, length = 60)
    private String login;

    @Column(nullable = false, length = 120)
    private String nombre;

    @Column(nullable = false, length = 200)
    private String claveHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Rol rol;

    /** Solo para el rol AFILIADO. */
    @ManyToOne
    @JoinColumn(name = "afiliado_id")
    private Afiliado afiliado;

    protected Usuario() {
    }

    private Usuario(String login, String nombre, String claveHash, Rol rol, Afiliado afiliado) {
        this.login = login;
        this.nombre = nombre;
        this.claveHash = claveHash;
        this.rol = rol;
        this.afiliado = afiliado;
    }

    public static Usuario afiliado(Afiliado afiliado, String claveHash) {
        return new Usuario(afiliado.getDocumento(), afiliado.getNombres(), claveHash, Rol.AFILIADO, afiliado);
    }

    public static Usuario personal(String login, String nombre, Rol rol, String claveHash) {
        return new Usuario(login, nombre, claveHash, rol, null);
    }

    public Long getId() { return id; }
    public String getLogin() { return login; }
    public String getNombre() { return nombre; }
    public String getClaveHash() { return claveHash; }
    public Rol getRol() { return rol; }
    public Afiliado getAfiliado() { return afiliado; }
}
