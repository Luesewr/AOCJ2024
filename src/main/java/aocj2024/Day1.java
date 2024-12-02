package aocj2024;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;

public class Day1 extends Day {
    @Override
    public void part1() {
        Stream<String> lines = getLinesFromFile("day1.txt");

        Pair<LocationList, LocationList> lists = LocationList.parseDoubleLocationList(lines);

        int difference = lists.getLeft().getLocationListDifference(lists.getRight());

        System.out.println(difference);
    }

    @Override
    public void part2() {
        Stream<String> lines = getLinesFromFile("day1.txt");

        Pair<LocationList, LocationList> lists = LocationList.parseDoubleLocationList(lines);

        int similarity = lists.getLeft().getLocationListSimilarity(lists.getRight());

        System.out.println(similarity);
    }

    private record LocationList(List<Integer> locationIDs) {
        private int getLocationListDifference(@NotNull LocationList other) {
            List<Integer> thisLocationList = this.locationIDs, otherLocationList = other.locationIDs;

            assert thisLocationList.size() == otherLocationList.size();

            int totalDifference = 0;

            for (int i = 0, size = locationIDs.size(); i < size; i++) {
                int difference = Math.abs(thisLocationList.get(i) - otherLocationList.get(i));

                totalDifference += difference;
            }

            return totalDifference;
        }

        private int getLocationListSimilarity(@NotNull LocationList other) {
            List<Integer> otherLocationList = other.locationIDs;
            int otherSize = otherLocationList.size();

            int rightIndex = 0;
            int rightCount = 1;

            int similarity = 0;

            for (Integer locationID : this.locationIDs) {
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

        private static Pair<LocationList, LocationList> parseDoubleLocationList(@NotNull Stream<String> lines) {
            Pattern pattern = Pattern.compile("(\\d+)\\s+(\\d+)");

            List<Pair<String, String>> pairs = lines.map(pattern::matcher)
                    .filter(Matcher::find)
                    .map(matcher -> Pair.of(matcher.group(1), matcher.group(2)))
                    .toList();

            List<Integer> locations1 = pairs
                    .stream()
                    .map(Pair::getLeft)
                    .map(Integer::parseInt)
                    .sorted()
                    .toList();

            List<Integer> locations2 = pairs
                    .stream()
                    .map(Pair::getRight)
                    .map(Integer::parseInt)
                    .sorted()
                    .toList();

            LocationList locationList1 = new LocationList(locations1);
            LocationList locationList2 = new LocationList(locations2);

            return Pair.of(locationList1, locationList2);
        }
    }
}
