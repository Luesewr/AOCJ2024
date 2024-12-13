package aocj2024;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Day13 extends Day {
    @Override
    public void part1() {
        List<String> lines = getLinesFromFile("day13.txt").toList();
        List<ClawMachine> clawMachines = ClawMachine.parseClawMachines(lines, 0L);

        long totalCost = clawMachines.stream()
                .map(clawMachine -> clawMachine.findCheapest(3, 1))
                .reduce(0L, Long::sum);

        System.out.println(totalCost);
    }

    @Override
    public void part2() {
        List<String> lines = getLinesFromFile("day13.txt").toList();
        List<ClawMachine> clawMachines = ClawMachine.parseClawMachines(lines, 10000000000000L);

        long totalCost = clawMachines.stream()
                .map(clawMachine -> clawMachine.findCheapest(3, 1))
                .reduce(0L, Long::sum);

        System.out.println(totalCost);
    }

    private record ClawMachine(Location aButton, Location bButton, Location prizeLocation) {
        private final static Pattern coordinateContainer = Pattern.compile(".+?: X[+=](\\d+), Y[+=](\\d+)");

        private long findCheapest(long aCost, long bCost) {
            long aNumerator = prizeLocation.y * bButton.x - prizeLocation.x * bButton.y;
            long aDenominator = aButton.y * bButton.x - aButton.x * bButton.y;

            if (aDenominator == 0 || aNumerator % aDenominator != 0) return 0;

            long aCount = aNumerator / aDenominator;

            long bNumerator = prizeLocation.x - aCount * aButton.x;
            long bDenominator = bButton.x;

            if (bDenominator == 0 || bNumerator % bDenominator != 0) return 0;

            long bCount = bNumerator / bDenominator;

            if (aCount < 0 || bCount < 0) return 0;

            return aCount * aCost + bCount * bCost;
        }

        private static List<ClawMachine> parseClawMachines(List<String> lines, long offset) {
            List<ClawMachine> clawMachines = new ArrayList<>();

            for (int i = 0; i < lines.size(); i += 4) {
                Location aButton = extractCoords(lines.get(i));
                Location bButton = extractCoords(lines.get(i + 1));
                Location prize = extractCoords(lines.get(i + 2));

                clawMachines.add(new ClawMachine(aButton, bButton, prize.offset(offset)));
            }

            return clawMachines;
        }

        private static Location extractCoords(String string) {
            Matcher matcher = coordinateContainer.matcher(string);

            boolean found = matcher.find();

            assert found;

            long x = Long.parseLong(matcher.group(1));
            long y = Long.parseLong(matcher.group(2));

            return new Location(x, y);
        }
    }
    
    private record Location(long x, long y) {
        private Location offset(long offset) {
            return new Location(x + offset, y + offset);
        }
    }
}
