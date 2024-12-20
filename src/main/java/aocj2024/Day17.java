package aocj2024;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Day17 extends Day {
    @Override
    public void part1() {
        List<String> lines = getLinesFromFile("day17.txt").toList();

        Program program = Program.parse(lines.get(4));
        ProgramState initialState = ProgramState.parse(program, lines.subList(0, 3));

        ProgramState finalState = program.execute(initialState);

        System.out.println(String.join(",", finalState.output.stream().map(String::valueOf).toList()));
    }

    @Override
    public void part2() {
        List<String> lines = getLinesFromFile("day17.txt").toList();

        List<Long> originalProgram = Arrays.stream(lines.get(4)
                        .substring(9)
                        .split(","))
                .map(Long::parseLong)
                .collect(Collectors.toList());

        Program program = Program.parse(lines.get(4));
        ProgramState initialState = ProgramState.parse(program, lines.subList(0, 3));

        Long result = program.getMatchingOutput(initialState, new ArrayList<>(originalProgram.size()), originalProgram);

        System.out.println(result);
    }

    private record Program(List<Instruction> instructionSet) {
        private ProgramState execute(ProgramState initialState) {
            ProgramState currentState = initialState;

            while (currentState.hasInstructionLeft()) {
                currentState = currentState.getInstruction().execute(currentState);
            }

            return currentState;
        }

        private static Program parse(String line) {
            Pattern pattern = Pattern.compile("^Program: ((?:\\d,)*\\d)$");

            Matcher matcher = pattern.matcher(line);
            boolean foundMatch = matcher.find();
            assert foundMatch;

            long[] digits = Arrays.stream(matcher.group(1).split(",")).mapToLong(Long::parseLong).toArray();
            List<Instruction> instructionSet = new ArrayList<>();

            for (int i = 0; i < digits.length - 1; i++) {
                long opcode = digits[i];
                long operand = digits[i + 1];

                instructionSet.add(switch ((int) opcode) {
                    case 0 -> new AdvInstruction(operand);
                    case 1 -> new BxlInstruction(operand);
                    case 2 -> new BstInstruction(operand);
                    case 3 -> new JnzInstruction(operand);
                    case 4 -> new BxcInstruction(operand);
                    case 5 -> new OutInstruction(operand);
                    case 6 -> new BdvInstruction(operand);
                    case 7 -> new CdvInstruction(operand);
                    default -> throw new IllegalStateException("Unexpected opcode: " + (int) opcode);
                });
            }

            return new Program(instructionSet);
        }

        private Long getMatchingOutput(ProgramState initialState, List<Long> currentMatch, List<Long> outputToMatch) {
            long baseOffset = power(8, currentMatch.size());
            long currentOffset = 0;

            for (long j = currentMatch.size() - 1, mult = 8; j >= 0; j--, mult *= 8) {
                currentOffset += currentMatch.get((int) j) * mult;
            }

            for (long digitOffset = 0; digitOffset < 8; digitOffset++) {
                long A = baseOffset + currentOffset + digitOffset;

                List<Long> output = execute(initialState.withA(A)).output;

                if (Objects.equals(output.get(0), outputToMatch.get(outputToMatch.size() - output.size()))) {
                    if (output.equals(outputToMatch)) {
                        return A;
                    }

                    currentMatch.add(digitOffset);

                    Long result = getMatchingOutput(initialState, currentMatch, outputToMatch);
                    if (result != null) return result;

                    currentMatch.remove(currentMatch.size() - 1);
                }
            }

            return null;
        }
    }

    private record ProgramState(Program program, long pointer, long A, long B, long C, List<Long> output) {
        private ProgramState next() {
            return new ProgramState(program, pointer + 2, A, B, C, output);
        }

        private ProgramState at(long newPointer) {
            return new ProgramState(program, newPointer, A, B, C, output);
        }

        private ProgramState withOutput(long outputValue) {
            List<Long> newOutput = new ArrayList<>(output);

            newOutput.add(outputValue);

            return new ProgramState(program, pointer, A, B, C, newOutput);
        }

        private ProgramState withA(long newA) {
            return new ProgramState(program, pointer, newA, B, C, output);
        }

        private ProgramState withB(long newB) {
            return new ProgramState(program, pointer, A, newB, C, output);
        }

        private ProgramState withC(long newC) {
            return new ProgramState(program, pointer, A, B, newC, output);
        }

        private boolean hasInstructionLeft() {
            return 0 <= pointer && pointer < program.instructionSet.size();
        }

        private Instruction getInstruction() {
            return program.instructionSet.get((int) pointer);
        }

        private static ProgramState parse(Program program, List<String> lines) {
            String allLines = String.join("\n", lines);

            Pattern pattern = Pattern.compile("^Register A: (\\d+)\nRegister B: (\\d+)\nRegister C: (\\d+)$");

            Matcher matcher = pattern.matcher(allLines);
            boolean foundMatch = matcher.find();
            assert foundMatch;

            long A = Long.parseLong(matcher.group(1));
            long B = Long.parseLong(matcher.group(2));
            long C = Long.parseLong(matcher.group(3));

            return new ProgramState(program, 0, A, B, C, new ArrayList<>());
        }
    }

    private abstract static class Instruction {
        protected long operand;

        public Instruction(long operand) {
            this.operand = operand;
        }

        protected abstract ProgramState execute(ProgramState state);

        protected long getOperandValue(ProgramState state) {
            if (0L <= operand && operand <= 3L) return operand;
            if (operand == 4L) return state.A;
            if (operand == 5L) return state.B;
            if (operand == 6L) return state.C;

            System.out.println("This program is not valid");
            return -1;
        }
    }

    private static class AdvInstruction extends Instruction {
        public AdvInstruction(long operand) {
            super(operand);
        }

        @Override
        protected ProgramState execute(ProgramState state) {
            long newA = state.A / (1L << getOperandValue(state));

            return state.next().withA(newA);
        }
    }

    private static class BxlInstruction extends Instruction {
        public BxlInstruction(long operand) {
            super(operand);
        }

        @Override
        protected ProgramState execute(ProgramState state) {
            return state.next().withB(state.B ^ operand);
        }
    }

    private static class BstInstruction extends Instruction {
        public BstInstruction(long operand) {
            super(operand);
        }

        @Override
        protected ProgramState execute(ProgramState state) {
            return state.next().withB(getOperandValue(state) % 8);
        }
    }

    private static class JnzInstruction extends Instruction {
        public JnzInstruction(long operand) {
            super(operand);
        }

        @Override
        protected ProgramState execute(ProgramState state) {
            if (state.A == 0L) return state.next();

            return state.at(operand);
        }
    }

    private static class BxcInstruction extends Instruction {
        public BxcInstruction(long operand) {
            super(operand);
        }

        @Override
        protected ProgramState execute(ProgramState state) {
            return state.next().withB(state.B ^ state.C);
        }
    }

    private static class OutInstruction extends Instruction {
        public OutInstruction(long operand) {
            super(operand);
        }

        @Override
        protected ProgramState execute(ProgramState state) {
            return state.next().withOutput(getOperandValue(state) % 8);
        }
    }

    private static class BdvInstruction extends Instruction {
        public BdvInstruction(long operand) {
            super(operand);
        }

        @Override
        protected ProgramState execute(ProgramState state) {
            long newB = state.A / (1L << getOperandValue(state));

            return state.next().withB(newB);
        }
    }

    private static class CdvInstruction extends Instruction {
        public CdvInstruction(long operand) {
            super(operand);
        }

        @Override
        protected ProgramState execute(ProgramState state) {
            long newC = state.A / (1L << getOperandValue(state));

            return state.next().withC(newC);
        }
    }

    private static long power(long a, int b) {
        if (b == 0) return 1;
        if (b == 1) return a;
        if (b % 2 == 0) return power(a * a, b/2);
        else return a * power(a * a, b / 2);
    }
}
