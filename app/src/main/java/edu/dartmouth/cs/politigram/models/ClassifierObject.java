package edu.dartmouth.cs.politigram.models;

public class ClassifierObject {
    String image;

    public ClassifierObject(String image, String result) {
        this.image = image;
        this.result = result;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    String result;

}
