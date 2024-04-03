import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;


// This template shows input parameters format.
// It is otherwise not mandatory to use, you can write everything from scratch if you wish.
public class TransactionProcessorSample {

    public static void main(final String[] args) throws IOException {
        List<User> users = TransactionProcessorSample.readUsers(Paths.get(args[0]));
        List<Transaction> transactions = TransactionProcessorSample.readTransactions(Paths.get(args[1]));
        List<BinMapping> binMappings = TransactionProcessorSample.readBinMappings(Paths.get(args[2]));

        List<Event> events = TransactionProcessorSample.processTransactions(users, transactions, binMappings);

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
            String country = lines.get(i)[3];
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

    private static List<BinMapping> readBinMappings(final Path filePath) {
        // ToDo Implementation
        return new ArrayList<>();
    }

    private static List<Event> processTransactions(final List<User> users, final List<Transaction> transactions, final List<BinMapping> binMappings) {
        // ToDo Implementation
        return null;
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
}


class Event {
    public static final String STATUS_DECLINED = "DECLINED";
    public static final String STATUS_APPROVED = "APPROVED";

    public String transactionId;
    public String status;
    public String message;
}
