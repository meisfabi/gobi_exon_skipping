package Model;

import java.util.TreeMap;

public class TranscriptEntry {


    private final TreeMap<Integer, FeatureRecord> positions = new TreeMap<>();

    public void addRecord(int start, FeatureRecord record) {
        positions.put(start, record);
    }

    public TreeMap<Integer, FeatureRecord> getPositions() {
        return positions;
    }
}
