import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


// This template shows input parameters format.
// It is otherwise not mandatory to use, you can write everything from scratch if you wish.
public class TransactionProcessorSample {

    public static void main(final String[] args) throws IOException {
        List<String> usedTransactionIds = new ArrayList<>();

        List<User> users = TransactionProcessorSample.readUsers(Paths.get(args[0]));
        List<Transaction> transactions = TransactionProcessorSample.readTransactions(Paths.get(args[1]));
        List<BinMapping> binMappings = TransactionProcessorSample.readBinMappings(Paths.get(args[2]));

        List<Event> events = TransactionProcessorSample.processTransactions(users, transactions, binMappings, usedTransactionIds);

        TransactionProcessorSample.writeBalances(Paths.get(args[3]), users);
        TransactionProcessorSample.writeEvents(Paths.get(args[4]), events);
    }

    private static List<User> readUsers(final Path filePath) throws IOException {
        List<String[]> lines = readCsvFile(filePath);
        List<User> users = new ArrayList<>();
        for (int i = 1; i < lines.size(); i++) {
            String id = lines.get(i)[0];
            String name = lines.get(i)[1];
            double balance = Double.parseDouble(lines.get(i)[2]);
            String countryCode = lines.get(i)[3];
            Locale locale = new Locale("en",countryCode);
            String country = locale.getISO3Country();
            boolean frozen = Boolean.parseBoolean(lines.get(i)[4]);
            double depositMin = Double.parseDouble(lines.get(i)[5]);
            double depositMax = Double.parseDouble(lines.get(i)[6]);
            double withdrawMin = Double.parseDouble(lines.get(i)[7]);
            double withdrawMax = Double.parseDouble(lines.get(i)[8]);
            users.add(new User(id, name, balance, country, frozen, depositMin, depositMax, withdrawMin, withdrawMax));
        }
        return users;
    }

    private static List<Transaction> readTransactions(final Path filePath) throws IOException {
        List<String[]> lines = readCsvFile(filePath);
        List<Transaction> transactions = new ArrayList<>();
        for (int i = 1; i < lines.size(); i++) {
            String id = lines.get(i)[0];
            String userId = lines.get(i)[1];
            String type = lines.get(i)[2];
            double amount = Double.parseDouble(lines.get(i)[3]);
            String method = lines.get(i)[4];
            String accountNumber = lines.get(i)[5];
            transactions.add(new Transaction(id, userId, type, amount, method, accountNumber));
        }
        return transactions;
    }

    private static List<BinMapping> readBinMappings(final Path filePath) throws IOException {
        List<String[]> lines = readCsvFile(filePath);
        List<BinMapping> binMappings = new ArrayList<>();
        for (int i = 1; i < lines.size(); i++) {
            String name = lines.get(i)[0];
            Long rangeFrom = Long.valueOf(lines.get(i)[1]);
            Long rangeTo = Long.valueOf(lines.get(i)[2]);
            String type = lines.get(i)[3];
            String country = lines.get(i)[4];
            binMappings.add(new BinMapping(name, rangeFrom, rangeTo, type, country));
        }
        return binMappings;
    }

    private static List<Event> processTransactions(final List<User> users, final List<Transaction> transactions, final List<BinMapping> binMappings, final List<String> usedTransactionIds) {
        List<Event> events = new ArrayList<>();
        for (Transaction transaction : transactions) {
            Event event = new Event();
            event.transactionId = transaction.getId();
            event.status = Event.STATUS_DECLINED;
            if (usedTransactionIds.contains(transaction.getId())) {
                event.message = "Transaction " + transaction.getId() + " already processed (id non-unique)";
                events.add(event);
                continue;
            }
            User user = findUserById(users, transaction.getUserId());
            if (user == null) {
                event.message = "User " + transaction.getUserId() + " not found";
                events.add(event);
                continue;
            }
            if (user.isFrozen()) {
                event.message = "User " + transaction.getUserId() + " is frozen";
                events.add(event);
                continue;
            }
            String type = transaction.getType();
            String method = transaction.getMethod();

            if (type.equals("DEPOSIT")) {
                if (isWithinDepositRange(transaction, user, event, events)) continue;
                if (method.equals("CARD")) {
                    if (!iscardTypeDC(transaction.getAccountNumber(), binMappings, user)) {
                        event.message = "Only DC cards allowed";
                        events.add(event);
                        continue;
                    }
                    user.deposit(transaction.getAmount());
                    usedTransactionIds.add(transaction.getId());
                    event.status = Event.STATUS_APPROVED;
                    event.message = "OK";
                    events.add(event);
                } else if (method.equals("TRANSFER")) {
                    // ToDo Implementation
                }
            }
            else if (type.equals("WITHDRAW")) {
                if (isWithinWithdrawRange(transaction, user, event, events)) continue;
                if (method.equals("CARD")) {
                    // ToDo Implementation
                }
                else if (method.equals("TRANSFER")) {
                    // ToDo Implementation
                }
            }
        }
        return events;
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

    private static boolean iscardTypeDC(String accountNumber, List<BinMapping> binMappings, User user) {
        Long accountNumberLong = Long.valueOf(accountNumber);
        accountNumberLong = accountNumberLong / 100000000;
        for (BinMapping binMapping : binMappings) {
            if (binMapping.getRangeFrom() <= accountNumberLong && accountNumberLong <= binMapping.getRangeTo()) {
                return binMapping.getType().equals("DC") && binMapping.getCountry().equals(user.getCountry());
            }
        }
        return false;
    }

    private static void writeBalances(final Path filePath, final List<User> users) {
        // ToDo Implementation
    }

    private static void writeEvents(final Path filePath, final List<Event> events) throws IOException {
        try (final FileWriter writer = new FileWriter(filePath.toFile(), false)) {
            writer.append("transaction_id,status,message\n");
            for (final var event : events) {
                writer.append(event.transactionId).append(",").append(event.status).append(",").append(event.message).append("\n");
            }
        }
    }

    private static List<String[]> readCsvFile(Path filePath) throws IOException {
        List<String[]> lines = new ArrayList<>();

        BufferedReader br = new BufferedReader(new FileReader(filePath.toFile()));
        String line;
        while ((line = br.readLine()) != null) {
            lines.add(line.split(","));
        }
        return lines;
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


class Event {
    public static final String STATUS_DECLINED = "DECLINED";
    public static final String STATUS_APPROVED = "APPROVED";

    public String transactionId;
    public String status;
    public String message;
}
