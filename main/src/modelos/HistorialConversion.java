package modelos;

import java.time.LocalDateTime;

public class HistorialConversion {
    private String monedaBase;
    private String monedaObjetivo;
    private double cantidad;
    private double resultado;
    private LocalDateTime fechaHora;

    public HistorialConversion (String monedaBase, String monedaObjetivo, double cantidad, double resultado){
        this.monedaBase = monedaBase;
        this.monedaObjetivo = monedaObjetivo;
        this.cantidad = cantidad;
        this.resultado = resultado;
        this.fechaHora = LocalDateTime.now();//Marca de tiempo automaticamente
    }

    public String getMonedaBase() {
        return monedaBase;
    }

    public String getMonedaObjetivo() {
        return monedaObjetivo;
    }

    public double getCantidad() {
        return cantidad;
    }

    public double getResultado() {
        return resultado;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }
}
