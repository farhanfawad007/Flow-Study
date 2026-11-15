import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

class StudySession {
    String subject;
    LocalDate date;
    LocalTime startTime;
    LocalTime endTime;
    long durationMinutes;

    public StudySession(String subject, LocalDate date, LocalTime startTime, LocalTime endTime) {
        this.subject = subject;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        // Time Calculation Logic
        this.durationMinutes = Duration.between(startTime, endTime).toMinutes();
        if (this.durationMinutes < 0) { // Handles sessions going past midnight
            this.durationMinutes += 24 * 60;
        }
    }
}

public class FocusFlow {
    private static List<StudySession> sessions = new ArrayList<>();
    private static int weeklyGoalMinutes = 0;
    private static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("   FocusFlow: Study Session Analytics Tracker");
        System.out.println("==================================================");

        boolean running = true;
        while (running) {
            System.out.println("\n--- Main Menu ---");
            System.out.println("1. Log a Study Session");
            System.out.println("2. Show Daily Focus Minutes");
            System.out.println("3. Show Time Per Subject (Last 7 Days)");
            System.out.println("4. View Study Streaks");
            System.out.println("5. Set & View Weekly Goal Progress");
            System.out.println("6. Exit");
            System.out.print("Select an option (1-6): ");

            String choice = scanner.nextLine();

            switch (choice) {
                case "1" -> logSession();
                case "2" -> showDailyFocus();
                case "3" -> showTimePerSubject();
                case "4" -> viewStreaks();
                case "5" -> manageWeeklyGoal();
                case "6" -> {
                    running = false;
                    System.out.println("Keep the momentum going! Exiting FocusFlow...");
                }
                default -> System.out.println("Invalid option. Please try again.");
            }
        }
    }

    // Feature 1: Log study sessions
    private static void logSession() {
        try {
            System.out.print("Enter Subject: ");
            String subject = scanner.nextLine();

            System.out.print("Enter Date (YYYY-MM-DD) or press Enter for Today: ");
            String dateInput = scanner.nextLine();
            LocalDate date = dateInput.isEmpty() ? LocalDate.now() : LocalDate.parse(dateInput);

            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
            System.out.print("Enter Start Time (HH:mm, 24-hour): ");
            LocalTime startTime = LocalTime.parse(scanner.nextLine(), timeFormatter);

            System.out.print("Enter End Time (HH:mm, 24-hour): ");
            LocalTime endTime = LocalTime.parse(scanner.nextLine(), timeFormatter);

            StudySession session = new StudySession(subject, date, startTime, endTime);
            sessions.add(session);
            System.out.println("Success! Logged " + session.durationMinutes + " minutes for " + subject + ".");
        } catch (DateTimeParseException e) {
            System.out.println("Error: Invalid date/time format. Please use YYYY-MM-DD and HH:mm.");
        }
    }

    // Feature 2: Show daily focus minutes
    private static void showDailyFocus() {
        System.out.print("Enter Date to check (YYYY-MM-DD) or press Enter for Today: ");
        String dateInput = scanner.nextLine();
        LocalDate date = dateInput.isEmpty() ? LocalDate.now() : LocalDate.parse(dateInput);

        long totalMinutes = sessions.stream()
                .filter(s -> s.date.equals(date))
                .mapToLong(s -> s.durationMinutes)
                .sum();

        System.out.println("\nTotal focus time on " + date + ": " + totalMinutes + " minutes (" + (totalMinutes/60) + "h " + (totalMinutes%60) + "m).");
    }

    // Feature 3: Show time per subject for the week
    private static void showTimePerSubject() {
        LocalDate oneWeekAgo = LocalDate.now().minusDays(7);
        
        Map<String, Long> subjectTimes = sessions.stream()
                .filter(s -> s.date.isAfter(oneWeekAgo) || s.date.equals(oneWeekAgo))
                .collect(Collectors.groupingBy(s -> s.subject, Collectors.summingLong(s -> s.durationMinutes)));

        System.out.println("\n--- Time Per Subject (Last 7 Days) ---");
        if (subjectTimes.isEmpty()) {
            System.out.println("No sessions logged in the last 7 days.");
        } else {
            subjectTimes.forEach((subject, minutes) -> 
                System.out.println("- " + subject + ": " + minutes + " mins (" + (minutes/60) + "h " + (minutes%60) + "m)")
            );
        }
    }

    // Feature 4: Calculate streaks
    private static void viewStreaks() {
        List<LocalDate> uniqueDates = sessions.stream()
                .map(s -> s.date)
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        if (uniqueDates.isEmpty()) {
            System.out.println("\nNo study data found. Log a session to start your streak!");
            return;
        }

        int longestStreak = 0;
        int currentStreak = 0;
        int tempStreak = 1;

        // Calculate Longest Streak
        for (int i = 1; i < uniqueDates.size(); i++) {
            if (ChronoUnit.DAYS.between(uniqueDates.get(i - 1), uniqueDates.get(i)) == 1) {
                tempStreak++;
            } else {
                longestStreak = Math.max(longestStreak, tempStreak);
                tempStreak = 1;
            }
        }
        longestStreak = Math.max(longestStreak, tempStreak);

        // Calculate Current Streak
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);
        
        if (uniqueDates.contains(today) || uniqueDates.contains(yesterday)) {
            LocalDate checkDate = uniqueDates.contains(today) ? today : yesterday;
            currentStreak = 1;
            
            while (uniqueDates.contains(checkDate.minusDays(1))) {
                currentStreak++;
                checkDate = checkDate.minusDays(1);
            }
        }

        System.out.println("\n--- Streak Tracker ---");
        System.out.println("🔥 Current Streak: " + currentStreak + " days");
        System.out.println("🏆 Longest Streak: " + longestStreak + " days");
    }

    // Feature 5: Set and show weekly goal
    private static void manageWeeklyGoal() {
        System.out.print("Enter your new weekly goal in minutes (Enter 0 to skip and view progress): ");
        try {
            int newGoal = Integer.parseInt(scanner.nextLine());
            if (newGoal > 0) {
                weeklyGoalMinutes = newGoal;
                System.out.println("Weekly goal set to " + weeklyGoalMinutes + " minutes!");
            }
        } catch (NumberFormatException e) {
            // Ignored, just proceed to show progress
        }

        if (weeklyGoalMinutes == 0) {
            System.out.println("\nYou haven't set a weekly goal yet.");
            return;
        }

        LocalDate oneWeekAgo = LocalDate.now().minusDays(7);
        long currentWeekMinutes = sessions.stream()
                .filter(s -> s.date.isAfter(oneWeekAgo) || s.date.equals(oneWeekAgo))
                .mapToLong(s -> s.durationMinutes)
                .sum();

        double progress = Math.min(100.0, ((double) currentWeekMinutes / weeklyGoalMinutes) * 100);

        System.out.println("\n--- Weekly Goal Progress ---");
        System.out.println("Goal: " + weeklyGoalMinutes + " mins");
        System.out.println("Completed: " + currentWeekMinutes + " mins");
        System.out.printf("Progress: %.1f%%\n", progress);
        
        // Console visual progress bar
        System.out.print("[");
        int filled = (int) (progress / 5);
        for (int i = 0; i < 20; i++) {
            if (i < filled) System.out.print("=");
            else if (i == filled) System.out.print(">");
            else System.out.print(" ");
        }
        System.out.println("]");
    }
}