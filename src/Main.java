import java.io.*;
import java.nio.file.*;
import java.util.*;

public class Main {

    private static final int FILE_COUNT = 30;
    private static final long FILE_SIZE_MB = 100;
    private static final long BYTES_PER_MB = 1024 * 1024;
    private static final int BUFFER_SIZE = 8192;

    public static void main(String[] args) {
        generateTestFiles();
        
        String directoryPath = "src/test";
        String searchWord = "hi";

        int coreCount = Runtime.getRuntime().availableProcessors();
        Thread[] threads = new Thread[coreCount];

        List<String> fileList = new ArrayList<>();

        try {
            collectFiles(directoryPath, fileList);

            List<List<String>> filesPerThread = new ArrayList<>();

            for (int i = 0; i < coreCount; i++) {
                filesPerThread.add(new ArrayList<>());
            }

            for (int i = 0; i < fileList.size(); i++) {
                filesPerThread.get(i % coreCount).add(fileList.get(i));
            }

            for (int i = 0; i < coreCount; i++) {
                List<String> filesToProcess = filesPerThread.get(i);

                if (!filesToProcess.isEmpty()) {
                    threads[i] = new Thread(() -> {
                        for (String file : filesToProcess) {
                            searchInFile(file, searchWord);
                        }
                    });
                    threads[i].start();
                }
            }

            for (int i = 0; i < coreCount; i++) {
                if (threads[i] != null) {
                    try {
                        threads[i].join();
                    } catch (InterruptedException e) {
                        System.out.println("error: " + e.getMessage());
                    }
                }
            }

        } catch (Exception e) {
            System.out.println("error: " + e.getMessage());
        }
    }

    private static void searchInFile(String file, String searchWord) {
        try (BufferedReader reader = Files.newBufferedReader(Path.of(file))) {
            String line = reader.readLine();
            int lineNumber = 0;

            while (line != null) {
                lineNumber++;
                if (line.contains(searchWord)) {
                    System.out.println("find" + file + ":" + lineNumber + ": " + line);
                }
                line = reader.readLine();
            }
        } catch (IOException e) {
            System.out.println("error: " + e.getMessage());
        }
    }

    private static void collectFiles(String directoryPath, List<String> fileList) {
        File[] files = new File(directoryPath).listFiles();

        if (files == null) {
            return;
        }

        for (File f : files) {
            if (f.isDirectory()) {
                collectFiles(f.getAbsolutePath(), fileList);
            } else {
                fileList.add(f.getAbsolutePath());
            }
        }
    }

    private static void generateTestFiles() {
        String testDirPath = "src/test";

        try {
            Path testDir = Paths.get(testDirPath);
            if (!Files.exists(testDir)) {
                Files.createDirectories(testDir);
                System.out.println("Created test directory: " + testDirPath);
            }

            Random random = new Random();

            System.out.println("Generating " + FILE_COUNT + " random text files (10MB each)...");

            for (int i = 1; i <= FILE_COUNT; i++) {
                String fileName = testDirPath + "/random_file_" + i + ".txt";
                generateRandomFile(fileName, FILE_SIZE_MB * BYTES_PER_MB, random);
                System.out.println("Generated file " + i + " of " + FILE_COUNT + ": " + fileName);
            }

            System.out.println("Successfully generated all test files.");

        } catch (Exception e) {
            System.err.println("Error generating test files: " + e.getMessage());
        }
    }

    private static void generateRandomFile(String fileName, long fileSize, Random random) throws IOException {
        try (BufferedWriter


                     writer = new BufferedWriter(new FileWriter(fileName), BUFFER_SIZE)) {
            char[] buffer = new char[BUFFER_SIZE];
            long bytesWritten = 0;
            int charCount = 0;

            while (bytesWritten < fileSize) {
                for (int i = 0; i < buffer.length && bytesWritten < fileSize; i++) {
                    buffer[i] = (char) (random.nextInt(95) + 32);
                    charCount++;

                    if (charCount > 0 && charCount % 100 == 0) {
                        writer.write(buffer, 0, i + 1);
                        writer.newLine();
                        bytesWritten += (i + 1) + System.lineSeparator().length();
                        charCount = 0;
                        break;
                    }

                    if (i == buffer.length - 1 || bytesWritten + i + 1 >= fileSize) {
                        int charsToWrite = i + 1;
                        writer.write(buffer, 0, charsToWrite);
                        bytesWritten += charsToWrite;
                        break;
                    }
                }

                if (random.nextInt(20) == 0) {
                    writer.newLine();
                    bytesWritten += System.lineSeparator().length();
                    charCount = 0;
                }
            }
        }
    }

}