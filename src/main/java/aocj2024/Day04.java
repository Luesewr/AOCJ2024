package aocj2024;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Day04 extends Day {
    @Override
    public void part1() {
        List<String> lines = getLinesFromFile("day04.txt").toList();
        Grid grid = new Grid(lines);

        int xmasCount = grid.countResults("XMAS", grid.grid);

        System.out.println(xmasCount);
    }

    @Override
    public void part2() {
        List<String> lines = getLinesFromFile("day04.txt").toList();
        Grid grid = new Grid(lines);

        int xmasCount = grid.countIntersections("MAS");

        System.out.println(xmasCount);
    }

    private static class Grid {
        private final List<GridPointContainer> grid;

        private Grid(List<String> stringGrid) {
            this.grid = new ArrayList<>();

            List<List<GridPoint>> gridPointsGrid = getGridPointsGrid(stringGrid);

            createVariants(gridPointsGrid);
        }

        private List<List<GridPoint>> getGridPointsGrid(List<String> stringGrid) {
            List<List<GridPoint>> gridPointsGrid = new ArrayList<>();

            for (int i = 0, stringGridSize = stringGrid.size(); i < stringGridSize; i++) {
                String string = stringGrid.get(i);
                char[] charArray = string.toCharArray();

                List<GridPoint> gridRow = new ArrayList<>();
                gridPointsGrid.add(gridRow);
                for (int j = 0, charArrayLength = charArray.length; j < charArrayLength; j++) {
                    char character = charArray[j];
                    GridPoint gridPoint = new GridPoint(character, new Point(j, i));
                    gridRow.add(gridPoint);
                }
            }

            return gridPointsGrid;
        }

        private void createVariants(List<List<GridPoint>> gridPoints) {
            Row.extractFromGrid(this, gridPoints);
            Column.extractFromGrid(this, gridPoints);
            MajorDiagonal.extractFromGrid(this, gridPoints);
            MinorDiagonal.extractFromGrid(this, gridPoints);
        }

        private int countIntersections(String string) {
            assert string.length() > 2;

            List<GridPointContainer> majorDiagonalContainers = this.grid.stream()
                    .filter(gridPointContainer -> gridPointContainer instanceof MajorDiagonal)
                    .toList();
            List<GridPointContainer> minorDiagonalContainers = this.grid.stream()
                    .filter(gridPointContainer -> gridPointContainer instanceof MinorDiagonal)
                    .toList();

            Set<GridPoint> gridPointSet = getResults(string, majorDiagonalContainers).stream()
                    .flatMap(findResults -> findResults.stream()
                            .flatMap(findResult -> Arrays.stream(findResult.gridPoints)
                                    .skip(1)
                                    .limit(findResult.gridPoints.length - 2))).collect(Collectors.toSet());
            Set<GridPoint> gridPointsRemoveSet = getResults(string, minorDiagonalContainers).stream()
                    .flatMap(findResults -> findResults.stream()
                            .flatMap(findResult -> Arrays.stream(findResult.gridPoints)
                                    .skip(1)
                                    .limit(findResult.gridPoints.length - 2))).collect(Collectors.toSet());
            gridPointSet.retainAll(gridPointsRemoveSet);

            return gridPointSet.size();
        }

        private int countResults(String string, List<GridPointContainer> containers) {
            return getResults(string, containers).stream()
                    .map(List::size)
                    .reduce(0, Integer::sum);
        }

        private List<List<FindResult>> getResults(String string, List<GridPointContainer> containers) {
            return containers.stream()
                    .map(gridPointContainer -> gridPointContainer.findResult(string))
                    .collect(Collectors.toList());
        }

        private void addAll(List<? extends GridPointContainer> gridPointContainers) {
            this.grid.addAll(gridPointContainers);
        }
    }

    private static abstract class GridPointContainer {
        private final List<GridPoint> gridPoints;

        private GridPointContainer() {
            this.gridPoints = new ArrayList<>();
        }

        private GridPointContainer(List<GridPoint> gridPoints) {
            this.gridPoints = gridPoints;
        }

        private List<FindResult> findResult(String string) {
            int resultLength = string.length();
            String reverseString = new StringBuilder(string).reverse().toString();
            return IntStream.rangeClosed(resultLength, gridPoints.size())
                    .mapToObj(i -> new FindResult(gridPoints.subList(i - resultLength, i).toArray(GridPoint[]::new)))
                    .filter(findResult -> findResult.toString().equals(string) || findResult.toString().equals(reverseString)).collect(Collectors.toList());
        }

        void add(GridPoint gridPoint) {
            this.gridPoints.add(gridPoint);
        }
    }

    private static class Row extends GridPointContainer {
        private Row(List<GridPoint> gridPoints) {
            super(gridPoints);
        }

        private static void extractFromGrid(Grid grid, List<List<GridPoint>> gridPointsGrid) {
            List<Row> rows = gridPointsGrid.stream().map(Row::new).toList();

            grid.addAll(rows);
        }
    }

    private static class Column extends GridPointContainer {
        private static void extractFromGrid(Grid grid, List<List<GridPoint>> gridPointsGrid) {
            List<Column> columns = new ArrayList<>();

            for (int i = 0; i < gridPointsGrid.get(0).size(); i++) {
                Column column = new Column();
                columns.add(column);

                for (List<GridPoint> gridPoints : gridPointsGrid) {
                    GridPoint gridPoint = gridPoints.get(i);
                    column.add(gridPoint);
                }
            }

            grid.addAll(columns);
        }
    }

    private static class MajorDiagonal extends GridPointContainer {
        private static void extractFromGrid(Grid grid, List<List<GridPoint>> gridPointsGrid) {
            List<MajorDiagonal> majorDiagonals = new ArrayList<>();

            for (int i = -(gridPointsGrid.get(0).size() - 1); i < gridPointsGrid.size(); i++) {
                MajorDiagonal majorDiagonal = new MajorDiagonal();
                majorDiagonals.add(majorDiagonal);

                for (int j = 0; j < gridPointsGrid.get(0).size(); j++) {
                    if (j + i < 0 || j + i >= gridPointsGrid.size()) continue;

                    List<GridPoint> gridPoints = gridPointsGrid.get(j + i);
                    GridPoint gridPoint = gridPoints.get(j);
                    majorDiagonal.add(gridPoint);
                }
            }

            grid.addAll(majorDiagonals);
        }
    }

    private static class MinorDiagonal extends GridPointContainer {
        private static void extractFromGrid(Grid grid, List<List<GridPoint>> gridPointsGrid) {
            List<MinorDiagonal> minorDiagonals = new ArrayList<>();

            int endIndex = gridPointsGrid.get(0).size() - 1;

            for (int i = -endIndex; i < gridPointsGrid.size(); i++) {
                MinorDiagonal minorDiagonal = new MinorDiagonal();
                minorDiagonals.add(minorDiagonal);

                for (int j = 0; j < gridPointsGrid.get(0).size(); j++) {
                    if (j + i < 0 || j + i >= gridPointsGrid.size()) continue;

                    List<GridPoint> gridPoints = gridPointsGrid.get(j + i);
                    GridPoint gridPoint = gridPoints.get(endIndex - j);
                    minorDiagonal.add(gridPoint);
                }
            }

            grid.addAll(minorDiagonals);
        }
    }

    private record GridPoint(char character, Point point) {
    }

    private record FindResult(GridPoint[] gridPoints) {
        @Override
        public String toString() {
            StringBuilder stringBuilder = new StringBuilder();

            for (GridPoint gridPoint : gridPoints) {
                stringBuilder.append(gridPoint.character);
            }

            return stringBuilder.toString();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            FindResult that = (FindResult) o;
            return Arrays.equals(gridPoints, that.gridPoints);
        }

        @Override
        public int hashCode() {
            return Arrays.hashCode(gridPoints);
        }
    }
}
