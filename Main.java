import java.util.Objects;

// ===== Productos (familia) =====
interface PaymentProcessor {
    String pay(String orderId, double amount);
}

interface ReceiptGenerator {
    String receipt(String transactionId, double amount);
}

// ===== Abstract Factory =====
interface PaymentGatewayFactory {
    PaymentProcessor createPaymentProcessor();
    ReceiptGenerator createReceiptGenerator();
}

// ===== Implementación Nequi =====
class NequiFactory implements PaymentGatewayFactory {
    public PaymentProcessor createPaymentProcessor() {
        return new NequiPayment();
    }
    public ReceiptGenerator createReceiptGenerator() {
        return new NequiReceipt();
    }
}

class NequiPayment implements PaymentProcessor {
    public String pay(String orderId, double amount) {
        // Regla demo: Nequi rechaza montos muy altos (simulación)
        if (amount <= 0 || amount > 2000000) return null;
        return "NQ-" + orderId; // id transacción simple
    }
}

class NequiReceipt implements ReceiptGenerator {
    public String receipt(String transactionId, double amount) {
        return "RECIBO NEQUI | TX=" + transactionId + " | $" + amount;
    }
}

// ===== Implementación Bancolombia =====
class BancolombiaFactory implements PaymentGatewayFactory {
    public PaymentProcessor createPaymentProcessor() {
        return new BancolombiaPayment();
    }
    public ReceiptGenerator createReceiptGenerator() {
        return new BancolombiaReceipt();
    }
}

class BancolombiaPayment implements PaymentProcessor {
    public String pay(String orderId, double amount) {
        // Regla demo: Bancolombia exige mínimo 5.000 (simulación)
        if (amount < 5000) return null;
        return "BC-" + orderId;
    }
}

class BancolombiaReceipt implements ReceiptGenerator {
    public String receipt(String transactionId, double amount) {
        return "RECIBO BANCOLOMBIA | TX=" + transactionId + " | $" + amount;
    }
}

// ===== Cliente (usa la fábrica sin saber cuál proveedor es) =====
class CheckoutService {
    private final PaymentProcessor payment;
    private final ReceiptGenerator receipt;

    CheckoutService(PaymentGatewayFactory factory) {
        Objects.requireNonNull(factory);
        this.payment = factory.createPaymentProcessor();
        this.receipt = factory.createReceiptGenerator();
    }

    public void checkout(String orderId, double amount) {
        String tx = payment.pay(orderId, amount);

        if (tx == null) {
            System.out.println("Pago rechazado | order=" + orderId + " | amount=" + amount);
            return;
        }

        System.out.println("Pago aprobado | TX=" + tx);
        System.out.println(receipt.receipt(tx, amount));
        System.out.println();
    }
}

// ===== Main =====
public class Main {
    public static void main(String[] args) {

        // Cambia SOLO la fábrica para cambiar el proveedor
        PaymentGatewayFactory factory = new NequiFactory();
        // PaymentGatewayFactory factory = new BancolombiaFactory();

        CheckoutService service = new CheckoutService(factory);

        service.checkout("ORD-01", 3000);       // Nequi aprueba, Bancolombia rechaza
        service.checkout("ORD-02", 15000);      // ambos podrían aprobar (según fábrica)
        service.checkout("ORD-03", 2500000);    // Nequi rechaza por regla demo
    }
}