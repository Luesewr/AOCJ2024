package aocj2024;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;

public class Day1 extends Day {
    @Override
    public void part1() {
        Scanner scanner = getScanner("day1.txt");

        Pair<LocationList, LocationList> lists = LocationList.parseDoubleLocationList((scanner));

        int difference = lists.getLeft().getLocationListDifference(lists.getRight());

        System.out.println(difference);
    }

    @Override
    public void part2() {
        Scanner scanner = getScanner("day1.txt");

        Pair<LocationList, LocationList> lists = LocationList.parseDoubleLocationList((scanner));

        int similarity = lists.getLeft().getLocationListSimilarity(lists.getRight());

        System.out.println(similarity);
    }

    private static class LocationList {
        private final List<Integer> locationIDs;

        private LocationList() {
            this.locationIDs = new ArrayList<>();
        }

        private void addLocationID(@NotNull Integer locationID) {
            this.locationIDs.add(locationID);
        }

        private void sortList() {
            this.locationIDs.sort(Integer::compareTo);
        }

        private int getLocationListDifference(@NotNull LocationList other) {
            this.sortList();
            other.sortList();

            List<Integer> thisLocationList = this.getLocationIDs(), otherLocationList = other.getLocationIDs();

            assert thisLocationList.size() == otherLocationList.size();

            int totalDifference = 0;

            for (int i = 0, size = getLocationIDs().size(); i < size; i++) {
                int difference = Math.abs(thisLocationList.get(i) - otherLocationList.get(i));

                totalDifference += difference;
            }

            return totalDifference;
        }

        private int getLocationListSimilarity(@NotNull LocationList other) {
            this.sortList();
            other.sortList();

            List<Integer> otherLocationList = other.getLocationIDs();
            int otherSize = otherLocationList.size();

            int rightIndex = 0;
            int rightCount = 1;

            int similarity = 0;

            for (Integer locationID : this.getLocationIDs()) {
                while (rightIndex < otherSize && locationID > otherLocationList.get(rightIndex)) {
                    rightIndex++;
                    rightCount = 1;
                }

                while (rightIndex + 1 < otherSize && locationID >= otherLocationList.get(rightIndex + 1)) {
                    rightIndex++;
                    rightCount++;
                }

                if (rightIndex < otherSize && locationID >= otherLocationList.get(rightIndex)) {
                    similarity += locationID * rightCount;
                }
            }

            return similarity;
        }

        private List<Integer> getLocationIDs() {
            return this.locationIDs;
        }

        private static Pair<LocationList, LocationList> parseDoubleLocationList(@NotNull Scanner scanner) {
            int index = 0;

            LocationList locationList1 = new LocationList(), locationList2 = new LocationList();

            while (scanner.hasNextInt()) {
                int locationID = scanner.nextInt();

                if (index % 2 == 0) {
                    locationList1.addLocationID(locationID);
                } else {
                    locationList2.addLocationID(locationID);
                }

                index++;
            }

            return Pair.of(locationList1, locationList2);
        }
    }
}
