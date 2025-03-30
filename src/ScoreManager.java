package src;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
public class ScoreManager {
  private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd:MM:yyyy HH:mm");
  public List<ScoreEntry> scores = new ArrayList<>();

  public void addScore(int score) {
    LocalDateTime now = LocalDateTime.now();
    String timestamp = now.format(formatter); // Formatowanie bieżącego czasu do odpowiedniego formatu
    scores.add(new ScoreEntry(score, timestamp));
    Collections.sort(scores);
  }

  public void saveScoresToFile(String filename) {
    try (FileWriter writer = new FileWriter(filename)) {
      for (ScoreEntry entry : scores) {
        writer.write(entry.score() + "," + entry.timestamp() + "\n");
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void loadScoresFromFile(String filename) {
    scores.clear(); // Wyczyść listę przed wczytaniem nowych wyników
    try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
      String line;
      while ((line = reader.readLine()) != null) {
        String[] parts = line.split(",");
        if (parts.length == 2) {
          int score = Integer.parseInt(parts[0]);
          String timestamp = parts[1];
          scores.add(new ScoreEntry(score, timestamp));
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
