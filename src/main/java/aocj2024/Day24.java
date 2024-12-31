package aocj2024;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.apache.commons.lang3.tuple.Pair;

public class Day24 extends Day {
    @Override
    public void part1() {
        List<String> lines = getLinesFromFile("day24.txt").toList();

        Adder adder = Adder.parse(lines);

        adder.evaluate();

        long output = adder.readOutput();

        System.out.println(output);
    }

    @Override
    public void part2() {
        List<String> lines = getLinesFromFile("day24.txt").toList();

        Adder adder = Adder.parse(lines);

        assert adder.input1.size() == adder.input2.size();

        int inputSize = adder.input1.size();

        Set<Wire> seenWires = new HashSet<>();

        List<GateGroup> incorrectGateGroups = new ArrayList<>();
        List<GateGroup> correctGateGroups = new ArrayList<>();

        for (int i = 0; i < inputSize; i++) {
            boolean correct = adder.test(1L << (inputSize - i - 1), 0L, 1L << (inputSize - i - 1)) &&
                    adder.test(0L, 1L << (inputSize - i - 1), 1L << (inputSize - i - 1));

            Set<Wire> input1DependentOutputs = adder.input1.get(i).getDependentOutputs();
            Set<Wire> input2DependentOutputs = adder.input2.get(i).getDependentOutputs();

            List<GateGroup> gateGroups = correct ? correctGateGroups : incorrectGateGroups;

            Set<Wire> wires = Stream.of(input1DependentOutputs, input2DependentOutputs).flatMap(Set::stream).collect(Collectors.toSet());
            wires.removeAll(seenWires);
            seenWires.addAll(wires);

            Set<LogicGate> swappableGates = wires.stream()
                    .filter(wire -> !wire.name.startsWith("z"))
                    .map(wire -> wire.input)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());

            gateGroups.add(new GateGroup(swappableGates, inputSize - i - 1));
        }

        List<GateGroup> gateGroups = new ArrayList<>(Stream.of(incorrectGateGroups, correctGateGroups).flatMap(List::stream).toList());

        System.out.println(gateGroups.stream().map(GateGroup::gates).map(Set::size).toList());

        PriorityQueue<IncorrectGateGroupQueueElement> pq = new PriorityQueue<>((o1, o2) -> {
            int substitutionComparison = Integer.compare(o2.substitutions.size(), o1.substitutions.size());

            return substitutionComparison != 0 ? substitutionComparison : Integer.compare(o1.getIncorrectCount(), o2.getIncorrectCount());
        });

        pq.add(new IncorrectGateGroupQueueElement(1, incorrectGateGroups, new HashMap<>()));

        while (!pq.isEmpty()) {
            IncorrectGateGroupQueueElement element = pq.poll();
            int pairsLimit = element.pairsLimit;
            Map<LogicGate, Wire> substitutions = element.substitutions;
            List<GateGroup> incorrectGroups = element.incorrectGroups;

            if (incorrectGroups.isEmpty()) {
                System.out.println("hey");
                break;
            }

            GateGroup gateGroup = element.incorrectGroups.get(0);
            int bit = gateGroup.bit;

            boolean correct1 = adder.test(1L << bit, 0L, 1L << bit, substitutions) &&
                    adder.test(0L, 1L << bit, 1L << bit, substitutions) &&
                    adder.test(1L << bit, 1L << bit, 1L << (bit + 1), substitutions);

            System.out.println("gateGroupBit = " + bit + ", " + pairsLimit + ", " + correct1);

            Adder.iterateOverPairs(List.of(), gateGroup, incorrectGateGroups, pairsLimit)
                    .filter(pairs -> pairs.stream()
                            .noneMatch(pair -> substitutions.containsKey(pair.getLeft()) || substitutions.containsKey(pair.getRight()))
                    )
                    .filter(pairs -> pairs.stream()
                            .noneMatch(pair -> pair.getLeft().equals(pair.getRight()))
                    )
                    .filter(pairs -> {
                        pairs.forEach(pair -> {
                            substitutions.put(pair.getLeft(), pair.getRight().output);
                            substitutions.put(pair.getRight(), pair.getLeft().output);
                        });

                        boolean correct = adder.test(1L << bit, 0L, 1L << bit, substitutions) &&
                                adder.test(0L, 1L << bit, 1L << bit, substitutions) &&
                                adder.test(1L << bit, 1L << bit, 1L << (bit + 1), substitutions);// &&
//                                adder.test(3L << (bit - 1), 3L << (bit - 1), 3L << bit, substitutions);

                        pairs.forEach(pair -> {
                            substitutions.remove(pair.getLeft());
                            substitutions.remove(pair.getRight());
                        });

                        return correct;
                    })
                    .map(pairs -> {
                        Map<LogicGate, Wire> newSubstitutions = new HashMap<>(substitutions);

                        pairs.forEach(pair -> {
                            newSubstitutions.put(pair.getLeft(), pair.getRight().output);
                            newSubstitutions.put(pair.getRight(), pair.getLeft().output);
                        });

                        List<GateGroup> newGateGroups = incorrectGroups.subList(1, incorrectGroups.size());

                        return new IncorrectGateGroupQueueElement(2, newGateGroups, newSubstitutions);
                    })
                    .forEach(pq::add);

            pq.add(new IncorrectGateGroupQueueElement(pairsLimit + 1, incorrectGroups, substitutions));
        }

//        List<LogicGate> incorrectGates = totalIncorrectWires.stream()
//                .filter(wire -> !wire.name.startsWith("z"))
//                .map(wire -> wire.input)
//                .filter(Objects::nonNull)
//                .collect(Collectors.toList());
//
//        LinkedList<Pair<List<Pair<Integer, List<LogicGate>>>, Map<LogicGate, Wire>>> gateGroupContenders = new LinkedList<>();
//        gateGroupContenders.add(Pair.of(incorrectGateGroups, new HashMap<>()));
//
//        boolean foundSwaps = false;
//
//        Map<LogicGate, Wire> finalSubstitutions = null;
//
////        Map<LogicGate, Wire> substitutions = new HashMap<>();
//        for (int pairsCount = 1; !foundSwaps; pairsCount++) {
//            List<Pair<List<Pair<Integer, List<LogicGate>>>, Map<LogicGate, Wire>>> contendersToAdd = new ArrayList<>();
//
//            for (Pair<List<Pair<Integer, List<LogicGate>>>, Map<LogicGate, Wire>> gateGroupContender : gateGroupContenders) {
//                List<Pair<Integer, List<LogicGate>>> gateGroups = gateGroupContender.getLeft();
//                Map<LogicGate, Wire> substitutions = gateGroupContender.getRight();
//
//                if (gateGroups.isEmpty()) {
//                    foundSwaps = true;
//                    finalSubstitutions = substitutions;
//                    System.out.println(pairsCount);
//                    break;
//                }
//
//                for (Pair<Integer, List<LogicGate>> gateGroup : gateGroups) {
//                    int bitOffset = gateGroup.getLeft();
//                    List<LogicGate> gates = gateGroup.getRight(); //?????
//
//                    Adder.iterateOverPairs(List.of(), gateGroup, gateGroups, pairsCount)
//                            .filter(pairs -> pairs.stream()
//                                    .noneMatch(pair -> substitutions.containsKey(pair.getLeft()) || substitutions.containsKey(pair.getRight()))
//                            )
//                            .filter(pairs -> pairs.stream()
//                                    .noneMatch(pair -> pair.getLeft().equals(pair.getRight()))
//                            )
//                            .filter(pairs -> {
//                                pairs.forEach(pair -> {
//                                    substitutions.put(pair.getLeft(), pair.getRight().output);
//                                    substitutions.put(pair.getRight(), pair.getLeft().output);
//                                });
//
//                                boolean correct = adder.test(1L << (inputSize - bitOffset - 1), 0L, 1L << (inputSize - bitOffset - 1), substitutions) &&
//                                        adder.test(0L, 1L << (inputSize - bitOffset - 1), 1L << (inputSize - bitOffset - 1), substitutions) &&
//                                        adder.test(1L << (inputSize - bitOffset - 1), 1L << (inputSize - bitOffset - 1), 1L << (inputSize - bitOffset), substitutions) &&
//                                        adder.test(3L << (inputSize - bitOffset - 2), 3L << (inputSize - bitOffset - 2), 3L << (inputSize - bitOffset - 1), substitutions);
//
//                                pairs.forEach(pair -> {
//                                    substitutions.remove(pair.getLeft());
//                                    substitutions.remove(pair.getRight());
//                                });
//
//                                return correct;
//                            })
//                            .map(pairs -> {
//                                Map<LogicGate, Wire> newSubstitutions = new HashMap<>(substitutions);
//
//                                pairs.forEach(pair -> {
//                                    newSubstitutions.put(pair.getLeft(), pair.getRight().output);
//                                    newSubstitutions.put(pair.getRight(), pair.getLeft().output);
//                                });
//
//                                List<Pair<Integer, List<LogicGate>>> newGateGroups = new ArrayList<>(gateGroups);
//                                newGateGroups.remove(gateGroup);
//
//                                System.out.println(newGateGroups.size());
//
//                                return Pair.of(newGateGroups, newSubstitutions);
//                            })
//                            .forEach(contendersToAdd::add);
//                }
//            }
//
//            System.out.println(contendersToAdd.stream().map(listMapPair -> listMapPair.getRight().values()).toList());
//            contendersToAdd.forEach(gateGroupContenders::addFirst);
//        }
//
//        System.out.println(finalSubstitutions);
//            System.out.println(pairsCount);
//            List<Pair<Integer, List<LogicGate>>> groupsToRemove = new ArrayList<>();
//
//            for (Pair<Integer, List<LogicGate>> gateGroup : incorrectGateGroups) {
//                int bitOffset = gateGroup.getLeft();
//                List<LogicGate> gates = gateGroup.getRight();
//
////                boolean foundCorrect = false;
//
//                Optional<List<Pair<LogicGate, LogicGate>>> swap = Adder.iterateOverPairs(List.of(), gates, incorrectGates, pairsCount)
//                        .filter(pairs -> pairs.stream()
//                                .noneMatch(pair -> substitutions.containsKey(pair.getLeft()) || substitutions.containsKey(pair.getRight()))
//                        )
//                        .filter(pairs -> pairs.stream()
//                                .noneMatch(pair -> pair.getLeft().equals(pair.getRight()))
//                        )
//                        .dropWhile(pairs -> {
//                            pairs.forEach(pair -> {
//                                substitutions.put(pair.getLeft(), pair.getRight().output);
//                                substitutions.put(pair.getRight(), pair.getLeft().output);
//                            });
//
//                            boolean correct = adder.test(1L << (inputSize - bitOffset - 1), 0L, 1L << (inputSize - bitOffset - 1), substitutions) &&
//                                    adder.test(0L, 1L << (inputSize - bitOffset - 1), 1L << (inputSize - bitOffset - 1), substitutions) &&
//                                    adder.test(1L << (inputSize - bitOffset - 1), 1L << (inputSize - bitOffset - 1), 1L << (inputSize - bitOffset), substitutions) &&
//                                    adder.test(3L << (inputSize - bitOffset - 2), 3L << (inputSize - bitOffset - 2), 3L << (inputSize - bitOffset - 1), substitutions);
//
//                            pairs.forEach(pair -> {
//                                substitutions.remove(pair.getLeft());
//                                substitutions.remove(pair.getRight());
//                            });
//
//                            return correct;
//                        }).findFirst();
//
//                if (swap.isPresent()) {
//                    swap.get().forEach(pair -> {
//                                substitutions.put(pair.getLeft(), pair.getRight().output);
//                                substitutions.put(pair.getRight(), pair.getLeft().output);
//                            });
//
//                    incorrectGates.removeAll(gates);
//                    groupsToRemove.add(gateGroup);
//                }




//                for (LogicGate gate1 : gates) {
//                    if (substitutions.containsKey(gate1)) continue;
//
//                    for (LogicGate gate2 : incorrectGates) {
//                        if (gate1.equals(gate2)) continue;
//                        if (substitutions.containsKey(gate2)) continue;
//
//                        substitutions.put(gate1, gate2.output);
//                        substitutions.put(gate2, gate1.output);
//
//                        boolean correct = adder.test(1L << (inputSize - bitOffset - 1), 0L, 1L << (inputSize - bitOffset - 1), substitutions) &&
//                                adder.test(0L, 1L << (inputSize - bitOffset - 1), 1L << (inputSize - bitOffset - 1), substitutions) &&
//                                adder.test(1L << (inputSize - bitOffset - 1), 1L << (inputSize - bitOffset - 1), 1L << (inputSize - bitOffset), substitutions) &&
//                                adder.test(3L << (inputSize - bitOffset - 2), 3L << (inputSize - bitOffset - 2), 3L << (inputSize - bitOffset - 1), substitutions);
//
//                        if (correct) {
//                            foundCorrect = true;
//                            break;
//                        }
//
//                        substitutions.remove(gate1);
//                        substitutions.remove(gate2);
//                    }
//
//                    if (foundCorrect) {
//                        break;
//                    }
//                }
//
//                if (!foundCorrect) {
//                    System.out.println("Could not find correct for bit: " + (inputSize - bitOffset - 1));
//                } else {
//                    System.out.println("Found correct for bit: " + (inputSize - bitOffset - 1));
//                    incorrectGates.removeAll(gates);
//                }
//            }
//
//            incorrectGateGroups.removeAll(groupsToRemove);
//        }
//
//        System.out.println(substitutions.values().stream().map(wire -> wire.name).sorted().collect(Collectors.joining(",")));
//
//        for (int i = 0; i < inputSize; i++) {
//            boolean correct = adder.test(1L << (inputSize - i - 1), 0L, 1L << (inputSize - i - 1), substitutions) &&
//                    adder.test(0L, 1L << (inputSize - i - 1), 1L << (inputSize - i - 1), substitutions);
//
//            if (!correct) {
//                System.out.println(i);
//            }
//        }

//        for (int i = 0; i < adder.input1.size(); i++) {
//            Wire input1Wire = adder.input1.get(i);
//
//            List<Wire> outputs = input1Wire.getFurthestOutputs().stream()
//                    .sorted((o1, o2) -> Objects.compare(o2, o1, Comparator.comparing(wire -> wire.name)))
//                    .toList();
//            List<Wire> expectedOutputs = adder.output.subList(0, i + 2);
//
//            if (!outputs.equals(expectedOutputs)) {
//                System.out.println(outputs);
//            }
//        }

//        for (int i = 0; i < adder.input2.size(); i++) {
//            Wire input2Wire = adder.input2.get(i);
//
//            System.out.println(input2Wire + ": " + input2Wire.getFurthestOutputs().stream().map(wire -> wire.name).sorted().toList());
//        }
//
//        for (int i = 0; i < adder.output.size(); i++) {
//            Wire outputWire = adder.output.get(i);
//
//            System.out.println(outputWire + ": " + outputWire.getFurthestInputs().stream().map(wire -> wire.name).sorted().toList());
//        }
    }

    private record IncorrectGateGroupQueueElement(int pairsLimit, List<GateGroup> incorrectGroups, Map<LogicGate, Wire> substitutions) {
        private int getIncorrectCount() {
            return incorrectGroups.stream().map(GateGroup::gates).map(Set::size).reduce(0, Integer::sum);
        }
    }

    private record GateGroup(Set<LogicGate> gates, int bit) { }

    private static class Adder {
        private final List<Wire> input1;
        private final List<Wire> input2;
        private final List<Wire> output;

        private Adder(Set<Wire> wires) {
            input1 = wires.stream()
                    .filter(wire -> wire.name.startsWith("x"))
                    .sorted((o1, o2) -> Objects.compare(o2, o1, Comparator.comparing(wire -> wire.name)))
                    .toList();
            input2 = wires.stream()
                    .filter(wire -> wire.name.startsWith("y"))
                    .sorted((o1, o2) -> Objects.compare(o2, o1, Comparator.comparing(wire -> wire.name)))
                    .toList();
            output = wires.stream()
                    .filter(wire -> wire.name.startsWith("z"))
                    .sorted((o1, o2) -> Objects.compare(o2, o1, Comparator.comparing(wire -> wire.name)))
                    .toList();
        }

        private void evaluate() {
            evaluate(Map.of());
        }

        private void evaluate(Map<LogicGate, Wire> substitutions) {
            Queue<Wire> queue = new LinkedList<>(Stream.of(input1, input2).flatMap(List::stream).toList());

            Set<Wire> processedWires = new HashSet<>();

            while (!queue.isEmpty()) {
                Wire wire = queue.poll();

                processedWires.add(wire);

                for (LogicGate gate : wire.outputs) {
                    if (gate.ready(processedWires)) {
                        Wire output = substitutions.getOrDefault(gate, gate.output);

                        gate.setOutput(output);
                        queue.add(output);
                    }
                }
            }
        }

        private void setInput1(long value) {
            setWiresToValue(value, input1);
        }

        private void setInput2(long value) {
            setWiresToValue(value, input2);
        }

        private void setWiresToValue(long value, List<Wire> wires) {
            String inputString = Long.toString(value, 2);

            List<Boolean> inputStates = Arrays.stream(inputString.split(""))
                    .map(digit -> digit.equals("1"))
                    .toList();

            IntStream.range(0, wires.size())
                    .forEach(i -> {
                        if (i < wires.size() - inputStates.size()) {
                            wires.get(i).on = false;
                        } else {
                            wires.get(i).on = inputStates.get(i - (wires.size() - inputStates.size()));
                        }
                    });
        }

        private Set<Wire> getAllUsed() {
            Queue<Wire> queue = new LinkedList<>(Stream.of(input1, input2, output).flatMap(List::stream).toList());

            Set<Wire> visited = new HashSet<>();

            while (!queue.isEmpty()) {
                Wire currentWire = queue.poll();

                if (visited.contains(currentWire)) continue;

                visited.add(currentWire);

                currentWire.outputs.stream()
                        .flatMap(wire -> Stream.of(wire.input1, wire.input2, wire.output))
                        .forEach(queue::add);

                if (currentWire.input != null) {
                    LogicGate input = currentWire.input;
                    queue.addAll(List.of(input.input1, input.input2, input.output));
                }
            }

            return visited;
        }

        private boolean test(long input1, long input2, long output) {
            setInput1(input1);
            setInput2(input2);

            evaluate();
            return readOutput() == output;
        }

        private boolean test(long input1, long input2, long output, Map<LogicGate, Wire> substitutions) {
            setInput1(input1);
            setInput2(input2);

            evaluate(substitutions);
            return readOutput() == output;
        }

        private long readOutput() {
            String outputString = output.stream()
                    .map(wire -> wire.on)
                    .map(on -> on ? "1" : "0")
                    .collect(Collectors.joining());

            return Long.parseLong(outputString, 2);
        }

        private static Stream<List<Pair<LogicGate, LogicGate>>> iterateOverPairs(List<LogicGate> current, GateGroup gateGroup, List<GateGroup> gateGroups, int pairsCount) {
            if (current.size() == pairsCount * 2) {
                AtomicInteger index = new AtomicInteger(0);

                List<Pair<LogicGate, LogicGate>> pairs = current.stream()
                        .collect(Collectors.groupingBy(element -> {
                            int i = index.getAndIncrement();
                            return (i % 2 == 0) ? i : i - 1;
                        })).values().stream()
                        .map(elements -> Pair.of(elements.get(0), elements.get(1)))
                        .toList();

                return Stream.of(pairs);
            } else if (current.size() > pairsCount * 2) {
                return Stream.of();
            }

            // Recursive step: try pairing each gate
            return gateGroup.gates.stream()
                    .filter(outer -> !current.contains(outer))
                    .flatMap(outer -> gateGroups.stream()
                            .filter(logicGates -> !logicGates.equals(gateGroup))
                            .map(GateGroup::gates)
                            .flatMap(Collection::stream)
                            .filter(inner -> !current.contains(inner))
                            .flatMap(inner -> {
                                List<LogicGate> newCurrent = new ArrayList<>(current);
                                newCurrent.add(outer);
                                newCurrent.add(inner);

                                return iterateOverPairs(newCurrent, gateGroup, gateGroups, pairsCount);
                            })
                    );
        }

        private static Adder parse(List<String> lines) {
            List<String> initialWireLines = lines.stream()
                    .takeWhile(string -> !string.isEmpty())
                    .toList();

            Set<Wire> initialWires = Wire.parse(initialWireLines);
            Map<String, Wire> wireLookup = initialWires.stream().collect(Collectors.toMap(wire -> wire.name, Function.identity()));

            List<String> gateLines = lines.stream()
                    .dropWhile(string -> !string.isEmpty())
                    .skip(1)
                    .toList();

            LogicGate.parse(gateLines, wireLookup);

            Set<Wire> wires = new HashSet<>(wireLookup.values());

            return new Adder(wires);
        }
    }

    private static class Wire {
        private final String name;
        private boolean on;
        private LogicGate input;
        private final Set<LogicGate> outputs;

        private static final Pattern pattern = Pattern.compile("^(.+?): ([01])$");

        private Wire(String name, boolean on) {
            this.name = name;
            this.on = on;
            this.outputs = new HashSet<>();
            this.input = null;
        }

        private void addOutput(LogicGate gate) {
            this.outputs.add(gate);
        }

        private void setInput(LogicGate gate) {
            this.input = gate;
        }

        private static Set<Wire> parse(List<String> lines) {
            Set<Wire> wires = new HashSet<>();

            lines.forEach(string -> {
                Matcher matcher = pattern.matcher(string);
                boolean found = matcher.find();
                assert found;
                wires.add(new Wire(matcher.group(1), matcher.group(2).equals("1")));
            });

            return wires;
        }

        private Set<Wire> getDependentOutputs() {
            Queue<Wire> queue = new LinkedList<>();
            queue.add(this);

            Set<Wire> visited = new HashSet<>();

            while (!queue.isEmpty()) {
                Wire currentWire = queue.poll();

                if (visited.contains(currentWire)) continue;

                visited.add(currentWire);

                currentWire.outputs.stream()
                        .map(wire -> wire.output)
                        .forEach(queue::add);
            }

            return visited;
        }

        private Set<Wire> getFurthestInputs() {
            Queue<Wire> queue = new LinkedList<>();
            queue.add(this);

            Set<Wire> visited = new HashSet<>();

            Set<Wire> furthestInputs = new HashSet<>();

            while (!queue.isEmpty()) {
                Wire currentWire = queue.poll();

                if (visited.contains(currentWire)) continue;

                visited.add(currentWire);

                if (currentWire.input == null) {
                    furthestInputs.add(currentWire);
                } else {
                    queue.add(currentWire.input.input1);
                    queue.add(currentWire.input.input2);
                }
            }

            return furthestInputs;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Wire wire = (Wire) o;
            return Objects.equals(name, wire.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name);
        }

        @Override
        public String toString() {
            return name;
        }
    }

    private static abstract class LogicGate {
        protected Wire input1;
        protected Wire input2;
        protected Wire output;

        public LogicGate(Wire input1, Wire input2, Wire output) {
            this.input1 = input1;
            this.input2 = input2;
            this.output = output;
        }

        private boolean ready(Set<Wire> processed) {
            return processed.contains(input1) && processed.contains(input2);
        }

        private static final Pattern pattern = Pattern.compile("^(\\S+) (XOR|OR|AND) (\\S+) -> (\\S+)$");

        private static void parse(List<String> lines, Map<String, Wire> wireLookup) {
            lines.forEach(string -> {
                Matcher matcher = pattern.matcher(string);
                boolean found = matcher.find();
                assert found;

                Wire input1 = wireLookup.computeIfAbsent(matcher.group(1), name -> new Wire(name, false));
                Wire input2 = wireLookup.computeIfAbsent(matcher.group(3), name -> new Wire(name, false));
                Wire output = wireLookup.computeIfAbsent(matcher.group(4), name -> new Wire(name, false));

                LogicGate gate = switch (matcher.group(2)) {
                    case "XOR" -> new XORGate(input1, input2, output);
                    case "OR" -> new ORGate(input1, input2, output);
                    case "AND" -> new ANDGate(input1, input2, output);
                    default -> throw new IllegalStateException("Unexpected value: " + matcher.group(2));
                };

                input1.addOutput(gate);
                input2.addOutput(gate);
                output.setInput(gate);
            });
        }

        public abstract void setOutput(Wire output);

        @Override
        public String toString() {
            return "Gate{" +
                    "i1=" + input1 +
                    ", i2=" + input2 +
                    ", o=" + output +
                    '}';
        }
    }

    private static class XORGate extends LogicGate {
        public XORGate(Wire input1, Wire input2, Wire output) {
            super(input1, input2, output);
        }

        @Override
        public void setOutput(Wire output) {
            output.on = input1.on ^ input2.on;
        }
    }

    private static class ORGate extends LogicGate {
        public ORGate(Wire input1, Wire input2, Wire output) {
            super(input1, input2, output);
        }

        @Override
        public void setOutput(Wire output) {
            output.on = input1.on || input2.on;
        }
    }

    private static class ANDGate extends LogicGate {
        public ANDGate(Wire input1, Wire input2, Wire output) {
            super(input1, input2, output);
        }

        @Override
        public void setOutput(Wire output) {
            output.on = input1.on && input2.on;
        }
    }
}
