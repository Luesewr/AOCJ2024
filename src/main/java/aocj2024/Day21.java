package aocj2024;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.tuple.Pair;

public class Day21 extends Day {
    @Override
    public void part1() {
        List<String> lines = getLinesFromFile("day21.txt").toList();

        Keypad directionPad1 = new DirectionKeypad(null, "Directionpad1");
        Keypad directionPad2 = new DirectionKeypad(directionPad1, "Directionpad2");
        Keypad directionPad3 = new DirectionKeypad(directionPad2, "Directionpad2");
        Keypad numberKeypad = new NumberKeypad(directionPad3, "Numberpad");

        long totalComplexities = 0;

        for (String sequence : lines) {
            long sequenceLength = 0;

            char prevButtonValue = 'A';

            for (int i = 0; i < sequence.length(); i++) {
                char buttonValue = sequence.charAt(i);

                List<Character> buttonPresses = numberKeypad.pressButton(prevButtonValue, buttonValue);
                sequenceLength += buttonPresses.size();

                prevButtonValue = buttonValue;
            }

            totalComplexities += sequenceLength * Integer.parseInt(sequence.substring(0, sequence.length() - 1));
        }

        System.out.println(totalComplexities);
    }

    @Override
    public void part2() {

    }

    private static abstract class Keypad {
        final Map<Character, KeypadButton> buttons;
        final Set<Point> buttonLocations;
        final Keypad input;
        final String name;

        private Keypad(Keypad input, String name) {
            Set<KeypadButton> buttonSet = getButtonSet();

            this.buttons = buttonSet.stream()
                    .collect(Collectors.toMap(
                            KeypadButton::value,
                            Function.identity()
                    ));
            this.buttonLocations = buttonSet.stream().map(KeypadButton::location).collect(Collectors.toSet());
            this.input = input;
            this.name = name;
        }

        private List<Character> pressButton(char fromButtonValue, char toButtonValue) {
            return pressButton(fromButtonValue, toButtonValue, 1);
        }

        private List<Character> pressButton(char fromButtonValue, Pair<Character, Integer> toButtonPair) {
            return pressButton(fromButtonValue, toButtonPair.getLeft(), toButtonPair.getRight());
        }

        private List<Character> pressButton(char fromButtonValue, char toButtonValue, int amount) {
            if (amount == 0) return List.of();
            if (input == null) {
//                System.out.println("Pressed " + toButtonValue + " " + amount + " times");
                return Collections.nCopies(amount, toButtonValue);
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

            List<Character> fewestButtons = null;

            boolean forwardsPossible = buttonLocations.contains(new Point(toButton.location.x, fromButton.location.y));
            boolean backwardsPossible = buttonLocations.contains(new Point(fromButton.location.x, toButton.location.y));

            if (forwardsPossible) {
                List<Character> forwards = new ArrayList<>();
                char currentButton = 'A';

                Pair<Character, Integer> firstButtonAmount = buttonsForTarget.get(0);
                char firstButton = firstButtonAmount.getLeft();
                int firstAmount = firstButtonAmount.getRight();

                if (firstAmount > 0) {
                    forwards.addAll(input.pressButton(currentButton, firstButtonAmount));
                    currentButton = firstButton;
                }

                Pair<Character, Integer> secondButtonAmount = buttonsForTarget.get(1);
                char secondButton = secondButtonAmount.getLeft();
                int secondAmount = secondButtonAmount.getRight();

                if (secondAmount > 0) {
                    forwards.addAll(input.pressButton(currentButton, secondButtonAmount));
                    currentButton = secondButton;
                }

                forwards.addAll(input.pressButton(currentButton, 'A', amount));

                fewestButtons = forwards;
            }

            if (backwardsPossible) {
                List<Character> backwards = new ArrayList<>();
                char currentButton = 'A';

                Pair<Character, Integer> firstButtonAmount = buttonsForTarget.get(1);
                char firstButton = firstButtonAmount.getLeft();
                int firstAmount = firstButtonAmount.getRight();

                if (firstAmount > 0) {
                    backwards.addAll(input.pressButton(currentButton, firstButtonAmount));
                    currentButton = firstButton;
                }

                Pair<Character, Integer> secondButtonAmount = buttonsForTarget.get(0);
                char secondButton = secondButtonAmount.getLeft();
                int secondAmount = secondButtonAmount.getRight();

                if (secondAmount > 0) {
                    backwards.addAll(input.pressButton(currentButton, secondButtonAmount));
                    currentButton = secondButton;
                }

                backwards.addAll(input.pressButton(currentButton, 'A', amount));

                if (fewestButtons == null || backwards.size() < fewestButtons.size()) {
                    fewestButtons = backwards;
                }
            }

//            System.out.println("pressed " + toButtonValue + " on " + name + " at " + toButton.location + " from " + fromButtonValue + " at " + fromButton.location);

            return fewestButtons;
        }

        abstract Set<KeypadButton> getButtonSet();
    }

    private class NumberKeypad extends Keypad {
        private NumberKeypad(Keypad input, String name) {
            super(input, name);
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
        private DirectionKeypad(Keypad input, String name) {
            super(input, name);
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
