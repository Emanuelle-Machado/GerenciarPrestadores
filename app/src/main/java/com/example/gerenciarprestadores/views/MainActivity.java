package com.example.gerenciarprestadores.views;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gerenciarprestadores.DAO.AgendamentoDao;
import com.example.gerenciarprestadores.Database.AppDatabase;
import com.example.gerenciarprestadores.R;
import com.example.gerenciarprestadores.model.Agendamento;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private AgendamentoAdapter adapter;
    private List<Agendamento> agendamentoList;
    private AppDatabase db;
    private AgendamentoDao agendamentoDao;
    private Button btnFiltrarData, btnMostrarTodos;
    private Calendar calendar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Configurar Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        recyclerView = findViewById(R.id.recyclerViewAgendamentos);
        btnFiltrarData = findViewById(R.id.btnFiltrarData);
        btnMostrarTodos = findViewById(R.id.btnMostrarTodos);
        agendamentoList = new ArrayList<>();
        adapter = new AgendamentoAdapter(agendamentoList);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        db = AppDatabase.getInstance(this);
        agendamentoDao = db.agendamentoDao();
        calendar = Calendar.getInstance();

        loadTodosAgendamentos();

        btnFiltrarData.setOnClickListener(v -> showDatePickerDialog());
        btnMostrarTodos.setOnClickListener(v -> loadTodosAgendamentos());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.menu_tipos_servico) {
            startActivity(new Intent(this, TipoServicoActivity.class));
            return true;
        } else if (itemId == R.id.menu_cadastrar_agendamento) {
            startActivity(new Intent(this, CadastroAgendamentoActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadTodosAgendamentos() {
        new Thread(() -> {
            agendamentoList.clear();
            List<Agendamento> agendamentos = agendamentoDao.getAll();
            agendamentoList.addAll(agendamentos);
            runOnUiThread(() -> {
                adapter.notifyDataSetChanged();
                Log.d("MainActivity", "Agendamentos carregados: " + agendamentos.size());
                if (agendamentoList.isEmpty()) {
                    Toast.makeText(this, "Nenhum agendamento encontrado", Toast.LENGTH_SHORT).show();
                }
            });
        }).start();
    }

    private void showDatePickerDialog() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.MONTH, month);
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    calendar.set(Calendar.HOUR_OF_DAY, 0);
                    calendar.set(Calendar.MINUTE, 0);
                    calendar.set(Calendar.SECOND, 0);
                    calendar.set(Calendar.MILLISECOND, 0);
                    long startOfDay = calendar.getTimeInMillis();
                    calendar.add(Calendar.DAY_OF_MONTH, 1);
                    long endOfDay = calendar.getTimeInMillis();
                    loadAgendamentosPorData(startOfDay, endOfDay);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void loadAgendamentosPorData(long startOfDay, long endOfDay) {
        new Thread(() -> {
            agendamentoList.clear();
            List<Agendamento> agendamentos = agendamentoDao.getByDateRange(startOfDay, endOfDay);
            agendamentoList.addAll(agendamentos);
            runOnUiThread(() -> {
                adapter.notifyDataSetChanged();
                Log.d("MainActivity", "Agendamentos filtrados para data: " + agendamentos.size());
                if (agendamentoList.isEmpty()) {
                    Toast.makeText(this, "Nenhum agendamento encontrado para a data selecionada", Toast.LENGTH_SHORT).show();
                }
            });
        }).start();
    }

    private class AgendamentoAdapter extends RecyclerView.Adapter<AgendamentoAdapter.ViewHolder> {
        private List<Agendamento> agendamentos;
        private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

        public AgendamentoAdapter(List<Agendamento> agendamentos) {
            this.agendamentos = agendamentos;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_agendamento, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            Agendamento agendamento = agendamentos.get(position);
            holder.tvNomeCliente.setText(agendamento.nomeCliente);
            holder.tvEndereco.setText(agendamento.endereco);
            holder.tvData.setText(dateFormat.format(agendamento.data));

            // Clique simples para abrir tela de serviços
            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, AdicionarServicosActivity.class);
                intent.putExtra("agendamentoId", agendamento.id);
                startActivity(intent);
            });

            // Clique longo para abrir detalhes do agendamento
            holder.itemView.setOnLongClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, DetalhesAgendamentoActivity.class);
                intent.putExtra("agendamentoId", agendamento.id);
                startActivity(intent);
                return true;
            });
        }

        @Override
        public int getItemCount() {
            return agendamentos.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvNomeCliente, tvEndereco, tvData;

            public ViewHolder(View itemView) {
                super(itemView);
                tvNomeCliente = itemView.findViewById(R.id.tvNomeCliente);
                tvEndereco = itemView.findViewById(R.id.tvEndereco);
                tvData = itemView.findViewById(R.id.tvData);
            }
        }
    }
}