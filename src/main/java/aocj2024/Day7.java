package aocj2024;

import java.util.Arrays;
import java.util.List;
import org.apache.commons.lang3.tuple.Pair;

public class Day7 extends Day {
    @Override
    public void part1() {
        List<Pair<Long, long[]>> equations = getLinesFromFile("day7.txt")
                .map(string -> string.split(": "))
                .map(strings -> Pair.of(strings[0], strings[1]))
                .map(pair -> Pair.of(Long.parseLong(pair.getLeft()), Arrays.stream(pair.getRight().split(" ")).mapToLong(Long::parseLong).toArray()))
                .toList();

        long calibratedTotal = equations.stream()
                .filter(equation -> evaluateOptions(equation.getLeft(), 0, equation.getRight()))
                .map(Pair::getLeft)
                .reduce(0L, Long::sum);

        System.out.println(calibratedTotal);
    }

    @Override
    public void part2() {
        List<Pair<Long, long[]>> equations = getLinesFromFile("day7.txt")
                .map(string -> string.split(": "))
                .map(strings -> Pair.of(strings[0], strings[1]))
                .map(pair -> Pair.of(Long.parseLong(pair.getLeft()), Arrays.stream(pair.getRight().split(" ")).mapToLong(Long::parseLong).toArray()))
                .toList();

        long calibratedTotal = equations.stream()
                .filter(equation -> evaluateOptionsWithConcat(equation.getLeft(), 0, equation.getRight()))
                .map(Pair::getLeft)
                .reduce(0L, Long::sum);

        System.out.println(calibratedTotal);
    }

    private static boolean evaluateOptions(long goal, long current, long[] input) {
        if (current > goal) return false;
        if (current == goal && input.length == 0) return true;
        if (input.length == 0) return false;

        long plusCurrent = current + input[0];
        long[] plusRest = Arrays.copyOfRange(input, 1, input.length);

        if (evaluateOptions(goal, plusCurrent, plusRest)) return true;

        long multiplyCurrent = current * input[0];
        long [] multiplyRest = Arrays.copyOfRange(input, 1, input.length);

        if (evaluateOptions(goal, multiplyCurrent, multiplyRest)) return true;

        return false;
    }

    private static boolean evaluateOptionsWithConcat(long goal, long current, long[] input) {
        if (current > goal) return false;
        if (current == goal && input.length == 0) return true;
        if (input.length == 0) return false;

        long plusCurrent = current + input[0];
        long[] plusRest = Arrays.copyOfRange(input, 1, input.length);

        if (evaluateOptionsWithConcat(goal, plusCurrent, plusRest)) return true;

        long multiplyCurrent = current * input[0];
        long[] multiplyRest = Arrays.copyOfRange(input, 1, input.length);

        if (evaluateOptionsWithConcat(goal, multiplyCurrent, multiplyRest)) return true;

        long concatCurrent = Long.parseLong(Long.toString(current) + input[0]);
        long[] concatRest = Arrays.copyOfRange(input, 1, input.length);

        if (evaluateOptionsWithConcat(goal, concatCurrent, concatRest)) return true;

        return false;
    }
}
