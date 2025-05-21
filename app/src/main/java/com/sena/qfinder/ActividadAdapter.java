package com.sena.qfinder;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sena.qfinder.models.ActividadGetResponse;

import java.util.List;

public class ActividadAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<ActividadGetResponse> listaActividades;
    private OnItemClickListener listener;

    // Tipo de vista
    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;

    public interface OnItemClickListener {
        void onEditarClick(int position);
        void onEliminarClick(int position);
    }

    public ActividadAdapter(List<ActividadGetResponse> listaActividades) {
        this.listaActividades = listaActividades;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setActividades(List<ActividadGetResponse> actividades) {
        this.listaActividades = actividades;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return (listaActividades != null && !listaActividades.isEmpty()) ? listaActividades.size() + 1 : 0;
    }

    @Override
    public int getItemViewType(int position) {
        return (position == 0) ? TYPE_HEADER : TYPE_ITEM;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_HEADER) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_encabezado, parent, false);
            return new HeaderViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_actividad, parent, false);
            return new ItemViewHolder(view, listener);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ItemViewHolder) {
            int realPosition = position - 1; // restamos 1 por el encabezado
            ActividadGetResponse actividad = listaActividades.get(realPosition);
            ((ItemViewHolder) holder).bind(actividad);
        }
    }

    public static class HeaderViewHolder extends RecyclerView.ViewHolder {
        public HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    public static class ItemViewHolder extends RecyclerView.ViewHolder {
        TextView txtNombre, txtFecha, txtHora, txtActividad;
        ImageButton btnEliminar, btnEditar;

        public ItemViewHolder(@NonNull View itemView, OnItemClickListener listener) {
            super(itemView);
            txtNombre = itemView.findViewById(R.id.txtNombre);
            txtFecha = itemView.findViewById(R.id.txtFecha);
            txtHora = itemView.findViewById(R.id.txtHora);
            txtActividad = itemView.findViewById(R.id.txtActividad);
            btnEliminar = itemView.findViewById(R.id.btnEliminar);
            btnEditar = itemView.findViewById(R.id.btnEditar);

            btnEditar.setOnClickListener(v -> {
                if (listener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    listener.onEditarClick(getAdapterPosition() - 1); // -1 por encabezado
                }
            });

            btnEliminar.setOnClickListener(v -> {
                if (listener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    listener.onEliminarClick(getAdapterPosition() - 1); // -1 por encabezado
                }
            });
        }

        public void bind(ActividadGetResponse actividad) {

            txtFecha.setText(formatDate(actividad.getFecha()));
            txtHora.setText(formatTime(actividad.getHora()));
            txtActividad.setText(actividad.getDescripcion());
        }

        private String formatDate(String fecha) {
            try {
                // Asume formato YYYY-MM-DD de la API
                String[] partes = fecha.split("-");
                return partes[2] + "/" + partes[1] + "/" + partes[0];
            } catch (Exception e) {
                return fecha;
            }
        }

        private String formatTime(String hora) {
            try {
                // Asume formato HH:MM:SS de la API
                return hora.substring(0, 5);
            } catch (Exception e) {
                return hora;
            }
        }
    }
}