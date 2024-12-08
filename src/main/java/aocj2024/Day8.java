package aocj2024;

import java.awt.Point;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.apache.commons.lang3.tuple.Pair;

public class Day8 extends Day {
    @Override
    public void part1() {
        List<String> lines = getLinesFromFile("day8.txt").toList();

        int width = lines.get(0).length(), height = lines.size();

        Map<Character, Set<Point>> antennas = IntStream.range(0, lines.size())
                .mapToObj(stringIndex -> IntStream.range(0, lines.get(stringIndex).length())
                        .filter(charIndex -> lines.get(stringIndex).charAt(charIndex) != '.')
                        .mapToObj(charIndex -> Pair.of(
                                lines.get(stringIndex).charAt(charIndex),
                                new Point(charIndex, stringIndex)
                        ))
                )
                .flatMap(Function.identity())
                .collect(Collectors.groupingBy(
                        Pair::getLeft,
                        Collectors.mapping(Pair::getRight, Collectors.toSet())
                ));

        Set<Point> uniqueAntiNodes = antennas.values().stream()
                .flatMap(antennasList -> antennasList.stream()
                        .flatMap(point1 -> antennasList.stream()
                                .filter(point2 -> !point1.equals(point2))
                                .map(point2 ->
                                    new Point(add(point2, difference(point1, point2))
                                ))
                        )
                )
                .filter(point -> isInBounds(point, width, height))
                .collect(Collectors.toSet());

        System.out.println(uniqueAntiNodes.size());
    }

    @Override
    public void part2() {
        List<String> lines = getLinesFromFile("day8.txt").toList();

        int width = lines.get(0).length(), height = lines.size();

        Map<Character, List<Point>> antennas = IntStream.range(0, lines.size())
                .mapToObj(stringIndex -> IntStream.range(0, lines.get(stringIndex).length())
                        .filter(charIndex -> lines.get(stringIndex).charAt(charIndex) != '.')
                        .mapToObj(charIndex -> Pair.of(
                                lines.get(stringIndex).charAt(charIndex),
                                new Point(charIndex, stringIndex)
                        ))
                )
                .flatMap(Function.identity())
                .collect(Collectors.groupingBy(
                        Pair::getLeft,
                        Collectors.mapping(Pair::getRight, Collectors.toList())
                ));

        Set<Point> uniqueAntiNodes = antennas.values().stream()
                .flatMap(antennasList -> antennasList.stream()
                        .flatMap(point1 -> antennasList.stream()
                                .filter(point2 -> !point1.equals(point2))
                                .flatMap(point2 -> IntStream.iterate(1, n -> n + 1)
                                        .mapToObj(n -> new Point(add(point2, multiply(difference(point1, point2), n))))
                                        .takeWhile(point -> isInBounds(point, width, height))
                                )
                        )
                )
                .collect(Collectors.toSet());

        uniqueAntiNodes.addAll(antennas.values().stream().flatMap(Collection::stream).toList());

        System.out.println(uniqueAntiNodes.size());
    }

    private static Point difference(Point point1, Point point2) {
        int dx = point2.x - point1.x;
        int dy = point2.y - point1.y;

        return new Point(dx, dy);
    }

    private static Point add(Point point1, Point point2) {
        int newX = point2.x + point1.x;
        int newY = point2.y + point1.y;

        return new Point(newX, newY);
    }

    private static Point multiply(Point point, int scalar) {
        int newX = point.x * scalar;
        int newY = point.y * scalar;

        return new Point(newX, newY);
    }

    private static boolean isInBounds(Point point, int width, int height) {
        return point.x >= 0 && point.y >= 0 && point.x < width && point.y < height;
    }
}
