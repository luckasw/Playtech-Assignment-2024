import Objects.*;

import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


// This template shows input parameters format.
// It is otherwise not mandatory to use, you can write everything from scratch if you wish.
public class TransactionProcessor {

    public static void main(final String[] args) throws IOException {
        List<String> usedTransactionIds = new ArrayList<>();

        final List<User> users = Reader.readUsers(Paths.get(args[0]));
        final List<Transaction> transactions = Reader.readTransactions(Paths.get(args[1]));
        final List<BinMapping> binMappings = Reader.readBinMappings(Paths.get(args[2]));

        List<Event> events = TransactionProcessor.processTransactions(users, transactions, binMappings, usedTransactionIds);

        TransactionProcessor.writeBalances(Paths.get(args[3]), users);
        TransactionProcessor.writeEvents(Paths.get(args[4]), events);
    }


    private static List<Event> processTransactions(final List<User> users, final List<Transaction> transactions, final List<BinMapping> binMappings, final List<String> usedTransactionIds) {
        List<Event> events = new ArrayList<>();
        List<Card> cards = new ArrayList<>();
        List<Account> accounts = new ArrayList<>();
        for (Transaction transaction : transactions) {
            Event event = new Event();
            event.transactionId = transaction.getId();
            event.status = Event.STATUS_DECLINED;
            User user = findUserById(users, transaction.getUserId());
            if (user == null) {
                event.message = "Objects.User " + transaction.getUserId() + " not found";
                events.add(event);
                continue;
            }
            if (usedTransactionIds.contains(transaction.getId())) {
                event.message = "Objects.Transaction " + transaction.getId() + " already processed (id non-unique)";
                events.add(event);
                continue;
            }
            usedTransactionIds.add(transaction.getId());
            if (user.isFrozen()) {
                event.message = "Objects.User " + transaction.getUserId() + " is frozen";
                events.add(event);
                continue;
            }
            String type = transaction.getType();
            String method = transaction.getMethod();

            if (type.equals("DEPOSIT")) {
                if (isWithinDepositRange(transaction, user, event, events)) continue;
                if (method.equals("CARD")) {
                    Card card = getCard(transaction.getAccountNumber(), cards);
                    if (card == null) {
                        BinMapping binMapping = findBinMapping(transaction.getAccountNumber(), binMappings);
                        if (binMapping == null) {
                            event.message = "Invalid card number";
                            events.add(event);
                            user.freeze();
                            continue;
                        }
                        if (!binMapping.getCountry().equals(user.getCountry())) {
                            event.message = "Invalid country " + binMapping.getCountry() + "; expected " + user.getCountry();
                            events.add(event);
                            continue;
                        }
                        if (!binMapping.getType().equals("DC")) {
                            event.message = "Only DC cards allowed; got " + binMapping.getType();
                            events.add(event);
                            continue;
                        }
                        depositSuccess(transaction, user, event, events);
                        card = new Card(transaction.getAccountNumber(), user.getId(), binMapping.getCountry(), true);
                        cards.add(card);
                        continue;
                    }
                    else if (!card.getCardHolderId().equals(user.getId())) {
                        event.message = "Objects.Card " + transaction.getAccountNumber() + " is in use by other user";
                        events.add(event);
                        user.freeze();
                        continue;
                    }
                    depositSuccess(transaction, user, event, events);
                } else if (method.equals("TRANSFER")) {
                    Account account = getAccount(transaction.getAccountNumber(), accounts);
                    if (account == null) {
                        if (isCheckDigitValid(transaction.getAccountNumber())) {
                            if (checkAccountNumberCountry(transaction, user, event, events)) continue;
                            depositSuccess(transaction, user, event, events);
                            accounts.add(new Account(transaction.getAccountNumber(), user.getId(), user.getCountry(), true));
                            continue;
                        }
                    } else if (!account.getAccuntHolderId().equals(user.getId())) {
                        event.message = "Objects.Account " + transaction.getAccountNumber() + " is in use by other user";
                        events.add(event);
                        user.freeze();
                        continue;
                    }
                    if (!isCheckDigitValid(transaction.getAccountNumber())) {
                        event.message = "Invalid iban " + transaction.getAccountNumber();
                        events.add(event);
                        continue;
                    }
                    if (checkAccountNumberCountry(transaction, user, event, events)) continue;
                    depositSuccess(transaction, user, event, events);
                }
            }
            else if (type.equals("WITHDRAW")) {
                if (isWithinWithdrawRange(transaction, user, event, events)) continue;
                if (method.equals("CARD")) {
                    Card card = getCard(transaction.getAccountNumber(), cards);
                    if (card == null) {
                        event.message = "Cannot withdraw with a new account " + transaction.getAccountNumber();
                        events.add(event);
                        continue;
                    }
                    BinMapping binMapping = findBinMapping(transaction.getAccountNumber(), binMappings);
                    if (binMapping == null) {
                        event.message = "Invalid card number";
                        events.add(event);
                        user.freeze();
                        continue;
                    }
                    if (!binMapping.getType().equals("DC")) {
                        event.message = "Only DC cards allowed; got " + binMapping.getType();
                        events.add(event);
                        continue;
                    }
                    if (!binMapping.getCountry().equals(user.getCountry())) {
                        event.message = "Invalid country " + binMapping.getCountry() + "; expected " + user.getCountry();
                        events.add(event);
                        continue;
                    }
                    if (!card.getCardHolderId().equals(user.getId())) {
                        event.message = "Objects.Card " + transaction.getAccountNumber() + " is in use by other user";
                        events.add(event);
                        user.freeze();
                        continue;
                    }
                    if (transaction.getAmount() > user.getBalance()) {
                        event.message = "Not enough balance to withdraw " + transaction.getAmount() + " - balance is too low at " + user.getBalance();
                        events.add(event);
                        continue;
                    }
                    withdrawSuccess(transaction, user, event, events);
                }
                else if (method.equals("TRANSFER")) {
                    Account account = getAccount(transaction.getAccountNumber(), accounts);
                    if (account == null) {
                        event.message = "Cannot withdraw with a new account " + transaction.getAccountNumber();
                        events.add(event);
                        continue;
                    }
                    if (!account.getAccuntHolderId().equals(user.getId())) {
                        event.message = "Objects.Account " + transaction.getAccountNumber() + " is in use by other user";
                        events.add(event);
                        user.freeze();
                        continue;
                    }
                    if (!isCheckDigitValid(transaction.getAccountNumber())) {
                        event.message = "Invalid iban " + transaction.getAccountNumber();
                        events.add(event);
                        continue;
                    }
                    if (transaction.getAmount() > user.getBalance()) {
                        event.message = "Not enough balance to withdraw " + transaction.getAmount() + " - balance is too low at " + user.getBalance();
                        events.add(event);
                        continue;
                    }
                    withdrawSuccess(transaction, user, event, events);

                }
            }
            else {
                event.message = "Invalid transaction type " + type;
                events.add(event);
                user.freeze();
            }
        }
        return events;
    }

    private static boolean checkAccountNumberCountry(Transaction transaction, User user, Event event, List<Event> events) {
        String countryCode = transaction.getAccountNumber().substring(0, 2);
        Locale locale = new Locale("en",countryCode);
        String country = locale.getISO3Country();
        if (!country.equals(user.getCountry())) {
            event.message = "Invalid country " + country + "; expected " + user.getCountry();
            events.add(event);
            return true;
        }
        return false;
    }

    private static void withdrawSuccess(Transaction transaction, User user, Event event, List<Event> events) {
        user.withdraw(transaction.getAmount());
        event.status = Event.STATUS_APPROVED;
        event.message = "OK";
        events.add(event);
    }

    private static Account getAccount(String accountNumber, List<Account> accounts) {
        for (Account account : accounts) {
            if (account.getAccountNumber().equals(accountNumber)) {
                return account;
            }
        }
        return null;
    }

    private static void depositSuccess(Transaction transaction, User user, Event event, List<Event> events) {
        user.deposit(transaction.getAmount());
        event.status = Event.STATUS_APPROVED;
        event.message = "OK";
        events.add(event);
    }

    private static Card getCard(String accountNumber, List<Card> cards) {
        for (Card card : cards) {
            if (card.getCardNumber().equals(accountNumber)) {
                return card;
            }
        }
        return null;
    }

    private static boolean isCheckDigitValid(String iban) {
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

    private static BinMapping findBinMapping(String accountNumber, List<BinMapping> binMappings) {
        try {
            Long accountNumberLong = Long.valueOf(accountNumber.substring(0, 10));
//            accountNumberLong = accountNumberLong / 100000000;
            for (BinMapping binMapping : binMappings) {
                if (binMapping.getRangeFrom() <= accountNumberLong && accountNumberLong <= binMapping.getRangeTo()) {
                    return binMapping;
                }
            }
        } catch (NumberFormatException e) {
            // ToDo Implementation
        }
        return null;
    }

    private static boolean isWithinDepositRange(Transaction transaction, User user, Event event, List<Event> events) {
        if (transaction.getAmount() < user.getDepositMin()) {
            event.message = "Amount " + transaction.getAmount() + " is under the deposit limit of " + user.getDepositMin();
            events.add(event);
            return true;
        }
        else if (transaction.getAmount() > user.getDepositMax()) {
            event.message = "Amount " + transaction.getAmount() + " is over the deposit limit of " + user.getDepositMax();
            events.add(event);
            return true;
        }
        return false;
    }

    private static boolean isWithinWithdrawRange(Transaction transaction, User user, Event event, List<Event> events) {
        if (transaction.getAmount() < user.getWithdrawMin()) {
            event.message = "Amount " + transaction.getAmount() + " is under the withdraw limit of " + user.getWithdrawMin();
            events.add(event);
            return true;
        }
        else if (transaction.getAmount() > user.getWithdrawMax()) {
            event.message = "Amount " + transaction.getAmount() + " is over the withdraw limit of " + user.getWithdrawMax();
            events.add(event);
            return true;
        }
        return false;
    }

    private static void writeBalances(final Path filePath, final List<User> users) throws IOException {
        try (final FileWriter writer = new FileWriter(filePath.toFile(), false)) {
            writer.append("user_id,balance\n");
            for (final var user : users) {
                writer.append(user.getId()).append(",").append(String.valueOf(user.getBalance())).append("\n");
            }
        }
    }

    private static void writeEvents(final Path filePath, final List<Event> events) throws IOException {
        try (final FileWriter writer = new FileWriter(filePath.toFile(), false)) {
            writer.append("transaction_id,status,message\n");
            for (final var event : events) {
                writer.append(event.transactionId).append(",").append(event.status).append(",").append(event.message).append("\n");
            }
        }
    }

    private static User findUserById(final List<User> users, final String userId) {
        for (final var user : users) {
            if (user.getId().equals(userId)) {
                return user;
            }
        }
        return null;
    }

}


