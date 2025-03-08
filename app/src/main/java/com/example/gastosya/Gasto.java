package com.example.gastosya;

import java.util.Date;

public class Gasto {
    private String id;
    private String nombre;
    private double cantidad;
    private String categoria;
    private Date fecha;

    public Gasto(String nombre, double cantidad, String categoria, Date fecha) {
        this.nombre = nombre;
        this.cantidad = cantidad;
        this.categoria = categoria;
        this.fecha = fecha;
    }

    public Gasto(String id, String nombre, double cantidad, String categoria, Date fecha) {
        this.id = id;
        this.nombre = nombre;
        this.cantidad = cantidad;
        this.categoria = categoria;
        this.fecha = fecha;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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