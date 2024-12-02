package aocj2024;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;

public class Day2 extends Day {
    @Override
    public void part1() {
        Scanner scanner = getScanner("day2.txt");
        int safeReports = 0;

        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            Report report = Report.parseReport(line);
            if (report.isSafe()) safeReports += 1;
        }

        System.out.println(safeReports);
    }

    @Override
    public void part2() {
        Scanner scanner = getScanner("day2.txt");
        int safeReports = 0;

        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            Report report = Report.parseReport(line);
            if (report.isTolerateSafe()) safeReports += 1;
        }

        System.out.println(safeReports);
    }

    private static final class Report {
        private final List<Level> levels;

        private Report(List<Level> levels) {
            this.levels = levels;
        }

        private boolean isSafe() {
            boolean safe = true;
            boolean increasing = true, decreasing = true;

            for (int i = 1, size = levels.size(); i < size && safe; i++) {
                Level prev = levels.get(i - 1), current = levels.get(i);
                safe = Level.isSafe(prev, current, increasing, decreasing);

                int compare = prev.compareTo(current);

                if (compare < 0) decreasing = false;
                if (compare > 0) increasing = false;
            }

            return safe;
        }

        private boolean isTolerateSafe() {
            for (int i = 0; i < levels.size(); i++) {
                boolean safe = true;
                boolean increasing = true, decreasing = true;

                for (int j = 0, size = levels.size(); j < size && safe; j++) {
                    int offset = 0;
                    if (j == i) continue;
                    if (j == i + 1) offset = 1;

                    Level prev = getLevel(j - 1 - offset), current = getLevel(j);

                    if (prev == null || current == null) continue;

                    safe = Level.isSafe(prev, current, increasing, decreasing);

                    int compare = prev.compareTo(current);

                    if (compare < 0) decreasing = false;
                    if (compare > 0) increasing = false;
                }

                if (safe) return true;
            }

            return false;
        }

        private Level getLevel(int index) {
            if (index < 0 || index >= levels.size()) return null;

            return levels.get(index);
        }

        private static Report parseReport(String reportString) {
            List<Level> levels = Arrays.stream(reportString.split(" "))
                    .map(Level::parseLevel)
                    .toList();
            return new Report(levels);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (obj == null || obj.getClass() != this.getClass()) return false;
            var that = (Report) obj;
            return Objects.equals(this.levels, that.levels);
        }
    }

    private record Level(int level) implements Comparable<Level> {
        private boolean isSafeDistance(Level o) {
            int distance = Math.abs(level - o.level);
            return distance >= 1 && distance <= 3;
        }

        private static Level parseLevel(String levelString) {
            int level = Integer.parseInt(levelString);
            return new Level(level);
        }

        private static boolean isSafe(Level l1, Level l2, boolean increasing, boolean decreasing) {
            if (l1 == null || l2 == null) return false;

            boolean isSafeDistance = l1.isSafeDistance(l2);
            int compare = l1.compareTo(l2);

            return ((increasing && compare < 0) || (decreasing && compare > 0)) && isSafeDistance;
        }

        @Override
        public int compareTo(Level o) {
            return Integer.compare(level, o.level);
        }
    }
}
