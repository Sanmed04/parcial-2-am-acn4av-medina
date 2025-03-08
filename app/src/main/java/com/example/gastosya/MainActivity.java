package com.example.gastosya;

import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import android.content.Intent;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.Source;

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
    private static final int NOTIFICATION_ID = 1;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private double limiteGastos = 500.0; // Valor por defecto hasta que se cargue de Firestore

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Verificar si el usuario está autenticado, si no, volver a LoginActivity
        if (mAuth.getCurrentUser() == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }



        // Cargar límite desde Firestore
        cargarLimiteDesdeFirestore();

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
        ImageButton btnConfig = findViewById(R.id.btnConfig);

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

        btnConfig.setOnClickListener(v -> mostrarDialogoConfiguracion());

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

    private void cargarLimiteDesdeFirestore() {
        if (mAuth.getCurrentUser() == null) {
            Log.e("GastosYa", "No hay usuario autenticado");
            Toast.makeText(this, "No hay usuario autenticado", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = mAuth.getCurrentUser().getUid();
        Log.d("GastosYa", "Intentando cargar límite para usuario: " + userId);

        db.collection("users").document(userId).get(Source.DEFAULT) // Usar fuente por defecto (cache o servidor)
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Double limite = documentSnapshot.getDouble("limiteGastos");
                        limiteGastos = (limite != null) ? limite : 500.0;
                        Log.d("GastosYa", "Límite cargado: " + limiteGastos);
                    } else {
                        Map<String, Object> userData = new HashMap<>();
                        userData.put("limiteGastos", 500.0);
                        db.collection("users").document(userId).set(userData)
                                .addOnSuccessListener(aVoid -> Log.d("GastosYa", "Límite por defecto guardado: 500.0"))
                                .addOnFailureListener(e -> Log.e("GastosYa", "Error al guardar límite por defecto: " + e.getMessage()));
                        limiteGastos = 500.0;
                    }
                })
                .addOnFailureListener(e -> {
                    limiteGastos = 500.0;
                    Log.e("GastosYa", "Error al cargar límite: " + e.getMessage(), e);
                    Toast.makeText(this, "Sin conexión, usando límite por defecto ($500)", Toast.LENGTH_SHORT).show();
                });
    }

    private void guardarLimiteEnFirestore(double nuevoLimite) {
        String userId = mAuth.getCurrentUser().getUid();
        Map<String, Object> userData = new HashMap<>();
        userData.put("limiteGastos", nuevoLimite);
        db.collection("users").document(userId).set(userData)
                .addOnSuccessListener(aVoid -> Toast.makeText(this, "Límite actualizado a $" + nuevoLimite, Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Error al guardar límite: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void mostrarDialogoConfiguracion() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Configurar Límite de Gastos");

        final EditText input = new EditText(this);
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        input.setHint("Ingrese el límite (e.g., 500.0)");
        input.setText(String.valueOf(limiteGastos));
        builder.setView(input);

        builder.setPositiveButton("Guardar", (dialog, which) -> {
            try {
                double nuevoLimite = Double.parseDouble(input.getText().toString());
                if (nuevoLimite <= 0) {
                    Toast.makeText(this, "El límite debe ser mayor a 0", Toast.LENGTH_SHORT).show();
                } else {
                    limiteGastos = nuevoLimite;
                    guardarLimiteEnFirestore(nuevoLimite);
                }
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Por favor ingrese un valor válido", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());
        builder.show();
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

        double totalGastos = calcularTotalGastos();
        if (totalGastos > limiteGastos) {
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
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Límite de gastos superado")
                .setContentText("Has gastado $" + String.format("%.2f", totalGastos) + ", superando el límite de $" + limiteGastos)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
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

    @Override
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
}