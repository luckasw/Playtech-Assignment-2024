package Objects;

public class Card {
    private final String cardNumber;
    private final String cardHolderId;
    private final String cardCountry;
    private boolean isValid;

    public Card(String cardNumber, String cardHolderId, String cardCountry, boolean isValid) {
        this.cardNumber = cardNumber;
        this.cardHolderId = cardHolderId;
        this.cardCountry = cardCountry;
        this.isValid = isValid;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public String getCardHolderId() {
        return cardHolderId;
    }

    public String getCardCountry() {
        return cardCountry;
    }

    public boolean isValid() {
        return isValid;
    }

    public void setCardValid() {
        isValid = true;
    }
    public void setCardInvalid() {
        isValid = false;
    }
}
