package Objects;

public class Account {
    private final String accountNumber;
    private final String accuntHolderId;
    private final String accountCountry;
    private boolean isValid;

    public Account(String accountNumber, String accuntHolderId, String accountCountry, boolean isValid) {
        this.accountNumber = accountNumber;
        this.accuntHolderId = accuntHolderId;
        this.accountCountry = accountCountry;
        this.isValid = isValid;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public String getAccuntHolderId() {
        return accuntHolderId;
    }

    public String getAccountCountry() {
        return accountCountry;
    }

    public boolean isValid() {
        return isValid;
    }

    public void setAccountValid() {
        isValid = true;
    }

    public void setAccountInvalid() {
        isValid = false;
    }
}
