package aocj2024;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.lang3.tuple.Pair;

public class Day5 extends Day {
    @Override
    public void part1() {
        List<String> lines = getLinesFromFile("day5.txt").toList();
        Map<Integer, Set<Integer>> shouldBeBefore = lines.stream()
                .takeWhile(string -> !string.isEmpty())
                .map(string -> string.split("\\|"))
                .map(strings -> Pair.of(Integer.parseInt(strings[0]), Integer.parseInt(strings[1])))
                .collect(Collectors.groupingBy(
                        Pair::getLeft,
                        Collectors.mapping(Pair::getRight, Collectors.toSet())
                ));

        int correctUpdates = lines.stream().dropWhile(string -> !string.isEmpty()).skip(1)
                .takeWhile(string -> !string.isEmpty())
                .map(string -> Arrays.stream(string.split(",")).map(Integer::parseInt).toList())
                .filter(integers -> integers.stream()
                        .allMatch(integer -> integers.stream()
                                .takeWhile(i -> !Objects.equals(i, integer))
                                .noneMatch(i -> shouldBeBefore.containsKey(integer) && shouldBeBefore.get(integer).contains(i))
                        ))
                .map(integers -> integers.get((integers.size() - 1) / 2))
                .reduce(0, Integer::sum);

        System.out.println(correctUpdates);
    }

    @Override
    public void part2() {
        List<String> lines = getLinesFromFile("day5.txt").toList();
        Map<Integer, Set<Integer>> shouldBeBefore = lines.stream()
                .takeWhile(string -> !string.isEmpty())
                .map(string -> string.split("\\|"))
                .map(strings -> Pair.of(Integer.parseInt(strings[0]), Integer.parseInt(strings[1])))
                .collect(Collectors.groupingBy(
                        Pair::getLeft,
                        Collectors.mapping(Pair::getRight, Collectors.toSet())
                ));

        int correctedUpdates = lines.stream().dropWhile(string -> !string.isEmpty()).skip(1)
                .takeWhile(string -> !string.isEmpty())
                .map(string -> Arrays.stream(string.split(",")).map(Integer::parseInt).toList())
                .filter(integers -> integers.stream()
                        .anyMatch(integer -> integers.stream()
                                .takeWhile(i -> !Objects.equals(i, integer))
                                .anyMatch(i -> shouldBeBefore.containsKey(integer) && shouldBeBefore.get(integer).contains(i))
                        ))
                .map(integers -> integers.stream()
                        .sorted((o1, o2) -> shouldBeBefore.containsKey(o1) && shouldBeBefore.get(o1).contains(o2) ? -1 : (
                                shouldBeBefore.containsKey(o2) && shouldBeBefore.get(o2).contains(o1) ? 1 : 0
                        )).collect(Collectors.toList()))
                .map(integers -> integers.get((integers.size() - 1) / 2))
                .reduce(0, Integer::sum);

        System.out.println(correctedUpdates);
    }
}
