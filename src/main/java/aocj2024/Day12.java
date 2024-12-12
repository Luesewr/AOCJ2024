package aocj2024;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.apache.commons.lang3.tuple.Pair;

public class Day12 extends Day {
    @Override
    public void part1() {
        List<String> lines = getLinesFromFile("day12.txt").toList();
        List<Region> regions = Region.parseRegions(lines);

        int totalPrice = regions.stream()
                .map(Region::getPrice)
                .reduce(0, Integer::sum);

        System.out.println(totalPrice);
    }

    @Override
    public void part2() {
        List<String> lines = getLinesFromFile("day12.txt").toList();
        List<Region> regions = Region.parseRegions(lines);

        int totalPrice = regions.stream()
                .map(Region::getBulkPrice)
                .reduce(0, Integer::sum);

        System.out.println(totalPrice);
    }

    private record GardenPot(Point location, char plantType, Set<GardenPot> neighbours) {
        private void getRegion(Set<GardenPot> regionGardenPots) {
            if (regionGardenPots.contains(this)) return;

            regionGardenPots.add(this);

            for (GardenPot neighbour : neighbours) {
                if (neighbour.plantType != this.plantType) continue;

                neighbour.getRegion(regionGardenPots);
            }
        }

        private Set<Fence> getFences() {
            Set<Point> possiblePoints = getPossibleNeighbourLocations();

            for (GardenPot neighbour : neighbours) {
                if (neighbour.plantType == this.plantType) possiblePoints.remove(neighbour.location);
            }

            return possiblePoints.stream()
                    .map(point -> new Fence(this.location, point))
                    .collect(Collectors.toSet());
        }

        private int getPerimeter() {
            return getFences().size();
        }

        private Set<Point> getPossibleNeighbourLocations() {
            List<Pair<Integer, Integer>> translations = List.of(
                    Pair.of(0, 1),
                    Pair.of(1, 0),
                    Pair.of(0, -1),
                    Pair.of(-1, 0)
            );

            Set<Point> possibleLocations = new HashSet<>();

            for (Pair<Integer, Integer> translation : translations) {
                Point possibleLocation = location.getLocation();
                possibleLocation.translate(translation.getLeft(), translation.getRight());
                possibleLocations.add(possibleLocation);
            }

            return possibleLocations;
        }

        private void addNeighbour(GardenPot neighbour) {
            neighbours.add(neighbour);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            GardenPot gardenPot = (GardenPot) o;
            return plantType == gardenPot.plantType && Objects.equals(location, gardenPot.location);
        }

        @Override
        public int hashCode() {
            return Objects.hash(location, plantType);
        }
    }

    private record Fence(Point source, Point destination) {
        private void getLine(Set<Fence> lineFences, Set<Fence> fencesAvailable) {
            if (lineFences.contains(this)) return;

            lineFences.add(this);

            for (Fence fence : fencesAvailable) {
                if (!this.isNeighbouring(fence)) continue;

                fence.getLine(lineFences, fencesAvailable);
            }
        }

        private boolean isNeighbouring(Fence fence) {
            return this.getDirection().equals(fence.getDirection()) && this.sourceDistance(fence) == 1 && this.destinationDistance(fence) == 1;
        }

        private Point getDirection() {
            int dx = destination.x - source.x;
            int dy = destination.y - source.y;

            return new Point(dx, dy);
        }

        private int sourceDistance(Fence fence) {
            return Math.abs(this.source.x - fence.source.x) + Math.abs(this.source.y - fence.source.y);
        }

        private int destinationDistance(Fence fence) {
            return Math.abs(this.destination.x - fence.destination.x) + Math.abs(this.destination.y - fence.destination.y);
        }
    }

    private record FenceLine(Set<Fence> fences) {
        private static Set<FenceLine> getLines(Set<Fence> fences) {
            Set<FenceLine> fencesLines = new HashSet<>();
            Set<Fence> usedFences = new HashSet<>();

            for (Fence fence : fences) {
                if (usedFences.contains(fence)) continue;

                Set<Fence> lineFences = new HashSet<>();

                fence.getLine(lineFences, fences);
                usedFences.addAll(lineFences);

                fencesLines.add(new FenceLine(lineFences));
            }

            return fencesLines;
        }
    }

    private record Region(char plantType, Set<GardenPot> gardenPots) {
        private int getArea() {
            return gardenPots.size();
        }

        private int getPerimeter() {
            return gardenPots.stream()
                    .map(GardenPot::getPerimeter)
                    .reduce(0, Integer::sum);
        }

        private Set<Fence> getFences() {
            return gardenPots.stream()
                    .map(GardenPot::getFences)
                    .flatMap(Set::stream)
                    .collect(Collectors.toSet());
        }

        private Set<FenceLine> getLines() {
            return FenceLine.getLines(getFences());
        }

        private int getLineCount() {
            return getLines().size();
        }

        private int getPrice() {
            return getArea() * getPerimeter();
        }

        private int getBulkPrice() {
            return getArea() * getLineCount();
        }

        private static List<Region> parseRegions(List<String> lines) {
            List<List<GardenPot>> gardenPotGrid = IntStream.range(0, lines.size())
                    .mapToObj(lineIndex -> IntStream.range(0, lines.get(lineIndex).length())
                            .mapToObj(potIndex -> new GardenPot(new Point(potIndex, lineIndex), lines.get(lineIndex).charAt(potIndex), new HashSet<>(4)))
                            .toList()
                    )
                    .toList();

            IntStream.range(0, gardenPotGrid.size())
                    .forEach(lineIndex -> IntStream.range(0, gardenPotGrid.get(lineIndex).size())
                            .forEach(entryIndex -> {
                                GardenPot gardenPot = gardenPotGrid.get(lineIndex).get(entryIndex);

                                if (lineIndex - 1 >= 0) {
                                    gardenPot.addNeighbour(gardenPotGrid.get(lineIndex - 1).get(entryIndex));
                                }
                                if (lineIndex + 1 < gardenPotGrid.size()) {
                                    gardenPot.addNeighbour(gardenPotGrid.get(lineIndex + 1).get(entryIndex));
                                }
                                if (entryIndex - 1 >= 0) {
                                    gardenPot.addNeighbour(gardenPotGrid.get(lineIndex).get(entryIndex - 1));
                                }
                                if (entryIndex + 1 < gardenPotGrid.get(lineIndex).size()) {
                                    gardenPot.addNeighbour(gardenPotGrid.get(lineIndex).get(entryIndex + 1));
                                }
                            }));

            Set<GardenPot> gardenPots = new HashSet<>();
            List<Region> regions = new ArrayList<>();

            for (GardenPot gardenPot : gardenPotGrid.stream().flatMap(List::stream).toList()) {
                if (gardenPots.contains(gardenPot)) continue;

                Set<GardenPot> regionGardenPots = new HashSet<>();

                gardenPot.getRegion(regionGardenPots);
                gardenPots.addAll(regionGardenPots);

                regions.add(new Region(gardenPot.plantType, regionGardenPots));
            }

            return regions;
        }
    }
}
