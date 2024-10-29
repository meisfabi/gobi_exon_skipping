package Model;

public class Transcript {

    private String transcriptId;
    private String transcriptName;
    private String transcriptSource;
    private TranscriptEntry transcriptEntry;

    public TranscriptEntry getTranscriptEntry() {
        if(transcriptEntry == null)
            transcriptEntry = new TranscriptEntry();
        return transcriptEntry;
    }

    public void setTranscriptSource(String transcriptSource) {
        this.transcriptSource = transcriptSource;
    }

    public String getTranscriptSource() {
        return transcriptSource;
    }

    public void setTranscriptName(String transcriptName) {
        this.transcriptName = transcriptName;
    }

    public String getTranscriptName() {
        return transcriptName;
    }

    public String getTranscriptId() {
        return transcriptId;
    }

    public void setTranscriptId(String transcriptId) {
        this.transcriptId = transcriptId;
    }
}
