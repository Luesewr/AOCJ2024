package aocj2024;

import static java.util.Collections.swap;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Day9 extends Day {
    @Override
    public void part1() {
        Stream<String> lines = getLinesFromFile("day9.txt");
        FragmentFileSystem fragmentFileSystem = FragmentFileSystem.parse(lines);

        fragmentFileSystem.fileRearrangement();
        long checksum = fragmentFileSystem.checksum();

        System.out.println(checksum);
    }

    @Override
    public void part2() {
        Stream<String> lines = getLinesFromFile("day9.txt");
        ChunkFileSystem chunkFileSystem = ChunkFileSystem.parse(lines);

        chunkFileSystem.defragment();
        long checksum = chunkFileSystem.checksum();

        System.out.println(checksum);
    }

    private record FileFragment(Integer fileID) {
    }

    private record FileChunk(int fileID, int startIndex, int size) { }

    private record FragmentFileSystem(List<FileFragment> fragments) {
        private void fileRearrangement() {
            int startIndex = 0;
            int endIndex = fragments().size() - 1;

            while (startIndex < endIndex) {
                if ((fragments.get(startIndex).fileID != null) || (fragments.get(endIndex).fileID == null)) {
                    while (fragments.get(startIndex).fileID != null) startIndex++;
                    while (fragments.get(endIndex).fileID == null) endIndex--;
                } else {
                    swap(fragments, startIndex, endIndex);
                    startIndex++;
                    endIndex--;
                }
            }
        }

        private long checksum() {
            long checksum = 0;

            for (long i = 0, fragmentsSize = fragments.size(); i < fragmentsSize; i++) {
                FileFragment fileFragment = fragments.get((int) i);

                if (fileFragment.fileID != null) {
                    checksum += i * fileFragment.fileID;
                }
            }

            return checksum;
        }

        public static FragmentFileSystem parse(Stream<String> lines) {
            String line = lines.collect(Collectors.joining());

            List<FileFragment> fileFragments = IntStream.range(0, line.length())
                    .mapToObj(n -> IntStream.range(0, Integer.parseInt(String.valueOf(line.charAt(n))))
                            .mapToObj(value -> n % 2 == 0 ? new FileFragment(n / 2) : new FileFragment(null))
                    )
                    .flatMap(Function.identity())
                    .collect(Collectors.toList());

            return new FragmentFileSystem(fileFragments);
        }
    }

    private record ChunkFileSystem(List<FileChunk> chunks) {
        private void defragment() {
            for (int i = chunks.size() - 1; i >= 0; i--) {
                FileChunk chunkToMove = chunks.get(i);

                for (int j = 0; j <= i - 1; j++) {
                    FileChunk chunkCandidate1 = chunks.get(j);
                    FileChunk chunkCandidate2 = chunks.get(j + 1);

                    int gapSize = chunkCandidate2.startIndex - (chunkCandidate1.startIndex + chunkCandidate1.size);

                    if (chunkToMove.size <= gapSize) {
                        FileChunk removedChunk = chunks.remove(i);
                        int startIndex = chunkCandidate1.startIndex + chunkCandidate1.size;
                        FileChunk newChunk = new FileChunk(removedChunk.fileID, startIndex, removedChunk.size);

                        chunks.add(j + 1, newChunk);
                        i++;
                        break;
                    }
                }
            }

            chunks.sort(Comparator.comparingInt(FileChunk::startIndex));
        }

        private long checksum() {
            long checksum = 0;

            for (FileChunk fileChunk : chunks) {
                for (long j = 0; j < fileChunk.size; j++) {
                    checksum += (fileChunk.startIndex + j) * fileChunk.fileID;
                }
            }

            return checksum;
        }

        private static ChunkFileSystem parse(Stream<String> lines) {
            String line = lines.collect(Collectors.joining());

            List<FileChunk> fileChunks = new ArrayList<>();

            int startIndex = 0;

            for (int i = 0; i < line.length(); i++) {
                int size = Integer.parseInt(String.valueOf(line.charAt(i)));

                if (i % 2 == 0) {
                    FileChunk fileChunk = new FileChunk(i / 2, startIndex, size);
                    fileChunks.add(fileChunk);
                }

                startIndex += size;
            }

            return new ChunkFileSystem(fileChunks);
        }
    }
}
