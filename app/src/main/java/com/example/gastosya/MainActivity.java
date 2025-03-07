package com.example.gastosya;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.pm.PackageManager;
import android.os.Build;
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
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class MainActivity extends AppCompatActivity {

    private List<Gasto> listaGastos;
    private GastoAdapter gastoAdapter;
    private Spinner spinnerCategoria;
    private EditText etNombreGasto;
    private EditText etCantidadGasto;
    private Date fecha;
    private BottomNavigationView bottomNavigationView;
    private static final String CHANNEL_ID = "GastosYaChannel";
    private static final double LIMITE_GASTOS = 500.0; // Límite hardcodeado por ahora
    private static final int NOTIFICATION_ID = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Crear canal de notificaciones
        createNotificationChannel();

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

        fecha = new Date();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "GastosYa Notifications";
            String description = "Notificaciones para exceso de gastos";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
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
        Gasto nuevoGasto = new Gasto(nombre, cantidad, categoria, new Date());
        listaGastos.add(nuevoGasto);
        gastoAdapter.notifyDataSetChanged();

        LayoutInflater inflater = getLayoutInflater();
        View toastLayout = inflater.inflate(R.layout.custom_toast, null);
        TextView textView = toastLayout.findViewById(R.id.toast_text);
        textView.setText("Gasto agregado");

        Toast toast = new Toast(getApplicationContext());
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, 100);
        toast.setView(toastLayout);
        toast.show();

        // Verificar si se superó el límite y enviar notificación
        double totalGastos = calcularTotalGastos();
        if (totalGastos > LIMITE_GASTOS) {
            enviarNotificacion(totalGastos);
        }
    }

    private double calcularTotalGastos() {
        double total = 0;
        for (Gasto gasto : listaGastos) {
            total += gasto.getCantidad();
        }
        return total;
    }

    private void enviarNotificacion(double totalGastos) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification) // Necesitarás un ícono en res/drawable
                .setContentTitle("Límite de gastos superado")
                .setContentText("Has gastado $" + String.format("%.2f", totalGastos) + ", superando el límite de $" + LIMITE_GASTOS)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

        // Verificar permiso en Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 1);
                return;
            }
        }

        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }

    private void eliminarGasto(int position) {
        if (position >= 0 && position < listaGastos.size()) {
            listaGastos.remove(position);
            gastoAdapter.notifyItemRemoved(position);
            gastoAdapter.notifyItemRangeChanged(position, listaGastos.size());

            // Crear Toast personalizado para eliminación
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