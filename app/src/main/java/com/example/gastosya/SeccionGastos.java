package com.example.gastosya;

import java.util.Date;
import java.util.List;

public class SeccionGastos {
    private Date fecha;
    private List<Gasto> gastosDelDia;

    public SeccionGastos(Date fecha, List<Gasto> gastosDelDia) {
        this.fecha = fecha;
        this.gastosDelDia = gastosDelDia;
    }

    public Date getFecha() {
        return fecha;
    }

    public List<Gasto> getGastosDelDia() {
        return gastosDelDia;
    }
}
