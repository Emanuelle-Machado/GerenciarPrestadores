package com.example.gerenciarprestadores.views;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.gerenciarprestadores.DAO.AgendamentoDao;
import com.example.gerenciarprestadores.Database.AppDatabase;
import com.example.gerenciarprestadores.R;
import com.example.gerenciarprestadores.model.Agendamento;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class CadastroAgendamentoActivity extends AppCompatActivity {
    private EditText etNomeCliente, etEndereco, etTelefone, etData, etHora;
    private Button btnSalvar;
    private AppDatabase db;
    private AgendamentoDao agendamentoDao;
    private Calendar calendar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro_agendamento);

        etNomeCliente = findViewById(R.id.etNomeCliente);
        etEndereco = findViewById(R.id.etEndereco);
        etTelefone = findViewById(R.id.etTelefone);
        etData = findViewById(R.id.etData);
        etHora = findViewById(R.id.etHora);
        btnSalvar = findViewById(R.id.btnSalvar);

        db = AppDatabase.getInstance(this);
        agendamentoDao = db.agendamentoDao();
        calendar = Calendar.getInstance();

        etData.setOnClickListener(v -> showDatePickerDialog());
        etHora.setOnClickListener(v -> showTimePickerDialog());

        btnSalvar.setOnClickListener(v -> salvarAgendamento());
    }

    private void showDatePickerDialog() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.MONTH, month);
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                    etData.setText(dateFormat.format(calendar.getTime()));
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void showTimePickerDialog() {
        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                (view, hourOfDay, minute) -> {
                    calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    calendar.set(Calendar.MINUTE, minute);
                    SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
                    etHora.setText(timeFormat.format(calendar.getTime()));
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
        );
        timePickerDialog.show();
    }

    private void salvarAgendamento() {
        String nomeCliente = etNomeCliente.getText().toString();
        String endereco = etEndereco.getText().toString();
        String telefone = etTelefone.getText().toString();
        String data = etData.getText().toString();
        String hora = etHora.getText().toString();

        if (nomeCliente.isEmpty() || endereco.isEmpty() || telefone.isEmpty() || data.isEmpty() || hora.isEmpty()) {
            Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show();
            return;
        }

        Agendamento agendamento = new Agendamento();
        agendamento.nomeCliente = nomeCliente;
        agendamento.endereco = endereco;
        agendamento.telefone = telefone;
        agendamento.data = calendar.getTime();

        new Thread(() -> {
            long agendamentoId = agendamentoDao.insert(agendamento);
            runOnUiThread(() -> {
                Toast.makeText(this, "Agendamento salvo com sucesso", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(CadastroAgendamentoActivity.this, AdicionarServicosActivity.class);
                intent.putExtra("agendamentoId", agendamentoId);
                startActivity(intent);
                finish();
            });
        }).start();
    }
}