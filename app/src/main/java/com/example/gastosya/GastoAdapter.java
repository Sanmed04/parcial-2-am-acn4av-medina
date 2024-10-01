package com.example.gastosya;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

public class GastoAdapter extends RecyclerView.Adapter<GastoAdapter.GastoViewHolder> {

    private final List<Gasto> gastos;
    private OnGastoClickListener listener;
    private final Map<String, Integer> categoriaIconoMap;

    public interface OnGastoClickListener {
        void onGastoClick(Gasto gasto);

        void onGastoEliminarClick(int position);
    }

    public GastoAdapter(List<Gasto> gastos, OnGastoClickListener onGastoClickListener) {
        this.gastos = gastos;

        categoriaIconoMap = new HashMap<>();

        categoriaIconoMap.put("Servicio", R.drawable.servicio);
        categoriaIconoMap.put("Compra", R.drawable.compra);
        categoriaIconoMap.put("Transacción", R.drawable.transaccion);
        categoriaIconoMap.put("Alimentacion", R.drawable.alimentacion);
        categoriaIconoMap.put("Salud", R.drawable.salud);
        categoriaIconoMap.put("Entretenimiento", R.drawable.entretenimiento);
        categoriaIconoMap.put("Transporte", R.drawable.transporte);
        categoriaIconoMap.put("Vivienda", R.drawable.vivienda);
        categoriaIconoMap.put("Educacion", R.drawable.educacion);



    }

    @NonNull
    @Override
    public GastoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_gasto, parent, false);
        return new GastoViewHolder(view);
    }

    @SuppressLint("DefaultLocale")
    @Override
    public void onBindViewHolder(@NonNull GastoViewHolder holder, int position) {
        Gasto gasto = gastos.get(position);
        holder.tvNombreGasto.setText(gasto.getNombre());
        holder.tvCantidadGasto.setText(String.format("$%.2f", gasto.getCantidad()));

        Integer icono = categoriaIconoMap.get(gasto.getCategoria());
        if (icono != null) {
            holder.imgCategoriaGasto.setImageResource(icono);
        } else {
            holder.imgCategoriaGasto.setImageResource(R.drawable.predeterminado); // Un icono por defecto
        }

        holder.btnEliminarGasto.setOnClickListener(v -> {
            if (listener != null) {
                listener.onGastoEliminarClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return gastos.size();
    }

    public static class GastoViewHolder extends RecyclerView.ViewHolder {
        ImageButton btnEliminarGasto;
        TextView tvNombreGasto, tvCantidadGasto;
        ImageView imgCategoriaGasto;


        public GastoViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombreGasto = itemView.findViewById(R.id.tvNombreGasto);
            tvCantidadGasto = itemView.findViewById(R.id.tvCantidadGasto);
            imgCategoriaGasto = itemView.findViewById(R.id.imgCategoriaGasto);
            btnEliminarGasto = itemView.findViewById(R.id.btnEliminarGasto);

        }
    }
}
