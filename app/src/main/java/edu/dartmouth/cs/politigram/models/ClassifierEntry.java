package edu.dartmouth.cs.politigram.models;

public class ClassifierEntry {

    String imageBytes;
    String label;
    String score;

    public ClassifierEntry(String imageBytes, String label, String score) {
        this.imageBytes = imageBytes;
        this.label = label;
        this.score = score;
    }

}
