package aocj2024;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import org.apache.commons.lang3.tuple.Pair;

public class Day21 extends Day {
    @Override
    public void part1() {
        List<String> lines = getLinesFromFile("day21.txt").toList();

        Keypad numberKeypad = Keypad.getKeypadStack(2);

        long totalComplexities = lines.stream().mapToLong(numberKeypad::getComplexity).sum();

        System.out.println(totalComplexities);
    }

    @Override
    public void part2() {
        List<String> lines = getLinesFromFile("day21.txt").toList();

        Keypad numberKeypad = Keypad.getKeypadStack(25);

        long totalComplexities = lines.stream().mapToLong(numberKeypad::getComplexity).sum();

        System.out.println(totalComplexities);
    }

    private static abstract class Keypad {
        final Map<Character, KeypadButton> buttons;
        final Set<Point> buttonLocations;
        final Map<String, Long> knownResults;
        final Keypad input;

        private Keypad(Keypad input) {
            Set<KeypadButton> buttonSet = getButtonSet();

            this.buttons = buttonSet.stream()
                    .collect(Collectors.toMap(
                            KeypadButton::value,
                            Function.identity()
                    ));
            this.buttonLocations = buttonSet.stream().map(KeypadButton::location).collect(Collectors.toSet());
            this.knownResults = new HashMap<>();
            this.input = input;
        }

        private long pressButton(char fromButtonValue, char toButtonValue) {
            return pressButton(fromButtonValue, toButtonValue, 1);
        }

        private long pressButton(char fromButtonValue, char toButtonValue, int amount) {
            if (amount == 0) return 0;
            if (input == null) {
                return amount;
            }

            String lookupKey = String.format("%c:%c:%d", fromButtonValue, toButtonValue, amount);

            if (knownResults.containsKey(lookupKey)) return knownResults.get(lookupKey);

            KeypadButton fromButton = buttons.get(fromButtonValue);
            KeypadButton toButton = buttons.get(toButtonValue);
            List<Pair<Character, Integer>> buttonsForTarget = new ArrayList<>();

            int dx = toButton.location.x - fromButton.location.x;
            int dy = toButton.location.y - fromButton.location.y;

            if (dx < 0) {
                buttonsForTarget.add(Pair.of('<', Math.abs(dx)));
            } else {
                buttonsForTarget.add(Pair.of('>', Math.abs(dx)));
            }

            if (dy < 0) {
                buttonsForTarget.add(Pair.of('^', Math.abs(dy)));
            } else {
                buttonsForTarget.add(Pair.of('v', Math.abs(dy)));
            }

            long fewestButtons = Long.MAX_VALUE;

            boolean forwardsPossible = buttonLocations.contains(new Point(toButton.location.x, fromButton.location.y));
            boolean backwardsPossible = buttonLocations.contains(new Point(fromButton.location.x, toButton.location.y));

            if (forwardsPossible) {
                fewestButtons = getInstructionOrderLength(amount, buttonsForTarget);
            }

            Collections.reverse(buttonsForTarget);

            if (backwardsPossible) {
                fewestButtons = Math.min(fewestButtons, getInstructionOrderLength(amount, buttonsForTarget));
            }

            knownResults.put(lookupKey, fewestButtons);

            return fewestButtons;
        }

        private long getInstructionOrderLength(int amount, List<Pair<Character, Integer>> instructionOrder) {
            long pressCount = 0;
            char currentButton = 'A';

            for (Pair<Character, Integer> buttonAmountPair : instructionOrder) {
                char buttonValue = buttonAmountPair.getLeft();
                int buttonAmount = buttonAmountPair.getRight();

                if (buttonAmount > 0) {
                    pressCount += input.pressButton(currentButton, buttonValue, buttonAmount);
                    currentButton = buttonValue;
                }
            }

            pressCount += input.pressButton(currentButton, 'A', amount);

            return pressCount;
        }

        private long getComplexity(String sequence) {
            long sequenceLength = 0;

            char prevButtonValue = 'A';

            for (int i = 0; i < sequence.length(); i++) {
                char buttonValue = sequence.charAt(i);

                long buttonPresses = pressButton(prevButtonValue, buttonValue);
                sequenceLength += buttonPresses;

                prevButtonValue = buttonValue;
            }

            String sequenceNumberPart = sequence.chars()
                    .mapToObj(c -> (char) c)
                    .filter(Character::isDigit)
                    .collect(
                            Collector.of(
                                    StringBuilder::new,
                                    StringBuilder::append,
                                    StringBuilder::append,
                                    StringBuilder::toString
                            )
                    );

            int sequenceNumberValue = Integer.parseInt(sequenceNumberPart);

            return sequenceLength * sequenceNumberValue;
        }

        abstract Set<KeypadButton> getButtonSet();

        private static Keypad getKeypadStack(int robots) {
            Keypad previousKeypad = new DirectionKeypad(null);

            for (int i = 0; i < robots; i++) {
                previousKeypad = new DirectionKeypad(previousKeypad);
            }

            return new NumberKeypad(previousKeypad);
        }
    }

    private static class NumberKeypad extends Keypad {
        private NumberKeypad(Keypad input) {
            super(input);
        }

        @Override
        Set<KeypadButton> getButtonSet() {
            return Set.of(
                    new KeypadButton('A', new Point(2, 3)),
                    new KeypadButton('0', new Point(1, 3)),
                    new KeypadButton('1', new Point(0, 2)),
                    new KeypadButton('2', new Point(1, 2)),
                    new KeypadButton('3', new Point(2, 2)),
                    new KeypadButton('4', new Point(0, 1)),
                    new KeypadButton('5', new Point(1, 1)),
                    new KeypadButton('6', new Point(2, 1)),
                    new KeypadButton('7', new Point(0, 0)),
                    new KeypadButton('8', new Point(1, 0)),
                    new KeypadButton('9', new Point(2, 0))
            );
        }
    }

    private static class DirectionKeypad extends Keypad {
        private DirectionKeypad(Keypad input) {
            super(input);
        }

        @Override
        Set<KeypadButton> getButtonSet() {
            return Set.of(
                    new KeypadButton('<', new Point(0, 1)),
                    new KeypadButton('v', new Point(1, 1)),
                    new KeypadButton('>', new Point(2, 1)),
                    new KeypadButton('^', new Point(1, 0)),
                    new KeypadButton('A', new Point(2, 0))
            );
        }
    }

    private record KeypadButton(char value, Point location) {
    }
}
