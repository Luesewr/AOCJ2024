package aocj2024;

import java.awt.Point;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;
import org.apache.commons.lang3.tuple.Pair;

public class Day18 extends Day {
    @Override
    public void part1() {
        List<String> lines = getLinesFromFile("day18.txt").toList();

        int width = 70, height = 70;

        MemorySpace memorySpace = MemorySpace.parse(lines, width, height);

        int fastestEscapeLength = memorySpace.getFastestEscapeLength(1024);

        System.out.println(fastestEscapeLength);
    }

    @Override
    public void part2() {
        List<String> lines = getLinesFromFile("day18.txt").toList();

        int width = 70, height = 70;

        MemorySpace memorySpace = MemorySpace.parse(lines, width, height);

        int heightLimit = lines.size();

        while (heightLimit >= 0 && memorySpace.getFastestEscapeLength(heightLimit) < 0) heightLimit--;

        Optional<FallingByte> fallingByte = memorySpace.findByte(heightLimit);

        assert fallingByte.isPresent();

        Point location = fallingByte.get().location;

        System.out.println(location.x + "," + location.y);
    }

    private record MemorySpace(FallingByte[][] grid, int width, int height) {
        private int getFastestEscapeLength(int heightLimit) {
            PriorityQueue<Pair<FallingByte, Integer>> pq = new PriorityQueue<>(Comparator.comparingInt(pair -> {
                FallingByte fallingByte = pair.getLeft();
                int pathLength = pair.getRight();
                return pathLength + Math.abs(fallingByte.location.x - width) + Math.abs(fallingByte.location.y - height);
            }));

            pq.add(Pair.of(grid[0][0], 0));

            Set<FallingByte> visited = new HashSet<>();

            while (!pq.isEmpty()) {
                Pair<FallingByte, Integer> element = pq.poll();

                FallingByte fallingByte = element.getLeft();
                int currentPathLength = element.getRight();

                if (visited.contains(fallingByte)) continue;

                visited.add(fallingByte);

                if (fallingByte.height < heightLimit) {
                    continue;
                }

                if (fallingByte.equals(grid[height][width])) {
                    return currentPathLength;
                }

                addNextBytes(pq, fallingByte, currentPathLength + 1);
            }

            return -1;
        }

        private boolean isInBounds(int x, int y) {
            return 0 <= x && x <= width && 0 <= y && y <= height;
        }

        private void addIfInBounds(PriorityQueue<Pair<FallingByte, Integer>> pq, int x, int y, int length) {
            if (isInBounds(x, y)) pq.add(Pair.of(grid[y][x], length));
        }

        private void addNextBytes(PriorityQueue<Pair<FallingByte, Integer>> pq, FallingByte fallingByte, int length) {
            addIfInBounds(pq, fallingByte.location.x + 1, fallingByte.location.y, length);
            addIfInBounds(pq, fallingByte.location.x - 1, fallingByte.location.y, length);
            addIfInBounds(pq, fallingByte.location.x, fallingByte.location.y + 1, length);
            addIfInBounds(pq, fallingByte.location.x, fallingByte.location.y - 1, length);
        }

        private Optional<FallingByte> findByte(int height) {
            return Arrays.stream(grid)
                    .flatMap(Arrays::stream)
                    .filter(fallingByte -> fallingByte.height == height)
                    .findFirst();
        }

        private static MemorySpace parse(List<String> lines, int width, int height) {
            FallingByte[][] grid = new FallingByte[height + 1][width + 1];

            IntStream.range(0, lines.size())
                    .mapToObj(fallingHeight -> FallingByte.parse(lines.get(fallingHeight), fallingHeight))
                    .forEach(fallingByte -> grid[fallingByte.location.y][fallingByte.location.x] = fallingByte);

            for (int y = 0, gridLength = grid.length; y < gridLength; y++) {
                FallingByte[] byteLine = grid[y];

                for (int x = 0; x < byteLine.length; x++) {
                    if (grid[y][x] != null) continue;

                    grid[y][x] = new FallingByte(new Point(x, y), Integer.MAX_VALUE);
                }
            }

            return new MemorySpace(grid, width, height);
        }
    }

    private record FallingByte(Point location, int height) {
        private static final Pattern pattern = Pattern.compile("(\\d+),(\\d+)");

        private static FallingByte parse(String line, int fallingHeight) {
            Matcher matcher = pattern.matcher(line);

            boolean found = matcher.find();

            assert found;

            int x = Integer.parseInt(matcher.group(1));
            int y = Integer.parseInt(matcher.group(2));

            return new FallingByte(new Point(x, y), fallingHeight);
        }
    }
}
