package com.example.gastosya;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
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
import com.google.firebase.firestore.SetOptions;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import android.content.Intent;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.Source;
import android.view.Gravity;
import android.view.LayoutInflater;
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
    private double limiteGastos = 50000.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        if (mAuth.getCurrentUser() == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        cargarLimiteDesdeFirestore();
        cargarGastosDesdeFirestore();

        createNotificationChannel();

        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_home) {
                return true;
            } else if (item.getItemId() == R.id.nav_graph) {
                Intent intent = new Intent(MainActivity.this, ResumenActivity.class);
                ArrayList<String> listaGastosString = new ArrayList<>();
                for (Gasto gasto : listaGastos) {
                    String entry = gasto.getId() + " - " + gasto.getNombre() + " - $" + gasto.getCantidad() + " - " + gasto.getCategoria() + " - " + gasto.getFecha().getTime();
                    listaGastosString.add(entry);
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

        spinnerCategoria.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                hideKeyboard();
            }
            return false;
        });

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

        btnConfig.setOnClickListener(v -> {
            ConfigurarLimiteDialogFragment dialog = new ConfigurarLimiteDialogFragment();
            dialog.show(getSupportFragmentManager(), "ConfigurarLimiteDialog");
        });

        fecha = new Date();


        View rootView = findViewById(android.R.id.content);
        rootView.setOnTouchListener((v, event) -> {
            hideKeyboard();
            return false;
        });
    }

    // Método para ocultar el teclado
    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        View view = getCurrentFocus();
        if (view == null) {
            view = new View(this);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public double getLimiteGastos() {
        return limiteGastos;
    }

    public void setLimiteGastos(double limite) {
        this.limiteGastos = limite;
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

        db.collection("users").document(userId).get(Source.DEFAULT)
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

    private void cargarGastosDesdeFirestore() {
        if (mAuth.getCurrentUser() == null) {
            Log.e("GastosYa", "No hay usuario autenticado para cargar gastos");
            return;
        }

        String userId = mAuth.getCurrentUser().getUid();
        Log.d("GastosYa", "Intentando cargar gastos para usuario: " + userId);

        db.collection("users").document(userId).collection("gastos")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    listaGastos.clear();
                    for (com.google.firebase.firestore.DocumentSnapshot doc : queryDocumentSnapshots) {
                        String id = doc.getId();
                        String nombre = doc.getString("nombre");
                        Double cantidad = doc.getDouble("cantidad");
                        String categoria = doc.getString("categoria");
                        Date fecha = doc.getDate("fecha");
                        if (nombre != null && cantidad != null && categoria != null && fecha != null) {
                            Gasto gasto = new Gasto(id, nombre, cantidad, categoria, fecha);
                            listaGastos.add(gasto);
                        }
                    }
                    gastoAdapter.notifyDataSetChanged();
                    Log.d("GastosYa", "Gastos cargados desde Firestore: " + listaGastos.size());
                })
                .addOnFailureListener(e -> {
                    Log.e("GastosYa", "Error al cargar gastos: " + e.getMessage() + " | UserID: " + userId);
                    Toast.makeText(this, "Error al cargar gastos: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    public void guardarLimiteEnFirestore(double nuevoLimite) {
        if (mAuth.getCurrentUser() == null) {
            Log.e("GastosYa", "No hay usuario autenticado para guardar el límite");
            Toast.makeText(this, "Error: No estás autenticado", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = mAuth.getCurrentUser().getUid();
        Log.d("GastosYa", "Guardando límite: " + nuevoLimite + " para usuario: " + userId);

        Map<String, Object> userData = new HashMap<>();
        userData.put("limiteGastos", nuevoLimite);

        db.collection("users").document(userId)
                .set(userData, SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    Log.d("GastosYa", "Límite guardado exitosamente en Firestore: " + nuevoLimite);
                    Toast.makeText(this, "Límite actualizado a $" + nuevoLimite, Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.e("GastosYa", "Error al guardar límite en Firestore: " + e.getMessage());
                    Toast.makeText(this, "Error al guardar límite: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void agregarGasto(String nombre, double cantidad) {
        String categoria = spinnerCategoria.getSelectedItem().toString();
        Date fechaActual = new Date();
        Gasto nuevoGasto = new Gasto(nombre, cantidad, categoria, fechaActual);
        listaGastos.add(nuevoGasto);
        gastoAdapter.notifyDataSetChanged();

        guardarGastoEnFirestore(nuevoGasto);

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

    private void guardarGastoEnFirestore(Gasto gasto) {
        if (mAuth.getCurrentUser() == null) {
            Log.e("GastosYa", "No hay usuario autenticado para guardar el gasto");
            Toast.makeText(this, "Error: No estás autenticado", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = mAuth.getCurrentUser().getUid();
        Map<String, Object> gastoData = new HashMap<>();
        gastoData.put("nombre", gasto.getNombre());
        gastoData.put("cantidad", gasto.getCantidad());
        gastoData.put("categoria", gasto.getCategoria());
        gastoData.put("fecha", gasto.getFecha());

        db.collection("users").document(userId).collection("gastos")
                .add(gastoData)
                .addOnSuccessListener(documentReference -> {
                    gasto.setId(documentReference.getId());
                    Log.d("GastosYa", "Gasto guardado en Firestore con ID: " + documentReference.getId());
                })
                .addOnFailureListener(e -> {
                    Log.e("GastosYa", "Error al guardar gasto en Firestore: " + e.getMessage());
                    Toast.makeText(this, "Error al guardar gasto: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
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
            Gasto gastoEliminado = listaGastos.get(position);
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
                    listaGastos.add(gasto);
                } catch (NumberFormatException e) {
                    Log.e("GastosYa", "Error parseando cantidad en gastoStr: " + gastoStr + " | " + e.getMessage());
                } catch (Exception e) {
                    Log.e("GastosYa", "Error procesando gastoStr: " + gastoStr + " | " + e.getMessage());
                }
            }
            gastoAdapter.notifyDataSetChanged();
        }
    }
}