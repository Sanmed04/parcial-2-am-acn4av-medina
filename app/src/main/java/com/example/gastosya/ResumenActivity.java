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

        // Recibimos los gastos pasados desde MainActivity
        ArrayList<String> listaGastos = getIntent().getStringArrayListExtra("listaGastos");

        if (listaGastos != null && !listaGastos.isEmpty()) {
            // Mostrar lista de gastos
            List<Gasto> gastos = new ArrayList<>();
            for (String gastoStr : listaGastos) {
                String[] partes = gastoStr.split(" - ");
                String nombre = partes[0];
                double cantidad = Double.parseDouble(partes[1].replace("$", ""));
                String categoria = partes[2];

                // Asignar una fecha predeterminada o la actual si es necesario
                Date fecha = new Date(); // Usa la fecha actual (puedes cambiarla si tienes otra fuente)

                Gasto gasto = new Gasto(nombre, cantidad, categoria, fecha); // Asegúrate de tener el campo fecha en el constructor
                gastos.add(gasto);
            }

            // Configurar RecyclerView para mostrar los gastos
            recyclerViewResumen.setLayoutManager(new LinearLayoutManager(this));
            gastoAdapter = new GastoAdapter(gastos, new GastoAdapter.OnGastoClickListener() {
                @Override
                public void onGastoClick(Gasto gasto) {
                    // Aquí puedes manejar el click de un gasto si quieres
                }

                @Override
                public void onGastoEliminarClick(int position) {
                    // Aquí puedes manejar la eliminación si lo deseas
                }
            });
            recyclerViewResumen.setAdapter(gastoAdapter);

            // Mostrar el mes y el resumen por categoría
            mostrarResumenMensual(gastos);
        } else {
            tvResumen.setText("No hay gastos registrados.");
        }
    }

    private void mostrarResumenMensual(List<Gasto> gastos) {
        // Obtenemos el mes actual
        SimpleDateFormat sdf = new SimpleDateFormat("MMMM yyyy");
        String mesActual = sdf.format(new java.util.Date());
        tvResumen.append("\n\nResumen del mes: " + mesActual);

        // Filtramos los gastos del mes actual
        double totalGastosMes = 0;
        Map<String, Double> gastosPorCategoria = new HashMap<>();

        for (Gasto gasto : gastos) {
            // Filtramos por mes
            SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM yyyy");
            String mesGasto = dateFormat.format(gasto.getFecha());
            if (mesGasto.equals(mesActual)) {
                totalGastosMes += gasto.getCantidad();
                gastosPorCategoria.put(gasto.getCategoria(),
                        gastosPorCategoria.getOrDefault(gasto.getCategoria(), 0.0) + gasto.getCantidad());
            }
        }

        // Mostrar el total de gastos
        tvResumen.append("\n\nTotal de Gastos: $" + String.format("%.2f", totalGastosMes));

        // Mostrar el porcentaje por categoría
        for (Map.Entry<String, Double> entry : gastosPorCategoria.entrySet()) {
            String categoria = entry.getKey();
            double totalCategoria = entry.getValue();
            double porcentaje = (totalCategoria / totalGastosMes) * 100;
            tvResumen.append("\n" + categoria + ": " + String.format("%.2f", porcentaje) + "%");
        }
    }
}
