package com.swiftpay.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.paging.PagingData;
import com.swiftpay.data.entity.Client;
import com.swiftpay.data.repository.ClientRepository;

/**
 * ViewModel para Clientes.
 */
public class ClientViewModel extends AndroidViewModel {

    private final ClientRepository repository;
    private final MutableLiveData<String> operationMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> operationSuccess = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    
    private final MutableLiveData<String> searchQuery = new MutableLiveData<>("");
    private final MutableLiveData<Long> filterCategoryId = new MutableLiveData<>(0L);
    private final MutableLiveData<Integer> filterIsActive = new MutableLiveData<>(null);

    public ClientViewModel(@NonNull Application application) {
        super(application);
        repository = new ClientRepository(application);
    }

    public LiveData<String> getOperationMessage() { return operationMessage; }
    public LiveData<Boolean> getOperationSuccess() { return operationSuccess; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }

    public void setFilters(String query, Long categoryId, Integer isActive) {
        searchQuery.setValue(query);
        filterCategoryId.setValue(categoryId);
        filterIsActive.setValue(isActive);
    }

    public LiveData<PagingData<Client>> getFilteredClientsPaged() {
        return repository.getFilteredClients(
                searchQuery.getValue(), 
                filterCategoryId.getValue(), 
                filterIsActive.getValue()
        );
    }

    public LiveData<Client> getClientById(long id) {
        return repository.getClientById(id);
    }

    public void saveClient(Client client, long userId) {
        isLoading.setValue(true);
        repository.saveClient(client, userId, (success, message) -> {
            isLoading.postValue(false);
            operationSuccess.postValue(success);
            operationMessage.postValue(message);
        });
    }

    public void deleteOrDeactivateClient(Client client, long adminUserId) {
        isLoading.setValue(true);
        repository.deleteOrDeactivateClient(client, adminUserId, (success, message) -> {
            isLoading.postValue(false);
            operationSuccess.postValue(success);
            operationMessage.postValue(message);
        });
    }

    public void getClientStats(long clientId, ClientRepository.ClientStatsCallback callback) {
        repository.getClientStats(clientId, callback);
    }

    public void fetchAllActiveClients(ClientRepository.GetClientsCallback callback) {
        repository.getAllActiveClientsSync(clients -> {
            new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> callback.onResult(clients));
        });
    }
}
