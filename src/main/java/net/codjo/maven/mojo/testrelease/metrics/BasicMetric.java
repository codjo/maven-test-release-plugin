package net.codjo.maven.mojo.testrelease.metrics;
/**
 *
 */
class BasicMetric {
    private String usedMemory;
    private long timeElapsed;
    private int testCount;


    public String getUsedMemory() {
        return usedMemory;
    }


    public void setUsedMemory(String usedMemory) {
        this.usedMemory = usedMemory;
    }


    public long getTimeElapsed() {
        return timeElapsed;
    }


    public void setTimeElapsed(long timeElapsed) {
        this.timeElapsed = timeElapsed;
    }


    public int getTestCount() {
        return testCount;
    }


    public void setTestCount(int testCount) {
        this.testCount = testCount;
    }
}
