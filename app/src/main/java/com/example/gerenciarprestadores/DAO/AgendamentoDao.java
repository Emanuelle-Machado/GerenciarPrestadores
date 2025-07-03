package com.example.gerenciarprestadores.DAO;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.gerenciarprestadores.model.Agendamento;

import java.util.Date;
import java.util.List;

@Dao
public interface AgendamentoDao {
    @Insert
    long insert(Agendamento agendamento);

    @Update
    void update(Agendamento agendamento);

    @Delete
    void delete(Agendamento agendamento);

    @Query("SELECT * FROM agendamento ORDER BY data DESC")
    List<Agendamento> getAll();

    @Query("SELECT * FROM agendamento WHERE data >= :startOfDay AND data < :endOfDay ORDER BY data")
    List<Agendamento> getByDateRange(long startOfDay, long endOfDay);

    @Query("SELECT * FROM agendamento WHERE id = :id")
    Agendamento getById(long id);
}

