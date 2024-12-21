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

public class Day20 extends Day {
    @Override
    public void part1() {
        Track track = Track.parse(getLinesFromFile("day20.txt").toList());
        long lowestScore = track.getCheatingLocationCount(2, 100);
        System.out.println(lowestScore);
    }

    @Override
    public void part2() {
        Track track = Track.parse(getLinesFromFile("day20.txt").toList());
        long lowestScore = track.getCheatingLocationCount(20, 100);
        System.out.println(lowestScore);
    }

    private record Track(TrackNode start, TrackNode end, List<TrackNode> nodes) {
        private static Track parse(List<String> lines) {
            List<List<TrackNode>> trackNodes = IntStream.range(0, lines.size())
                    .mapToObj(lineIndex -> IntStream.range(0, lines.get(lineIndex).length())
                            .mapToObj(nodeIndex ->
                                    new TrackNode(
                                            lines.get(lineIndex).charAt(nodeIndex),
                                            new Point(nodeIndex, lineIndex),
                                            new HashSet<>()
                                    )
                            )
                            .toList()
                    )
                    .toList();

            TrackNode start = trackNodes.stream()
                    .map(nodes -> nodes.stream()
                            .filter(node -> node.type == 'S')
                            .findFirst()
                    )
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .findFirst()
                    .orElse(null);
            TrackNode end = trackNodes.stream()
                    .map(nodes -> nodes.stream()
                            .filter(node -> node.type == 'E')
                            .findFirst()
                    )
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .findFirst()
                    .orElse(null);

            IntStream.range(0, trackNodes.size())
                    .forEach(lineIndex -> IntStream.range(0, trackNodes.get(lineIndex).size())
                            .forEach(nodeIndex -> {
                                TrackNode trackNode = trackNodes.get(lineIndex).get(nodeIndex);

                                if (lineIndex - 1 >= 0) {
                                    trackNode.addNeighbour(trackNodes.get(lineIndex - 1).get(nodeIndex), new Point(0, -1));
                                }
                                if (lineIndex + 1 < trackNodes.size()) {
                                    trackNode.addNeighbour(trackNodes.get(lineIndex + 1).get(nodeIndex), new Point(0, 1));
                                }
                                if (nodeIndex - 1 >= 0) {
                                    trackNode.addNeighbour(trackNodes.get(lineIndex).get(nodeIndex - 1), new Point(-1, 0));
                                }
                                if (nodeIndex + 1 < trackNodes.get(lineIndex).size()) {
                                    trackNode.addNeighbour(trackNodes.get(lineIndex).get(nodeIndex + 1), new Point(1, 0));
                                }
                            }));

            List<TrackNode> nodes = trackNodes.stream()
                    .flatMap(List::stream)
                    .toList();

            return new Track(start, end, nodes);
        }

        private long getCheatingLocationCount(int maxCheatingLocations, int minimumTimeSave) {
            Map<TrackNode, Integer> endDistanceMap = getDistanceMap(end);
            Map<TrackNode, Integer> startDistanceMap = getDistanceMap(start);
            List<Integer> cheatingTimeSaves = getCheatingTimeSaves(startDistanceMap, endDistanceMap, maxCheatingLocations, minimumTimeSave);

            return cheatingTimeSaves.size();
        }

        private Map<TrackNode, Integer> getDistanceMap(TrackNode node) {
            Map<TrackNode, Integer> distances = new HashMap<>();

            PriorityQueue<TrackQueueElement> pq = new PriorityQueue<>(Comparator.comparingInt(TrackQueueElement::totalScore));

            pq.add(new TrackQueueElement(node, 0, Set.of()));
            distances.put(node, 0);

            while (!pq.isEmpty()) {
                TrackQueueElement element = pq.poll();

                int currentBestDistance = element.totalScore;

                for (TrackNodeConnection connection : element.node.connections) {
                    int cost = 1;

                    int totalCost = currentBestDistance + cost;

                    if (totalCost <= distances.getOrDefault(connection.end, Integer.MAX_VALUE)) {
                        if (connection.end.type == '#') {
                            continue;
                        }

                        distances.put(connection.end, totalCost);

                        pq.add(new TrackQueueElement(connection.end, totalCost, element.cheatingLocations));
                    }
                }
            }

            return distances;
        }

        private List<Integer> getCheatingTimeSaves(Map<TrackNode, Integer> startDistanceMap, Map<TrackNode, Integer> endDistanceMap, int maxCheatingLocations, int minimumTimeSave) {
            int establishedDistance = endDistanceMap.get(start);
            int aimDistance = establishedDistance - minimumTimeSave;

            List<Integer> shortcutDistances = new ArrayList<>();

            startDistanceMap.forEach((startTrackNode, startDistance) -> endDistanceMap.forEach((endTrackNode, endDistance) -> {
                int shortcutLength = startTrackNode.distance(endTrackNode);
                if (shortcutLength <= maxCheatingLocations) {
                    int totalDistance = startDistance + shortcutLength + endDistance;
                    if (totalDistance <= aimDistance) {
                        shortcutDistances.add(totalDistance);
                    }
                }
            }));

            return shortcutDistances;
        }
    }

    private record TrackNode(char type, Point location, Set<TrackNodeConnection> connections) {
        private void addNeighbour(TrackNode trackNode, Point direction) {
            TrackNodeConnection connection = new TrackNodeConnection(this, trackNode, direction);

            connections.add(connection);
        }

        private int distance(TrackNode other) {
            return Math.abs(this.location.x - other.location.x) + Math.abs(this.location.y - other.location.y);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            TrackNode trackNode = (TrackNode) o;
            return type == trackNode.type && Objects.equals(location, trackNode.location);
        }

        @Override
        public int hashCode() {
            return Objects.hash(type, location);
        }

        @Override
        public String toString() {
            return "TrackNode{" +
                    "type=" + type +
                    ", location=" + location +
                    '}';
        }
    }

    private record TrackQueueElement(TrackNode node, int totalScore, Set<TrackNode> cheatingLocations) { }

    private record TrackNodeConnection(TrackNode start, TrackNode end, Point direction) { }
}
