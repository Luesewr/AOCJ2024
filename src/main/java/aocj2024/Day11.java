package aocj2024;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Day11 extends Day {
    @Override
    public void part1() {
        String line = getLinesFromFile("day11.txt").findFirst().orElse("");

        StoneArrangement stoneArrangement = StoneArrangement.parse(line);
        StoneArrangement endStoneArrangement = stoneArrangement.iterateNTimes(25);

        System.out.println(endStoneArrangement.countStones());
    }

    @Override
    public void part2() {
        String line = getLinesFromFile("day11.txt").findFirst().orElse("");

        StoneArrangement stoneArrangement = StoneArrangement.parse(line);
        StoneArrangement endStoneArrangement = stoneArrangement.iterateNTimes(75);

        System.out.println(endStoneArrangement.countStones());
    }

    private record Stone(long value, long count) implements Comparable<Stone> {
        private Stream<Stone> iterate() {
            String valueString = String.valueOf(value);

            if (value == 0) {
                return Stream.of(new Stone(1, count));
            }
            if (valueString.length() % 2 == 0) {
                String leftHalf = valueString.substring(0, valueString.length() / 2);
                String rightHalf = valueString.substring(valueString.length() / 2);

                long leftNumber = Integer.parseInt(leftHalf);
                long rightNumber = Integer.parseInt(rightHalf);

                return Stream.of(new Stone(leftNumber, count), new Stone(rightNumber, count));
            }
            return Stream.of(new Stone(value * 2024, count));
        }

        private List<Stone> merge(Stone stone) {
            if (equals(stone)) {
                return List.of(new Stone(value, this.count + stone.count));
            }
            return List.of(this, stone);
        }

        private static Stone parse(String string) {
            return new Stone(Long.parseLong(string), 1);
        }

        @Override
        public int compareTo(Stone o) {
            return Long.compare(value, o.value);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Stone stone = (Stone) o;
            return value == stone.value;
        }
    }

    private record StoneArrangement(List<Stone> stones) {
        private StoneArrangement iterate() {
            List<Stone> newStones = stones.stream()
                    .flatMap(Stone::iterate)
                    .collect(Collectors.toList());

            List<Stone> mergedStones = mergeStones(newStones);

            return new StoneArrangement(mergedStones);
        }

        private StoneArrangement iterateNTimes(int n) {
            return IntStream.range(0, n)
                    .boxed()
                    .reduce(this, (acc, unused) -> acc.iterate(), (v1, v2) -> v2);
        }

        private long countStones() {
            return stones.stream()
                    .map(Stone::count)
                    .reduce(0L, Long::sum);
        }

        private static StoneArrangement parse(String line) {
            List<Stone> stones = Arrays.stream(line.split(" "))
                    .map(Stone::parse)
                    .collect(Collectors.toList());

            List<Stone> mergedStones = mergeStones(stones);

            return new StoneArrangement(mergedStones);
        }

        private static List<Stone> mergeStones(List<Stone> stones) {
            if (stones.isEmpty()) return stones;

            stones.sort(Stone::compareTo);

            List<Stone> newStones = new ArrayList<>(stones.size());

            for (Stone stone : stones) {
                if (newStones.isEmpty()) {
                    newStones.add(stone);
                    continue;
                }

                Stone poppedStone = newStones.remove(newStones.size() - 1);

                newStones.addAll(poppedStone.merge(stone));
            }

            return newStones;
        }
    }
}
