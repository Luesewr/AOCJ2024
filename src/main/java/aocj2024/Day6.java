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

// 684 too low
// 396 too low
// 1949 too high

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
        List<String> lines = getLinesFromFile("aoc06_input.txt").toList();
//        List<String> lines = getLinesFromFile("day6.txt").toList();
//        List<String> lines = getLinesFromFile("day6_sample.txt").toList();

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
            this.width = findWidth();
            this.height = findHeight();
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

            while (isGuardInBounds(guard)) {
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
            Set<Pair<Point, Integer>> loopTriggerLocations = new HashSet<>();

            while (isGuardInBounds(guard)) {
                if (loopTriggerLocations.contains(Pair.of(guard.getLocation(), guard.getRight())) &&
                        !guard.facesObstacle(obstacles)) {
                    possibleObstacles.add(guard.nextLocation());
                }

                if (guard.facesObstacle(obstacles)) {
                    Guard reverseGuard = guard.getEvilClone();
                    backtrackTriggerLocations(reverseGuard, loopTriggerLocations);
                    guard.turnRight();
                } else {
                    guard.moveToNextLocation();
                }
            }

            return possibleObstacles;
        }

        private void backtrackTriggerLocations(Guard guard, Set<Pair<Point, Integer>> loopTriggerLocations) {
            while (isGuardInBounds(guard) && !loopTriggerLocations.contains(Pair.of(guard.getLocation(), guard.getBehind()))) {
                loopTriggerLocations.add(Pair.of(guard.getLocation(), guard.getBehind()));

                if (guard.hasObstacleToRight(obstacles)) {
                    guard.turnLeft();
                    backtrackTriggerLocations(guard.getClone(), loopTriggerLocations);
                    guard.turnRight();
                }

                if (guard.facesObstacle(obstacles)) {
                    break;
                }

                guard.moveToNextLocation();
            }
        }

        private int findWidth() {
            return this.layout.get(0).length();
        }

        private int findHeight() {
            return this.layout.size();
        }

        private boolean isGuardInBounds(Guard guard) {
            return isPointInBounds(guard.location);
        }

        private boolean isPointInBounds(Point point) {
            return point.x >= 0 && point.x < this.width && point.y >= 0 && point.y < this.height;
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

        private void turnRight() {
            this.direction = getRight();
        }

        private void turnLeft() {
            this.direction = getLeft();
        }

        private void turnAround() {
            this.direction = getBehind();
        }

        private int getRight() {
            return (this.direction + 1) % 4;
        }

        private int getLeft() {
            return (this.direction + 3) % 4;
        }

        private int getBehind() {
            return (this.direction + 2) % 4;
        }

        private Point nextLocation() {
            Point directionTranslation = directionLookup.get(direction);
            Point newLocation = getLocation();
            newLocation.translate(directionTranslation.x, directionTranslation.y);

            return newLocation;
        }

        private boolean hasObstacleToRight(Set<Point> obstacles) {
            turnRight();
            boolean hasObstacle = facesObstacle(obstacles);
            turnLeft();
            return hasObstacle;
        }

        private boolean hasObstacleToLeft(Set<Point> obstacles) {
            turnLeft();
            boolean hasObstacle = facesObstacle(obstacles);
            turnRight();
            return hasObstacle;
        }

        private boolean facesObstacle(Set<Point> obstacles) {
            return obstacles.contains(nextLocation());
        }

        private void moveToNextLocation() {
            this.location = nextLocation();
        }

        protected Guard getClone() {
            return new Guard(getLocation(), direction);
        }

        protected Guard getEvilClone() {
            return new Guard(getLocation(), getBehind());
        }

        private Point getLocation() {
            return this.location.getLocation();
        }
    }
}
