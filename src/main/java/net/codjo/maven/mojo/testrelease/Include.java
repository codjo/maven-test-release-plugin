package net.codjo.maven.mojo.testrelease;
public class Include {

    private String file;
    private String output;


    public String getFile() {
        return file;
    }


    public void setFile(String file) {
        this.file = file;
    }


    public String getOutput() {
        return output;
    }


    public void setOutput(String output) {
        this.output = output;
    }


    public void resolveOutput(String defaultOutput) {
        if (output == null) {
            output = defaultOutput;
        }
    }
}