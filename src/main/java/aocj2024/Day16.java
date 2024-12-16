package aocj2024;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.stream.IntStream;
import org.apache.commons.lang3.tuple.Pair;

public class Day16 extends Day {
    @Override
    public void part1() {
        Maze maze = Maze.parse(getLinesFromFile("day16.txt").toList());
        int lowestScore = maze.getLowestScore();
        System.out.println(lowestScore);
    }

    @Override
    public void part2() {
        Maze maze = Maze.parse(getLinesFromFile("day16.txt").toList());
        long bestTiles = maze.countBestTile();
        System.out.println(bestTiles);
    }

    private record Maze(MazeNode start, MazeNode end, List<MazeNode> nodes) {
        private static Maze parse(List<String> lines) {
            List<List<MazeNode>> mazeNodes = IntStream.range(0, lines.size())
                    .mapToObj(lineIndex -> IntStream.range(0, lines.get(lineIndex).length())
                            .mapToObj(nodeIndex ->
                                    new MazeNode(
                                            lines.get(lineIndex).charAt(nodeIndex),
                                            new Point(nodeIndex, lineIndex),
                                            new HashSet<>()
                                    )
                            )
                            .toList()
                    )
                    .toList();

            MazeNode start = mazeNodes.stream()
                    .map(nodes -> nodes.stream()
                            .filter(node -> node.type == 'S')
                            .findFirst()
                    )
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .findFirst()
                    .orElse(null);
            MazeNode end = mazeNodes.stream()
                    .map(nodes -> nodes.stream()
                            .filter(node -> node.type == 'E')
                            .findFirst()
                    )
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .findFirst()
                    .orElse(null);

            IntStream.range(0, mazeNodes.size())
                    .forEach(lineIndex -> IntStream.range(0, mazeNodes.get(lineIndex).size())
                            .forEach(nodeIndex -> {
                                MazeNode mazeNode = mazeNodes.get(lineIndex).get(nodeIndex);

                                if (lineIndex - 1 >= 0) {
                                    mazeNode.addNeighbour(mazeNodes.get(lineIndex - 1).get(nodeIndex), new Point(0, -1));
                                }
                                if (lineIndex + 1 < mazeNodes.size()) {
                                    mazeNode.addNeighbour(mazeNodes.get(lineIndex + 1).get(nodeIndex), new Point(0, 1));
                                }
                                if (nodeIndex - 1 >= 0) {
                                    mazeNode.addNeighbour(mazeNodes.get(lineIndex).get(nodeIndex - 1), new Point(-1, 0));
                                }
                                if (nodeIndex + 1 < mazeNodes.get(lineIndex).size()) {
                                    mazeNode.addNeighbour(mazeNodes.get(lineIndex).get(nodeIndex + 1), new Point(1, 0));
                                }
                            }));

            List<MazeNode> nodes = mazeNodes.stream()
                    .flatMap(List::stream)
                    .filter(node -> node.type != '#')
                    .toList();

            return new Maze(start, end, nodes);
        }

        private int getLowestScore() {
            List<MazeQueueElement> bestPaths = getTravelMap();

            return bestPaths.stream()
                    .map(MazeQueueElement::totalScore)
                    .min(Comparator.comparingInt(o -> o)).orElse(Integer.MAX_VALUE);

        }

        private long countBestTile() {
            List<MazeQueueElement> bestPaths = getTravelMap();

            return bestPaths.stream()
                    .map(MazeQueueElement::nodes)
                    .flatMap(List::stream)
                    .distinct()
                    .count();
        }

        private List<MazeQueueElement> getTravelMap() {
            Map<Pair<MazeNode, Point>, Integer> distances = new HashMap<>();

            PriorityQueue<MazeQueueElement> pq = new PriorityQueue<>(Comparator.comparingInt(element -> element.totalScore));

            pq.add(new MazeQueueElement(start, new Point(1, 0), 0, List.of(start)));
            distances.put(Pair.of(start, new Point(1, 0)), 0);

            int bestScore = Integer.MAX_VALUE;
            List<MazeQueueElement> bestPaths = new ArrayList<>();

            while (!pq.isEmpty()) {
                MazeQueueElement element = pq.poll();

                Pair<MazeNode, Point> elementKey = Pair.of(element.node, element.direction);
                int currentBestDistance = distances.computeIfAbsent(elementKey, key -> Integer.MAX_VALUE);

                if (element.node.equals(end)) {
                    if (currentBestDistance < bestScore) {
                        bestPaths.clear();
                        bestScore = currentBestDistance;
                    }

                    if (currentBestDistance == bestScore) {
                        bestPaths.add(element);
                    }

                    continue;
                }

                for (MazeNodeConnection connection : element.node.connections) {
                    int cost = element.getDirectionCost(connection) + 1;
                    Pair<MazeNode, Point> connectionKey = Pair.of(connection.end, connection.direction);

                    int totalCost = currentBestDistance + cost;

                    if (totalCost <= distances.computeIfAbsent(connectionKey, key -> Integer.MAX_VALUE)) {
                        distances.replace(Pair.of(connection.end, connection.direction), totalCost);

                        List<MazeNode> newNodes = new ArrayList<>(element.nodes);
                        newNodes.add(connection.end);

                        pq.add(new MazeQueueElement(connection.end, connection.direction, totalCost, newNodes));
                    }
                }
            }

            return bestPaths;
        }
    }

    private record MazeNode(char type, Point location, Set<MazeNodeConnection> connections) {
        private void addNeighbour(MazeNode mazeNode, Point direction) {
            if (mazeNode.type == '#') return;

            MazeNodeConnection connection = new MazeNodeConnection(this, mazeNode, direction);

            connections.add(connection);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            MazeNode mazeNode = (MazeNode) o;
            return type == mazeNode.type && Objects.equals(location, mazeNode.location);
        }

        @Override
        public int hashCode() {
            return Objects.hash(type, location);
        }

        @Override
        public String toString() {
            return "MazeNode{" +
                    "type=" + type +
                    ", location=" + location +
                    '}';
        }
    }

    private record MazeQueueElement(MazeNode node, Point direction, int totalScore, List<MazeNode> nodes) {
        private int getDirectionCost(MazeNodeConnection connection) {
            if (connection.direction.equals(direction)) return 0;
            if (connection.direction.equals(new Point(-direction.x, -direction.y))) return 2000;
            return 1000;
        }
    }

    private record MazeNodeConnection(MazeNode start, MazeNode end, Point direction) { }
}
