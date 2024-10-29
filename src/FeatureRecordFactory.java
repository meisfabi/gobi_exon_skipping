import Model.FeatureRecord;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class FeatureRecordFactory {
    private static final Queue<FeatureRecord> pool = new ConcurrentLinkedQueue<>();

    public static FeatureRecord borrowFeatureRecord() {
        FeatureRecord featureRecord = pool.poll();
        if (featureRecord == null) {
            featureRecord = new FeatureRecord();
        }
        return featureRecord;
    }

    public static void returnFeatureRecord(FeatureRecord featureRecord) {
        featureRecord.clear();
        pool.offer(featureRecord);
    }
}
