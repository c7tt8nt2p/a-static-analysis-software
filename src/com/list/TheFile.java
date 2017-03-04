package com.list;

import javafx.scene.control.Label;

public class TheFile {
    private String fileName;
    private String fileStatus;
    private String analysis;
    private String filePath;

    public TheFile(String fileName, String fileStatus,String analysis, String filePath) {
        this.fileName = fileName;
        this.fileStatus = fileStatus;
        this.analysis = analysis;
        this.filePath = filePath;
    }

    public String getAnalysis() { return analysis; }

    public void setAnalysis(String analysis) { this.analysis = analysis; }

    public String getFileName() {
        return fileName;
    }

    public String getFileStatus() {
        return fileStatus;
    }

    public String getFilePath() { return filePath; }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TheFile theFile = (TheFile) o;

        return fileName.equals(theFile.fileName);
    }
}
