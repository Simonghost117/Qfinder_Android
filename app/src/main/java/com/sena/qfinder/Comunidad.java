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

        adapter = new ComunidadAdapter(getContext(), listaComunidades);
        recyclerView.setAdapter(adapter);

        return view;
    }

    // Adapter definido dentro de la misma clase
    private class ComunidadAdapter extends RecyclerView.Adapter<ComunidadAdapter.ViewHolder> {

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

            holder.imgComunidad.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Obtén el FragmentManager desde el Context de la Activity
                    if (context instanceof androidx.fragment.app.FragmentActivity) {
                        FragmentManager fragmentManager = ((androidx.fragment.app.FragmentActivity) context).getSupportFragmentManager();

                        // 2. Crear una instancia del Fragment que queremos llamar
                        PerfilComunidad pf = new PerfilComunidad();

                        // 3. Iniciar una transacción
                        FragmentTransaction transactionPF = fragmentManager.beginTransaction();

                        // 4. Reemplazar el contenido del contenedor con el nuevo Fragment
                        transactionPF.replace(R.id.fragment_container, pf); // Asegúrate de tener un ViewGroup con este ID en tu Activity

                        // 5. Opcionalmente, agregar a la pila de retroceso
                        transactionPF.addToBackStack(null);

                        // 6. Confirmar la transacción
                        transactionPF.commit();
                    } else {
                        Toast.makeText(context, "Error al obtener FragmentManager", Toast.LENGTH_SHORT).show();
                    }
                }
            });

            // Manejar clic en el botón "Unirme"
            holder.btnUnirme1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Cuando se hace clic en el botón "Unirme", reemplaza el fragmento actual con el fragmento de Chat
                    ChatComunidad chatFragment = ChatComunidad.newInstance(comunidad[0]); // Nombre de la comunidad
                    FragmentTransaction transaction = ((androidx.fragment.app.FragmentActivity) context).getSupportFragmentManager().beginTransaction();
                    transaction.replace(R.id.fragment_container, chatFragment);  // Asegúrate de tener un ViewGroup con este ID en tu Activity
                    transaction.addToBackStack(null);  // Opcional: Para permitir volver al fragmento anterior
                    transaction.commit();
                }
            });
        }

        @Override
        public int getItemCount() {
            return comunidades.size();
        }

        // Asignación de elementos XML de los items agregados
        public class ViewHolder extends RecyclerView.ViewHolder {
            TextView nombre, miembros;
            ImageView imgComunidad;
            Button btnUnirme1; // Define el botón

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                nombre = itemView.findViewById(R.id.nombre_comunidad);
                miembros = itemView.findViewById(R.id.miembros_comunidad);
                imgComunidad = itemView.findViewById(R.id.imgComunidad);
                btnUnirme1 = itemView.findViewById(R.id.btnUnirme1); // Encuentra el botón
            }
        }
    }
}
