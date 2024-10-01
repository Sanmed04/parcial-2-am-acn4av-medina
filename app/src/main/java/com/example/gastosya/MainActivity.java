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
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private List<Gasto> listaGastos;
    private GastoAdapter gastoAdapter;
    private Spinner spinnerCategoria;
    private EditText etNombreGasto;
    private EditText etCantidadGasto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etNombreGasto = findViewById(R.id.etNombreGasto);
        etCantidadGasto = findViewById(R.id.etCantidadGasto);
        Button btnAgregar = findViewById(R.id.btnAgregarGasto);

        listaGastos = new ArrayList<>();
        RecyclerView recyclerView = findViewById(R.id.recyclerViewGastos);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        gastoAdapter = new GastoAdapter(listaGastos);
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

        Gasto nuevoGasto = new Gasto(nombre, cantidad, categoria);
        listaGastos.add(nuevoGasto);
        gastoAdapter.notifyDataSetChanged();
    }
}
