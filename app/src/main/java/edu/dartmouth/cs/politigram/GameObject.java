package edu.dartmouth.cs.politigram;

public class GameObject {

    String score;
    String dateTime;

    public GameObject(String score, String dateTime){
        this.score = score;
        this.dateTime = dateTime;
    }

    public String getScore() {
        return score;
    }

    public void setScore(String score) {
        this.score = score;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }
}
