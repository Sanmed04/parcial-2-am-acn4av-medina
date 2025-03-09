package com.example.gastosya;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class SeccionGastoAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;

    private List<Object> items;
    private GastoAdapter.OnItemClickListener listener;

    public SeccionGastoAdapter(List<SeccionGastos> secciones, GastoAdapter.OnItemClickListener listener) {
        this.listener = listener;
        items = new ArrayList<>();
        for (SeccionGastos seccion : secciones) {
            items.add(seccion);
            items.addAll(seccion.getGastosDelDia());
        }
    }

    public Object getItem(int position) {
        if (position >= 0 && position < items.size()) {
            return items.get(position);
        }
        return null;
    }

    public void removeItem(int position) {
        if (position >= 0 && position < items.size() && items.get(position) instanceof Gasto) {
            items.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, getItemCount());
        }
    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position) instanceof SeccionGastos ? TYPE_HEADER : TYPE_ITEM;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_HEADER) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_fecha_header, parent, false);
            return new HeaderViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_gasto, parent, false);
            return new GastoViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == TYPE_HEADER) {
            SeccionGastos seccion = (SeccionGastos) items.get(position);
            ((HeaderViewHolder) holder).bind(seccion.getFecha());
        } else {
            Gasto gasto = (Gasto) items.get(position);
            ((GastoViewHolder) holder).bind(gasto, position, listener);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class HeaderViewHolder extends RecyclerView.ViewHolder {
        TextView tvFecha;

        public HeaderViewHolder(View itemView) {
            super(itemView);
            tvFecha = itemView.findViewById(R.id.tvFecha);
        }

        public void bind(Date fecha) {
            SimpleDateFormat sdf = new SimpleDateFormat("d 'de' MMMM", Locale.getDefault());
            if (isHoy(fecha)) {
                tvFecha.setText("Hoy");
            } else {
                tvFecha.setText(sdf.format(fecha));
            }
        }

        private boolean isHoy(Date date) {
            Calendar cal1 = Calendar.getInstance();
            Calendar cal2 = Calendar.getInstance();
            cal2.setTime(date);
            return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                    cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
        }
    }

    // ViewHolder para los ítems
    public static class GastoViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombre, tvCantidad;
        ImageView imgCategoria;
        ImageButton btnEliminar;

        public GastoViewHolder(View itemView) {
            super(itemView);
            tvNombre = itemView.findViewById(R.id.tvNombre);
            tvCantidad = itemView.findViewById(R.id.tvCantidad);
            imgCategoria = itemView.findViewById(R.id.imgCategoriaGasto);
            btnEliminar = itemView.findViewById(R.id.btnEliminar);
        }

        public void bind(Gasto gasto, int position, GastoAdapter.OnItemClickListener listener) {
            tvNombre.setText(gasto.getNombre());
            tvCantidad.setText("$" + String.format("%.2f", gasto.getCantidad()));

            Map<String, Integer> categoriaIconoMap = new HashMap<>();
            categoriaIconoMap.put("Servicio", R.drawable.baseline_home_repair_service_24);
            categoriaIconoMap.put("Compra", R.drawable.baseline_shopping_bag_24);
            categoriaIconoMap.put("Transaccion", R.drawable.baseline_handshake_24);
            categoriaIconoMap.put("Alimentacion", R.drawable.baseline_fastfood_24);
            categoriaIconoMap.put("Salud", R.drawable.baseline_local_hospital_24);
            categoriaIconoMap.put("Entretenimiento", R.drawable.baseline_live_tv_24);
            categoriaIconoMap.put("Transporte", R.drawable.baseline_directions_transit_24);
            categoriaIconoMap.put("Vivienda", R.drawable.baseline_house_24);
            categoriaIconoMap.put("Educacion", R.drawable.baseline_school_24);

            Integer icono = categoriaIconoMap.get(gasto.getCategoria());
            if (icono != null) {
                imgCategoria.setImageResource(icono);
            } else {
                imgCategoria.setImageResource(R.drawable.predeterminado);
            }

            if (btnEliminar != null) {
                btnEliminar.setOnClickListener(v -> listener.onEliminarClick(position));
            }
        }
    }
}