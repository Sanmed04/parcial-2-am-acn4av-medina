package com.example.gastosya;

import java.util.Date;

public class Gasto {
    private final String nombre;
    private final double cantidad;
    private final String categoria;
    private final Date fecha; // Nueva propiedad para la fecha

    // Modificación del constructor para inicializar la fecha
    public Gasto(String nombre, double cantidad, String categoria, Date fecha) {
        this.nombre = nombre;
        this.cantidad = cantidad;
        this.categoria = categoria;
        this.fecha = new Date(); // Asigna la fecha actual al crear el gasto
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
