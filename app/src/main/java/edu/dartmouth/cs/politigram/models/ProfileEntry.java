package edu.dartmouth.cs.politigram.models;

public class ProfileEntry {

    private String mProfilePicture;
    private String mUsername;
    private Integer mSliderPosition;

    public ProfileEntry(String mProfilePicture, String mUsername, Integer mSliderPosition) {
        this.mProfilePicture = mProfilePicture;
        this.mUsername = mUsername;
        this.mSliderPosition = mSliderPosition;
    }

    // Additional getters and setters to be used in later MyRuns when edit profile feature is added

    public String getProfilePicture() {
        return mProfilePicture;
    }

    public void getProfilePicture(String mProfilePicture) {
        this.mProfilePicture = mProfilePicture;
    }

    public String getUsername() {
        return mUsername;
    }

    public void setUsername(String mUsername) {
        this.mUsername = mUsername;
    }

    public Integer getSliderPosition() {
        return mSliderPosition;
    }

    public void setSliderPosition(Integer mSliderPosition) {
        this.mSliderPosition = mSliderPosition;
    }
}
