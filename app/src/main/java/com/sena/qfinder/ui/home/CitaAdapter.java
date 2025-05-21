package com.sena.qfinder.ui.home;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sena.qfinder.R;
import com.sena.qfinder.models.CitaMedica;

import java.util.List;

public class CitaAdapter extends RecyclerView.Adapter<CitaAdapter.CitaViewHolder> {
    private List<CitaMedica> listaCitas;

    public CitaAdapter(List<CitaMedica> listaCitas) {
        this.listaCitas = listaCitas;
    }

    @NonNull
    @Override
    public CitaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_cita_medica, parent, false);
        return new CitaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CitaViewHolder holder, int position) {
        CitaMedica cita = listaCitas.get(position);
        holder.textFecha.setText(cita.getFechaCita());
        holder.textMotivo.setText(cita.getMotivoCita());
        holder.textEstado.setText(cita.getEstadoCita());
    }

    @Override
    public int getItemCount() {
        return listaCitas.size();
    }

    public static class CitaViewHolder extends RecyclerView.ViewHolder {
        TextView textFecha, textMotivo, textEstado;

        public CitaViewHolder(@NonNull View itemView) {
            super(itemView);
            textFecha = itemView.findViewById(R.id.textFechaCita);
            textMotivo = itemView.findViewById(R.id.textMotivoCita);
            textEstado = itemView.findViewById(R.id.textEstadoCita);
        }
    }
}
