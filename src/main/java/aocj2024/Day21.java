package aocj2024;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.commons.lang3.tuple.Pair;

public class Day21 extends Day {
    @Override
    public void part1() {
        List<String> lines = getLinesFromFile("day21_sample.txt").toList();

        Keypad directionPad1 = new DirectionKeypad(null);
        Keypad directionPad2 = new DirectionKeypad(directionPad1);
        Keypad numberKeypad = new NumberKeypad(directionPad2);

        for (String sequence : lines) {
            long buttonTotal = 0;

            char prevButtonValue = 'A';

            for (int i = 0; i < sequence.length(); i++) {
                char buttonValue = sequence.charAt(i);

                buttonTotal += numberKeypad.pressButton(prevButtonValue, buttonValue);

                prevButtonValue = buttonValue;
            }

            System.out.println(sequence + ": " + buttonTotal);
        }
    }

    @Override
    public void part2() {

    }

    private static abstract class Keypad {
        final Map<Character, KeypadButton> buttons;
        final Set<Point> buttonLocations;
        final Keypad input;

        private Keypad(Keypad input) {
            Set<KeypadButton> buttonSet = getButtonSet();

            this.buttons = buttonSet.stream()
                    .collect(Collectors.toMap(
                            KeypadButton::value,
                            Function.identity()
                    ));
            this.buttonLocations = buttonSet.stream().map(KeypadButton::location).collect(Collectors.toSet());
            this.input = input;
        }

        private long pressButton(char fromButtonValue, char toButtonValue) {
            return pressButton(fromButtonValue, toButtonValue, 1);
        }

        private long pressButton(char fromButtonValue, Pair<Character, Integer> toButtonPair) {
            return pressButton(fromButtonValue, toButtonPair.getLeft(), toButtonPair.getRight());
        }

        private long pressButton(char fromButtonValue, char toButtonValue, int amount) {
            if (amount == 0) return 0;
            if (input == null) {
                System.out.println(Character.toString(toButtonValue).repeat(amount));
                return amount;
            }

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

            long fewestButtons = Integer.MAX_VALUE;

            boolean forwardsPossible = buttonLocations.contains(new Point(toButton.location.x, fromButton.location.y));
            boolean backwardsPossible = buttonLocations.contains(new Point(fromButton.location.x, toButton.location.y));

            if (forwardsPossible) {
                long forwards = input.pressButton('A', buttonsForTarget.get(0)) +
                        input.pressButton(buttonsForTarget.get(0).getLeft(), buttonsForTarget.get(1)) +
                        input.pressButton(buttonsForTarget.get(1).getLeft(), 'A', amount);

                fewestButtons = Math.min(fewestButtons, forwards);
            }

            if (backwardsPossible) {
                long backwards = input.pressButton('A', buttonsForTarget.get(1)) +
                        input.pressButton(buttonsForTarget.get(1).getLeft(), buttonsForTarget.get(0)) +
                        input.pressButton(buttonsForTarget.get(0).getLeft(), 'A', amount);

                fewestButtons = Math.min(fewestButtons, backwards);
            }

            return fewestButtons;
        }

        abstract Set<KeypadButton> getButtonSet();
    }

    private class NumberKeypad extends Keypad {
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

    private class DirectionKeypad extends Keypad {
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

    private record KeypadButton(char value, Point location) { }
}
