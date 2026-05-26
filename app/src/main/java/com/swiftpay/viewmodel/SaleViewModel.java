package com.swiftpay.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.paging.PagingData;
import com.swiftpay.data.entity.CartItem;
import com.swiftpay.data.entity.DiscountCode;
import com.swiftpay.data.entity.Product;
import com.swiftpay.data.entity.Sale;
import com.swiftpay.data.entity.SaleItem;
import com.swiftpay.data.repository.DiscountRepository;
import com.swiftpay.data.repository.ProductRepository;
import com.swiftpay.data.repository.SaleRepository;
import com.swiftpay.util.Constants;
import java.util.ArrayList;
import java.util.List;

public class SaleViewModel extends AndroidViewModel {

    private final SaleRepository repository;
    private final ProductRepository productRepository;
    private final DiscountRepository discountRepository;

    private final MutableLiveData<List<CartItem>> cartItems = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Double> subtotal = new MutableLiveData<>(0.0);
    private final MutableLiveData<Double> total = new MutableLiveData<>(0.0);
    private final MutableLiveData<DiscountCode> appliedDiscount = new MutableLiveData<>(null);
    private final MutableLiveData<Long> selectedClientId = new MutableLiveData<>(null);

    private final MutableLiveData<String> operationMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> operationSuccess = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    
    private final MutableLiveData<String> filterStatus = new MutableLiveData<>(null);
    private final MutableLiveData<String> filterQuery = new MutableLiveData<>(null);

    public SaleViewModel(@NonNull Application application) {
        super(application);
        repository = new SaleRepository(application);
        productRepository = new ProductRepository(application);
        discountRepository = new DiscountRepository(application);
    }

    public LiveData<List<Product>> getActiveProducts() {
        return productRepository.getActiveProducts();
    }

    public LiveData<List<CartItem>> getCartItems() { return cartItems; }
    public LiveData<Double> getSubtotal() { return subtotal; }
    public LiveData<Double> getTotal() { return total; }
    public LiveData<DiscountCode> getAppliedDiscount() { return appliedDiscount; }
    public LiveData<String> getOperationMessage() { return operationMessage; }
    public LiveData<Boolean> getOperationSuccess() { return operationSuccess; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    
    public void setSelectedClient(Long clientId) {
        this.selectedClientId.setValue(clientId);
    }
    
    public LiveData<Long> getSelectedClientId() { return selectedClientId; }

    private void recalculateTotals() {
        List<CartItem> items = cartItems.getValue();
        if (items == null) return;

        double sum = 0;
        for (CartItem item : items) {
            sum += item.getSubtotal();
        }
        subtotal.setValue(sum);

        DiscountCode discount = appliedDiscount.getValue();
        if (discount != null) {
            double discountAmount = sum * (discount.getDiscountPercentage() / 100.0);
            total.setValue(sum - discountAmount);
        } else {
            total.setValue(sum);
        }
    }

    public void addProductToCart(Product product, int quantity) {
        List<CartItem> items = cartItems.getValue();
        if (items == null) items = new ArrayList<>();

        for (CartItem item : items) {
            if (item.getProductId() == product.getId()) {
                item.setQuantity(item.getQuantity() + quantity);
                cartItems.setValue(items);
                recalculateTotals();
                return;
            }
        }

        items.add(new CartItem(product.getId(), product.getName(), product.getSku(), product.getPrice(), product.getPrice(), quantity));
        cartItems.setValue(items);
        recalculateTotals();
    }

    public void updateCartItemQuantity(long productId, int newQuantity) {
        List<CartItem> items = cartItems.getValue();
        if (items == null) return;
        
        if (newQuantity <= 0) {
            items.removeIf(item -> item.getProductId() == productId);
        } else {
            for (CartItem item : items) {
                if (item.getProductId() == productId) {
                    item.setQuantity(newQuantity);
                    break;
                }
            }
        }
        cartItems.setValue(items);
        recalculateTotals();
    }

    public boolean updateCartItemPrice(long productId, double newPrice) {
        List<CartItem> items = cartItems.getValue();
        if (items == null) return false;

        for (CartItem item : items) {
            if (item.getProductId() == productId) {
                if (newPrice < item.getCatalogPrice() * Constants.MIN_PRICE_PERCENTAGE) {
                    operationMessage.setValue("El precio no puede ser menor al 50% del catálogo");
                    return false;
                }
                item.setUnitPrice(newPrice);
                break;
            }
        }
        cartItems.setValue(items);
        recalculateTotals();
        return true;
    }

    public void clearCart() {
        cartItems.setValue(new ArrayList<>());
        appliedDiscount.setValue(null);
        selectedClientId.setValue(null);
        recalculateTotals();
    }

    public void applyDiscount(String code) {
        if (code == null || code.trim().isEmpty()) {
            appliedDiscount.setValue(null);
            recalculateTotals();
            operationMessage.setValue("Descuento eliminado");
            return;
        }

        String normalizedCode = code.trim().toUpperCase(java.util.Locale.ROOT);
        isLoading.setValue(true);
        discountRepository.validateDiscountCode(normalizedCode, (discount, error) -> {
            isLoading.postValue(false);
            if (error != null) {
                operationMessage.postValue(error);
            } else {
                appliedDiscount.postValue(discount);
                new android.os.Handler(android.os.Looper.getMainLooper()).post(this::recalculateTotals);
                operationMessage.postValue(
                        "Descuento aplicado: " + discount.getCode()
                                + " (" + discount.getDiscountPercentage() + "%)");
            }
        });
    }

    public void resetOperationSuccess() {
        operationSuccess.setValue(null);
        operationMessage.setValue(null);
    }

    public void processPayment(String paymentMethod, double amountReceived, long sellerId, Long cashRegisterId) {
        if (sellerId <= 0) {
            operationMessage.setValue("Sesión inválida. Vuelve a iniciar sesión.");
            return;
        }

        List<CartItem> items = cartItems.getValue();
        if (items == null || items.isEmpty()) {
            operationMessage.setValue("El carrito está vacío");
            return;
        }

        double currentTotal = total.getValue() != null ? total.getValue() : 0.0;
        double change = 0.0;

        if (Constants.PAYMENT_EFECTIVO.equals(paymentMethod)) {
            if (amountReceived < currentTotal) {
                operationMessage.setValue("El monto recibido es insuficiente");
                return;
            }
            change = amountReceived - currentTotal;
        } else {
            amountReceived = currentTotal;
        }

        Sale sale = new Sale();
        sale.setClientId(selectedClientId.getValue());
        sale.setSellerId(sellerId);
        sale.setCashRegisterId(cashRegisterId);
        Double subtotalValue = subtotal.getValue();
        sale.setSubtotal(subtotalValue != null ? subtotalValue : currentTotal);
        
        DiscountCode discount = appliedDiscount.getValue();
        if (discount != null) {
            sale.setDiscountPercentage(discount.getDiscountPercentage());
            sale.setDiscountCodeId(discount.getId());
        } else {
            sale.setDiscountPercentage(0.0);
        }
        
        sale.setTotal(currentTotal);
        sale.setPaymentMethod(paymentMethod);
        sale.setAmountReceived(amountReceived);
        sale.setChangeAmount(change);
        sale.setStatus("PAGADA");

        List<SaleItem> saleItems = new ArrayList<>();
        for (CartItem c : items) {
            SaleItem si = new SaleItem();
            si.setProductId(c.getProductId());
            si.setQuantity(c.getQuantity());
            si.setUnitPrice(c.getUnitPrice());
            si.setCatalogPrice(c.getCatalogPrice());
            saleItems.add(si);
        }

        isLoading.setValue(true);
        repository.createSale(sale, saleItems, (success, msg, saleId) -> {
            isLoading.setValue(false);
            if (success) {
                clearCart();
                operationSuccess.setValue(true);
            } else {
                operationMessage.setValue(msg);
            }
        });
    }

    public void setFilters(String status, String query) {
        filterStatus.setValue(status);
        filterQuery.setValue(query);
    }

    public LiveData<PagingData<Sale>> getFilteredSalesPaged() {
        return repository.getFilteredSales(filterQuery.getValue(), filterStatus.getValue());
    }

    public void updateStatus(long saleId, String newStatus, long changedByUserId) {
        isLoading.setValue(true);
        repository.updateStatus(saleId, newStatus, changedByUserId, (success, msg, id) -> {
            isLoading.setValue(false);
            if (success) {
                operationSuccess.setValue(true);
            } else {
                operationMessage.setValue(msg);
            }
        });
    }

    public LiveData<Sale> getSaleById(long id) {
        return repository.getSaleById(id);
    }

    public LiveData<java.util.List<com.swiftpay.data.entity.SaleItemWithProduct>> getSaleItems(long saleId) {
        return repository.getSaleItems(saleId);
    }

    public LiveData<java.util.List<com.swiftpay.data.entity.SaleStatusHistory>> getSaleStatusHistory(long saleId) {
        return repository.getSaleStatusHistory(saleId);
    }
}
