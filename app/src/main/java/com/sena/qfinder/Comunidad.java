package com.sena.qfinder;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.content.Context;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
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

        // Pasamos el FragmentManager seguro al adapter
        adapter = new ComunidadAdapter(getContext(), listaComunidades, getParentFragmentManager());
        recyclerView.setAdapter(adapter);

        return view;
    }

    // Adapter definido dentro de la misma clase
    private class ComunidadAdapter extends RecyclerView.Adapter<ComunidadAdapter.ViewHolder> {

        private Context context;
        private List<String[]> comunidades;
        private FragmentManager fragmentManager;

        public ComunidadAdapter(Context context, List<String[]> comunidades, FragmentManager fragmentManager) {
            this.context = context;
            this.comunidades = comunidades;
            this.fragmentManager = fragmentManager;
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

            holder.imgComunidad.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try {
                        PerfilComunidad pf = new PerfilComunidad();
                        FragmentTransaction transactionPF = ((FragmentActivity) context).getSupportFragmentManager().beginTransaction();
                        transactionPF.replace(R.id.fragment_container, pf);
                        transactionPF.addToBackStack(null);
                        transactionPF.commit();

                    } catch (Exception e) {
                        Toast.makeText(context, "Error al abrir el perfil: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        e.printStackTrace();
                    }
                }
            });

            holder.btnUnirme1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        ChatComunidad chatFragment = ChatComunidad.newInstance(comunidad[0]);
                        FragmentTransaction transaction = fragmentManager.beginTransaction();
                        transaction.replace(R.id.fragment_container, chatFragment);
                        transaction.addToBackStack(null);
                        transaction.commit();
                    } catch (Exception e) {
                        Toast.makeText(context, "Error al abrir chat: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        e.printStackTrace();
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return comunidades.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            TextView nombre, miembros;
            ImageView imgComunidad;
            Button btnUnirme1;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                nombre = itemView.findViewById(R.id.nombre_comunidad);
                miembros = itemView.findViewById(R.id.miembros_comunidad);
                imgComunidad = itemView.findViewById(R.id.imgComunidad);
                btnUnirme1 = itemView.findViewById(R.id.btnUnirme1);
            }
        }
    }
}
