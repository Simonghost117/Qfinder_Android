package com.sena.qfinder;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.content.Context;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class Comunidad extends Fragment {

    private RecyclerView recyclerView;
    private ComunidadAdapter adapter;
    private List<String[]> listaComunidades;

    public Comunidad() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_comunidad, container, false);

        recyclerView = view.findViewById(R.id.recyclerComunidades);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        listaComunidades = new ArrayList<>();
        listaComunidades.add(new String[]{"Familias Unidas 1", "1.2 mill. de miembros"});
        listaComunidades.add(new String[]{"Familias Unidas 2", "850 mil miembros"});
        listaComunidades.add(new String[]{"Cuidadores Activos", "600 mil miembros"});

        adapter = new ComunidadAdapter(getContext(), listaComunidades);
        recyclerView.setAdapter(adapter);

        return view;
    }

    // Adapter definido dentro de la misma clase
    private static class ComunidadAdapter extends RecyclerView.Adapter<ComunidadAdapter.ViewHolder> {

        private Context context;
        private List<String[]> comunidades;

        public ComunidadAdapter(Context context, List<String[]> comunidades) {
            this.context = context;
            this.comunidades = comunidades;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_comunidad, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            String[] comunidad = comunidades.get(position);
            holder.nombre.setText(comunidad[0]);
            holder.miembros.setText(comunidad[1]);
        }

        @Override
        public int getItemCount() {
            return comunidades.size();
        }

        public static class ViewHolder extends RecyclerView.ViewHolder {
            TextView nombre, miembros;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                nombre = itemView.findViewById(R.id.nombre_comunidad);
                miembros = itemView.findViewById(R.id.miembros_comunidad);
            }
        }
    }
}
