package aocj2024;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Day10 extends Day {

    @Override
    public void part1() {
        List<String> lines = getLinesFromFile("day10.txt").toList();
        TopographicalMap topologicalMap = TopographicalMap.parseFromGrid(lines);

        int trailheadCount = topologicalMap.countTrailheads();

        System.out.println(trailheadCount);
    }

    @Override
    public void part2() {
        List<String> lines = getLinesFromFile("day10.txt").toList();
        TopographicalMap topologicalMap = TopographicalMap.parseFromGrid(lines);

        int trailheadCount = topologicalMap.countHikingTrails();

        System.out.println(trailheadCount);
    }

    private record TopographicalMap(List<List<TopographicalEntry>> topologicalEntries) {
        private TopographicalMap(List<List<TopographicalEntry>> topologicalEntries) {
            this.topologicalEntries = topologicalEntries;
            assignNeighbours();
        }

        private int countTrailheads() {
            return topologicalEntries.stream()
                    .flatMap(List::stream)
                    .filter(TopographicalEntry::isStart)
                    .map(TopographicalEntry::getTrailheads)
                    .map(Set::size)
                    .reduce(0, Integer::sum);
        }

        private int countHikingTrails() {
            return topologicalEntries.stream()
                    .flatMap(List::stream)
                    .filter(TopographicalEntry::isHead)
                    .map(TopographicalEntry::countHikingTrails)
                    .reduce(0, Integer::sum);
        }

        private void assignNeighbours() {
            IntStream.range(0, topologicalEntries.size())
                    .forEach(lineIndex -> IntStream.range(0, topologicalEntries.get(lineIndex).size())
                            .forEach(entryIndex -> {
                                TopographicalEntry entry = topologicalEntries.get(lineIndex).get(entryIndex);

                                if (lineIndex - 1 >= 0) {
                                    entry.addNeighbour(topologicalEntries.get(lineIndex - 1).get(entryIndex));
                                }
                                if (lineIndex + 1 < topologicalEntries.size()) {
                                    entry.addNeighbour(topologicalEntries.get(lineIndex + 1).get(entryIndex));
                                }
                                if (entryIndex - 1 >= 0) {
                                    entry.addNeighbour(topologicalEntries.get(lineIndex).get(entryIndex - 1));
                                }
                                if (entryIndex + 1 < topologicalEntries.get(lineIndex).size()) {
                                    entry.addNeighbour(topologicalEntries.get(lineIndex).get(entryIndex + 1));
                                }
                            }));
        }

        private static TopographicalMap parseFromGrid(List<String> lines) {
            List<List<TopographicalEntry>> topologicalEntries = IntStream.range(0, lines.size())
                    .mapToObj(lineIndex -> IntStream.range(0, lines.get(lineIndex).length())
                            .mapToObj(entryIndex -> {
                                char character = lines.get(lineIndex).charAt(entryIndex);
                                Set<TopographicalEntry> neighbours = new HashSet<>();
                                int height = Integer.parseInt(Character.toString(character));
                                return new TopographicalEntry(new Point(entryIndex, lineIndex), height, neighbours);
                            })
                            .toList())
                    .toList();

            return new TopographicalMap(topologicalEntries);
        }
    }

    private record TopographicalEntry(Point location, int height, Set<TopographicalEntry> neighbours) {
        private void addNeighbour(TopographicalEntry neighbour) {
            this.neighbours.add(neighbour);
        }

        private boolean isStart() {
            return this.height == 0;
        }

        private boolean isHead() {
            return this.height == 9;
        }

        private Set<TopographicalEntry> getTrailheads() {
            return getTrailhead(1);
        }

        private Set<TopographicalEntry> getTrailhead(int searchHeight) {
            if (searchHeight == 10 && height == 9) {
                return Set.of(this);
            }

            return neighbours.stream()
                    .filter(topographicalEntry -> topographicalEntry.height == searchHeight)
                    .map(topographicalEntry -> topographicalEntry.getTrailhead(searchHeight + 1))
                    .flatMap(Set::stream)
                    .collect(Collectors.toSet());
        }

        private int countHikingTrails() {
            return countHikingTrails(8);
        }

        private int countHikingTrails(int searchHeight) {
            if (searchHeight == -1 && height == 0) {
                return 1;
            }

            return neighbours.stream()
                    .filter(topographicalEntry -> topographicalEntry.height == searchHeight)
                    .map(topographicalEntry -> topographicalEntry.countHikingTrails(searchHeight - 1))
                    .reduce(0, Integer::sum);
        }

        @Override
        public String toString() {
            return "Entry{" +
                    "location=" + location +
                    ", height=" + height +
                    ", neighbours=" + neighbours.stream().map(TopographicalEntry::location).toList() +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) return false;
            TopographicalEntry that = (TopographicalEntry) o;
            return height == that.height && Objects.equals(location, that.location);
        }

        @Override
        public int hashCode() {
            return Objects.hash(location, height);
        }
    }
}
