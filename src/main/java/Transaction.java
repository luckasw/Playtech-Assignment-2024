class Transaction {
    private final String id;
    private final String userId;
    private final String type;
    private final double amount;
    private final String method;
    private final String accountNumber;

    public Transaction(String id, String userId, String type, double amount, String method, String accountNumber) {
        this.id = id;
        this.userId = userId;
        this.type = type;
        this.amount = amount;
        this.method = method;
        this.accountNumber = accountNumber;
    }

    public String getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public String getType() {
        return type;
    }

    public double getAmount() {
        return amount;
    }

    public String getMethod() {
        return method;
    }

    public String getAccountNumber() {
        return accountNumber;
    }
}
