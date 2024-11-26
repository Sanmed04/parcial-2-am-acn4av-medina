package com.example.gastosya;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import android.content.Intent;

public class MainActivity extends AppCompatActivity {

    private List<Gasto> listaGastos;
    private GastoAdapter gastoAdapter;
    private Spinner spinnerCategoria;
    private EditText etNombreGasto;
    private EditText etCantidadGasto;
    private Date fecha;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnResumen = findViewById(R.id.btnResumen);
        btnResumen.setOnClickListener(v -> {
            // Al hacer clic en el botón Ver Resumen, pasamos la lista de gastos a ResumenActivity
            Intent intent = new Intent(MainActivity.this, ResumenActivity.class);
            // Convertimos la lista de gastos en un ArrayList de String para pasarlo entre Activities
            ArrayList<String> listaGastosString = new ArrayList<>();
            for (Gasto gasto : listaGastos) {
                listaGastosString.add(gasto.getNombre() + " - $" + gasto.getCantidad() + " - " + gasto.getCategoria());
            }
            intent.putStringArrayListExtra("listaGastos", listaGastosString); // Pasamos los gastos como extra
            startActivity(intent);
        });

        etNombreGasto = findViewById(R.id.etNombreGasto);
        etCantidadGasto = findViewById(R.id.etCantidadGasto);
        Button btnAgregar = findViewById(R.id.btnAgregarGasto);

        listaGastos = new ArrayList<>();
        RecyclerView recyclerView = findViewById(R.id.recyclerViewGastos);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        gastoAdapter = new GastoAdapter(listaGastos, new GastoAdapter.OnGastoClickListener() {
            @Override
            public void onGastoClick(Gasto gasto) {
                // Aquí puedes manejar el click de un gasto si quieres
            }

            @Override
            public void onGastoEliminarClick(int position) {
                listaGastos.remove(position);
                gastoAdapter.notifyItemRemoved(position);
                gastoAdapter.notifyItemRangeChanged(position, listaGastos.size());
            }
        });
        recyclerView.setAdapter(gastoAdapter);

        spinnerCategoria = findViewById(R.id.spinnerCategoria);
        String[] categorias = {"Servicio", "Compra", "Transacción", "Alimentación", "Entretenimiento", "Transporte", "Salud", "Vivienda", "Educación"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categorias);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategoria.setAdapter(adapter);

        btnAgregar.setOnClickListener(v -> {
            String nombre = etNombreGasto.getText().toString();
            double cantidad;

            try {
                cantidad = Double.parseDouble(etCantidadGasto.getText().toString());
                agregarGasto(nombre, cantidad);
                etNombreGasto.setText("");
                etCantidadGasto.setText("");
            } catch (NumberFormatException e) {
                Toast.makeText(MainActivity.this, "Por favor ingrese una cantidad válida", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void agregarGasto(String nombre, double cantidad) {
        String categoria = spinnerCategoria.getSelectedItem().toString();

        Gasto nuevoGasto = new Gasto(nombre, cantidad, categoria, fecha);
        listaGastos.add(nuevoGasto);
        gastoAdapter.notifyDataSetChanged();
    }

    private void eliminarGasto(int position) {
        if (position >= 0 && position < listaGastos.size()) {
            listaGastos.remove(position);
            gastoAdapter.notifyItemRemoved(position);
            gastoAdapter.notifyItemRangeChanged(position, listaGastos.size());
        }
    }
}
