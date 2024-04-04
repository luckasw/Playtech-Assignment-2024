import Objects.BinMapping;
import Objects.Transaction;
import Objects.User;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Reader {
    public static List<User> readUsers(final Path filePath) throws IOException {
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

    public static List<Transaction> readTransactions(final Path filePath) throws IOException {
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

    public static List<BinMapping> readBinMappings(final Path filePath) throws IOException {
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
