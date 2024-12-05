package aocj2024;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class Day5 extends Day {
    @Override
    public void part1() {
        List<String> lines = getLinesFromFile("day5.txt").toList();

        List<PageList> pageLists = PageList.setupPageListsFromLines(lines);

        int correctCount = pageLists.stream()
                .filter(PageList::isCorrect)
                .map(PageList::getMiddleValue)
                .reduce(0, Integer::sum);

        System.out.println(correctCount);
    }

    @Override
    public void part2() {
        List<String> lines = getLinesFromFile("day5.txt").toList();

        List<PageList> pageLists = PageList.setupPageListsFromLines(lines);

        int correctedCount = pageLists.stream()
                .filter(pageList -> !pageList.isCorrect())
                .map(pageList -> pageList.pages.stream()
                        .sorted()
                        .toList()
                )
                .map(PageList::new)
                .map(PageList::getMiddleValue)
                .reduce(0, Integer::sum);

        System.out.println(correctedCount);
    }

    private record PageList(List<Page> pages) {
        private static List<PageList> setupPageListsFromLines(List<String> lines) {
            List<String> pageRuleStrings = lines.stream().takeWhile(string -> !string.isEmpty()).toList();
            List<String> pageListsStrings = lines.stream().dropWhile(string -> !string.isEmpty()).skip(1).toList();

            return PageList.parsePageLists(pageListsStrings, pageRuleStrings);
        }

        private static List<PageList> parsePageLists(List<String> pageListsStrings, List<String> pageRuleStrings) {
            Map<String, Page> pageLookup = new HashMap<>();

            List<PageList> pageLists = pageListsStrings.stream()
                    .map(pageListString -> parsePageList(pageLookup, pageListString))
                    .toList();

            pageRuleStrings.stream()
                    .map(string -> string.split("\\|"))
                    .forEach(strings -> pageLookup.get(strings[0]).shouldBeBefore.add(pageLookup.get(strings[1])));

            return pageLists;
        }

        private static PageList parsePageList(Map<String, Page> pageLookup, String pageListString) {
            List<Page> pages = Arrays.stream(pageListString.split(","))
                    .map(pageString -> Page.parsePage(pageLookup, pageString))
                    .toList();
            return new PageList(pages);
        }

        public boolean isCorrect() {
            return this.pages.stream()
                    .allMatch(page -> this.pages.stream()
                            .takeWhile(innerPage -> !Objects.equals(innerPage, page))
                            .noneMatch(page.shouldBeBefore::contains)
                    );
        }

        public int getMiddleValue() {
            return Integer.parseInt(this.pages.get((this.pages.size() - 1) / 2).value);
        }
    }

    private record Page(String value, Set<Page> shouldBeBefore) implements Comparable<Page> {
        private static Page parsePage(Map<String, Page> pageLookup, String pageString) {
            return pageLookup.computeIfAbsent(pageString, value -> new Page(value, new HashSet<>()));
        }

        @Override
        public int compareTo(Page o) {
            return this.shouldBeBefore.contains(o) ? -1 : (shouldBeBefore.contains(this) ? 1 : 0);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Page page = (Page) o;
            return Objects.equals(value, page.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(value);
        }
    }
}
