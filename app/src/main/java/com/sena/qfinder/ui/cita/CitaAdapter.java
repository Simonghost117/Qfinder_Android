package com.sena.qfinder.ui.cita;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sena.qfinder.R;
import com.sena.qfinder.data.models.CitaMedica;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CitaAdapter extends RecyclerView.Adapter<CitaAdapter.CitaViewHolder> {
    private List<CitaMedica> listaCitas;

    public CitaAdapter(List<CitaMedica> listaCitas) {
        this.listaCitas = listaCitas;
    }

    public void updateData(List<CitaMedica> nuevasCitas) {
        this.listaCitas = nuevasCitas != null ? nuevasCitas : new ArrayList<>();
        notifyDataSetChanged();
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

        holder.textFecha.setText("Fecha_Cita: " + formatFecha(cita.getFechaCita()));

        if (cita.getTituloCita() != null && !cita.getTituloCita().isEmpty()) {
            holder.textTitulo.setText("Título: " + cita.getTituloCita());
            holder.textTitulo.setVisibility(View.VISIBLE);
        } else {
            holder.textTitulo.setVisibility(View.GONE);
        }

        holder.textMotivo.setText("Descripción: " + cita.getDescripcion());
        holder.textEstado.setText("Estado: " + cita.getEstado());
        holder.textHoraCita.setText("Hora_Cita: " + cita.getHoraCita());
    }

    private String formatFecha(String fecha) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            return outputFormat.format(inputFormat.parse(fecha));
        } catch (Exception e) {
            return fecha;
        }
    }

    @Override
    public int getItemCount() {
        return listaCitas.size();
    }

    public static class CitaViewHolder extends RecyclerView.ViewHolder {
        TextView textFecha, textMotivo, textEstado, textTitulo, textHoraCita;

        public CitaViewHolder(@NonNull View itemView) {
            super(itemView);
            textFecha = itemView.findViewById(R.id.textFechaCita);
            textTitulo = itemView.findViewById(R.id.textTituloCita);
            textMotivo = itemView.findViewById(R.id.textMotivoCita);
            textEstado = itemView.findViewById(R.id.textEstadoCita);
            textHoraCita = itemView.findViewById(R.id.horaCita);
        }
    }
}