package edu.dartmouth.cs.politigram.models;

// Used to represent each entry within Boards RecyclerView.
// Each component has a header/title (bold), data, timestamp (bold), and email (hashed or unhashed depending on user's privacy setting during upload).
public class ClassifierHistoryComponent {

    String imageBytes;
    String label;
    String score;

    public ClassifierHistoryComponent(String imageBytes, String label, String score) {
        this.imageBytes = imageBytes;
        this.label = label;
        this.score = score;
    }

    public String getImageBytes() {
        return imageBytes;
    }

    public void setImageBytes(String imageBytes) {
        this.imageBytes = imageBytes;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getScore() {
        return score;
    }

    public void setScore(String score) {
        this.score = score;
    }

}
