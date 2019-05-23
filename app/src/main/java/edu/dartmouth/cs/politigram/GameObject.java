package edu.dartmouth.cs.politigram;

public class GameObject {

    int score;
    String dateTime;

    public GameObject(int score, String dateTime){
        this.score = score;
        this.dateTime = dateTime;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }
}
