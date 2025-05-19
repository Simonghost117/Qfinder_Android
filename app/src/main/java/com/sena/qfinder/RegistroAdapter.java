package com.sena.qfinder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;

import com.sena.qfinder.model.Registro;

import java.util.List;

public class RegistroAdapter extends RecyclerView.Adapter<RegistroAdapter.ViewHolder> {
    private List<Registro> registros;

    public RegistroAdapter(List<Registro> registros) {
        this.registros = registros;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_registro, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Registro registro = registros.get(position);
        holder.titulo.setText(registro.getTitulo());
        holder.fecha.setText(registro.getFecha());
    }

    @Override
    public int getItemCount() {
        return registros.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView titulo, fecha;

        public ViewHolder(View itemView) {
            super(itemView);
            titulo = itemView.findViewById(R.id.titulo);
            fecha = itemView.findViewById(R.id.fecha);
        }
    }

    public void actualizar(List<Registro> nuevos) {
        this.registros = nuevos;
        notifyDataSetChanged();
    }
}
