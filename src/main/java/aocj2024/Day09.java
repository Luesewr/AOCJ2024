package aocj2024;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Day09 extends Day {
    @Override
    public void part1() {
        Stream<String> lines = getLinesFromFile("day09.txt");
        FileSystem fileSystem = FileSystem.parse(lines, 1);

        fileSystem.defragment();
        long checksum = fileSystem.checksum();

        System.out.println(checksum);
    }

    @Override
    public void part2() {
        Stream<String> lines = getLinesFromFile("day09.txt");
        FileSystem fileSystem = FileSystem.parse(lines, Integer.MAX_VALUE);

        fileSystem.defragment();
        long checksum = fileSystem.checksum();

        System.out.println(checksum);
    }

    private record FileFragment(int fileID, int startIndex, int size) { }

    private record FileSystem(List<FileFragment> chunks) {
        private void defragment() {
            int minEmptyIndex = 0;

            for (int i = chunks.size() - 1; i >= 0; i--) {
                FileFragment chunkToMove = chunks.get(i);

                boolean reachedGap = false;

                for (int j = minEmptyIndex; j <= i - 1; j++) {
                    FileFragment chunkCandidate1 = chunks.get(j);
                    FileFragment chunkCandidate2 = chunks.get(j + 1);

                    int gapSize = chunkCandidate2.startIndex - (chunkCandidate1.startIndex + chunkCandidate1.size);

                    if (chunkToMove.size <= gapSize) {
                        FileFragment removedChunk = chunks.remove(i);
                        int startIndex = chunkCandidate1.startIndex + chunkCandidate1.size;
                        FileFragment newChunk = new FileFragment(removedChunk.fileID, startIndex, removedChunk.size);

                        chunks.add(j + 1, newChunk);
                        i++;
                        break;
                    }

                    if (!reachedGap && gapSize == 0) {
                        minEmptyIndex = Integer.max(minEmptyIndex, j + 1);
                    } else {
                        reachedGap = true;
                    }
                }
            }
        }

        private long checksum() {
            long checksum = 0;

            for (FileFragment fileFragment : chunks) {
                for (long j = 0; j < fileFragment.size; j++) {
                    checksum += (fileFragment.startIndex + j) * fileFragment.fileID;
                }
            }

            return checksum;
        }

        private static FileSystem parse(Stream<String> lines, int maxChunkSize) {
            String line = lines.collect(Collectors.joining());

            List<FileFragment> fileFragments = new ArrayList<>();

            int startIndex = 0;

            for (int i = 0; i < line.length(); i++) {
                int size = Integer.parseInt(String.valueOf(line.charAt(i)));

                if (i % 2 == 0) {
                    int chunkSize = Integer.min(size, maxChunkSize);

                    for (int j = 0; j < size; j += chunkSize) {
                        FileFragment fileFragment = new FileFragment(i / 2, startIndex + j, chunkSize);
                        fileFragments.add(fileFragment);
                    }
                }

                startIndex += size;
            }

            return new FileSystem(fileFragments);
        }
    }
}
