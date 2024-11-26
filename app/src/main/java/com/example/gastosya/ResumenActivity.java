package com.example.gastosya;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Date;

public class ResumenActivity extends AppCompatActivity {

    private TextView tvResumen;
    private RecyclerView recyclerViewResumen;
    private GastoAdapter gastoAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resumen);

        tvResumen = findViewById(R.id.tvResumen);
        recyclerViewResumen = findViewById(R.id.recyclerViewResumen);


        ArrayList<String> listaGastos = getIntent().getStringArrayListExtra("listaGastos");

        if (listaGastos != null && !listaGastos.isEmpty()) {

            List<Gasto> gastos = new ArrayList<>();
            for (String gastoStr : listaGastos) {
                String[] partes = gastoStr.split(" - ");
                String nombre = partes[0];
                double cantidad = Double.parseDouble(partes[1].replace("$", ""));
                String categoria = partes[2];

                Date fecha = new Date();

                Gasto gasto = new Gasto(nombre, cantidad, categoria, fecha);
                gastos.add(gasto);
            }

            recyclerViewResumen.setLayoutManager(new LinearLayoutManager(this));
            gastoAdapter = new GastoAdapter(gastos, new GastoAdapter.OnGastoClickListener() {
                @Override
                public void onGastoClick(Gasto gasto) {
                }

                @Override
                public void onGastoEliminarClick(int position) {
                }
            });
            recyclerViewResumen.setAdapter(gastoAdapter);

            mostrarResumenMensual(gastos);
        } else {
            tvResumen.setText("No hay gastos registrados.");
        }
    }

    private void mostrarResumenMensual(List<Gasto> gastos) {
        SimpleDateFormat sdf = new SimpleDateFormat("MMMM yyyy");
        String mesActual = sdf.format(new java.util.Date());
        tvResumen.append("\n\nResumen del mes: " + mesActual);

        double totalGastosMes = 0;
        Map<String, Double> gastosPorCategoria = new HashMap<>();

        for (Gasto gasto : gastos) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM yyyy");
            String mesGasto = dateFormat.format(gasto.getFecha());
            if (mesGasto.equals(mesActual)) {
                totalGastosMes += gasto.getCantidad();
                gastosPorCategoria.put(gasto.getCategoria(),
                        gastosPorCategoria.getOrDefault(gasto.getCategoria(), 0.0) + gasto.getCantidad());
            }
        }

        tvResumen.append("\n\nTotal de Gastos: $" + String.format("%.2f", totalGastosMes));

        for (Map.Entry<String, Double> entry : gastosPorCategoria.entrySet()) {
            String categoria = entry.getKey();
            double totalCategoria = entry.getValue();
            double porcentaje = (totalCategoria / totalGastosMes) * 100;
            tvResumen.append("\n" + categoria + ": " + String.format("%.2f", porcentaje) + "%");
        }
    }
}
