package refdiff.core.rast;

import java.util.Objects;

public class Location {
	private String file;
	private int begin;
	private int end;
	private int bodyBegin;
	private int bodyEnd;
	
	public Location() {}
	
	public Location(String file, int begin, int end, int bodyBegin, int bodyEnd) {
		this.file = file;
		this.begin = begin;
		this.end = end;
		this.bodyBegin = bodyBegin;
		this.bodyEnd = bodyEnd;
	}
	
	public Location(String file, int begin, int end) {
		this(file, begin, end, begin, end);
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
	
	public int getBodyBegin() {
		return bodyBegin;
	}

	public int getBodyEnd() {
		return bodyEnd;
	}	
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Location) {
			Location otherLocation = (Location) obj;
			return 
				Objects.equals(this.file, otherLocation.file) &&
				Objects.equals(this.begin, otherLocation.begin) &&
				Objects.equals(this.end, otherLocation.end) &&
				Objects.equals(this.bodyBegin, otherLocation.bodyBegin) &&
				Objects.equals(this.bodyEnd, otherLocation.bodyEnd);
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(this.file, this.begin, this.end, this.bodyBegin, this.bodyEnd);
	}
	
	@Override
	public String toString() {
		return String.format("%s:%d:%d:%d:%d", file, begin, end, bodyBegin, bodyEnd);
	}

}
