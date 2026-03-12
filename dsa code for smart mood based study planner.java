import java.util.*;

// ════════════════════════════════════════════════
//  DSA STRUCTURES
// ════════════════════════════════════════════════

/* ── Singly Linked List Node (CO2) ── */
class SubjectNode {
    String subject;
    int difficulty;
    SubjectNode next;

    SubjectNode(String subject, int difficulty) {
        this.subject = subject;
        this.difficulty = difficulty;
    }
}

/* ── Singly Linked List for selected subjects (CO2) ── */
class SubjectLinkedList {
    SubjectNode head;
    int size;

    void add(String subject, int difficulty) {
        SubjectNode node = new SubjectNode(subject, difficulty);
        if (head == null) {
            head = node;
        } else {
            SubjectNode temp = head;
            while (temp.next != null) temp = temp.next;
            temp.next = node;
        }
        size++;
    }

    boolean contains(String subject) {
        SubjectNode temp = head;
        while (temp != null) {
            if (temp.subject.equals(subject)) return true;
            temp = temp.next;
        }
        return false;
    }

    void remove(String subject) {
        if (head == null) return;
        if (head.subject.equals(subject)) { head = head.next; size--; return; }
        SubjectNode temp = head;
        while (temp.next != null) {
            if (temp.next.subject.equals(subject)) {
                temp.next = temp.next.next;
                size--;
                return;
            }
            temp = temp.next;
        }
    }

    void display() {
        if (head == null) { System.out.println("  (none)"); return; }
        SubjectNode temp = head;
        while (temp != null) {
            System.out.print("  [" + temp.subject + " | Diff: " + temp.difficulty + "]");
            if (temp.next != null) System.out.print(" -> ");
            temp = temp.next;
        }
        System.out.println();
    }

    List<SubjectNode> toList() {
        List<SubjectNode> list = new ArrayList<>();
        SubjectNode temp = head;
        while (temp != null) { list.add(temp); temp = temp.next; }
        return list;
    }
}

/* ── Min-Heap Priority Queue by difficulty (CO3) ── */
class SubjectHeap {
    private List<SubjectNode> heap = new ArrayList<>();

    void push(SubjectNode node) {
        heap.add(node);
        heapifyUp(heap.size() - 1);
    }

    SubjectNode pop() {
        if (heap.isEmpty()) return null;
        if (heap.size() == 1) return heap.remove(0);
        SubjectNode top = heap.get(0);
        heap.set(0, heap.remove(heap.size() - 1));
        heapifyDown(0);
        return top;
    }

    private void heapifyUp(int i) {
        while (i > 0) {
            int parent = (i - 1) / 2;
            if (heap.get(parent).difficulty <= heap.get(i).difficulty) break;
            Collections.swap(heap, parent, i);
            i = parent;
        }
    }

    private void heapifyDown(int i) {
        int n = heap.size();
        while (true) {
            int smallest = i, l = 2 * i + 1, r = 2 * i + 2;
            if (l < n && heap.get(l).difficulty < heap.get(smallest).difficulty) smallest = l;
            if (r < n && heap.get(r).difficulty < heap.get(smallest).difficulty) smallest = r;
            if (smallest == i) break;
            Collections.swap(heap, i, smallest);
            i = smallest;
        }
    }

    int size() { return heap.size(); }
    boolean isEmpty() { return heap.isEmpty(); }
}

/* ── Stack for undo / last-subject review (CO3) ── */
class StudyStack {
    private List<String> data = new ArrayList<>();

    void push(String item) { data.add(item); }
    String pop() { return data.isEmpty() ? null : data.remove(data.size() - 1); }
    String peek() { return data.isEmpty() ? null : data.get(data.size() - 1); }
    boolean isEmpty() { return data.isEmpty(); }
    int size() { return data.size(); }
}

/* ── Queue for breaks (CO3) ── */
class BreakQueue {
    private List<Integer> data = new ArrayList<>();

    void enqueue(int mins) { data.add(mins); }
    int dequeue() { return data.isEmpty() ? 0 : data.remove(0); }
    boolean isEmpty() { return data.isEmpty(); }
    int size() { return data.size(); }
}

/* ── Session record (stored in ArrayList history) ── */
class SessionBlock {
    String type;       // study | break | review | light
    String name;
    int duration;
    String priority;   // high | medium | low | null
    int difficulty;
    String emoji;
    String meta;

    SessionBlock(String type, String name, int duration, String priority,
                 int difficulty, String emoji, String meta) {
        this.type = type;
        this.name = name;
        this.duration = duration;
        this.priority = priority;
        this.difficulty = difficulty;
        this.emoji = emoji;
        this.meta = meta;
    }

    void display(int idx) {
        String pTag = (priority != null) ? " [" + priority.toUpperCase() + "]" : "";
        System.out.printf("  %2d. %s %-28s %4d min   %s%n",
                idx, emoji, name + pTag, duration, meta);
    }
}

/* ── Plan record saved in user history ── */
class PlanRecord {
    String dateTime;
    String mood;
    int energy;
    String subjects;
    int totalStudy;
    int totalBreak;
    int efficiency;
    List<SessionBlock> schedule;

    PlanRecord(String dateTime, String mood, int energy, String subjects,
               int totalStudy, int totalBreak, int efficiency, List<SessionBlock> schedule) {
        this.dateTime = dateTime;
        this.mood = mood;
        this.energy = energy;
        this.subjects = subjects;
        this.totalStudy = totalStudy;
        this.totalBreak = totalBreak;
        this.efficiency = efficiency;
        this.schedule = schedule;
    }

    void displaySummary(int idx) {
        System.out.printf("  %d. %s | Mood: %-12s | Energy: %d/10 | Study: %d min | Eff: %d%%%n",
                idx, dateTime, mood, energy, totalStudy, efficiency);
    }
}

/* ── User (stored in HashMap) ── */
class User {
    String name;
    String email;
    String password;
    ArrayList<PlanRecord> history = new ArrayList<>();

    User(String name, String email, String password) {
        this.name = name;
        this.email = email;
        this.password = password;
    }
}

// ════════════════════════════════════════════════
//  MAIN SYSTEM
// ════════════════════════════════════════════════
class MoodFlowSystem {

    /* ── HashMap as user database (CO4) ── */
    HashMap<String, User> userDB = new HashMap<>();
    User currentUser = null;

    /* ── Global action stack for undo ── */
    StudyStack actionStack = new StudyStack();

    Scanner sc = new Scanner(System.in);

    /* ── MOOD CONFIG ── */
    // Keys: mood -> [studyDur, breakDur, sessions]
    HashMap<String, int[]> moodTiming = new HashMap<>();
    HashMap<String, String[]> moodTips  = new HashMap<>();

    /* ── SUBJECT DIFFICULTY (Binary Search demo) (CO1) ── */
    // Sorted arrays for binary search demonstration
    String[] sortedSubjects = {
        "Biology","Chemistry","Computer Science","Economics",
        "Geography","History","Languages","Literature","Mathematics","Physics"
    };
    int[] sortedDifficulty = {6, 7, 7, 6, 4, 5, 5, 4, 9, 8};

    MoodFlowSystem() {
        seedDemoUsers();
        initMoodConfig();
    }

    void seedDemoUsers() {
        userDB.put("demo@moodflow.app", new User("Demo Student", "demo@moodflow.app", "Demo1234"));
        userDB.put("student@college.edu", new User("Student", "student@college.edu", "student@123"));
    }

    void initMoodConfig() {
        // [studyDur, breakDur, sessions]
        moodTiming.put("motivated", new int[]{50, 10, 4});
        moodTiming.put("calm",      new int[]{45, 12, 3});
        moodTiming.put("stressed",  new int[]{25, 15, 3});
        moodTiming.put("tired",     new int[]{20, 20, 2});
        moodTiming.put("anxious",   new int[]{30, 15, 3});
        moodTiming.put("distracted",new int[]{25, 10, 3});

        moodTips.put("motivated", new String[]{
            "Strike while the iron is hot — tackle your hardest subject first.",
            "Use Pomodoro: 50 min study, 10 min break to sustain peak performance.",
            "Set stretch goals — your motivation window is precious.",
            "Stay hydrated and keep snacks nearby to preserve your flow state."
        });
        moodTips.put("calm", new String[]{
            "Your relaxed state is ideal for deep reading and conceptual understanding.",
            "Avoid multitasking — calm focus goes furthest on one subject at a time.",
            "This is a great time for revision and consolidating notes.",
            "Light lo-fi music can enhance your calm state."
        });
        moodTips.put("stressed", new String[]{
            "Shorter sessions reduce overwhelm. 25 minutes feels manageable.",
            "Start with a confident subject to build momentum.",
            "Box breathing: inhale 4s, hold 4s, exhale 4s, hold 4s.",
            "Write down your worries before studying to clear mental space."
        });
        moodTips.put("tired", new String[]{
            "Short sessions only. Even 20 minutes of focused study is valuable.",
            "Avoid new complex topics — review familiar material instead.",
            "A 15-20 minute power nap before studying improves retention.",
            "Stand or walk while reviewing flashcards to fight drowsiness."
        });
        moodTips.put("anxious", new String[]{
            "Break large tasks into the smallest possible micro-steps.",
            "Ground yourself: name 5 things you see, 4 you touch, 3 you hear.",
            "Avoid checking grades or deadlines right before a session.",
            "Reward yourself after each session — positive reinforcement resets anxiety loops."
        });
        moodTips.put("distracted", new String[]{
            "Put your phone in another room or use an app blocker.",
            "Use the 2-minute rule: if it takes under 2 min, do it then return.",
            "Start with the boring subject you have been avoiding.",
            "Body-doubling: study with a friend or on a virtual study stream."
        });
    }

    // ── Binary Search for difficulty (CO1: searching algorithms) ──
    int getDifficulty(String subject) {
        int lo = 0, hi = sortedSubjects.length - 1;
        while (lo <= hi) {
            int mid = (lo + hi) / 2;
            int cmp = sortedSubjects[mid].compareTo(subject);
            if (cmp == 0) return sortedDifficulty[mid];
            else if (cmp < 0) lo = mid + 1;
            else hi = mid - 1;
        }
        return 5; // default
    }

    // ── Bubble Sort on plan history by efficiency (CO1: sorting algorithms) ──
    void bubbleSortHistory(ArrayList<PlanRecord> list) {
        int n = list.size();
        for (int i = 0; i < n - 1; i++) {
            for (int j = 0; j < n - i - 1; j++) {
                if (list.get(j).efficiency < list.get(j + 1).efficiency) {
                    PlanRecord temp = list.get(j);
                    list.set(j, list.get(j + 1));
                    list.set(j + 1, temp);
                }
            }
        }
    }

    // ════ AUTH ════

    void signup() {
        System.out.print("Full name   : ");
        String name = sc.nextLine().trim();
        System.out.print("Email       : ");
        String email = sc.nextLine().trim().toLowerCase();
        if (userDB.containsKey(email)) {
            System.out.println("✗ Account already exists. Please login.");
            return;
        }
        System.out.print("Password    : ");
        String password = sc.nextLine();
        userDB.put(email, new User(name, email, password));
        System.out.println("✓ Account created! You can now login.");
    }

    boolean login() {
        System.out.print("Email       : ");
        String email = sc.nextLine().trim().toLowerCase();
        System.out.print("Password    : ");
        String password = sc.nextLine();
        User user = userDB.get(email);
        if (user != null && user.password.equals(password)) {
            currentUser = user;
            System.out.println("✓ Login successful. Welcome, " + currentUser.name + "!");
            return true;
        }
        System.out.println("✗ Invalid email or password. Try demo@moodflow.app / Demo1234");
        return false;
    }

    void logout() {
        if (currentUser == null) { System.out.println("Not logged in."); return; }
        System.out.println("Goodbye, " + currentUser.name + "!");
        currentUser = null;
        actionStack = new StudyStack();
    }

    // ════ SUBJECT PICKER ════

    void showAvailableSubjects() {
        System.out.println("\n  Available Subjects:");
        System.out.println("  1.Mathematics    2.Physics       3.Chemistry     4.Computer Science");
        System.out.println("  5.Economics      6.Biology       7.History       8.Geography");
        System.out.println("  9.Literature    10.Languages");
    }

    SubjectLinkedList pickSubjects() {
        SubjectLinkedList list = new SubjectLinkedList();
        showAvailableSubjects();
        System.out.print("\n  Enter subject names separated by commas: ");
        String[] parts = sc.nextLine().split(",");
        for (String p : parts) {
            String subj = p.trim();
            int diff = getDifficulty(subj); // Binary Search (CO1)
            if (!list.contains(subj)) {
                list.add(subj, diff);
                System.out.println("  + Added: " + subj + " (Difficulty: " + diff + "/10) [Binary Search used]");
            }
        }
        return list;
    }

    // ════ PLAN GENERATOR ════

    void generatePlan() {
        if (currentUser == null) { System.out.println("Please login first."); return; }

        System.out.println("\n  ──── SELECT MOOD ────");
        System.out.println("  1.motivated  2.calm  3.stressed  4.tired  5.anxious  6.distracted");
        System.out.print("  Your mood: ");
        String mood = sc.nextLine().trim().toLowerCase();
        if (!moodTiming.containsKey(mood)) { System.out.println("✗ Invalid mood. Try one from the list."); return; }

        System.out.print("  Energy level (1-10): ");
        int energy;
        try { energy = Integer.parseInt(sc.nextLine().trim()); }
        catch (NumberFormatException e) { System.out.println("✗ Invalid energy value."); return; }
        energy = Math.max(1, Math.min(10, energy));

        SubjectLinkedList subjectList = pickSubjects();
        if (subjectList.size == 0) { System.out.println("✗ No valid subjects entered."); return; }

        // ── Load config ──
        int[] timing  = moodTiming.get(mood);
        double ef     = energy / 10.0;
        int studyMin  = (int) Math.round(timing[0] * (0.5 + ef * 0.5));
        int breakMin  = (int) Math.round(timing[1] * (1.5 - ef * 0.5));
        int numSessions = Math.max(1, (int) Math.round(timing[2] * ef + 0.5));

        // ── Min-Heap: push subjects sorted by difficulty (CO3) ──
        SubjectHeap heap = new SubjectHeap();
        for (SubjectNode sn : subjectList.toList()) heap.push(sn);

        // ── Queue: prepare breaks (CO3) ──
        BreakQueue breakQ = new BreakQueue();
        for (int i = 0; i < numSessions + 1; i++) breakQ.enqueue(breakMin);

        // ── Stack: track subject order for review (CO3) ──
        StudyStack studyStack = new StudyStack();

        // ── Pop from heap (sorted easy→hard) into array ──
        List<SubjectNode> orderedSubs = new ArrayList<>();
        while (!heap.isEmpty()) orderedSubs.add(heap.pop());

        // ── Build schedule ──
        List<SessionBlock> schedule = new ArrayList<>();

        for (int s = 0; s < numSessions; s++) {
            SubjectNode sub = orderedSubs.get(s % orderedSubs.size());
            String priority = sub.difficulty >= 8 ? "high" : sub.difficulty >= 6 ? "medium" : "low";
            schedule.add(new SessionBlock(
                "study", sub.subject, studyMin, priority,
                sub.difficulty, subjectEmoji(sub.subject),
                "Focus Block " + (s + 1) + " | Difficulty " + sub.difficulty + "/10"
            ));
            studyStack.push(sub.subject);

            if (s < numSessions - 1) {
                int bDur = breakQ.dequeue();
                schedule.add(new SessionBlock(
                    "break", "Rest & Recharge", bDur, null,
                    0, "\u2615", "Take a mindful break — stretch, breathe, hydrate"
                ));
            }
        }

        // ── Review block using Stack (CO3: peek last studied) ──
        if (!studyStack.isEmpty()) {
            int reviewDur = Math.max(10, (int) Math.round(studyMin * 0.3));
            schedule.add(new SessionBlock(
                "review", "Quick Review: " + studyStack.peek(), reviewDur, "medium",
                0, "\uD83D\uDD01", "Spaced repetition consolidates memory"
            ));
        }

        // ── Wind-down for low energy ──
        if (energy <= 4) {
            schedule.add(new SessionBlock(
                "light", "Wind-Down Reading", 15, "low",
                0, "\uD83D\uDCDA", "Easy reading to end the session gently"
            ));
        }

        // ── Compute stats ──
        int totalStudy = 0, totalBreak = 0;
        for (SessionBlock blk : schedule) {
            if (blk.type.equals("break")) totalBreak += blk.duration;
            else totalStudy += blk.duration;
        }
        int efficiency = (totalStudy + totalBreak > 0)
            ? (int) Math.round((totalStudy * 100.0) / (totalStudy + totalBreak)) : 0;

        // ── Display plan ──
        displayPlan(mood, energy, schedule, totalStudy, totalBreak, efficiency);
        showMoodTips(mood);

        // ── Save to history ──
        PlanRecord rec = new PlanRecord(
            new Date().toString(), mood, energy,
            subjectList.toList().stream().map(sn -> sn.subject)
                .reduce("", (a, b) -> a.isEmpty() ? b : a + ", "),
            totalStudy, totalBreak, efficiency, schedule
        );
        currentUser.history.add(0, rec);
        actionStack.push("Generated plan: " + mood + " | " + numSessions + " sessions | " + totalStudy + " min study");
        System.out.println("\n✓ Plan saved to history.");
    }

    void displayPlan(String mood, int energy, List<SessionBlock> sched,
                     int totalStudy, int totalBreak, int efficiency) {
        System.out.println("\n╔══════════════════════════════════════════════════════════════╗");
        System.out.printf("║   MOODFLOW ADAPTIVE PLAN  |  Mood: %-10s | Energy: %d/10  ║%n", mood.toUpperCase(), energy);
        System.out.println("╠══════════════════════════════════════════════════════════════╣");
        System.out.printf("║  Total Study : %3d min    Break Time  : %3d min              ║%n", totalStudy, totalBreak);
        System.out.printf("║  Sessions    : %3d        Focus Eff   : %3d%%                 ║%n",
                sched.stream().filter(s -> s.type.equals("study")).count(), efficiency);
        System.out.println("╠══════════════════════════════════════════════════════════════╣");
        System.out.println("║  SESSION SCHEDULE                                            ║");
        System.out.println("╚══════════════════════════════════════════════════════════════╝");
        int idx = 1;
        for (SessionBlock blk : sched) {
            blk.display(idx++);
        }
        System.out.println("\n  Focus Efficiency Bar:");
        printProgressBar(efficiency);
    }

    void printProgressBar(int percent) {
        int filled = percent / 5; // 20 chars = 100%
        System.out.print("  [");
        for (int i = 0; i < 20; i++) System.out.print(i < filled ? "█" : "░");
        System.out.println("] " + percent + "%");
    }

    void showMoodTips(String mood) {
        String[] tips = moodTips.get(mood);
        if (tips == null) return;
        System.out.println("\n  💡 Personalized Tips for mood: " + mood.toUpperCase());
        System.out.println("  " + "─".repeat(58));
        for (String tip : tips) System.out.println("  ◆ " + tip);
    }

    // ════ HISTORY ════

    void showHistory() {
        if (currentUser == null) { System.out.println("Please login first."); return; }
        if (currentUser.history.isEmpty()) { System.out.println("No plan history yet."); return; }
        System.out.println("\n===== PLAN HISTORY (most recent first) =====");
        int i = 1;
        for (PlanRecord r : currentUser.history) r.displaySummary(i++);
    }

    void sortHistoryByEfficiency() {
        if (currentUser == null || currentUser.history.isEmpty()) {
            System.out.println("No history available."); return;
        }
        ArrayList<PlanRecord> sorted = new ArrayList<>(currentUser.history);
        bubbleSortHistory(sorted); // CO1: Bubble Sort
        System.out.println("\n===== HISTORY SORTED BY EFFICIENCY (Bubble Sort) =====");
        int i = 1;
        for (PlanRecord r : sorted) r.displaySummary(i++);
    }

    // ════ UNDO ════

    void undoLastAction() {
        String action = actionStack.pop(); // CO3: Stack
        if (action == null) System.out.println("No recent actions to undo.");
        else System.out.println("↺ Undo: " + action);
    }

    // ════ TIMER (console simulation) ════

    void runTimerSimulation() {
        if (currentUser == null) { System.out.println("Please login first."); return; }
        System.out.print("Enter session duration to simulate (minutes): ");
        int mins;
        try { mins = Integer.parseInt(sc.nextLine().trim()); }
        catch (NumberFormatException e) { System.out.println("Invalid input."); return; }
        System.out.println("  Simulating " + mins + " min timer...");
        for (int i = mins; i >= 0; i -= (mins / 4 + 1)) {
            System.out.println("  ⏱  " + String.format("%02d:%02d", i, 0) + " remaining");
        }
        System.out.println("  ✅ Session complete! Great work.");
        actionStack.push("Ran " + mins + "-min timer simulation");
    }

    // ════ SUBJECT EMOJI ════

    String subjectEmoji(String name) {
        switch (name) {
            case "Mathematics":    return "\uD83D\uDCCF";
            case "Physics":        return "\u269B\uFE0F";
            case "Chemistry":      return "\uD83E\uDDEA";
            case "Biology":        return "\uD83E\uDDEC";
            case "History":        return "\uD83D\uDCDC";
            case "Literature":     return "\uD83D\uDCD6";
            case "Computer Science": return "\uD83D\uDCBB";
            case "Economics":      return "\uD83D\uDCCA";
            case "Geography":      return "\uD83C\uDF0D";
            case "Languages":      return "\uD83D\uDDE3\uFE0F";
            default:               return "\uD83D\uDCDA";
        }
    }

    // ════ MENU ════

    void showMenu() {
        System.out.println("╔══════════════════════════════════════════════╗");
        System.out.println("║    MoodFlow — Adaptive Study Planner (Java)  ║");
        System.out.println("║         DSA Implementation                   ║");
        System.out.println("╚══════════════════════════════════════════════╝");

        while (true) {
            System.out.println("\n===== MOODFLOW MENU =====");
            if (currentUser != null)
                System.out.println("  Logged in as: " + currentUser.name + " <" + currentUser.email + ">");
            System.out.println("  1.  Sign Up");
            System.out.println("  2.  Login");
            System.out.println("  3.  Logout");
            System.out.println("  ─────────────────────────");
            System.out.println("  4.  Generate Study Plan   [MinHeap + Stack + Queue]");
            System.out.println("  5.  View Plan History     [ArrayList]");
            System.out.println("  6.  Sort History          [Bubble Sort]");
            System.out.println("  7.  Undo Last Action      [Stack]");
            System.out.println("  8.  Timer Simulation");
            System.out.println("  ─────────────────────────");
            System.out.println("  9.  DSA Structures Info   [CO mapping]");
            System.out.println("  10. Exit");
            System.out.print("  Enter choice: ");

            String choice = sc.nextLine().trim();
            switch (choice) {
                case "1":  signup();             break;
                case "2":  login();              break;
                case "3":  logout();             break;
                case "4":  generatePlan();       break;
                case "5":  showHistory();        break;
                case "6":  sortHistoryByEfficiency(); break;
                case "7":  undoLastAction();     break;
                case "8":  runTimerSimulation(); break;
                case "9":  showDSAInfo();        break;
                case "10":
                    System.out.println("Exiting MoodFlow. Stay focused!");
                    return;
                default:
                    System.out.println("✗ Invalid choice.");
            }
        }
    }

    void showDSAInfo() {
        System.out.println("\n===== DSA STRUCTURES USED IN MOODFLOW =====");
        System.out.println();
        System.out.println("  CO1 — Algorithm Analysis & Searching/Sorting");
        System.out.println("        • Binary Search   : getDifficulty() - O(log n)");
        System.out.println("        • Bubble Sort     : sortHistoryByEfficiency() - O(n²)");
        System.out.println();
        System.out.println("  CO2 — ADTs: Arrays & Linked Lists");
        System.out.println("        • SubjectLinkedList (Singly Linked) : selected subjects");
        System.out.println("          Operations: add, remove, contains, display, toList");
        System.out.println();
        System.out.println("  CO3 — Stacks, Queues, Heaps");
        System.out.println("        • StudyStack  (Stack)    : action undo + subject review peek");
        System.out.println("        • BreakQueue  (Queue)    : FIFO break slot delivery");
        System.out.println("        • SubjectHeap (Min-Heap) : priority-order subject scheduling");
        System.out.println();
        System.out.println("  CO4 — Hash-Based Structures & Java Collections");
        System.out.println("        • HashMap<String, User>       : O(1) user lookup (userDB)");
        System.out.println("        • HashMap<String, int[]>      : mood config lookup");
        System.out.println("        • ArrayList<PlanRecord>       : user plan history");
        System.out.println("        • ArrayList<SessionBlock>     : generated schedule");
        System.out.println();
        System.out.println("  CO5 — Practical Applications");
        System.out.println("        • Full Mood-Adaptive Study Planner");
        System.out.println("        • Personalized scheduling with energy factor");
        System.out.println("        • Spaced repetition via Stack peek");
        System.out.println();
        System.out.println("  CO6 — Program Design");
        System.out.println("        • Auth system (Signup/Login/Logout)");
        System.out.println("        • Persistent user session within runtime");
        System.out.println("        • Undo system via Stack");
        System.out.println("        • Console timer simulation");
    }
}

// ════════════════════════════════════════════════
//  ENTRY POINT
// ════════════════════════════════════════════════
public class MoodFlowPlanner {
    public static void main(String[] args) {
        MoodFlowSystem system = new MoodFlowSystem();
        system.showMenu();
    }
}
