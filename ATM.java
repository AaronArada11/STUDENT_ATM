import java.io.*;
import java.util.*;

public class ATM {
private static final Scanner sc = new Scanner(System.in);
private final Map<String, AccountOperations> accounts = new HashMap<>();
private final File storageFile = new File("accounts.txt");

public ATM() {
loadAccounts();
if (accounts.isEmpty()) {

Account a1 = new Account("202410159", "Enzo", 1500.00, "1234");
Account a2 = new Account("202411323", "Aaron", 850.50, "2222");
Account a3 = new Account("202330011", "Renee", 3000.00, "3333");
Account a4 = new Account("202411944", "Cris", 3000.00, "4444");
accounts.put(a1.getAccountNumber(), a1);
accounts.put(a2.getAccountNumber(), a2);
accounts.put(a3.getAccountNumber(), a3);
accounts.put(a4.getAccountNumber(), a4);
}
}

public static void main(String[] args) {
ATM atm = new ATM();
System.out.println("GRP6 ATM Simulator!");

Runtime.getRuntime().addShutdownHook(new Thread(() -> {
atm.saveAccounts();
sc.close();
}));

while (true) {
System.out.print("\nEnter student number (or 'exit' to quit): ");
String accNum = sc.nextLine().trim();
if (accNum.equalsIgnoreCase("exit")) {
System.out.println("Goodbye!");
break;
}

System.out.print("Enter PIN: ");
String pin = sc.nextLine().trim();

AccountOperations account = atm.authenticate(accNum, pin);
if (account == null) {
System.out.println("Authentication failed. Try again.");
continue;
}

System.out.printf("\nHello, %s!\n", account.getHolderName());
atm.sessionLoop(account);
}
}

private AccountOperations authenticate(String accountNumber, String pin) {
AccountOperations acc = accounts.get(accountNumber);
if (acc != null && acc.checkPin(pin)) return acc;
return null;
}

private void sessionLoop(AccountOperations acc) {
while (true) {
System.out.println("\nSelect an option:");
System.out.println("1) Balance inquiry");
System.out.println("2) Withdraw");
System.out.println("3) Deposit");
System.out.println("4) Transfer");
System.out.println("5) Mini statement");
System.out.println("6) Change PIN");
System.out.println("7) Logout");
System.out.print("Select: ");

String choice = sc.nextLine().trim();
switch (choice) {
case "1":
System.out.printf("\nAvailable balance: PHP%.2f\n", acc.getBalance());
break;
case "2":
handleWithdraw(acc);
break;
case "3":
handleDeposit(acc);
break;
case "4":
handleTransfer(acc);
break;
case "5":
acc.printMiniStatement();
break;
case "6":
handleChangePin(acc);
break;
case "7":
System.out.println("Logging out...");
saveAccounts();
return;
default:
System.out.println("Invalid choice. Try again.");
}
}
}

private void handleWithdraw(AccountOperations acc) {
System.out.print("Enter amount to withdraw: ");
String s = sc.nextLine().trim();
try {
double amount = Double.parseDouble(s);
if (amount <= 0) {
System.out.println("Amount must be positive.");
return;
}
if (acc.withdraw(amount)) {
System.out.printf("Please take your cash. New balance: PHP%.2f\n", acc.getBalance());
saveAccounts();
} else {
System.out.println("Insufficient balance.");
}
} catch (NumberFormatException e) {
System.out.println("Invalid number.");
}
}

private void handleDeposit(AccountOperations acc) {
System.out.print("Enter amount to deposit: ");
String s = sc.nextLine().trim();
try {
double amount = Double.parseDouble(s);
if (amount <= 0) {
System.out.println("Amount must be positive.");
return;
}
acc.deposit(amount);
System.out.printf("Deposit successful. New balance: PHP%.2f\n", acc.getBalance());
saveAccounts();
} catch (NumberFormatException e) {
System.out.println("Invalid number.");
}
}

private void handleTransfer(AccountOperations acc) {
System.out.print("Enter destination account number: ");
String dest = sc.nextLine().trim();
AccountOperations to = accounts.get(dest);
if (to == null) {
System.out.println("Destination account not found.");
return;
}
System.out.print("Enter amount to transfer: ");
String s = sc.nextLine().trim();
try {
double amount = Double.parseDouble(s);
if (amount <= 0) {
System.out.println("Amount must be positive.");
return;
}
if (acc.transferTo(to, amount)) {
System.out.printf("Transfer successful. Your new balance: PHP%.2f\n", acc.getBalance());
saveAccounts();
} else {
System.out.println("Insufficient funds for transfer.");
}
} catch (NumberFormatException e) {
System.out.println("Invalid number.");
}
}

private void handleChangePin(AccountOperations acc) {
System.out.print("Enter current PIN: ");
String cur = sc.nextLine().trim();
if (!acc.checkPin(cur)) {
System.out.println("Current PIN incorrect.");
return;
}
System.out.print("Enter new PIN (4 digits): ");
String np = sc.nextLine().trim();
if (!np.matches("\\d{4}")) {
System.out.println("PIN must be exactly 4 digits.");
return;
}
acc.changePin(np);
System.out.println("PIN changed successfully.");
saveAccounts();
}

@SuppressWarnings("unchecked")
private void loadAccounts() {
if (!storageFile.exists()) return;
try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(storageFile))) {
Object obj = ois.readObject();
if (obj instanceof Map) {
Map<String, Account> loaded = (Map<String, Account>) obj;
accounts.clear();
accounts.putAll(loaded);
}
} catch (IOException | ClassNotFoundException e) {
System.out.println("Failed to load accounts: " + e.getMessage());
}
}

private void saveAccounts() {
try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(storageFile))) {

Map<String, Account> toSave = new HashMap<>();
for (Map.Entry<String, AccountOperations> e : accounts.entrySet()) {
if (e.getValue() instanceof Account) {
toSave.put(e.getKey(), (Account) e.getValue());
}
}
oos.writeObject(toSave);
} catch (IOException e) {
System.out.println("Failed to save accounts: " + e.getMessage());
}
}
}

interface AccountOperations extends Serializable {
String getAccountNumber();
String getHolderName();
double getBalance();
boolean checkPin(String candidate);
void changePin(String newPin);
void deposit(double amount);
boolean withdraw(double amount);
boolean transferTo(AccountOperations other, double amount);
void printMiniStatement();
}

class Account implements AccountOperations {
private static final long serialVersionUID = 1L;

private final String accountNumber;
private final String holderName;
private double balance;
private String pin;
private final List<String> transactions = new ArrayList<>();

public Account(String accountNumber, String holderName, double balance, String pin) {
this.accountNumber = accountNumber;
this.holderName = holderName;
this.balance = balance;
this.pin = pin;
transactions.add(String.format("Account created with balance PHP%.2f", balance));
}

public String getAccountNumber() { return accountNumber; }
public String getHolderName() { return holderName; }
public double getBalance() { return balance; }

public boolean checkPin(String candidate) { return pin.equals(candidate); }

public void changePin(String newPin) {
this.pin = newPin;
transactions.add("PIN changed");
}

public void deposit(double amount) {
balance += amount;
transactions.add(String.format("Deposit: PHP%.2f | Balance: PHP%.2f", amount, balance));
}

public boolean withdraw(double amount) {
if (amount > balance) return false;
balance -= amount;
transactions.add(String.format("Withdraw: PHP%.2f | Balance: PHP%.2f", amount, balance));
return true;
}

public boolean transferTo(AccountOperations other, double amount) {
if (amount > balance) return false;
if (!(other instanceof Account)) return false;
Account otherAcc = (Account) other;
balance -= amount;
otherAcc.balance += amount;
transactions.add(String.format("Transfer to %s: PHP%.2f | Balance: PHP%.2f", other.getAccountNumber(), amount, balance));
otherAcc.transactions.add(String.format("Transfer from %s: PHP%.2f | Balance: PHP%.2f", accountNumber, amount, otherAcc.balance));
return true;
}

public void printMiniStatement() {
System.out.println("--- Mini Statement (latest 10) ---");
int start = Math.max(0, transactions.size() - 10);
for (int i = transactions.size() - 1; i >= start; i--) {
System.out.println(transactions.get(i));
}
System.out.println("-------------------------------");
}
}

