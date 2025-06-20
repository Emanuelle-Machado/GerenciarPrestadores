package com.example.gerenciarprestadores.Database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.room.TypeConverter;

import com.example.gerenciarprestadores.DAO.AgendamentoDao;
import com.example.gerenciarprestadores.DAO.PagamentoDao;
import com.example.gerenciarprestadores.DAO.ServicoDao;
import com.example.gerenciarprestadores.DAO.TipoServicoDao;
import com.example.gerenciarprestadores.model.Agendamento;
import com.example.gerenciarprestadores.model.Pagamento;
import com.example.gerenciarprestadores.model.Servico;
import com.example.gerenciarprestadores.model.TipoServico;

import java.util.Date;

@Database(entities = {TipoServico.class, Agendamento.class, Servico.class, Pagamento.class}, version = 1, exportSchema = false)
@TypeConverters({Converters.class})
public abstract class AppDatabase extends RoomDatabase {
    public abstract TipoServicoDao tipoServicoDao();
    public abstract AgendamentoDao agendamentoDao();
    public abstract ServicoDao servicoDao();
    public abstract PagamentoDao pagamentoDao();

    private static volatile AppDatabase INSTANCE;

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "service_provider_db")
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}

class Converters {
    @TypeConverter
    public static Date fromTimestamp(Long value) {
        return value == null ? null : new Date(value);
    }

    @TypeConverter
    public static Long dateToTimestamp(Date date) {
        return date == null ? null : date.getTime();
    }
}