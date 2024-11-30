package aocj2024;

import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Scanner;

public abstract class Day {
    public abstract void part1();

    public abstract void part2();

    public Scanner getScanner(String filename) {
        try {
            URL resource = getClass().getResource("/" + filename);
            return new Scanner(Paths.get(resource.toURI()).toFile());
        } catch (FileNotFoundException | URISyntaxException e) {
            return null;
        }
    }
}
