package co.edu.uniquindio.saludagil.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "formula_item")
public class FormulaItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "formula_id")
    private Formula formula;

    @ManyToOne(optional = false)
    @JoinColumn(name = "medicamento_id")
    private Medicamento medicamento;

    private int cantidadPrescrita;

    private int cantidadEntregada;

    protected FormulaItem() {
    }

    FormulaItem(Formula formula, Medicamento medicamento, int cantidadPrescrita) {
        this.formula = formula;
        this.medicamento = medicamento;
        this.cantidadPrescrita = cantidadPrescrita;
    }

    public int getCantidadPendiente() {
        return cantidadPrescrita - cantidadEntregada;
    }

    public void registrarEntrega(int cantidad) {
        if (cantidad < 0 || cantidad > getCantidadPendiente()) {
            throw new IllegalArgumentException("Cantidad de entrega inválida");
        }
        this.cantidadEntregada += cantidad;
    }

    public Long getId() { return id; }
    public Formula getFormula() { return formula; }
    public Medicamento getMedicamento() { return medicamento; }
    public int getCantidadPrescrita() { return cantidadPrescrita; }
    public int getCantidadEntregada() { return cantidadEntregada; }
}
