import java.util.UUID;

class Transaction {
    private final UUID id;
    private final UUID userId;
    private final String type;
    private final double amount;
    private final String currency;
    private final String accountNumber;

    public Transaction(UUID id, UUID userId, String type, double amount, String currency, String accountNumber) {
        this.id = id;
        this.userId = userId;
        this.type = type;
        this.amount = amount;
        this.currency = currency;
        this.accountNumber = accountNumber;
    }

    public UUID getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getType() {
        return type;
    }

    public double getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    public String getAccountNumber() {
        return accountNumber;
    }
}
