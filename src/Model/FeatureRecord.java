package Model;

import augmentedTree.Interval;

public class FeatureRecord {

        // Fields
        private String feature;
        private String proteinId;
        private String tag;
        private String ccdsId;
        private int start;
        private int stop;
        private double score;
        private char strand;
        private int frame;
        private String exonNumber;


        // 3. feature
        public String getFeature() {
            return feature;
        }

        public void setFeature(String feature) {
            this.feature = feature;
        }

        // 4. start
        public int getStart() {
            return start;
        }

        public void setStart(int start) {
            this.start = start;
        }

        // 5. end
        public int getStop() {
            return stop;
        }

        public void setStop(int end) {
            this.stop = end;
        }

        // 6. score
        public double getScore() {
            return score;
        }

        public void setScore(double score) {
            this.score = score;
        }

        // 7. strand
        public char getStrand() {
            return strand;
        }

        public void setStrand(char strand) {
            if (strand != '-' && strand != '+' && strand != '.') {
                throw new IllegalArgumentException("Strand must be '+', '-', or '.'");
            }
            this.strand = strand;
        }

        // 8. frame
        public int getFrame() {
            return frame;
        }

        public void setFrame(int frame) {
            this.frame = frame;
        }

        // 9. protein_id
        public String getProteinId() {
            return proteinId;
        }

        public void setProteinId(String proteinId) {
            this.proteinId = proteinId;
        }


        // 12. exon_number

        public String getExonNumber() {
            return exonNumber;
        }

        public void setExonNumber(String exonNumber) {
            this.exonNumber = exonNumber;
        }

        // 14. ccds_id

        public String getCcdsId() {
            return ccdsId;
        }
        public void setCcdsId(String ccdsId) {
            this.ccdsId = ccdsId;
        }


        // 16. tag
        public String getTag() {
            return tag;
        }

        public void setTag(String tag) {
            this.tag = tag;
        }

        // 17. transcript_name

    }


