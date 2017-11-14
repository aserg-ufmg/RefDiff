package refdiff.core.rast;

import java.util.Objects;

public class Location {
	private String file;
	private int begin;
	private int end;
	
	public Location() {}
	
	public Location(String file, int begin, int end) {
		this.file = file;
		this.begin = begin;
		this.end = end;
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
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Location) {
			Location otherLocation = (Location) obj;
			return Objects.equals(this.file, otherLocation.file) &&
				Objects.equals(this.begin, otherLocation.begin) &&
				Objects.equals(this.end, otherLocation.end);
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(this.file, this.begin, this.end);
	}
	
	@Override
	public String toString() {
		return String.format("%s:%d:%d", file, begin, end);
	}
}
