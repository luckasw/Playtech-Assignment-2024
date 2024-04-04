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
//        List<String> usedTransactionIds = new ArrayList<>();

        final List<User> users = Reader.readUsers(Paths.get(args[0]));
        final List<Transaction> transactions = Reader.readTransactions(Paths.get(args[1]));
        final List<BinMapping> binMappings = Reader.readBinMappings(Paths.get(args[2]));
        TransactionHandler transactionHandler = new TransactionHandler(users, binMappings);
        for (Transaction transaction : transactions) {
            transactionHandler.processTransaction(transaction);
        }

        List<User> processedUser = transactionHandler.getUsers();
        List<Event> events = transactionHandler.getEvents();

        TransactionProcessor.writeBalances(Paths.get(args[3]), users);
        TransactionProcessor.writeEvents(Paths.get(args[4]), events);
    }


    private static void writeBalances(final Path filePath, final List<User> users) throws IOException {
        try (final FileWriter writer = new FileWriter(filePath.toFile(), false)) {
            writer.append("USER_ID,BALANCE\n");
            for (final var user : users) {
                String formattedBalance = String.format(Locale.US, "%.2f", user.getBalance());
                writer.append(user.getId()).append(",").append(formattedBalance).append("\n");
//                writer.append(user.getId()).append(",").append(String.valueOf(user.getBalance())).append("\n");
            }
        }
    }

    private static void writeEvents(final Path filePath, final List<Event> events) throws IOException {
        try (final FileWriter writer = new FileWriter(filePath.toFile(), false)) {
            writer.append("TRANSACTION_ID,STATUS,MESSAGE\n");
            for (final var event : events) {
                writer.append(event.transactionId).append(",").append(event.status).append(",").append(event.message).append("\n");
            }
        }
    }
}


