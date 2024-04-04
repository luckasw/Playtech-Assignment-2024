import Objects.*;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TransactionHandler {
    final private List<User> users;
    final private List<BinMapping> binMappings;
    final private List<Event> events = new ArrayList<>();
    final private List<Card> cards = new ArrayList<>();
    final private List<Account> accounts = new ArrayList<>();
    final private List<String> usedTransactions = new ArrayList<>();

    public TransactionHandler(List<User> users, List<BinMapping> binMappings) {
        this.users = users;
        this.binMappings = binMappings;
    }

    public void processTransaction(Transaction transaction) {
        Event event = new Event();
        event.transactionId = transaction.getId();
        event.status = Event.STATUS_DECLINED;
        User user = getUser(transaction.getUserId());
        if (checkUser(user, event, transaction)) {
            if (checkTransaction(transaction, event)) {
                handleTransaction(transaction, user, event);
            }

        }
    }

    private void handleTransaction(Transaction transaction, User user, Event event) {
        if (transaction.getType().equals("DEPOSIT")) {
            handleDeposit(transaction, user, event);
        } else if (transaction.getType().equals("WITHDRAW")) {
            handleWithdraw(transaction, user, event);
        }
    }

    private void handleWithdraw(Transaction transaction, User user, Event event) {
        if (transaction.getMethod().equals("CARD")) {
            handleCardAction(transaction, user, event, false);
        } else if (transaction.getMethod().equals("TRANSFER")) {
            handleAccountAction(transaction, user, event, false);
        }
    }

    private void handleDeposit(Transaction transaction, User user, Event event) {
        if (transaction.getMethod().equals("CARD")) {
            handleCardAction(transaction, user, event, true);
        } else if (transaction.getMethod().equals("TRANSFER")) {
            handleAccountAction(transaction, user, event, true);
        }
    }

    private void handleAccountAction(Transaction transaction, User user, Event event, boolean deposit) {
        Account account = getAccount(transaction.getAccountNumber());
        if (account == null) {
            if (!deposit) {
                event.message = "Cannot withdraw with a new account " + transaction.getAccountNumber();
                events.add(event);
                return;
            }
            if (checkNewAccount(transaction, user, event)) {
                if (deposit && isWithinDepositRange(user, transaction, event)) {
                    depositSuccess(transaction, user, event);
                    accounts.add(new Account(transaction.getAccountNumber(), transaction.getUserId(), user.getCountry(), true));
                }
            }
        } else if (checkAccount(account, event, transaction)) {
            if (deposit && isWithinDepositRange(user, transaction, event)) {
                depositSuccess(transaction, user, event);
            } else if (!deposit && isWithinWithdrawRange(user, transaction, event)){
                withdrawSuccess(transaction, user, event);
            }
        }

    }

    private void handleCardAction(Transaction transaction, User user, Event event, boolean deposit) {
        Card card = getCard(transaction.getAccountNumber());
        if (card == null) {
            if (!deposit) {
                event.message = "Cannot withdraw with a new account " + transaction.getAccountNumber();
                events.add(event);
                return;
            }
            if (checkNewCard(transaction, user, event)) {
                if (deposit && isWithinDepositRange(user, transaction, event)) {
                    depositSuccess(transaction, user, event);
                    cards.add(new Card(transaction.getAccountNumber(), transaction.getUserId(),
                            transaction.getAccountNumber().substring(0, 2), true));
                }
            }
        } else if (checkCard(card, event, transaction)) {
            if (deposit && isWithinDepositRange(user, transaction, event)) {
                depositSuccess(transaction, user, event);
            } else if (!deposit && isWithinWithdrawRange(user, transaction, event)) {
                withdrawSuccess(transaction, user, event);
            }
        }
    }

    private boolean checkNewAccount(Transaction transaction, User user, Event event) {
        if (isCheckDigitValid(transaction.getAccountNumber())) {
            if (checkAccountCountry(transaction, user, event)) {
                return true;
            } else {
                event.message = "Invalid account country " +
                        transaction.getAccountNumber().substring(0, 2) + "; expected " + user.getCountry().substring(0, 2);
                events.add(event);
                return false;
            }
        }
        event.message = "Invalid iban " + transaction.getAccountNumber();
        events.add(event);
        return false;
    }

    private boolean checkAccountCountry(Transaction transaction, User user, Event event) {
        String countryCode = transaction.getAccountNumber().substring(0, 2);
        Locale locale = new Locale("en",countryCode);
        String country = locale.getISO3Country();
        if (!country.equals(user.getCountry())) {
            return false;
        }
        return true;
    }

    private boolean isCheckDigitValid(String iban) {
        iban = iban.substring(4) + iban.substring(0, 4);
        String total = "";
        for (int i = 0; i < iban.length(); i++) {
            int value = Character.getNumericValue(iban.charAt(i));
            if (value < 0 || value > 35) {
                return false;
            }
            total += value;
        }

        BigInteger bigInt = new BigInteger(total);
        return bigInt.mod(new BigInteger("97")).intValue() == 1;
    }

    private boolean checkAccount(Account account, Event event, Transaction transaction) {
        if (account.getAccuntHolderId().equals(transaction.getUserId())) {
            return true;
        }
        event.message = "Account " + transaction.getAccountNumber() + " is in use by other user";
        events.add(event);

        return false;
    }

    private void withdrawSuccess(Transaction transaction, User user, Event event) {
        user.withdraw(transaction.getAmount());
        event.status = Event.STATUS_APPROVED;
        event.message = "OK";
        events.add(event);
    }

    private void depositSuccess(Transaction transaction, User user, Event event) {
        user.deposit(transaction.getAmount());
        event.status = Event.STATUS_APPROVED;
        event.message = "OK";
        events.add(event);
    }

    private boolean checkNewCard(Transaction transaction, User user, Event event) {
        BinMapping binMapping = getBinMapping(transaction.getAccountNumber());
        if (binMapping == null) {
            event.message = "Invalid card number";
            events.add(event);
            user.freeze();
            return false;
        }
        if (!binMapping.getCountry().equals(user.getCountry())) {
            event.message = "Invalid country " + binMapping.getCountry() + "; expected " + user.getCountry().substring(0, 2)
                    + " (" + user.getCountry() + ")";
            events.add(event);
            return false;
        }
        if (!binMapping.getType().equals("DC")) {
            event.message = "Only DC cards allowed; got " + binMapping.getType();
            events.add(event);
            return false;
        }

        return true;
    }


    private BinMapping getBinMapping(String accountNumber) {
        try {
            Long accountNumberLong = Long.valueOf(accountNumber.substring(0, 10));
            for (BinMapping binMapping : binMappings) {
                if (binMapping.getRangeFrom() <= accountNumberLong && accountNumberLong <= binMapping.getRangeTo()) {
                    return binMapping;
                }
            }
        } catch (NumberFormatException e) {
            return null;
        }
        return null;
    }

    private boolean checkCard(Card card, Event event, Transaction transaction) {
        if (card.getCardHolderId().equals(transaction.getUserId())) {
            return true;
        }
        event.message = "Account " + transaction.getAccountNumber() + " is in use by other user";
        events.add(event);

        return false;
    }

    private Card getCard(String accountNumber) {
        for (Card card : cards) {
            if (card.getCardNumber().equals(accountNumber)) {
                return card;
            }
        }
        return null;
    }

    private Account getAccount(String accountNumber) {
        for (Account account : accounts) {
            if (account.getAccountNumber().equals(accountNumber)) {
                return account;
            }
        }
        return null;
    }

    private boolean isWithinDepositRange(User user, Transaction transaction, Event event) {
        if (transaction.getAmount() < user.getDepositMin()) {
            event.message = "Amount " + transaction.getAmount() + " is under the deposit limit of " + user.getDepositMin();
            events.add(event);
            return false;
        } else if (transaction.getAmount() > user.getDepositMax()) {
            event.message = "Amount " + transaction.getAmount() + " is over the deposit limit of " + user.getDepositMax();
            events.add(event);
            return false;
        }

        return true;
    }

    private boolean isWithinWithdrawRange(User user, Transaction transaction, Event event) {
        if (transaction.getAmount() < user.getWithdrawMin()) {
            event.message = "Amount " + transaction.getAmount() + " is under the withdraw limit of " + user.getWithdrawMin();
            events.add(event);
            return false;
        }
        else if (transaction.getAmount() > user.getWithdrawMax()) {
            event.message = "Amount " + transaction.getAmount() + " is over the withdraw limit of " + user.getWithdrawMax();
            events.add(event);
            return false;
        } else if (transaction.getAmount() > user.getBalance()) {
            event.message = "Not enough balance to withdraw " + transaction.getAmount() + " - balance is too low at " + user.getBalance();
            events.add(event);
            return false;
        }

        return true;
    }

    private boolean checkTransaction(Transaction transaction, Event event) {
        if (usedTransactions.contains(transaction.getId())) {
            event.message = "Transaction " + transaction.getId() + " already processed (id non-unique)";
            events.add(event);
            return false;
        }
        if (transaction.getAmount() <= 0) {
            event.message = "Invalid amount " + transaction.getAmount();
            events.add(event);
            usedTransactions.add(transaction.getId());
            return false;
        }
        usedTransactions.add(transaction.getId());
        return true;
    }

    private boolean checkUser(User user, Event event, Transaction transaction) {
        if (user == null) {
            event.message = "User " + transaction.getUserId() + " not found in Users";
            events.add(event);
            return false;
        } else if (user.isFrozen()) {
            event.message = "User " + transaction.getUserId() + " is frozen";
            events.add(event);
            return false;
        }
        return true;
    }

    private User getUser(String userId) {
        for (User user : users) {
            if (user.getId().equals(userId)) {
                return user;
            }
        }
        return null;
    }

    public List<User> getUsers() {
        return users;
    }

    public List<Event> getEvents() {
        return events;
    }
}
