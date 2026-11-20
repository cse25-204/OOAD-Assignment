package dao;

import model.Account;
import model.Customer;
import model.Transaction;
import model.TransactionType;
import model.User;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
/**
 * DAO implementations that keep everything in memory instead of a database.
 * Used only to run the test suite without needing the SQLite driver; the real
 * application uses the Jdbc* classes.
 */
public class InMemoryStore {

    private final Map<String, Customer> customerMap = new LinkedHashMap<>();
    private final Map<String, Account> accountMap = new LinkedHashMap<>();
    private final List<Transaction> ledger = new ArrayList<>();
    private final Map<String, User> userMap = new LinkedHashMap<>();
    private long nextTransactionId = 1;

    public final CustomerDAO customers = new CustomerDAO() {
        @Override
        public void save(Customer c) {
            customerMap.put(c.getCustomerId(), c);
        }

        @Override
        public Optional<Customer> findById(String id) {
            return Optional.ofNullable(customerMap.get(id));
        }

        @Override
        public List<Customer> findAll() {
            return new ArrayList<>(customerMap.values());
        }

        @Override
        public List<Customer> search(String text) {
            String t = text.trim().toLowerCase();
            List<Customer> out = new ArrayList<>();
            for (Customer c : customerMap.values()) {
                String hay = (c.getCustomerId() + " " + c.getDisplayName() + " " + c.getPhone()
                        + " " + c.getEmail()).toLowerCase();
                if (hay.contains(t)) {
                    out.add(c);
                }
            }
            return out;
        }

        @Override
        public void update(Customer c) {
            customerMap.put(c.getCustomerId(), c);
        }

        @Override
        public void delete(String id) {
            customerMap.remove(id);
        }

        @Override
        public int count() {
            return customerMap.size();
        }

        @Override
        public String nextCustomerId() {
            long max = 0;
            for (String id : customerMap.keySet()) {
                max = Math.max(max, Long.parseLong(id.substring(1)));
            }
            return String.format("C%05d", max + 1);
        }
    };

    public final AccountDAO accounts = new AccountDAO() {
        @Override
        public void save(Account a, Transaction openingTx) {
            accountMap.put(a.getAccountNumber(), a);
            addTx(openingTx);
        }

        @Override
        public Optional<Account> findByNumber(String number) {
            return Optional.ofNullable(accountMap.get(number));
        }

        @Override
        public List<Account> findByCustomer(String customerId) {
            List<Account> out = new ArrayList<>();
            for (Account a : accountMap.values()) {
                if (a.getOwner().getCustomerId().equals(customerId)) {
                    out.add(a);
                }
            }
            return out;
        }

        @Override
        public List<Account> findAll() {
            return new ArrayList<>(accountMap.values());
        }

        @Override
        public void update(Account a, Transaction tx) {
            accountMap.put(a.getAccountNumber(), a);
            addTx(tx);
        }

        @Override
        public void delete(String number) {
            accountMap.remove(number);
            ledger.removeIf(t -> t.getAccountNumber().equals(number));
        }

        @Override
        public int count() {
            return accountMap.size();
        }

        @Override
        public long maxSequence() {
            long max = 0;
            for (String n : accountMap.keySet()) {
                max = Math.max(max, Long.parseLong(n.substring(1)));
            }
            return max;
        }
    };

    public final TransactionDAO transactions = new TransactionDAO() {
        @Override
        public void save(Transaction tx) {
            addTx(tx);
        }

        @Override
        public List<Transaction> findByAccount(String number) {
            List<Transaction> out = new ArrayList<>();
            for (Transaction t : ledger) {
                if (t.getAccountNumber().equals(number)) {
                    out.add(t);
                }
            }
            return out;
        }

        @Override
        public boolean interestPaidForMonth(String number, YearMonth month) {
            for (Transaction t : ledger) {
                if (t.getAccountNumber().equals(number) && t.getType() == TransactionType.INTEREST
                        && YearMonth.from(t.getTimestamp()).equals(month)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public int count() {
            return ledger.size();
        }
    };

    public final UserDAO users = new UserDAO() {
        @Override
        public void save(User u) {
            userMap.put(u.getUsername(), u);
        }

        @Override
        public Optional<User> findByUsername(String username) {
            return Optional.ofNullable(userMap.get(username));
        }

        @Override
        public boolean existsByUsername(String username) {
            return userMap.containsKey(username);
        }

        @Override
        public int count() {
            return userMap.size();
        }
    };

    private void addTx(Transaction tx) {
        if (tx != null) {
            tx.setId(nextTransactionId++);
            ledger.add(tx);
        }
    }
}
