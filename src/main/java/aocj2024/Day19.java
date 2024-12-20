package aocj2024;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

public class Day19 extends Day {
    @Override
    public void part1() {
        List<String> lines = getLinesFromFile("day19.txt").toList();

        List<String> availableTowelLines = lines.stream().takeWhile(string -> !string.isEmpty()).toList();
        List<String> stripePatternLines = lines.stream().dropWhile(string -> !string.isEmpty()).skip(1).toList();

        List<Towel> availableTowels = Towel.parse(availableTowelLines);
        List<StripePattern> stripePatterns = StripePattern.parse(stripePatternLines);

        Optional<Integer> optionalBiggestTowel = availableTowels.stream().map(Towel::colors).map(String::length).max(Integer::compareTo);
        assert optionalBiggestTowel.isPresent();
        int biggestTowel = optionalBiggestTowel.get();

        long possiblePatternCount = stripePatterns.stream()
                .filter(stripePattern -> stripePattern.getArrangementCount(availableTowels, biggestTowel) > 0)
                .count();

        System.out.println(possiblePatternCount);
    }

    @Override
    public void part2() {
        List<String> lines = getLinesFromFile("day19.txt").toList();

        List<String> availableTowelLines = lines.stream().takeWhile(string -> !string.isEmpty()).toList();
        List<String> stripePatternLines = lines.stream().dropWhile(string -> !string.isEmpty()).skip(1).toList();

        List<Towel> availableTowels = Towel.parse(availableTowelLines);
        List<StripePattern> stripePatterns = StripePattern.parse(stripePatternLines);

        Optional<Integer> optionalBiggestTowel = availableTowels.stream().map(Towel::colors).map(String::length).max(Integer::compareTo);
        assert optionalBiggestTowel.isPresent();
        int biggestTowel = optionalBiggestTowel.get();

        long possiblePatternCount = stripePatterns.stream()
                .map(stripePattern -> stripePattern.getArrangementCount(availableTowels, biggestTowel))
                .reduce(0L, Long::sum);

        System.out.println(possiblePatternCount);
    }

    private record Towel(String colors) {
        private static List<Towel> parse(List<String> lines) {
            return lines.stream()
                    .flatMap(string -> Arrays.stream(string.split(", "))
                            .map(Towel::new)
                    )
                    .toList();
        }
    }

    private record StripePattern(String colors) {
        private long getArrangementCount(List<Towel> towels, int maxTowelSize) {
            long[] visitedIndices = new long[colors.length() + maxTowelSize];

            visitedIndices[0] = 1;

            IntStream.range(0, colors.length())
                    .filter(i -> visitedIndices[i] != 0)
                    .forEach(i -> IntStream.rangeClosed(1, maxTowelSize)
                            .filter(length -> i + length <= colors.length())
                            .mapToObj(length -> new Towel(colors.substring(i, i + length)))
                            .map(towels::indexOf)
                            .filter(index -> index >= 0)
                            .map(towels::get)
                            .map(Towel::colors)
                            .map(String::length)
                            .distinct()
                            .forEach(length -> visitedIndices[i + length] += visitedIndices[i])
                    );

            return visitedIndices[colors.length()];
        }

        private static List<StripePattern> parse(List<String> lines) {
            return lines.stream().map(StripePattern::new).toList();
        }
    }
}
