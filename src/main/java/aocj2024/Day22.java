package aocj2024;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.LongSummaryStatistics;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class Day22 extends Day {
    @Override
    public void part1() {
        List<String> lines = getLinesFromFile("day22.txt").toList();

        List<SecretKey> secretKeys = lines.stream().map(Long::parseLong).map(SecretKey::new).toList();

        for (int i = 0; i < 2000; i++) {
            secretKeys = secretKeys.stream().map(SecretKey::next).toList();
        }

        long totalSecretKeys = secretKeys.stream().mapToLong(SecretKey::key).sum();

        System.out.println(totalSecretKeys);
    }

    @Override
    public void part2() {
        List<String> lines = getLinesFromFile("day22.txt").toList();

        List<Monkey> monkeys = lines.stream().map(Long::parseLong).map(SecretKey::new).map(Monkey::new).toList();

        List<Map<List<Long>, Long>> sequenceLookups = monkeys.stream().map(monkey -> monkey.getCombinationPrices(2000, 4)).toList();

        Optional<Long> optionalMostBananas = sequenceLookups.stream()
                .flatMap(map -> map.entrySet().stream())
                .collect(Collectors.groupingBy(
                        Map.Entry::getKey,
                        Collectors.summarizingLong(Map.Entry::getValue)
                ))
                .values().stream()
                .map(LongSummaryStatistics::getSum)
                .max(Long::compare);

        assert optionalMostBananas.isPresent();

        long mostBananas = optionalMostBananas.get();

        System.out.println(mostBananas);
    }

    private record SecretKey(long key) {
        private SecretKey next() {
            return step1().step2().step3();
        }

        private SecretKey step1() {
            return new SecretKey(((key * 64) ^ key) % 16777216);
        }

        private SecretKey step2() {
            return new SecretKey(((key / 32) ^ key) % 16777216);
        }

        private SecretKey step3() {
            return new SecretKey(((key * 2048) ^ key) % 16777216);
        }

        private long getPrice() {
            return key % 10;
        }
    }

    private record Monkey(SecretKey startingKey) {
        private Map<List<Long>, Long> getCombinationPrices(int iterations, int sequenceSizeLimit) {
            SecretKey secretKey = startingKey;

            LinkedList<Long> sequence = new LinkedList<>();

            Long previousPrice = null;

            Map<List<Long>, Long> sequenceLookup = new HashMap<>();

            for (int i = 0; i < iterations; i++) {
                secretKey = secretKey.next();
                long currentPrice = secretKey.getPrice();

                if (previousPrice != null) {
                    long difference = currentPrice - previousPrice;

                    sequence.addLast(difference);

                    if (sequence.size() > sequenceSizeLimit) {
                        sequence.removeFirst();
                    }

                    sequenceLookup.putIfAbsent(sequence.stream().toList(), currentPrice);
                }

                previousPrice = currentPrice;
            }

            return sequenceLookup;
        }
    }
}
