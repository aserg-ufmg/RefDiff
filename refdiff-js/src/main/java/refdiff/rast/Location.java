package refdiff.rast;

public class Location {
    public String file;
    public int begin;
    public int end;

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
}
