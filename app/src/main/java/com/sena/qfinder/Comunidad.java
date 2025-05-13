package com.sena.qfinder;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.List;

public class Comunidad extends Fragment {

    private RecyclerView recyclerView;
    private ComunidadAdapter adapter;
    private List<ComunidadModelo> listaComunidades;
    private ComunidadDB db;

    public Comunidad() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_comunidad, container, false);

        recyclerView = view.findViewById(R.id.recyclerComunidades);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        db = new ComunidadDB(getContext());
        listaComunidades = db.obtenerComunidades();

        adapter = new ComunidadAdapter(getContext(), listaComunidades, getParentFragmentManager());
        recyclerView.setAdapter(adapter);

        EditText buscador = view.findViewById(R.id.buscar_comunidad);
        buscador.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.filtrar(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        FloatingActionButton fab = view.findViewById(R.id.fabAddComunidad);
        fab.setOnClickListener(v -> mostrarDialogoCrearComunidad());

        return view;
    }

    private void mostrarDialogoCrearComunidad() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Nueva comunidad");

        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_crear_comunidad, null);
        EditText etNombre = dialogView.findViewById(R.id.etNombreComunidad);
        EditText etMiembros = dialogView.findViewById(R.id.etMiembrosComunidad);
        builder.setView(dialogView);

        builder.setPositiveButton("Crear", (dialog, which) -> {
            String nombre = etNombre.getText().toString().trim();
            String miembros = etMiembros.getText().toString().trim();

            if (!nombre.isEmpty() && !miembros.isEmpty()) {
                db.agregarComunidad(nombre, miembros);
                listaComunidades.clear();
                listaComunidades.addAll(db.obtenerComunidades());
                adapter.notifyDataSetChanged();

                PerfilComunidad pf = PerfilComunidad.newInstance(nombre, miembros);
                FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.fragment_container, pf);
                transaction.addToBackStack(null);
                transaction.commit();
            } else {
                Toast.makeText(getContext(), "Completa todos los campos", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancelar", null);
        builder.show();
    }

    private class ComunidadAdapter extends RecyclerView.Adapter<ComunidadAdapter.ViewHolder> {

        private Context context;
        private List<ComunidadModelo> comunidades;
        private List<ComunidadModelo> comunidadesOriginal;
        private FragmentManager fragmentManager;

        public ComunidadAdapter(Context context, List<ComunidadModelo> comunidades, FragmentManager fragmentManager) {
            this.context = context;
            this.comunidades = new ArrayList<>(comunidades);
            this.comunidadesOriginal = new ArrayList<>(comunidades);
            this.fragmentManager = fragmentManager;
        }

        public void filtrar(String texto) {
            comunidades.clear();
            if (texto.isEmpty()) {
                comunidades.addAll(comunidadesOriginal);
            } else {
                String textoFiltrado = texto.toLowerCase();
                for (ComunidadModelo item : comunidadesOriginal) {
                    if (item.getNombre().toLowerCase().contains(textoFiltrado)) {
                        comunidades.add(item);
                    }
                }
            }
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_comunidad, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            ComunidadModelo comunidad = comunidades.get(position);
            holder.nombre.setText(comunidad.getNombre());
            holder.miembros.setText(comunidad.getMiembros());

            boolean unido = obtenerEstadoUnion(comunidad.getNombre());
            holder.btnUnirme1.setText(unido ? "Unido" : "Unirme");
            holder.btnUnirme1.setBackgroundColor(context.getResources().getColor(unido ? R.color.colorUnido : R.color.colorUnirse));

            holder.btnUnirme1.setOnClickListener(v -> {
                ChatComunidad chat = ChatComunidad.newInstance(comunidad.getNombre());
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                transaction.replace(R.id.fragment_container, chat);
                transaction.addToBackStack(null);
                transaction.commit();

                if (!unido) {
                    guardarEstadoUnion(comunidad.getNombre(), true);
                    holder.btnUnirme1.setText("Unido");
                    holder.btnUnirme1.setBackgroundColor(context.getResources().getColor(R.color.colorUnido));
                }
            });

            holder.imgComunidad.setOnClickListener(view -> {
                PerfilComunidad pf = PerfilComunidad.newInstance(comunidad.getNombre(), comunidad.getMiembros());
                FragmentTransaction transaction = ((FragmentActivity) context).getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.fragment_container, pf);
                transaction.addToBackStack(null);
                transaction.commit();
            });

            holder.btnOpciones.setOnClickListener(v -> {
                PopupMenu popup = new PopupMenu(context, holder.btnOpciones);
                popup.getMenuInflater().inflate(R.menu.menu_comunidad_item, popup.getMenu());

                popup.setOnMenuItemClickListener(item -> {
                    int itemId = item.getItemId();
                    if (itemId == R.id.menu_editar) {
                        mostrarDialogoEditar(position);
                        return true;
                    } else if (itemId == R.id.menu_eliminar) {
                        db.eliminarComunidad(comunidad.getId());
                        comunidades.remove(position);
                        comunidadesOriginal.remove(position);
                        notifyItemRemoved(position);
                        return true;
                    } else if (itemId == R.id.btnsalirComunidad) {
                        guardarEstadoUnion(comunidad.getNombre(), false);
                        holder.btnUnirme1.setText("Unirme");
                        holder.btnUnirme1.setBackgroundColor(context.getResources().getColor(R.color.colorUnirse));

                        FragmentTransaction tx = fragmentManager.beginTransaction();
                        tx.replace(R.id.fragment_container, new Comunidad());
                        tx.commit();
                        return true;
                    }
                    return false;
                });

                popup.show();
            });
        }

        @Override
        public int getItemCount() {
            return comunidades.size();
        }

        private void mostrarDialogoEditar(int pos) {
            ComunidadModelo comunidad = comunidades.get(pos);
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Editar comunidad");

            View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_crear_comunidad, null);
            EditText etNombre = dialogView.findViewById(R.id.etNombreComunidad);
            EditText etMiembros = dialogView.findViewById(R.id.etMiembrosComunidad);

            etNombre.setText(comunidad.getNombre());
            etMiembros.setText(comunidad.getMiembros());
            builder.setView(dialogView);

            builder.setPositiveButton("Guardar", (dialog, which) -> {
                comunidad.setNombre(etNombre.getText().toString().trim());
                comunidad.setMiembros(etMiembros.getText().toString().trim());
                db.editarComunidad(comunidad.getId(), comunidad.getNombre(), comunidad.getMiembros());
                notifyItemChanged(pos);
            });

            builder.setNegativeButton("Cancelar", null);
            builder.show();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            TextView nombre, miembros;
            ImageView imgComunidad, btnOpciones;
            Button btnUnirme1;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                nombre = itemView.findViewById(R.id.nombre_comunidad);
                miembros = itemView.findViewById(R.id.miembros_comunidad);
                imgComunidad = itemView.findViewById(R.id.imgComunidad);
                btnUnirme1 = itemView.findViewById(R.id.btnUnirme1);
                btnOpciones = itemView.findViewById(R.id.btnOpciones);
            }
        }
    }

    public class ComunidadModelo {
        private int id;
        private String nombre;
        private String miembros;

        public ComunidadModelo(int id, String nombre, String miembros) {
            this.id = id;
            this.nombre = nombre;
            this.miembros = miembros;
        }

        public int getId() { return id; }
        public void setId(int id) { this.id = id; }

        public String getNombre() { return nombre; }
        public void setNombre(String nombre) { this.nombre = nombre; }

        public String getMiembros() { return miembros; }
        public void setMiembros(String miembros) { this.miembros = miembros; }
    }

    public class ComunidadDB extends SQLiteOpenHelper {
        private static final String DB_NAME = "QFinder.db";
        private static final int DB_VERSION = 1;

        public ComunidadDB(Context context) {
            super(context, DB_NAME, null, DB_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE IF NOT EXISTS comunidades (id INTEGER PRIMARY KEY AUTOINCREMENT, nombre TEXT, miembros TEXT)");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS comunidades");
            onCreate(db);
        }

        public void agregarComunidad(String nombre, String miembros) {
            SQLiteDatabase db = getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("nombre", nombre);
            values.put("miembros", miembros);
            db.insert("comunidades", null, values);
            db.close();
        }

        public List<ComunidadModelo> obtenerComunidades() {
            List<ComunidadModelo> lista = new ArrayList<>();
            SQLiteDatabase db = getReadableDatabase();
            Cursor cursor = db.rawQuery("SELECT * FROM comunidades", null);
            while (cursor.moveToNext()) {
                int id = cursor.getInt(0);
                String nombre = cursor.getString(1);
                String miembros = cursor.getString(2);
                lista.add(new ComunidadModelo(id, nombre, miembros));
            }
            cursor.close();
            db.close();
            return lista;
        }

        public void editarComunidad(int id, String nombre, String miembros) {
            SQLiteDatabase db = getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("nombre", nombre);
            values.put("miembros", miembros);
            db.update("comunidades", values, "id=?", new String[]{String.valueOf(id)});
            db.close();
        }

        public void eliminarComunidad(int id) {
            SQLiteDatabase db = getWritableDatabase();
            db.delete("comunidades", "id=?", new String[]{String.valueOf(id)});
            db.close();
        }
    }

    private void guardarEstadoUnion(String nombreComunidad, boolean unido) {
        SharedPreferences prefs = requireContext().getSharedPreferences("UnionComunidad", Context.MODE_PRIVATE);
        prefs.edit().putBoolean(nombreComunidad, unido).apply();
    }

    private boolean obtenerEstadoUnion(String nombreComunidad) {
        SharedPreferences prefs = requireContext().getSharedPreferences("UnionComunidad", Context.MODE_PRIVATE);
        return prefs.getBoolean(nombreComunidad, false);
    }
}