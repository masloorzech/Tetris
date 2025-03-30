package src;

public class ScoreEntry implements Comparable<ScoreEntry> {

  private final int score;
  private final String timestamp;

  public ScoreEntry(int score, String timestamp){
    this.score = score;
    this.timestamp = timestamp;
  }

  public int score(){
    return score;
  }
  public String timestamp(){
    return timestamp;
  }

  @Override
  public int compareTo(ScoreEntry other) {
    return Integer.compare(other.score, this.score);
  }

  @Override
  public String toString() {
    return timestamp + " " + score;
  }
}
