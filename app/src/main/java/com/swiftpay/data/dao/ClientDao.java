package com.swiftpay.data.dao;

import androidx.lifecycle.LiveData;
import androidx.paging.PagingSource;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import com.swiftpay.data.entity.Client;
import java.util.List;

@Dao
public interface ClientDao {
    @Insert
    long insert(Client client);
    @Update
    void update(Client client);
    @Delete
    void delete(Client client);
    @Query("SELECT * FROM clients WHERE id = :id")
    LiveData<Client> getById(long id);
    @Query("SELECT * FROM clients WHERE id = :id")
    Client getByIdSync(long id);
    @Query("SELECT * FROM clients ORDER BY full_name ASC")
    LiveData<List<Client>> getAll();
    @Query("SELECT * FROM clients WHERE is_active = 1 ORDER BY full_name ASC")
    PagingSource<Integer, Client> getAllActivePaged();
    @Query("SELECT * FROM clients WHERE is_active = 1 ORDER BY full_name ASC")
    List<Client> getAllActiveSync();
    @Query("SELECT * FROM clients ORDER BY full_name ASC")
    PagingSource<Integer, Client> getAllPaged();
    @Query("SELECT COUNT(*) FROM clients")
    LiveData<Integer> getCount();
    @Query("SELECT COUNT(*) FROM clients WHERE is_active = 1")
    LiveData<Integer> getActiveCount();

    @androidx.room.RawQuery(observedEntities = Client.class)
    PagingSource<Integer, Client> getFilteredClients(androidx.sqlite.db.SupportSQLiteQuery query);

    // Métricas simples
    @Query("SELECT COUNT(*) FROM sales WHERE client_id = :clientId")
    int getSalesCountForClient(long clientId);

    @Query("SELECT SUM(total) FROM sales WHERE client_id = :clientId")
    Double getTotalSalesForClient(long clientId);
}
