package com.example.gastosya;

public class Gasto {
    private String nombre;
    private double cantidad;

    public Gasto(String nombre, double cantidad) {
        this.nombre = nombre;
        this.cantidad = cantidad;
    }

    public String getNombre() {
        return nombre;
    }

    public double getCantidad() {
        return cantidad;
    }
}
