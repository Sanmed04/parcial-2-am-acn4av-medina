package com.example.gastosya;

public class Gasto {
    private final String nombre;
    private final double cantidad;
    private final String categoria;

    public Gasto(String nombre, double cantidad, String categoria) {
        this.nombre = nombre;
        this.cantidad = cantidad;
        this.categoria = categoria;
    }

    public String getNombre() {
        return nombre;
    }

    public double getCantidad() {
        return cantidad;
    }

    public String getCategoria() {
        return categoria;
    }
}
