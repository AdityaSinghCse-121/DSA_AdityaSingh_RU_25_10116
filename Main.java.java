import java.util.*;

/**
 * Project 44: Online Examination System
 * Concepts used: ArrayList, Stack, Queue, Sorting
 *
 *  ArrayList -> question bank, student records, result list
 *  Queue     -> students waiting to appear for the exam (FIFO)
 *  Stack     -> navigation history inside an exam ("Previous" button, LIFO)
 *  Sorting   -> rank list (by score) and question list (by marks / id)
 */

// ---------------------------------------------------------------- Main system
public class OnlineExaminationSystem {

    private static final Scanner sc = new Scanner(System.in);

    // ArrayList : question bank & all results
    private final ArrayList<Question> questionBank = new ArrayList<>();
    private final ArrayList<Student> allStudents   = new ArrayList<>();
    private final ArrayList<Student> resultList    = new ArrayList<>();

    // Queue : students waiting for the exam (First-In-First-Out)
    private final Queue<Student> waitingQueue = new LinkedList<>();

    private int nextQuestionId = 1;

    // ---------------------------------------------------------- input helpers
    private static int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = sc.nextLine().trim();
            try {
                return Integer.parseInt(line);
            } catch (NumberFormatException e) {
                System.out.println("  Invalid number, try again.");
            }
        }
    }

    private static String readLine(String prompt) {
        System.out.print(prompt);
        return sc.nextLine().trim();
    }

    // ---------------------------------------------------------- admin module
    void addQuestion(String text, String[] opts, int correct, int marks) {
        questionBank.add(new Question(nextQuestionId++, text, opts, correct, marks));
    }

    private void addQuestionInteractive() {
        String text = readLine("Enter question text: ");
        String[] opts = new String[4];
        for (int i = 0; i < 4; i++) opts[i] = readLine("  Option " + (i + 1) + ": ");
        int correct;
        do { correct = readInt("Correct option (1-4): "); } while (correct < 1 || correct > 4);
        int marks = readInt("Marks: ");
        addQuestion(text, opts, correct, marks);
        System.out.println("Question added. Total questions: " + questionBank.size());
    }

    private void viewQuestions() {
        if (questionBank.isEmpty()) { System.out.println("Question bank is empty."); return; }
        System.out.println("\nID  Marks  Question");
        System.out.println("--------------------------------------------------");
        for (Question q : questionBank)
            System.out.printf("%-3d %-6d %s%n", q.getId(), q.getMarks(), q.getText());
    }

    private void removeQuestion() {
        int id = readInt("Enter question ID to delete: ");
        boolean removed = questionBank.removeIf(q -> q.getId() == id);
        System.out.println(removed ? "Question deleted." : "Question ID not found.");
    }

    /** SORTING: question bank sorted by marks (high -> low) using Comparator. */
    private void sortQuestionsByMarks() {
        questionBank.sort((a, b) -> b.getMarks() - a.getMarks());
        System.out.println("Questions sorted by marks (descending).");
        viewQuestions();
    }

    // ---------------------------------------------------------- student module
    private void registerStudent() {
        int roll = readInt("Roll number: ");
        for (Student s : allStudents)
            if (s.getRollNo() == roll) {
                System.out.println("Roll number already registered.");
                return;
            }
        String name = readLine("Name: ");
        Student s = new Student(roll, name);
        allStudents.add(s);
        waitingQueue.offer(s);                                   // ENQUEUE
        System.out.println(name + " registered. Position in queue: " + waitingQueue.size());
    }

    private void showQueue() {
        if (waitingQueue.isEmpty()) { System.out.println("No students waiting."); return; }
        System.out.println("\nWaiting queue (front -> rear):");
        int pos = 1;
        for (Student s : waitingQueue)
            System.out.println("  " + (pos++) + ". " + s.getName()
                    + " (Roll " + s.getRollNo() + ")");
    }

    /** Takes the next student from the queue and conducts the exam. */
    private void conductExam() {
        if (questionBank.isEmpty()) {
            System.out.println("No questions available. Ask admin to add some.");
            return;
        }
        Student student = waitingQueue.poll();                   // DEQUEUE
        if (student == null) { System.out.println("No students in queue."); return; }

        System.out.println("\n===== EXAM STARTED for " + student.getName() + " =====");
        System.out.println("Enter 1-4 to answer, S to skip, P to go to previous question.");

        int n = questionBank.size();
        int[] answers = new int[n];                              // 0 = unanswered
        Stack<Integer> history = new Stack<>();                  // STACK of visited indexes
        int current = 0;

        while (current < n) {
            questionBank.get(current).display(current + 1);
            String in = readLine("Your choice: ").toUpperCase();

            if (in.equals("P")) {
                if (history.isEmpty()) {
                    System.out.println("  Already at the first question.");
                } else {
                    current = history.pop();                     // POP -> go back
                }
            } else if (in.equals("S")) {
                history.push(current);                           // PUSH before moving forward
                current++;
            } else if (in.matches("[1-4]")) {
                answers[current] = Integer.parseInt(in);
                history.push(current);
                current++;
            } else {
                System.out.println("  Invalid input.");
            }
        }

        // Evaluate
        int score = 0, correct = 0, total = 0;
        for (int i = 0; i < n; i++) {
            Question q = questionBank.get(i);
            total += q.getMarks();
            if (answers[i] == q.getCorrect()) { score += q.getMarks(); correct++; }
        }
        student.setResult(score, correct);
        resultList.add(student);

        System.out.println("\n===== EXAM FINISHED =====");
        System.out.println("Student  : " + student.getName());
        System.out.println("Correct  : " + correct + "/" + n);
        System.out.println("Score    : " + score + "/" + total);
        System.out.printf ("Percent  : %.2f%%%n", total == 0 ? 0 : score * 100.0 / total);
        System.out.println("Result   : " + (score * 100.0 / total >= 40 ? "PASS" : "FAIL"));
    }

    // ---------------------------------------------------------- results (sorting)
    /** SORTING: Selection sort written by hand - highest score first. */
    private void sortResultsManually(ArrayList<Student> list) {
        for (int i = 0; i < list.size() - 1; i++) {
            int max = i;
            for (int j = i + 1; j < list.size(); j++)
                if (list.get(j).getScore() > list.get(max).getScore()) max = j;
            Student tmp = list.get(i);
            list.set(i, list.get(max));
            list.set(max, tmp);
        }
    }

    private void showRankList() {
        if (resultList.isEmpty()) { System.out.println("No results yet."); return; }
        ArrayList<Student> ranked = new ArrayList<>(resultList);
        sortResultsManually(ranked);

        System.out.println("\n================ RANK LIST ================");
        System.out.printf("%-5s %-6s %-15s %-8s%n", "Rank", "Roll", "Name", "Score");
        System.out.println("-------------------------------------------");
        int rank = 1;
        for (Student s : ranked)
            System.out.printf("%-5d %-6d %-15s %-8d%n",
                    rank++, s.getRollNo(), s.getName(), s.getScore());
    }

    private void showResultsByName() {
        if (resultList.isEmpty()) { System.out.println("No results yet."); return; }
        ArrayList<Student> byName = new ArrayList<>(resultList);
        Collections.sort(byName,
                Comparator.comparing(Student::getName, String.CASE_INSENSITIVE_ORDER));
        System.out.println("\nResults sorted by name:");
        for (Student s : byName)
            System.out.printf("  %-15s Roll %-4d Score %d%n",
                    s.getName(), s.getRollNo(), s.getScore());
    }

    // ---------------------------------------------------------- sample data
    private void loadSampleQuestions() {
        addQuestion("Which data structure follows LIFO?",
                new String[]{"Queue", "Stack", "Array", "Tree"}, 2, 2);
        addQuestion("Which data structure follows FIFO?",
                new String[]{"Stack", "Graph", "Queue", "Heap"}, 3, 2);
        addQuestion("Which Java class provides a resizable array?",
                new String[]{"ArrayList", "LinkedHashMap", "TreeSet", "Vector[]"}, 1, 1);
        addQuestion("Worst-case time complexity of Bubble Sort?",
                new String[]{"O(n)", "O(log n)", "O(n^2)", "O(n log n)"}, 3, 3);
        addQuestion("Which method removes the top element of a Stack?",
                new String[]{"peek()", "pop()", "push()", "poll()"}, 2, 1);
    }

    // ---------------------------------------------------------- menus
    private void adminMenu() {
        while (true) {
            System.out.println("\n--- ADMIN MENU ---");
            System.out.println("1. Add question");
            System.out.println("2. View questions");
            System.out.println("3. Delete question");
            System.out.println("4. Sort questions by marks");
            System.out.println("5. Back");
            int ch = readInt("Choice: ");
            switch (ch) {
                case 1 -> addQuestionInteractive();
                case 2 -> viewQuestions();
                case 3 -> removeQuestion();
                case 4 -> sortQuestionsByMarks();
                case 5 -> { return; }
                default -> System.out.println("Invalid choice.");
            }
        }
    }

    private void studentMenu() {
        while (true) {
            System.out.println("\n--- STUDENT MENU ---");
            System.out.println("1. Register for exam (join queue)");
            System.out.println("2. View waiting queue");
            System.out.println("3. Start exam (next student in queue)");
            System.out.println("4. Back");
            int ch = readInt("Choice: ");
            switch (ch) {
                case 1 -> registerStudent();
                case 2 -> showQueue();
                case 3 -> conductExam();
                case 4 -> { return; }
                default -> System.out.println("Invalid choice.");
            }
        }
    }

    void run() {
        loadSampleQuestions();
        while (true) {
            System.out.println("\n=========================================");
            System.out.println("      ONLINE EXAMINATION SYSTEM");
            System.out.println("=========================================");
            System.out.println("1. Admin");
            System.out.println("2. Student");
            System.out.println("3. Rank list (sorted by score)");
            System.out.println("4. Results sorted by name");
            System.out.println("5. Exit");
            int ch = readInt("Choice: ");
            switch (ch) {
                case 1 -> adminMenu();
                case 2 -> studentMenu();
                case 3 -> showRankList();
                case 4 -> showResultsByName();
                case 5 -> { System.out.println("Thank you!"); return; }
                default -> System.out.println("Invalid choice.");
            }
        }
    }

    public static void main(String[] args) {
        new OnlineExaminationSystem().run();
    }
}

// ---------------------------------------------------------------- Question
class Question {
    private final int id;
    private final String text;
    private final String[] options;
    private final int correct;      // 1-based index of the correct option
    private final int marks;

    Question(int id, String text, String[] options, int correct, int marks) {
        this.id = id;
        this.text = text;
        this.options = options;
        this.correct = correct;
        this.marks = marks;
    }

    int getId()            { return id; }
    String getText()       { return text; }
    String[] getOptions()  { return options; }
    int getCorrect()       { return correct; }
    int getMarks()         { return marks; }

    void display(int number) {
        System.out.println("\nQ" + number + ". " + text + "  [" + marks + " mark(s)]");
        for (int i = 0; i < options.length; i++)
            System.out.println("   " + (i + 1) + ") " + options[i]);
    }
}

// ---------------------------------------------------------------- Student
class Student {
    private final int rollNo;
    private final String name;
    private int score;
    private int correctCount;
    private boolean attempted;

    Student(int rollNo, String name) {
        this.rollNo = rollNo;
        this.name = name;
    }

    int getRollNo()          { return rollNo; }
    String getName()         { return name; }
    int getScore()           { return score; }
    int getCorrectCount()    { return correctCount; }
    boolean isAttempted()    { return attempted; }

    void setResult(int score, int correctCount) {
        this.score = score;
        this.correctCount = correctCount;
        this.attempted = true;
    }
}
