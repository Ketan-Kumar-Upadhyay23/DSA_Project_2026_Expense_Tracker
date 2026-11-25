import java.io.*;
import java.util.Scanner;
import java.util.Stack;

public class Main {

    // Node of our own linked list
    static class Node {
        String id, category;
        int amount;
        Node next;

        Node(String id, String category, int amount) {
            this.id = id;
            this.category = category;
            this.amount = amount;
        }
    }

    static Node head = null;
    static Stack<String> undoStack = new Stack<>();
    static final String FILE = "expenses.txt";

    // Number of expenses in the list
    static int count() {
        int c = 0;
        for (Node t = head; t != null; t = t.next) c++;
        return c;
    }

    // Next ID: X101 if empty, otherwise highest ID + 1
    static String nextId() {
        int max = 100;
        for (Node t = head; t != null; t = t.next) {
            int n = Integer.parseInt(t.id.substring(1));
            if (n > max) max = n;
        }
        return "X" + (max + 1);
    }

    // Add node at the end of the list
    static void addNode(String id, String category, int amount) {
        Node n = new Node(id, category, amount);
        if (head == null) {
            head = n;
        } else {
            Node t = head;
            while (t.next != null) t = t.next;
            t.next = n;
        }
    }

    // 1. Add expense
    static void addExpense(Scanner sc) {
        System.out.print("Category: ");
        String category = sc.nextLine().trim();
        System.out.print("Amount: ");
        int amount;
        try {
            amount = Integer.parseInt(sc.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Invalid amount. Amount must be greater than 0.");
            return;
        }
        if (amount <= 0) {
            System.out.println("Invalid amount. Amount must be greater than 0.");
            return;
        }
        String id = nextId();
        addNode(id, category, amount);
        undoStack.push(id);
        System.out.println("Expense added. ID: " + id + " | " + category + " | " + amount);
    }

    // 2. View all
    static void viewAll() {
        if (head == null) {
            System.out.println("No expenses.");
            return;
        }
        int i = 1;
        for (Node t = head; t != null; t = t.next) {
            System.out.println(i++ + ". " + t.id + " | " + t.category + " | " + t.amount);
        }
    }

    // 3. Sort by amount (low to high) - bubble sort on the linked list
    static void sortList() {
        if (head == null) return;
        boolean swapped;
        do {
            swapped = false;
            for (Node t = head; t.next != null; t = t.next) {
                if (t.amount > t.next.amount) {
                    String id = t.id, cat = t.category;
                    int amt = t.amount;
                    t.id = t.next.id;
                    t.category = t.next.category;
                    t.amount = t.next.amount;
                    t.next.id = id;
                    t.next.category = cat;
                    t.next.amount = amt;
                    swapped = true;
                }
            }
        } while (swapped);
    }

    // 4. Binary search (sorts first, then searches)
    static void binarySearch(int key) {
        sortList();
        int n = count();
        Node[] arr = new Node[n];
        Node t = head;
        for (int i = 0; i < n; i++) {
            arr[i] = t;
            t = t.next;
        }
        int low = 0, high = n - 1;
        while (low <= high) {
            int mid = (low + high) / 2;
            if (arr[mid].amount == key) {
                System.out.println("Found: " + arr[mid].id + " | " + arr[mid].category + " | " + arr[mid].amount);
                return;
            } else if (arr[mid].amount < key) {
                low = mid + 1;
            } else {
                high = mid - 1;
            }
        }
        System.out.println("Not found.");
    }

    // 5. Undo last added expense
    static void undo() {
        if (undoStack.isEmpty()) {
            System.out.println("No expense to undo.");
            return;
        }
        String id = undoStack.pop();
        if (head != null && head.id.equals(id)) {
            head = head.next;
        } else {
            Node t = head;
            while (t != null && t.next != null) {
                if (t.next.id.equals(id)) {
                    t.next = t.next.next;
                    break;
                }
                t = t.next;
            }
        }
        System.out.println("Last expense " + id + " removed. Total expenses: " + count());
    }

    // Load from file at start
    static void load() {
        File f = new File(FILE);
        if (!f.exists()) return;
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] p = line.split(",");
                if (p.length == 3) {
                    addNode(p[0], p[1], Integer.parseInt(p[2]));
                    undoStack.push(p[0]);
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading file.");
        }
    }

    // Save to file on exit
    static void save() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(FILE))) {
            for (Node t = head; t != null; t = t.next) {
                pw.println(t.id + "," + t.category + "," + t.amount);
            }
        } catch (IOException e) {
            System.out.println("Error saving file.");
        }
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        load();
        int choice = 0;
        while (choice != 6) {
            System.out.println("\n--- Expense Tracker ---");
            System.out.println("1. Add expense");
            System.out.println("2. View all expenses");
            System.out.println("3. Sort by amount (low to high)");
            System.out.println("4. Binary search by amount");
            System.out.println("5. Undo last expense");
            System.out.println("6. Exit");
            System.out.print("Choice: ");
            try {
                choice = Integer.parseInt(sc.nextLine().trim());
            } catch (NumberFormatException e) {
                choice = 0;
            }
            switch (choice) {
                case 1: addExpense(sc); break;
                case 2: viewAll(); break;
                case 3: sortList(); viewAll(); break;
                case 4:
                    System.out.print("Enter amount to search: ");
                    try {
                        binarySearch(Integer.parseInt(sc.nextLine().trim()));
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid amount.");
                    }
                    break;
                case 5: undo(); break;
                case 6: save(); System.out.println("Saved. Goodbye!"); break;
                default: System.out.println("Invalid choice.");
            }
        }
        sc.close();
    }
}