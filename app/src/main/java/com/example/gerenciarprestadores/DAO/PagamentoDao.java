package com.example.gerenciarprestadores.DAO;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.gerenciarprestadores.model.Pagamento;

import java.util.List;

@Dao
public interface PagamentoDao {
    @Insert
    long insert(Pagamento pagamento);

    @Query("SELECT * FROM pagamento WHERE servicoId = :servicoId")
    List<Pagamento> getByServicoId(long servicoId);

    @Query("SELECT SUM(valorPago) FROM pagamento WHERE servicoId = :servicoId")
    double getTotalPagoByServicoId(long servicoId);
}
