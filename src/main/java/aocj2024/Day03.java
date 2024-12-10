package aocj2024;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Day03 extends Day {
    @Override
    public void part1() {
        String input = getLinesFromFile("day03.txt")
                .collect(Collectors.joining());

        MulInstruction.disabled = false;
        DoInstruction.disabled = true;
        DontInstruction.disabled = true;

        List<Instruction> instructions = Instruction.parseAll(input);

        int total = Instruction.executeAll(instructions);

        System.out.println(total);
    }

    @Override
    public void part2() {
        String input = getLinesFromFile("day03.txt")
                .collect(Collectors.joining());

        MulInstruction.disabled = false;
        DoInstruction.disabled = false;
        DontInstruction.disabled = false;

        List<Instruction> instructions = Instruction.parseAll(input);

        int total = Instruction.executeAll(instructions);

        System.out.println(total);
    }

    private record RunState(int total) { }
    
    private static abstract class Instruction {
        int index;
        
        private Instruction(int index, String args) throws ParseException {
            this.index = index;
            parseArgs(args);
        }

        private static int executeAll(List<Instruction> instructionSet) {
            RunState resultingState = instructionSet
                    .stream()
                    .reduce(
                            new RunState(0),
                            (state, instruction) -> instruction.execute(state),
                            (oldState, newState) -> newState
                    );

            return resultingState.total;
        }
        
        protected abstract RunState execute(RunState state);
        
        private static List<Instruction> parseAll(String input) {
            Pattern pattern = Pattern.compile("(mul|do|don't)\\(([0-9,]*?)\\)");
            Matcher matcher = pattern.matcher(input);

            List<Instruction> instructionSet = new ArrayList<>();

            while (matcher.find()) {
                String opType = matcher.group(1);
                String args = matcher.group(2);
                int index = matcher.start();

                try {
                    Instruction instruction = switch (opType) {
                        case "mul" -> new MulInstruction(index, args);
                        case "do" -> new DoInstruction(index, args);
                        case "don't" -> new DontInstruction(index, args);
                        default -> throw new ParseException("Could not match operator", index);
                    };

                    instructionSet.add(instruction);
                } catch (ParseException ignored) {}
            }

            return instructionSet;
        }
        
        protected abstract void parseArgs(String args) throws ParseException;
    }
    
    private static class MulInstruction extends Instruction {
        private static boolean disabled = false;
        private static final Pattern pattern = Pattern.compile("^(\\d{1,3}),(\\d{1,3})$");
        
        private int left;
        private int right;
        
        private MulInstruction(int index, String args) throws ParseException {
            super(index, args);
        }

        @Override
        protected RunState execute(RunState state) {
            if (disabled) return state;

            return new RunState(state.total + left * right);
        }

        @Override
        protected void parseArgs(String args) throws ParseException {
            Matcher matcher = pattern.matcher(args);

            if (matcher.find()) {
                this.left = Integer.parseInt(matcher.group(1));
                this.right = Integer.parseInt(matcher.group(2));
            } else {
                throw new ParseException("Could not match args", this.index);
            }
        }
    }
    
    private static class DoInstruction extends Instruction {
        private static boolean disabled = false;

        private DoInstruction(int index, String args) throws ParseException {
            super(index, args);
        }

        @Override
        protected RunState execute(RunState state) {
            if (disabled) return state;

            MulInstruction.disabled = false;

            return state;
        }

        @Override
        protected void parseArgs(String args) throws ParseException {
            if (!args.isEmpty()) throw new ParseException("Args were not empty", this.index);
        }
    }
    
    private static class DontInstruction extends Instruction {
        private static boolean disabled = false;

        private DontInstruction(int index, String args) throws ParseException {
            super(index, args);
        }

        @Override
        protected RunState execute(RunState state) {
            if (disabled) return state;

            MulInstruction.disabled = true;

            return state;
        }

        @Override
        protected void parseArgs(String args) throws ParseException {
            if (!args.isEmpty()) throw new ParseException("Args were not empty", this.index);
        }
    }
}
