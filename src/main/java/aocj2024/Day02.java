package aocj2024;

import java.util.Arrays;
import java.util.List;

public class Day02 extends Day {
    @Override
    public void part1() {
        long safeReports = getLinesFromFile("day02.txt")
                .map(Report::parseReport)
                .filter(Report::isSafe)
                .count();

        System.out.println(safeReports);
    }

    @Override
    public void part2() {
        long safeReports = getLinesFromFile("day02.txt")
                .map(Report::parseReport)
                .filter(Report::isTolerateSafe)
                .count();

        System.out.println(safeReports);
    }

    private record Report(List<Level> levels) {
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
