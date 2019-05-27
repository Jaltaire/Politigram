package edu.dartmouth.cs.politigram;

import java.util.Comparator;

public class BoardObject {

    String user;
    String score;
    String dateTime;

    public BoardObject(String user, String score, String dateTime) {
        this.user = user;
        this.score = score;
        this.dateTime = dateTime;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
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

    public int compareTo(BoardObject compareObject) {

        int compareScore = Integer.parseInt(((BoardObject) compareObject).getScore());
        return compareScore - Integer.parseInt(this.score);

    }

    public static Comparator<BoardObject> BoardObjectComparator
            = new Comparator<BoardObject>() {

        public int compare(BoardObject boardObject1, BoardObject boardObject2) {

            Integer score1 = Integer.parseInt(boardObject1.getScore());
            Integer score2 = Integer.parseInt(boardObject2.getScore());
            return score2.compareTo(score1);
        }

    };
}
