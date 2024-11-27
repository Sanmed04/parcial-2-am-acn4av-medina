package com.example.gastosya;

import android.annotation.SuppressLint;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.formatter.ValueFormatter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import android.content.Intent;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import android.widget.TextView;

public class ResumenActivity extends AppCompatActivity {

    private RecyclerView recyclerViewResumen;
    private GastoAdapter gastoAdapter;
    private PieChart pieChart;
    private List<Gasto> gastos = new ArrayList<>();
    private List<String> categorias;
    private BottomNavigationView bottomNavigationView;

    private TextView tvTotalGastos;

    @SuppressLint("NonConstantResourceId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resumen);

        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setSelectedItemId(R.id.nav_graph);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_home) {
                Intent intent = new Intent(ResumenActivity.this, MainActivity.class);

                ArrayList<String> listaGastosString = new ArrayList<>();
                for (Gasto gasto : gastos) {
                    listaGastosString.add(gasto.getNombre() + " - $" + gasto.getCantidad() + " - " + gasto.getCategoria());
                }

                intent.putStringArrayListExtra("listaGastos", listaGastosString);
                startActivity(intent);
                return true;
            } else if (item.getItemId() == R.id.nav_graph) {
                return true;
            }
            return false;
        });

        ArrayList<String> listaGastosString = getIntent().getStringArrayListExtra("listaGastos");

        if (listaGastosString != null && !listaGastosString.isEmpty()) {
            for (String gastoStr : listaGastosString) {
                String[] partes = gastoStr.split(" - ");
                String nombre = partes[0];
                double cantidad = Double.parseDouble(partes[1].replace("$", ""));
                String categoria = partes[2];
                Date fecha = new Date();

                Gasto gasto = new Gasto(nombre, cantidad, categoria, fecha);
                gastos.add(gasto);
            }

            recyclerViewResumen = findViewById(R.id.recyclerViewResumen);
            recyclerViewResumen.setLayoutManager(new LinearLayoutManager(this));
            gastoAdapter = new GastoAdapter(gastos, new GastoAdapter.OnItemClickListener() {
                @Override
                public void onEliminarClick(int position) {
                    eliminarGasto(position);
                }
            });
            recyclerViewResumen.setAdapter(gastoAdapter);


            tvTotalGastos = findViewById(R.id.tvTotalGastos);

            mostrarTotalGastos();

            mostrarGraficoPorCategoria();
        }
    }

    private void eliminarGasto(int position) {
        if (position >= 0 && position < gastos.size()) {
            gastos.remove(position);
            gastoAdapter.notifyItemRemoved(position);
            gastoAdapter.notifyItemRangeChanged(position, gastos.size());
        }
    }

    private void mostrarGraficoPorCategoria() {
        Map<String, Double> gastosPorCategoria = new HashMap<>();

        for (Gasto gasto : gastos) {
            String categoria = gasto.getCategoria();
            gastosPorCategoria.put(categoria,
                    gastosPorCategoria.getOrDefault(categoria, 0.0) + gasto.getCantidad());
        }

        List<PieEntry> entries = new ArrayList<>();
        categorias = new ArrayList<>();

        for (Map.Entry<String, Double> entry : gastosPorCategoria.entrySet()) {
            String categoria = entry.getKey();
            double total = entry.getValue();
            entries.add(new PieEntry((float) total));
            categorias.add(categoria);
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(new int[]{
                getColor(R.color.colorOne),
                getColor(R.color.colorTwo),
                getColor(R.color.colorThree),
                getColor(R.color.colorFour),
                getColor(R.color.colorFive),
                getColor(R.color.colorSix),
                getColor(R.color.colorSeven),
                getColor(R.color.colorEight),
                getColor(R.color.colorNine),
        });

        dataSet.setValueTextSize(16f);

        dataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.format(Locale.getDefault(), "%.1f%%", value);
            }
        });

        PieData pieData = new PieData(dataSet);
        pieChart = findViewById(R.id.pieChart);
        pieChart.setData(pieData);
        pieChart.setUsePercentValues(true);
        pieChart.getDescription().setEnabled(false);

        Legend legend = pieChart.getLegend();
        legend.setEnabled(false);

        pieChart.invalidate();

        pieChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(com.github.mikephil.charting.data.Entry e, Highlight h) {
                int index = (int) h.getX();
                String categoriaSeleccionada = categorias.get(index);

                List<Gasto> gastosFiltrados = new ArrayList<>();
                for (Gasto gasto : gastos) {
                    if (gasto.getCategoria().equals(categoriaSeleccionada)) {
                        gastosFiltrados.add(gasto);
                    }
                }

                gastoAdapter.updateData(gastosFiltrados);
            }

            @Override
            public void onNothingSelected() {
                gastoAdapter.updateData(gastos);
            }
        });
    }

    private void mostrarTotalGastos() {
        double totalGastos = 0;
        for (Gasto gasto : gastos) {
            totalGastos += gasto.getCantidad();
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM", Locale.getDefault());
        String mesActual = dateFormat.format(Calendar.getInstance().getTime());

        mesActual = mesActual.substring(0, 1).toUpperCase() + mesActual.substring(1).toLowerCase();

        tvTotalGastos.setText("Total " + mesActual + ": $" + String.format(Locale.getDefault(), "%.2f", totalGastos));
    }
}
