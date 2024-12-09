package aocj2024;

import java.awt.Point;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Day8 extends Day {
    @Override
    public void part1() {
        List<String> lines = getLinesFromFile("day8.txt").toList();
        int width = lines.get(0).length(), height = lines.size();

        Stream<FrequencyContainer> antennaFrequencyContainers = FrequencyContainer.parseFromGrid(lines);

        Set<Point> uniqueAntiNodeLocations = FrequencyContainer.getProjectedAntiNodes(
                        antennaFrequencyContainers,
                        () -> IntStream.of(1),
                        width,
                        height
                )
                .map(AntiNode::location)
                .collect(Collectors.toSet());

        System.out.println(uniqueAntiNodeLocations.size());
    }

    @Override
    public void part2() {
        List<String> lines = getLinesFromFile("day8.txt").toList();
        int width = lines.get(0).length(), height = lines.size();

        Stream<FrequencyContainer> antennaFrequencyContainers = FrequencyContainer.parseFromGrid(lines);

        Set<Point> uniqueAntiNodeLocations = FrequencyContainer.getProjectedAntiNodes(
                        antennaFrequencyContainers,
                        () -> IntStream.iterate(0, n -> n + 1),
                        width,
                        height
                )
                .map(AntiNode::location)
                .collect(Collectors.toSet());

        System.out.println(uniqueAntiNodeLocations.size());
    }

    private record FrequencyContainer(char frequency, Set<Antenna> antennas) {
        private Stream<AntiNode> projectAntiNodes(Supplier<IntStream> supplier, int width, int height) {
            return antennas.stream()
                    .flatMap(antenna1 -> antennas.stream()
                            .filter(antenna2 -> !antenna1.equals(antenna2))
                            .flatMap(antenna2 -> supplier.get()
                                    .mapToObj(n -> new AntiNode(
                                            antenna2.add(antenna2.difference(antenna1).multiply(n)).location,
                                            frequency
                                    ))
                                    .takeWhile(antiNode -> antiNode.isInBounds(width, height))
                            )
                    );
        }

        private static Stream<FrequencyContainer> sortByFrequency(Stream<Antenna> antennas) {
            return antennas
                    .collect(Collectors.groupingBy(
                            Antenna::frequency,
                            Collectors.mapping(Function.identity(), Collectors.toSet())
                    ))
                    .entrySet().stream()
                    .map(characterListEntry -> new FrequencyContainer(
                            characterListEntry.getKey(),
                            characterListEntry.getValue()
                    ));
        }

        private static Stream<FrequencyContainer> parseFromGrid(List<String> lines) {
            Stream<Antenna> antennas = Antenna.parseFromGrid(lines);
            return FrequencyContainer.sortByFrequency(antennas);
        }

        private static Stream<AntiNode> getProjectedAntiNodes(Stream<FrequencyContainer> frequencyContainers, Supplier<IntStream> supplier, int width, int height) {
            return frequencyContainers
                    .flatMap(frequencyContainer -> frequencyContainer.projectAntiNodes(
                            supplier,
                            width,
                            height
                    ));
        }
    }

    private record Antenna(Point location, char frequency) {
        private Antenna add(Antenna antenna) {
            int newX = this.location.x + antenna.location.x;
            int newY = this.location.y + antenna.location.y;

            return new Antenna(new Point(newX, newY), frequency);
        }

        private Antenna difference(Antenna antenna) {
            int newX = this.location.x - antenna.location.x;
            int newY = this.location.y - antenna.location.y;

            return new Antenna(new Point(newX, newY), frequency);
        }

        private Antenna multiply(int scalar) {
            int newX = this.location.x * scalar;
            int newY = this.location.y * scalar;

            return new Antenna(new Point(newX, newY), frequency);
        }

        private static Stream<Antenna> parseFromGrid(List<String> lines) {
            return IntStream.range(0, lines.size())
                    .mapToObj(stringIndex -> IntStream.range(0, lines.get(stringIndex).length())
                            .filter(charIndex -> lines.get(stringIndex).charAt(charIndex) != '.')
                            .mapToObj(charIndex -> new Antenna(
                                    new Point(charIndex, stringIndex),
                                    lines.get(stringIndex).charAt(charIndex)
                            ))
                    )
                    .flatMap(Function.identity());
        }
    }

    private record AntiNode(Point location, char frequency) {
        private boolean isInBounds(int width, int height) {
            return location.x >= 0 && location.y >= 0 && location.x < width && location.y < height;
        }
    }
}
