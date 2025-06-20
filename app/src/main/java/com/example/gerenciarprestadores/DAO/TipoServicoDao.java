package com.example.gerenciarprestadores.DAO;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.gerenciarprestadores.model.TipoServico;

import java.util.List;

@Dao
public interface TipoServicoDao {
    @Insert
    long insert(TipoServico tipoServico);

    @Update
    void update(TipoServico tipoServico);

    @Delete
    void delete(TipoServico tipoServico);

    @Query("SELECT * FROM tipo_servico")
    List<TipoServico> getAll();

    @Query("SELECT * FROM tipo_servico WHERE id = :id")
    TipoServico getById(long id);
}
