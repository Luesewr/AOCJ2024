package aocj2024;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.tuple.Pair;

public class Day23 extends Day {
    @Override
    public void part1() {
        List<String> lines = getLinesFromFile("day23.txt").toList();

        Collection<Computer> computers = Computer.parse(lines);

        long interconnectedCount = computers.stream()
                .filter(computer1 -> computer1.name.startsWith("t"))
                .flatMap(computer1 -> computer1.connections.stream()
                        .map(computer2 -> {
                            Set<Computer> intersection = new HashSet<>(computer1.connections);
                            intersection.retainAll(computer2.connections);
                            return Pair.of(computer2, intersection);
                        })
                        .filter(computerSetPair -> !computerSetPair.getRight().isEmpty())
                        .flatMap(computerSetPair -> computerSetPair.getRight().stream()
                                .map(computer3 -> Set.of(computer1, computerSetPair.getLeft(), computer3))
                        )
                )
                .distinct()
                .count();

        System.out.println(interconnectedCount);
    }

    @Override
    public void part2() {
        List<String> lines = getLinesFromFile("day23.txt").toList();

        Set<Computer> computers = new HashSet<>(Computer.parse(lines));

        Set<Computer> biggestConnected = computers.stream()
                .flatMap(computer -> {
                    Set<Set<Computer>> groups = new HashSet<>();
                    computer.getGroups(groups, computers);
                    return groups.stream();
                })
                .max(Comparator.comparingInt(Set::size))
                .orElse(Set.of());

        System.out.println(biggestConnected.stream().map(Computer::name).sorted().collect(Collectors.joining(",")));
    }

    private record Computer(String name, Set<Computer> connections) {
        private Computer(String name) {
            this(name, new HashSet<>());
        }

        private void addConnection(Computer computer) {
            this.connections.add(computer);
        }

        private Set<Computer> getSurrounding() {
            Set<Computer> surrounding = new HashSet<>(connections);
            surrounding.add(this);

            return surrounding;
        }

        private void getGroups(Set<Set<Computer>> groups, Set<Computer> sharing) {
            getGroups(groups, Set.of(this), sharing);
        }

        private Set<Set<Computer>> getGroups(Set<Set<Computer>> groups, Set<Computer> group, Set<Computer> sharing) {
            if (groups.contains(group)) return Set.of();

            Set<Computer> newSharing = new HashSet<>(sharing);
            newSharing.retainAll(getSurrounding());

            if (group.size() > newSharing.size() || !newSharing.containsAll(group)) {
                return Set.of();
            }

            for (Computer connection : connections) {
                if (!sharing.contains(connection)) continue;
                if (group.contains(connection)) continue;

                Set<Computer> newGroup = new HashSet<>(group);
                newGroup.add(connection);

                Set<Set<Computer>> subGroups = connection.getGroups(groups, newGroup, newSharing);

                int largestGroup = subGroups.stream().map(Set::size).max(Integer::compareTo).orElse(0);

                Set<Set<Computer>> largestGroups = subGroups.stream()
                        .filter(computers -> computers.size() == largestGroup)
                        .collect(Collectors.toSet());

                groups.addAll(largestGroups);
            }

            if (groups.isEmpty()) {
                groups.add(group);
            }

            int largestGroup = groups.stream().map(Set::size).max(Integer::compareTo).orElse(0);

            return groups.stream()
                    .filter(computers -> computers.size() == largestGroup)
                    .collect(Collectors.toSet());
        }

        private static Collection<Computer> parse(List<String> lines) {
            Map<String, Computer> computerMap = new HashMap<>();

            lines.stream()
                    .flatMap(string -> {
                        String[] elements = string.split("-");
                        return Stream.of(Pair.of(elements[0], elements[1]), Pair.of(elements[1], elements[0]));
                    })
                    .forEach(connectionPair ->
                            computerMap.computeIfAbsent(connectionPair.getLeft(), Computer::new).addConnection(
                                    computerMap.computeIfAbsent(connectionPair.getRight(), Computer::new)
                            )
                    );

            return computerMap.values();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Computer computer = (Computer) o;
            return Objects.equals(name, computer.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name);
        }

        @Override
        public String toString() {
            return "Computer{" +
                    "name='" + name + '\'' +
                    ", connections=" + connections.stream().map(Computer::name).toList() +
                    '}';
        }
    }
}
