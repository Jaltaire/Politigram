package edu.dartmouth.cs.politigram.utils;

public class PoliticalLeaningConversion {

    // Handle text for political leaning relative to SeekBar position.
    public static String handlePoliticalLeaningValue(int leaning) {

        String leaningLabel;

        if (leaning >= 0 && leaning < 14) leaningLabel = "FAR LEFT";
        else if (leaning >= 14 && leaning < 28) leaningLabel = "LEFT";
        else if (leaning >= 28 && leaning < 42) leaningLabel = "CENTER-LEFT";
        else if (leaning >= 42 && leaning <= 58) leaningLabel = "CENTER";
        else if (leaning > 58 && leaning <= 72) leaningLabel = "CENTER-RIGHT";
        else if (leaning > 72 && leaning <= 86) leaningLabel = "RIGHT";
        else leaningLabel = "FAR RIGHT";

        return leaningLabel;

    }

}
