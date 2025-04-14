package com.lucene.model;

import com.lucene.util.MathUtil;

public class WatchResult {

    private String fileName;
    private String filePath;
    private float score;
    private String content;
    private String eventType;

    public WatchResult() {
        this.fileName = "";
        this.filePath = "";
        this.score = 0.0f;
        this.content = "";
        this.eventType = "";
    }

    public WatchResult(String fileName, String filePath, float score, String content, String eventType) {
        this.fileName = fileName;
        this.filePath = filePath;
        this.score = score;
        this.content = content;
        this.eventType = eventType;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public float getScore() {
        return score;
    }

    public void setScore(float score) {
        this.score = score;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String toString2() {
        return "* Score: " + MathUtil.foundUpToThousandth(score) + "       [" + fileName + "]       " + filePath + "\n" +
                "   " + content;
    }

    public String printChangeEvent() {
        return "[" + eventType + "]    " + fileName + "    ||   " + filePath + "\n";
    }

    public static class Builder {
        private String fileName = "";
        private String filePath = "";
        private float score = 0.0f;
        private String content = "";
        private String eventType = "";

        public Builder fileName(String fileName) {
            this.fileName = fileName;
            return this;
        }

        public Builder filePath(String filePath) {
            this.filePath = filePath;
            return this;
        }

        public Builder score(float score) {
            this.score = score;
            return this;
        }

        public Builder content(String content) {
            this.content = content;
            return this;
        }

        public Builder eventType(String eventType) {
            this.eventType = eventType;
            return this;
        }

        public WatchResult build() {
            return new WatchResult(fileName, filePath, score, content, eventType);
        }
    }
}
