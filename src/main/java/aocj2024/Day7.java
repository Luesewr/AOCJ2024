package aocj2024;

import java.util.Arrays;
import java.util.List;
import org.apache.commons.lang3.function.TriFunction;

public class Day7 extends Day {
    @Override
    public void part1() {
        List<String> lines = getLinesFromFile("day7.txt").toList();
        List<Equation> equations = Equation.parseEquations(lines);

        long calibratedTotal = Equation.sumCalibrated(equations, Equation::evaluateOptions);

        System.out.println(calibratedTotal);
    }

    @Override
    public void part2() {
        List<String> lines = getLinesFromFile("day7.txt").toList();
        List<Equation> equations = Equation.parseEquations(lines);

        long calibratedTotal = Equation.sumCalibrated(equations, Equation::evaluateOptionsWithConcat);

        System.out.println(calibratedTotal);
    }

    private record Equation(long[] values, long goal) {
        private boolean evaluateOptions(long current, int offset) {
            if (current > goal) return false;
            if (current == goal && values.length == offset) return true;
            if (offset >= values.length) return false;

            long plusCurrent = current + values[offset];
            if (evaluateOptions(plusCurrent, offset + 1)) return true;

            long multiplyCurrent = current * values[offset];
            return evaluateOptions(multiplyCurrent, offset + 1);
        }

        private boolean evaluateOptionsWithConcat(long current, int offset) {
            if (current > goal) return false;
            if (current == goal && values.length == offset) return true;
            if (offset >= values.length) return false;

            long plusCurrent = current + values[offset];
            if (evaluateOptionsWithConcat(plusCurrent, offset + 1)) return true;

            long multiplyCurrent = current * values[offset];
            if (evaluateOptionsWithConcat(multiplyCurrent, offset + 1)) return true;

            long concatCurrent = Long.parseLong(Long.toString(current) + values[offset]);
            return evaluateOptionsWithConcat(concatCurrent, offset + 1);
        }

        private static long sumCalibrated(List<Equation> equations, TriFunction<Equation, Long, Integer, Boolean> calibrateFunction) {
            return equations.stream()
                    .filter(equation -> calibrateFunction.apply(equation, 0L, 0))
                    .map(equation -> equation.goal)
                    .reduce(0L, Long::sum);
        }

        private static List<Equation> parseEquations(List<String> lines) {
            return lines.stream()
                    .map(Equation::parseEquation)
                    .toList();
        }

        private static Equation parseEquation(String equationString) {
            String[] parts = equationString.split(": ");

            long goal = Long.parseLong(parts[0]);
            long[] values = Arrays.stream(parts[1].split(" ")).mapToLong(Long::parseLong).toArray();

            return new Equation(values, goal);
        }
    }
}
