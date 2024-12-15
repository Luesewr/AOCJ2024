package aocj2024;

import java.awt.Point;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Day15 extends Day {
    @Override
    public void part1() {
        List<String> lines = getLinesFromFile("day15.txt").toList();

        Warehouse warehouse = Warehouse.parse(lines, 1);

        List<Instruction> instructionSet = Instruction.parse(lines);

        warehouse.executeInstructions(instructionSet);

        System.out.println(warehouse.sumGPSCoordinates());
    }

    @Override
    public void part2() {
        List<String> lines = getLinesFromFile("day15.txt").toList();

        Warehouse warehouse = Warehouse.parse(lines, 2);

        List<Instruction> instructionSet = Instruction.parse(lines);

        warehouse.executeInstructions(instructionSet);

        System.out.println(warehouse.sumGPSCoordinates());
    }

    private record Warehouse(WarehouseRobot robot, Map<Point, WarehouseObstacle> obstacles, int width, int height) {
        private void executeInstructions(List<Instruction> instructionSet) {
            for (Instruction instruction : instructionSet) {
                robot.executeInstruction(this, instruction);
            }
        }

        private long sumGPSCoordinates() {
            return obstacles.values().stream()
                    .filter(warehouseObstacle -> warehouseObstacle instanceof WarehouseBox)
                    .distinct()
                    .map(WarehouseObstacle::getGPSCoordinates)
                    .reduce(0L, Long::sum);
        }

        private static Warehouse parse(List<String> lines, int cellWidth) {
            int width = lines.get(0).length();
            int height = lines.indexOf("");

            Map<Point, WarehouseObstacle> obstaclesLookup = new HashMap<>();
            Set<WarehouseObstacle> obstacles = Stream.concat(
                    WarehouseBox.parse(lines, cellWidth),
                    WarehouseWall.parse(lines, cellWidth)
            ).collect(Collectors.toSet());

            for (WarehouseObstacle obstacle : obstacles) {
                for (int i = 0; i < obstacle.width(); i++) {
                    obstaclesLookup.put(new Point(obstacle.location().x + i, obstacle.location().y), obstacle);
                }
            }

            WarehouseRobot robot = WarehouseRobot.parse(lines, cellWidth);

            return new Warehouse(robot, obstaclesLookup, width, height);
        }
    }

    private interface WarehouseObstacle {
        default void executeInstruction(Warehouse warehouse, Instruction instruction) {
            if (isPushable(warehouse, instruction)) {
                push(warehouse, instruction, new HashSet<>());
            }
        }

        default boolean isPushable(Warehouse warehouse, Instruction instruction) {
            Set<WarehouseObstacle> pushableObstacles = getPushableObstacles(warehouse, instruction);

            if (pushableObstacles.isEmpty()) return true;

            return pushableObstacles.stream()
                    .allMatch(obstacle -> obstacle.isPushable(warehouse, instruction));
        }

        default void push(Warehouse warehouse, Instruction instruction, Set<WarehouseObstacle> pushedObstacles) {
            if (pushedObstacles.contains(this)) return;

            pushedObstacles.add(this);

            Set<WarehouseObstacle> pushableObstacles = getPushableObstacles(warehouse, instruction);

            pushableObstacles
                    .forEach(obstacle -> obstacle.push(warehouse, instruction, pushedObstacles));

            move(warehouse, instruction);
        }

        default void move(Warehouse warehouse, Instruction instruction) {
            Point instructionLocation = getInstructionLocation(instruction);

            for (int i = 0; i < width(); i++) {
                warehouse.obstacles.remove(new Point(location().x + i, location().y));
            }

            location().move(instructionLocation.x, instructionLocation.y);

            for (int i = 0; i < width(); i++) {
                warehouse.obstacles.put(new Point(location().x + i, location().y), this);
            }
        }

        default Set<WarehouseObstacle> getPushableObstacles(Warehouse warehouse, Instruction instruction) {
            return getInstructionLocations(instruction).stream()
                    .map(warehouse.obstacles::get)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
        }

        default Set<Point> getInstructionLocations(Instruction instruction) {
            return IntStream.range(0, width())
                    .mapToObj(i -> Stream.of(getInstructionLocation(instruction))
                            .peek(point -> point.translate(i, 0)))
                    .flatMap(Function.identity())
                    .filter(point -> !(point.x >= location().x && point.x <= location().x + width() - 1 && point.y == location().y))
                    .collect(Collectors.toSet());
        }

        default Point getInstructionLocation(Instruction instruction) {
            Point instructionLocation = location().getLocation();

            Point translation = instruction.getTranslation();
            instructionLocation.translate(translation.x, translation.y);

            return instructionLocation;
        }

        default long getGPSCoordinates() {
            return location().y * 100L + location().x;
        }

        Point location();

        int width();
    }

    private record WarehouseRobot(Point location, int width) implements WarehouseObstacle {
        private static WarehouseRobot parse(List<String> lines, int cellWidth) {
            List<String> gridLines = lines.stream()
                    .takeWhile(string -> !string.isEmpty())
                    .toList();

            return IntStream.range(0, gridLines.size())
                    .mapToObj(lineIndex -> IntStream.range(0, gridLines.get(lineIndex).length())
                            .filter(i -> gridLines.get(lineIndex).charAt(i) == '@')
                            .mapToObj(i -> new WarehouseRobot(new Point(i * cellWidth, lineIndex), 1))
                            .findFirst()
                    )
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .findFirst().orElse(null);
        }
    }

    private record WarehouseBox(Point location, int width) implements WarehouseObstacle {
        private static Stream<WarehouseObstacle> parse(List<String> lines, int cellWidth) {
            List<String> gridLines = lines.stream()
                    .takeWhile(string -> !string.isEmpty())
                    .toList();

            return IntStream.range(0, gridLines.size())
                    .mapToObj(lineIndex -> IntStream.range(0, gridLines.get(lineIndex).length())
                            .filter(i -> gridLines.get(lineIndex).charAt(i) == 'O')
                            .mapToObj(i -> new WarehouseBox(new Point(i * cellWidth, lineIndex), cellWidth))
                    )
                    .flatMap(Function.identity());
        }
    }

    private record WarehouseWall(Point location, int width) implements WarehouseObstacle {
        @Override
        public void push(Warehouse warehouse, Instruction instruction, Set<WarehouseObstacle> pushedObstacles) {
        }

        @Override
        public boolean isPushable(Warehouse warehouse, Instruction instruction) {
            return false;
        }

        private static Stream<WarehouseObstacle> parse(List<String> lines, int cellWidth) {
            List<String> gridLines = lines.stream()
                    .takeWhile(string -> !string.isEmpty())
                    .toList();

            return IntStream.range(0, gridLines.size())
                    .mapToObj(lineIndex -> IntStream.range(0, gridLines.get(lineIndex).length())
                            .filter(i -> gridLines.get(lineIndex).charAt(i) == '#')
                            .mapToObj(i -> new WarehouseWall(new Point(i * cellWidth, lineIndex), cellWidth))
                    )
                    .flatMap(Function.identity());
        }
    }

    private record Instruction(char direction) {
        private Point getTranslation() {
            return switch (direction) {
                case '^' -> new Point(0, -1);
                case '>' -> new Point(1, 0);
                case 'v' -> new Point(0, 1);
                case '<' -> new Point(-1, 0);
                default -> new Point(0, 0);
            };
        }

        private static List<Instruction> parse(List<String> lines) {
            return lines.stream()
                    .dropWhile(string -> !string.isEmpty())
                    .skip(1)
                    .flatMap(string -> string.chars()
                            .mapToObj(direction -> new Instruction((char) direction))
                    )
                    .toList();
        }
    }
}
