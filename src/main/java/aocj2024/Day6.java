package aocj2024;

import java.awt.Point;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.apache.commons.lang3.tuple.Pair;

public class Day6 extends Day {
    @Override
    public void part1() {
        List<String> lines = getLinesFromFile("day6.txt").toList();

        Laboratory laboratory = new Laboratory(lines);
        Point startingLocation = laboratory.findStartingLocation();

        assert startingLocation != null;

        Guard guard = new Guard(startingLocation);

        Set<Point> traversedLocations = laboratory.getTraverseLaboratoryLocations(guard);

        System.out.println(traversedLocations.size());
    }

    @Override
    public void part2() {
        List<String> lines = getLinesFromFile("day6.txt").toList();

        Laboratory laboratory = new Laboratory(lines);
        Point startingLocation = laboratory.findStartingLocation();

        assert startingLocation != null;

        Guard guard = new Guard(startingLocation);

        Set<Point> loopingLocations = laboratory.getPossibleLoopingObstacles(guard);

        System.out.println(loopingLocations.size());
    }

    private static class Laboratory {
        private final List<String> layout;
        private final Set<Point> obstacles;
        private final int width;
        private final int height;

        private Laboratory(List<String> layout) {
            this.layout = layout;
            this.width = layout.get(0).length();
            this.height = layout.size();
            this.obstacles = readInObstacles();
        }

        private Set<Point> readInObstacles() {
            return IntStream.range(0, this.height)
                    .mapToObj(stringIndex -> IntStream.range(0, this.width)
                            .filter(characterIndex -> this.layout.get(stringIndex).charAt(characterIndex) == '#')
                            .mapToObj(characterIndex -> new Point(characterIndex, stringIndex))
                    )
                    .flatMap(Function.identity())
                    .collect(Collectors.toSet());
        }

        private Point findStartingLocation() {
            return IntStream.range(0, this.height)
                    .mapToObj(stringIndex -> IntStream.range(0, this.layout.get(stringIndex).length())
                            .filter(characterIndex -> this.layout.get(stringIndex).charAt(characterIndex) == '^')
                            .mapToObj(characterIndex -> new Point(characterIndex, stringIndex))
                            .findFirst()
                    )
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .findFirst()
                    .orElse(null);
        }

        public Set<Point> getTraverseLaboratoryLocations(Guard guard) {
            Set<Point> visitedLocations = new HashSet<>();

            while (guard.isInBounds(this)) {
                visitedLocations.add(guard.location.getLocation());

                if (obstacles.contains(guard.nextLocation())) {
                    guard.turnRight();
                } else {
                    guard.moveToNextLocation();
                }
            }

            return visitedLocations;
        }

        private Set<Point> getPossibleLoopingObstacles(Guard guard) {
            Set<Point> possibleObstacles = new HashSet<>();
            Set<Point> visitedLocations = new HashSet<>();

            while (guard.isInBounds(this)) {
                visitedLocations.add(guard.location);

                if (guard.facesObstacle(obstacles)) {
                    guard.turnRight();
                } else {
                    if (!visitedLocations.contains(guard.nextLocation()) && checkForLoop(guard)) {
                        possibleObstacles.add(guard.nextLocation());
                    }

                    guard.moveToNextLocation();
                }
            }

            return possibleObstacles;
        }

        private boolean checkForLoop(Guard originGuard) {
            boolean looping = false;

            obstacles.add(originGuard.nextLocation());

            Guard guard = originGuard.getClone().turnRight();

            Set<Pair<Point, Integer>> visitedSpots = new HashSet<>();

            while (guard.isInBounds(this)) {
                Pair<Point, Integer> spot = Pair.of(guard.getLocation(), guard.direction);

                if (visitedSpots.contains(spot)) {
                    looping = true;
                    break;
                }

                visitedSpots.add(spot);

                if (guard.facesObstacle(obstacles)) {
                    guard.turnRight();
                } else {
                    guard.moveToNextLocation();
                }
            }

            obstacles.remove(originGuard.nextLocation());

            return looping;
        }
    }

    protected static class Guard {
        private static final Map<Integer, Point> directionLookup = Map.of(
                0, new Point(0, -1),
                1, new Point(1, 0),
                2, new Point(0, 1),
                3, new Point(-1, 0)
        );

        private Point location;
        private int direction;

        private Guard(Point location) {
            this.location = location;
            this.direction = 0;
        }

        private Guard(Point location, int direction) {
            this.location = location;
            this.direction = direction;
        }

        private Guard turnRight() {
            this.direction = getRight();
            return this;
        }

        private int getRight() {
            return (this.direction + 1) % 4;
        }

        private Point nextLocation() {
            Point directionTranslation = directionLookup.get(direction);
            Point newLocation = getLocation();
            newLocation.translate(directionTranslation.x, directionTranslation.y);

            return newLocation;
        }

        private boolean facesObstacle(Set<Point> obstacles) {
            return obstacles.contains(nextLocation());
        }

        private void moveToNextLocation() {
            this.location = nextLocation();
        }

        private boolean isInBounds(Laboratory laboratory) {
            return location.x >= 0 && location.x < laboratory.width && location.y >= 0 && location.y < laboratory.height;
        }

        protected Guard getClone() {
            return new Guard(getLocation(), direction);
        }

        private Point getLocation() {
            return this.location.getLocation();
        }
    }
}
