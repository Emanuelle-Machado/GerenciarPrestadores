package com.example.gerenciarprestadores.DAO;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.gerenciarprestadores.model.Servico;

import java.util.List;

@Dao
public interface ServicoDao {
    @Insert
    long insert(Servico servico);

    @Update
    void update(Servico servico);

    @Delete
    void delete(Servico servico);

    @Query("SELECT * FROM servico WHERE agendamentoId = :agendamentoId")
    List<Servico> getByAgendamentoId(long agendamentoId);
}
