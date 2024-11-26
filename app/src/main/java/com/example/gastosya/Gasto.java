package com.example.gastosya;

import java.util.Date;

public class Gasto {
    private final String nombre;
    private final double cantidad;
    private final String categoria;
    private final Date fecha;


    public Gasto(String nombre, double cantidad, String categoria, Date fecha) {
        this.nombre = nombre;
        this.cantidad = cantidad;
        this.categoria = categoria;
        this.fecha = new Date();
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

    public Date getFecha() {
        return fecha;
    }
}
