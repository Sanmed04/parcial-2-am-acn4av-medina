package com.example.gastosya;

import android.annotation.SuppressLint;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import android.util.Log;
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
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;
import java.text.ParseException;
import java.util.Collections;

public class ResumenActivity extends AppCompatActivity {

    private RecyclerView recyclerViewResumen;
    private SeccionGastoAdapter seccionGastoAdapter;
    private PieChart pieChart;
    private TextView tvNoGastos;
    private List<Gasto> gastos = new ArrayList<>();
    private List<SeccionGastos> seccionesGastos = new ArrayList<>();
    private List<String> categorias;
    private BottomNavigationView bottomNavigationView;
    private TextView tvTotalGastos;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    private final Map<String, Integer> categoriaColorMap = new HashMap<>();

    @SuppressLint("NonConstantResourceId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resumen);

        categoriaColorMap.put("Servicio", R.color.servicio_color);
        categoriaColorMap.put("Compra", R.color.compra_color);
        categoriaColorMap.put("Transaccion", R.color.transaccion_color);
        categoriaColorMap.put("Alimentacion", R.color.alimentacion_color);
        categoriaColorMap.put("Salud", R.color.salud_color);
        categoriaColorMap.put("Entretenimiento", R.color.entretenimiento_color);
        categoriaColorMap.put("Transporte", R.color.transporte_color);
        categoriaColorMap.put("Vivienda", R.color.vivienda_color);
        categoriaColorMap.put("Educacion", R.color.educacion_color);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        recyclerViewResumen = findViewById(R.id.recyclerViewResumen);
        recyclerViewResumen.setLayoutManager(new LinearLayoutManager(this));
        pieChart = findViewById(R.id.pieChart);
        tvTotalGastos = findViewById(R.id.tvTotalGastos);
        tvNoGastos = findViewById(R.id.tvNoGastos);

        bottomNavigationView.setSelectedItemId(R.id.nav_graph);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_home) {
                Intent intent = new Intent(ResumenActivity.this, MainActivity.class);
                ArrayList<String> listaGastosString = new ArrayList<>();
                for (Gasto gasto : gastos) {
                    String id = gasto.getId() != null ? gasto.getId() : "";
                    listaGastosString.add(id + " - " + gasto.getNombre() + " - $" + gasto.getCantidad() + " - " +
                            gasto.getCategoria() + " - " + gasto.getFecha().getTime());
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

        if (listaGastosString == null || listaGastosString.isEmpty()) {
            pieChart.setVisibility(View.GONE);
            tvNoGastos.setVisibility(View.VISIBLE);
            tvTotalGastos.setText("Total " + getMesActual() + ": $0.00");
        } else {
            pieChart.setVisibility(View.VISIBLE);
            tvNoGastos.setVisibility(View.GONE);

            gastos.clear();
            for (String gastoStr : listaGastosString) {
                try {
                    String[] partes = gastoStr.split(" - ");
                    if (partes.length != 5) {
                        Log.e("GastosYa", "Formato inválido en gastoStr: " + gastoStr);
                        continue;
                    }
                    String id = partes[0];
                    String nombre = partes[1];
                    String cantidadStr = partes[2].replace("$", "");
                    double cantidad = Double.parseDouble(cantidadStr);
                    String categoria = partes[3];
                    long timestamp = Long.parseLong(partes[4]);
                    Date fecha = new Date(timestamp);
                    Gasto gasto = new Gasto(id, nombre, cantidad, categoria, fecha);
                    gastos.add(gasto);
                } catch (Exception e) {
                    Log.e("GastosYa", "Error procesando gastoStr: " + gastoStr + " | " + e.getMessage());
                }
            }

            Collections.sort(gastos, (g1, g2) -> g2.getFecha().compareTo(g1.getFecha()));

            Map<String, List<Gasto>> gastosPorFecha = new HashMap<>();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            for (Gasto gasto : gastos) {
                String fechaStr = sdf.format(gasto.getFecha());
                if (!gastosPorFecha.containsKey(fechaStr)) {
                    gastosPorFecha.put(fechaStr, new ArrayList<>());
                }
                gastosPorFecha.get(fechaStr).add(gasto);
            }

            seccionesGastos.clear();
            for (Map.Entry<String, List<Gasto>> entry : gastosPorFecha.entrySet()) {
                try {
                    Date fecha = sdf.parse(entry.getKey());
                    seccionesGastos.add(new SeccionGastos(fecha, entry.getValue()));
                } catch (ParseException e) {
                    Log.e("GastosYa", "Error parseando fecha: " + entry.getKey());
                }
            }

            Collections.sort(seccionesGastos, (s1, s2) -> s2.getFecha().compareTo(s1.getFecha()));

            seccionGastoAdapter = new SeccionGastoAdapter(seccionesGastos, this::eliminarGasto);
            recyclerViewResumen.setAdapter(seccionGastoAdapter);

            mostrarTotalGastos();
            mostrarGraficoPorCategoria();
        }
    }

    private void eliminarGasto(int position) {
        if (position >= 0 && position < seccionGastoAdapter.getItemCount()) {
            Object item = seccionGastoAdapter.getItem(position);
            if (item instanceof Gasto) {
                Gasto gastoEliminado = (Gasto) item;
                String gastoId = gastoEliminado.getId();

                if (gastoId != null && mAuth.getCurrentUser() != null) {
                    String userId = mAuth.getCurrentUser().getUid();
                    db.collection("users").document(userId).collection("gastos").document(gastoId)
                            .delete()
                            .addOnSuccessListener(aVoid -> Log.d("GastosYa", "Gasto eliminado de Firestore con ID: " + gastoId))
                            .addOnFailureListener(e -> {
                                Log.e("GastosYa", "Error al eliminar gasto de Firestore: " + e.getMessage());
                                Toast.makeText(this, "Error al eliminar gasto: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            });
                }

                gastos.remove(gastoEliminado);
                seccionGastoAdapter.removeItem(position);
                seccionGastoAdapter.notifyItemRemoved(position);
                seccionGastoAdapter.notifyItemRangeChanged(position, seccionGastoAdapter.getItemCount());

                if (gastos.isEmpty()) {
                    pieChart.setVisibility(View.GONE);
                    tvNoGastos.setVisibility(View.VISIBLE);
                    tvTotalGastos.setText("Total " + getMesActual() + ": $0.00");
                } else {
                    mostrarGraficoPorCategoria();
                    mostrarTotalGastos();
                }

                LayoutInflater inflater = getLayoutInflater();
                View toastLayout = inflater.inflate(R.layout.custom_toast, null);
                TextView textView = toastLayout.findViewById(R.id.toast_text);
                textView.setText("Gasto eliminado");

                Toast toast = new Toast(getApplicationContext());
                toast.setDuration(Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, 100);
                toast.setView(toastLayout);
                toastLayout.setBackgroundResource(R.drawable.toast_background_delete);
                toast.show();
            }
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
        List<Integer> colors = new ArrayList<>();
        categorias = new ArrayList<>();

        for (Map.Entry<String, Double> entry : gastosPorCategoria.entrySet()) {
            String categoria = entry.getKey();
            double total = entry.getValue();
            entries.add(new PieEntry((float) total));
            categorias.add(categoria);

            Log.d("GastosYa", "Categoría procesada: " + categoria);

            Integer color = categoriaColorMap.get(categoria);
            if (color != null) {
                colors.add(ContextCompat.getColor(this, color));
                Log.d("GastosYa", "Color encontrado para " + categoria + ": " + color);
            } else {
                colors.add(ContextCompat.getColor(this, R.color.default_color));
                Log.w("GastosYa", "No se encontró color para " + categoria + ", usando default_color");
            }
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(colors);
        dataSet.setValueTextSize(16f);
        dataSet.setDrawValues(false);
        PieData pieData = new PieData(dataSet);
        pieChart.setData(pieData);
        pieChart.setUsePercentValues(false);
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

                actualizarListaFiltrada(gastosFiltrados);
            }

            @Override
            public void onNothingSelected() {
                actualizarListaCompleta();
            }
        });
    }

    private void mostrarTotalGastos() {
        double totalGastos = 0;
        for (Gasto gasto : gastos) {
            totalGastos += gasto.getCantidad();
        }

        tvTotalGastos.setText("Total " + getMesActual() + ": $" + String.format(Locale.getDefault(), "%.2f", totalGastos));
    }

    private void actualizarListaFiltrada(List<Gasto> gastosFiltrados) {
        Map<String, List<Gasto>> gastosPorFecha = new HashMap<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        for (Gasto gasto : gastosFiltrados) {
            String fechaStr = sdf.format(gasto.getFecha());
            if (!gastosPorFecha.containsKey(fechaStr)) {
                gastosPorFecha.put(fechaStr, new ArrayList<>());
            }
            gastosPorFecha.get(fechaStr).add(gasto);
        }

        List<SeccionGastos> seccionesFiltradas = new ArrayList<>();
        for (Map.Entry<String, List<Gasto>> entry : gastosPorFecha.entrySet()) {
            try {
                Date fecha = sdf.parse(entry.getKey());
                seccionesFiltradas.add(new SeccionGastos(fecha, entry.getValue()));
            } catch (ParseException e) {
                Log.e("GastosYa", "Error parseando fecha: " + entry.getKey());
            }
        }

        Collections.sort(seccionesFiltradas, (s1, s2) -> s2.getFecha().compareTo(s1.getFecha()));

        seccionGastoAdapter = new SeccionGastoAdapter(seccionesFiltradas, this::eliminarGasto);
        recyclerViewResumen.setAdapter(seccionGastoAdapter);
    }

    private void actualizarListaCompleta() {
        Map<String, List<Gasto>> gastosPorFecha = new HashMap<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        for (Gasto gasto : gastos) {
            String fechaStr = sdf.format(gasto.getFecha());
            if (!gastosPorFecha.containsKey(fechaStr)) {
                gastosPorFecha.put(fechaStr, new ArrayList<>());
            }
            gastosPorFecha.get(fechaStr).add(gasto);
        }

        seccionesGastos.clear();
        for (Map.Entry<String, List<Gasto>> entry : gastosPorFecha.entrySet()) {
            try {
                Date fecha = sdf.parse(entry.getKey());
                seccionesGastos.add(new SeccionGastos(fecha, entry.getValue()));
            } catch (ParseException e) {
                Log.e("GastosYa", "Error parseando fecha: " + entry.getKey());
            }
        }

        Collections.sort(seccionesGastos, (s1, s2) -> s2.getFecha().compareTo(s1.getFecha()));

        seccionGastoAdapter = new SeccionGastoAdapter(seccionesGastos, this::eliminarGasto);
        recyclerViewResumen.setAdapter(seccionGastoAdapter);
    }

    private String getMesActual() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM", Locale.getDefault());
        String mesActual = dateFormat.format(Calendar.getInstance().getTime());
        return mesActual.substring(0, 1).toUpperCase() + mesActual.substring(1).toLowerCase();
    }
}