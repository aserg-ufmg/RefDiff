package refdiff.core.rast;

public class Location {
    private String file;
    private int begin;
    private int end;

    public Location() {
    }

    public Location(String file, int begin, int end) {
        this.file = file;
        this.begin = begin;
        this.end = end;
    }

    @Override
    public String toString() {
        return String.format("%s:%d:%d", file, begin, end);
    }

    public String getFile() {
        return file;
    }

    public int getBegin() {
        return begin;
    }

    public int getEnd() {
        return end;
    }

}
