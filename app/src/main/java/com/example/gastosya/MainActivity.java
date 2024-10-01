package com.example.gastosya;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private List<Gasto> listaGastos;
    private GastoAdapter gastoAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listaGastos = new ArrayList<>();
        RecyclerView recyclerView = findViewById(R.id.recyclerViewGastos);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        gastoAdapter = new GastoAdapter(listaGastos);
        recyclerView.setAdapter(gastoAdapter);

        Button btnAgregarGasto = findViewById(R.id.btnAgregarGasto);
        btnAgregarGasto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                agregarGasto("Cafe", 4000.00);
            }
        });
    }

    private void agregarGasto(String nombre, double cantidad) {
        listaGastos.add(new Gasto(nombre, cantidad));
        gastoAdapter.notifyDataSetChanged();
    }
}