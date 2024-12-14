package aocj2024;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Day14 extends Day {
    @Override
    public void part1() {
        List<String> lines = getLinesFromFile("day14.txt").toList();

        Area area = Area.parse(lines, 101, 103).getPredictedArea(100);

        System.out.println(area.getSafetyFactor());
    }

    @Override
    public void part2() {
        List<String> lines = getLinesFromFile("day14.txt").toList();

        Area area = Area.parse(lines, 101, 103);

        double lowestVariance = Double.MAX_VALUE;
        int lowestVarianceTimestamp = 0;

        Area currentArea = area.getPredictedArea(1);
        int seconds = 1;

        while (!currentArea.equals(area)) {
            double variance = currentArea.variance();

            if (variance < lowestVariance)  {
                lowestVariance = variance;
                lowestVarianceTimestamp = seconds;
            }

            currentArea = currentArea.getPredictedArea(1);
            seconds++;
        }

        System.out.println(lowestVarianceTimestamp);
    }

    private record Area(List<SecurityRobot> robots, int width, int height) {
        private Area getPredictedArea(int seconds) {
            return new Area(getPredictedRobots(seconds), width, height);
        }

        private List<SecurityRobot> getPredictedRobots(int seconds) {
            return robots.stream()
                    .map(robot -> robot.predict(seconds, width, height))
                    .toList();
        }

        private double variance() {
            double averageX = robots.stream().map(SecurityRobot::location).mapToInt(point -> point.x).average().orElse(0);
            double averageY = robots.stream().map(SecurityRobot::location).mapToInt(point -> point.y).average().orElse(0);

            double variance = 0;

            for (SecurityRobot robot : robots) {
                double dx = robot.location.x - averageX;
                double dy = robot.location.y - averageY;

                variance += dx * dx + dy * dy;
            }

            return variance / (robots.size() - 1);
        }

        private List<List<SecurityRobot>> getQuadrants() {
            List<List<SecurityRobot>> quadrants = new ArrayList<>(4);

            for (int i = 0; i < 4; i++) {
                quadrants.add(new ArrayList<>());
            }

            robots.forEach(robot -> {
                if (robot.location.x != width / 2 && robot.location.y != height / 2) {
                    quadrants.get(((robot.location.x - 1) / (width / 2)) * 2 + ((robot.location.y - 1) / (height / 2))).add(robot);
                }
            });

            return quadrants;
        }

        private int getSafetyFactor() {
            List<List<SecurityRobot>> quadrants = getQuadrants();

            return quadrants.stream()
                    .map(List::size)
                    .reduce(1, (left, right) -> left * right);
        }

        private static Area parse(List<String> lines, int width, int height) {
            List<SecurityRobot> robots = SecurityRobot.parseRobots(lines);

            return new Area(robots, width, height);
        }
    }

    private record SecurityRobot(Point location, Point velocity) {
        private static final Pattern robotPattern = Pattern.compile("p=(-?\\d+),(-?\\d+) v=(-?\\d+),(-?\\d+)");

        private SecurityRobot predict(int seconds, int width, int height) {
            int newX = Math.floorMod(location.x + velocity.x * seconds, width);
            int newY = Math.floorMod(location.y + velocity.y * seconds, height);

            return new SecurityRobot(new Point(newX, newY), velocity.getLocation());
        }

        private static List<SecurityRobot> parseRobots(List<String> lines) {
            return lines.stream()
                    .map(SecurityRobot::parse)
                    .toList();
        }

        private static SecurityRobot parse(String line) {
            Matcher matcher = robotPattern.matcher(line);

            boolean found = matcher.find();

            assert found;

            int px = Integer.parseInt(matcher.group(1)), py = Integer.parseInt(matcher.group(2));
            int vx = Integer.parseInt(matcher.group(3)), vy = Integer.parseInt(matcher.group(4));

            return new SecurityRobot(new Point(px, py), new Point(vx, vy));
        }
    }
}
