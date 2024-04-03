import java.util.UUID;

class User {
    private final UUID id;
    private String name;
    private double balance;
    private String country;
    private boolean frozen;
    private double depositMin;
    private double depositMax;
    private double withdrawMin;
    private double withdrawMax;

    public User(UUID id, String name, double balance, String country, boolean frozen, double depositMin, double depositMax, double withdrawMin, double withdrawMax) {
        this.id = id;
        this.name = name;
        this.balance = balance;
        this.country = country;
        this.frozen = frozen;
        this.depositMin = depositMin;
        this.depositMax = depositMax;
        this.withdrawMin = withdrawMin;
        this.withdrawMax = withdrawMax;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public boolean isFrozen() {
        return frozen;
    }

    public void setFrozen(boolean frozen) {
        this.frozen = frozen;
    }

    public double getDepositMin() {
        return depositMin;
    }

    public void setDepositMin(double depositMin) {
        this.depositMin = depositMin;
    }

    public double getDepositMax() {
        return depositMax;
    }

    public void setDepositMax(double depositMax) {
        this.depositMax = depositMax;
    }

    public double getWithdrawMin() {
        return withdrawMin;
    }

    public void setWithdrawMin(double withdrawMin) {
        this.withdrawMin = withdrawMin;
    }

    public double getWithdrawMax() {
        return withdrawMax;
    }

    public void setWithdrawMax(double withdrawMax) {
        this.withdrawMax = withdrawMax;
    }
}