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

public class ResumenActivity extends AppCompatActivity {

    private RecyclerView recyclerViewResumen;
    private GastoAdapter gastoAdapter;
    private PieChart pieChart;
    private TextView tvNoGastos;
    private List<Gasto> gastos = new ArrayList<>();
    private List<String> categorias;
    private BottomNavigationView bottomNavigationView;
    private TextView tvTotalGastos;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @SuppressLint("NonConstantResourceId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resumen);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        recyclerViewResumen = findViewById(R.id.recyclerViewResumen);
        pieChart = findViewById(R.id.pieChart);
        tvTotalGastos = findViewById(R.id.tvTotalGastos);
        tvNoGastos = findViewById(R.id.tvNoGastos);

        bottomNavigationView.setSelectedItemId(R.id.nav_graph);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_home) {
                Intent intent = new Intent(ResumenActivity.this, MainActivity.class);
                ArrayList<String> listaGastosString = new ArrayList<>();
                for (Gasto gasto : gastos) {
                    String id = gasto.getId() != null ? gasto.getId() : ""; // Asegurar que id no sea null
                    listaGastosString.add(id + " - " + gasto.getNombre() + " - $" + gasto.getCantidad() + " - " + gasto.getCategoria());
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

        recyclerViewResumen.setLayoutManager(new LinearLayoutManager(this));
        gastoAdapter = new GastoAdapter(gastos, new GastoAdapter.OnItemClickListener() {
            @Override
            public void onEliminarClick(int position) {
                eliminarGasto(position);
            }
        });
        recyclerViewResumen.setAdapter(gastoAdapter);

        if (listaGastosString == null || listaGastosString.isEmpty()) {
            pieChart.setVisibility(View.GONE);
            tvNoGastos.setVisibility(View.VISIBLE);
            tvTotalGastos.setText("Total " + getMesActual() + ": $0.00");
        } else {
            pieChart.setVisibility(View.VISIBLE);
            tvNoGastos.setVisibility(View.GONE);

            gastos.clear(); // Limpiar la lista antes de cargar nuevos datos
            for (String gastoStr : listaGastosString) {
                try {
                    String[] partes = gastoStr.split(" - ");
                    if (partes.length != 4) {
                        Log.e("GastosYa", "Formato inválido en gastoStr: " + gastoStr);
                        continue; // Saltar este elemento si el formato es incorrecto
                    }
                    String id = partes[0];
                    String nombre = partes[1];
                    String cantidadStr = partes[2].replace("$", "");
                    double cantidad = Double.parseDouble(cantidadStr);
                    String categoria = partes[3];
                    Date fecha = new Date();
                    Gasto gasto = new Gasto(id, nombre, cantidad, categoria, fecha);
                    gastos.add(gasto);
                } catch (Exception e) {
                    Log.e("GastosYa", "Error procesando gastoStr: " + gastoStr + " | " + e.getMessage());
                }
            }

            mostrarTotalGastos();
            mostrarGraficoPorCategoria();
        }
    }

    private void eliminarGasto(int position) {
        if (position >= 0 && position < gastos.size()) {
            Gasto gastoEliminado = gastos.get(position);
            String gastoId = gastoEliminado.getId();

            if (gastoId != null && mAuth.getCurrentUser() != null) {
                String userId = mAuth.getCurrentUser().getUid();
                db.collection("users").document(userId).collection("gastos").document(gastoId)
                        .delete()
                        .addOnSuccessListener(aVoid -> {
                            Log.d("GastosYa", "Gasto eliminado de Firestore con ID: " + gastoId);
                        })
                        .addOnFailureListener(e -> {
                            Log.e("GastosYa", "Error al eliminar gasto de Firestore: " + e.getMessage());
                            Toast.makeText(this, "Error al eliminar gasto: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        });
            }

            gastos.remove(position);
            gastoAdapter.notifyItemRemoved(position);
            gastoAdapter.notifyItemRangeChanged(position, gastos.size());

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

        tvTotalGastos.setText("Total " + getMesActual() + ": $" + String.format(Locale.getDefault(), "%.2f", totalGastos));
    }

    private String getMesActual() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM", Locale.getDefault());
        String mesActual = dateFormat.format(Calendar.getInstance().getTime());
        return mesActual.substring(0, 1).toUpperCase() + mesActual.substring(1).toLowerCase();
    }
}