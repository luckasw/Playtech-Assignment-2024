import Objects.BinMapping;
import Objects.Transaction;
import Objects.User;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class TransactionProcessorTest {

    @Test
    public void testUsingManual_test_data_75_validations() {
        String binsPath = "src/test/resources/manual_test_data_75%_validations/input/bins.csv";
        String transactionsPath = "src/test/resources/manual_test_data_75%_validations/input/transactions.csv";
        String usersPath = "src/test/resources/manual_test_data_75%_validations/input/users.csv";
        String outputBalancesPath = "src/test/resources/outputBalances.csv";
        String outputEventsPath = "src/test/resources/outputEvents.csv";

        String[] args = {usersPath, transactionsPath, binsPath, outputBalancesPath, outputEventsPath};
        try {
            TransactionProcessor.main(args);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try {
            List<String> outputBalances = Files.readAllLines(Paths.get(outputBalancesPath));
            List<String> expectedBalances = Files.readAllLines(Paths.get("src/test/resources/manual_test_data_75%_validations/output example/balances.csv"));
            assertEquals(expectedBalances, outputBalances);

            List<String> outputEvents = Files.readAllLines(Paths.get(outputEventsPath));
            List<String> expectedEvents = Files.readAllLines(Paths.get("src/test/resources/manual_test_data_75%_validations/output example/events.csv"));
            assertEquals(expectedEvents, outputEvents);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testUsingTest_random_data_50_validations() {
        String binsPath = "src/test/resources/test_random_data_50%_validations/input/bins.csv";
        String transactionsPath = "src/test/resources/test_random_data_50%_validations/input/transactions.csv";
        String usersPath = "src/test/resources/test_random_data_50%_validations/input/users.csv";
        String outputBalancesPath = "src/test/resources/outputBalances.csv";
        String outputEventsPath = "src/test/resources/outputEvents.csv";

        String[] args = {usersPath, transactionsPath, binsPath, outputBalancesPath, outputEventsPath};
        try {
            TransactionProcessor.main(args);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try {
            List<String> outputBalances = Files.readAllLines(Paths.get(outputBalancesPath));
            List<String> expectedBalances = Files.readAllLines(Paths.get("src/test/resources/test_random_data_50%_validations/output example/balances.csv"));
            assertEquals(expectedBalances, outputBalances);

            List<String> outputEvents = Files.readAllLines(Paths.get(outputEventsPath));
            List<String> expectedEvents = Files.readAllLines(Paths.get("src/test/resources/test_random_data_50%_validations/output example/events.csv"));
            assertEquals(expectedEvents, outputEvents);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testUsingTest_random_data_small() {
        String binsPath = "src/test/resources/test_random_data_small/input/bins.csv";
        String transactionsPath = "src/test/resources/test_random_data_small/input/transactions.csv";
        String usersPath = "src/test/resources/test_random_data_small/input/users.csv";
        String outputBalancesPath = "src/test/resources/outputBalances.csv";
        String outputEventsPath = "src/test/resources/outputEvents.csv";

        String[] args = {usersPath, transactionsPath, binsPath, outputBalancesPath, outputEventsPath};
        try {
            TransactionProcessor.main(args);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try {
            List<String> outputBalances = Files.readAllLines(Paths.get(outputBalancesPath));
            List<String> expectedBalances = Files.readAllLines(Paths.get("src/test/resources/test_random_data_small/output example/balances.csv"));
            assertEquals(expectedBalances, outputBalances);

            List<String> outputEvents = Files.readAllLines(Paths.get(outputEventsPath));
            List<String> expectedEvents = Files.readAllLines(Paths.get("src/test/resources/test_random_data_small/output example/events.csv"));
            assertEquals(expectedEvents, outputEvents);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    @Test
    public void testNegativeTransaction() {
        Transaction transaction = new Transaction("1", "1", "DEPOSIT", -1, "CARD", "1234");
        User user = new User("1", "John", 100.0, "EE", false, 10.0, 100.0, 10.0, 100.0);
        List<User> users = new ArrayList<>();
        users.add(user);
        List<BinMapping> binMappings = new ArrayList<>();
        TransactionHandler transactionHandler = new TransactionHandler(users, binMappings);

        transactionHandler.processTransaction(transaction);

        assertEquals("Invalid amount -1.0", transactionHandler.getEvents().getFirst().message);
    }

    @Test
    public void testZeroTransaction() {
        Transaction transaction = new Transaction("1", "1", "DEPOSIT", 0, "CARD", "1234");
        User user = new User("1", "John", 100.0, "EE", false, 10.0, 100.0, 10.0, 100.0);
        List<User> users = new ArrayList<>();
        users.add(user);
        List<BinMapping> binMappings = new ArrayList<>();
        TransactionHandler transactionHandler = new TransactionHandler(users, binMappings);

        transactionHandler.processTransaction(transaction);

        assertEquals("Invalid amount 0.0", transactionHandler.getEvents().getFirst().message);
    }

    @Test
    public void testWithdrawBeforeDeposit() {
        Transaction transaction = new Transaction("1", "1", "WITHDRAW", 1000, "CARD", "1234");
        User user = new User("1", "John", 100.0, "EE", false, 10.0, 100.0, 10.0, 100.0);
        List<User> users = new ArrayList<>();
        users.add(user);
        List<BinMapping> binMappings = new ArrayList<>();
        TransactionHandler transactionHandler = new TransactionHandler(users, binMappings);

        transactionHandler.processTransaction(transaction);

        assertEquals("Cannot withdraw with a new account 1234", transactionHandler.getEvents().getFirst().message);
    }

    @Test
    public void testWithdrawMoreThanBalance() {
        Transaction transaction1 = new Transaction("1", "1", "DEPOSIT", 100, "CARD", "544698200023455555");
        Transaction transaction2 = new Transaction("2", "1", "WITHDRAW", 1000, "CARD", "544698200023455555");
        User user = new User("1", "John", 100.0, "EST", false, 10.0, 100.0, 10.0, 100.0);
        List<User> users = List.of(user);
        BinMapping binMapping = new BinMapping("Birch", 5446982000L, 5446982099L, "DC", "EST");
        List<BinMapping> binMappings = List.of(binMapping);
        TransactionHandler transactionHandler = new TransactionHandler(users, binMappings);

        transactionHandler.processTransaction(transaction1);
        transactionHandler.processTransaction(transaction2);

        assertEquals("Amount 1000.0 is over the withdraw limit of 100.0", transactionHandler.getEvents().getLast().message);
    }

    @Test
    public void testWithdrawMoreThanLimit() {
        Transaction transaction1 = new Transaction("1", "1", "DEPOSIT", 100, "CARD", "544698200023455555");
        Transaction transaction2 = new Transaction("2", "1", "WITHDRAW", 101, "CARD", "544698200023455555");
        User user = new User("1", "John", 100.0, "EST", false, 10.0, 100.0, 10.0, 100.0);
        List<User> users = List.of(user);
        BinMapping binMapping = new BinMapping("Birch", 5446982000L, 5446982099L, "DC", "EST");
        List<BinMapping> binMappings = List.of(binMapping);
        TransactionHandler transactionHandler = new TransactionHandler(users, binMappings);

        transactionHandler.processTransaction(transaction1);
        transactionHandler.processTransaction(transaction2);

        assertEquals("Amount 101.0 is over the withdraw limit of 100.0", transactionHandler.getEvents().getLast().message);
    }

    @Test
    public void testWithdrawLessThanMin() {
        Transaction transaction1 = new Transaction("1", "1", "DEPOSIT", 100, "CARD", "544698200023455555");
        Transaction transaction2 = new Transaction("2", "1", "WITHDRAW", 9, "CARD", "544698200023455555");
        User user = new User("1", "John", 100.0, "EST", false, 10.0, 100.0, 10.0, 100.0);
        List<User> users = List.of(user);
        BinMapping binMapping = new BinMapping("Birch", 5446982000L, 5446982099L, "DC", "EST");
        List<BinMapping> binMappings = List.of(binMapping);
        TransactionHandler transactionHandler = new TransactionHandler(users, binMappings);

        transactionHandler.processTransaction(transaction1);
        transactionHandler.processTransaction(transaction2);

        assertEquals("Amount 9.0 is under the withdraw limit of 10.0", transactionHandler.getEvents().getLast().message);
    }

    @Test
    public void testDepositLessThanMin() {
        Transaction transaction = new Transaction("1", "1", "DEPOSIT", 9, "CARD", "544698200023455555");
        User user = new User("1", "John", 100.0, "EST", false, 10.0, 100.0, 10.0, 100.0);
        List<User> users = List.of(user);
        BinMapping binMapping = new BinMapping("Birch", 5446982000L, 5446982099L, "DC", "EST");
        List<BinMapping> binMappings = List.of(binMapping);
        TransactionHandler transactionHandler = new TransactionHandler(users, binMappings);

        transactionHandler.processTransaction(transaction);

        assertEquals("Amount 9.0 is under the deposit limit of 10.0", transactionHandler.getEvents().getLast().message);
    }

    @Test
    public void testDepositMoreThanLimit() {
        Transaction transaction = new Transaction("1", "1", "DEPOSIT", 101, "CARD", "544698200023455555");
        User user = new User("1", "John", 100.0, "EST", false, 10.0, 100.0, 10.0, 100.0);
        List<User> users = List.of(user);
        BinMapping binMapping = new BinMapping("Birch", 5446982000L, 5446982099L, "DC", "EST");
        List<BinMapping> binMappings = List.of(binMapping);
        TransactionHandler transactionHandler = new TransactionHandler(users, binMappings);

        transactionHandler.processTransaction(transaction);

        assertEquals("Amount 101.0 is over the deposit limit of 100.0", transactionHandler.getEvents().getLast().message);
    }

    @Test
    public void testDepositWithBadIBAN() {
        Transaction transaction = new Transaction("1", "1", "DEPOSIT", 100, "TRANSFER", "544698200023455555");
        User user = new User("1", "John", 100.0, "EST", false, 10.0, 100.0, 10.0, 100.0);
        List<User> users = List.of(user);
        BinMapping binMapping = new BinMapping("Birch", 5446982000L, 5446982099L, "DC", "EST");
        List<BinMapping> binMappings = List.of(binMapping);
        TransactionHandler transactionHandler = new TransactionHandler(users, binMappings);

        transactionHandler.processTransaction(transaction);

        assertEquals("Invalid iban 544698200023455555", transactionHandler.getEvents().getLast().message);
    }

    @Test
    public void testDepositWithGoodIBAN() {
        Transaction transaction = new Transaction("1", "1", "DEPOSIT", 100, "TRANSFER", "GB90BARC20038077143493");
        User user = new User("1", "John", 100.0, "GBR", false, 10.0, 100.0, 10.0, 100.0);
        List<User> users = List.of(user);
        BinMapping binMapping = new BinMapping("Birch", 5446982000L, 5446982099L, "DC", "EST");
        List<BinMapping> binMappings = List.of(binMapping);
        TransactionHandler transactionHandler = new TransactionHandler(users, binMappings);

        transactionHandler.processTransaction(transaction);

        assertEquals("OK", transactionHandler.getEvents().getLast().message);
    }
}
