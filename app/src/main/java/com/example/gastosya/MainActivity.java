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
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private List<Gasto> listaGastos;
    private GastoAdapter gastoAdapter;
    private Spinner spinnerCategoria;
    private EditText etNombreGasto;
    private EditText etCantidadGasto;
    private Date fecha;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNavigationView = findViewById(R.id.bottomNavigationView);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_home) {
                return true;
            } else if (item.getItemId() == R.id.nav_graph) {

                Intent intent = new Intent(MainActivity.this, ResumenActivity.class);
                ArrayList<String> listaGastosString = new ArrayList<>();
                for (Gasto gasto : listaGastos) {
                    listaGastosString.add(gasto.getNombre() + " - $" + gasto.getCantidad() + " - " + gasto.getCategoria());
                }
                intent.putStringArrayListExtra("listaGastos", listaGastosString);
                startActivity(intent);
                return true;
            }
            return false;
        });

        Button btnAgregar = findViewById(R.id.btnAgregarGasto);
        btnAgregar.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ResumenActivity.class);
            ArrayList<String> listaGastosString = new ArrayList<>();
            for (Gasto gasto : listaGastos) {
                listaGastosString.add(gasto.getNombre() + " - $" + gasto.getCantidad() + " - " + gasto.getCategoria());
            }
            intent.putStringArrayListExtra("listaGastos", listaGastosString);
            startActivity(intent);
        });

        etNombreGasto = findViewById(R.id.etNombreGasto);
        etCantidadGasto = findViewById(R.id.etCantidadGasto);


        listaGastos = new ArrayList<>();
        RecyclerView recyclerView = findViewById(R.id.recyclerViewGastos);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        gastoAdapter = new GastoAdapter(listaGastos, new GastoAdapter.OnItemClickListener() {
            @Override
            public void onEliminarClick(int position) {
                eliminarGasto(position);
            }
        });
        recyclerView.setAdapter(gastoAdapter);


        spinnerCategoria = findViewById(R.id.spinnerCategoria);
        String[] categorias = {"Servicio", "Compra", "Transaccion", "Alimentacion", "Entretenimiento", "Transporte", "Salud", "Vivienda", "Educacion"};
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

    protected void onResume() {
        super.onResume();

        ArrayList<String> listaGastosString = getIntent().getStringArrayListExtra("listaGastos");
        if (listaGastosString != null && !listaGastosString.isEmpty()) {
            listaGastos.clear();

            for (String gastoStr : listaGastosString) {
                String[] partes = gastoStr.split(" - ");
                String nombre = partes[0];
                double cantidad = Double.parseDouble(partes[1].replace("$", ""));
                String categoria = partes[2];
                Date fecha = new Date();

                Gasto gasto = new Gasto(nombre, cantidad, categoria, fecha);
                listaGastos.add(gasto);
            }

            gastoAdapter.notifyDataSetChanged();
        }
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
