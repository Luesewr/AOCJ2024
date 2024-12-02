package aocj2024;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;

public abstract class Day {
    public abstract void part1();

    public abstract void part2();

    public Stream<String> getLinesFromFile(String filename) {
        InputStream resource = getClass().getResourceAsStream("/" + filename);

        assert resource != null;

        Reader reader = new InputStreamReader(resource, StandardCharsets.UTF_8);
        BufferedReader bufferedReader = new BufferedReader(reader);
        return bufferedReader.lines().onClose(() -> {
            try {
                bufferedReader.close();
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        });
    }
}
