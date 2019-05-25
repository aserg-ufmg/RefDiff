package refdiff.core.cst;

import java.util.Objects;

public class Location {
	private String file;
	private int begin;
	private int end;
	private int line;
	private int bodyBegin;
	private int bodyEnd;
	
	public Location() {}
	
	public Location(String file, int begin, int end, int line, int bodyBegin, int bodyEnd) {
		this.file = file;
		this.begin = begin;
		this.end = end;
		this.line = line;
		this.bodyBegin = bodyBegin;
		this.bodyEnd = bodyEnd;
	}
	
	public Location(String file, int begin, int end, int line) {
		this(file, begin, end, line, begin, end);
	}
	
	public static Location of(String file, int begin, int end, int bodyBegin, int bodyEnd, CharSequence fileContent) {
		int line = findLineNumber(begin, fileContent);
		return new Location(file, begin, end, line, bodyBegin, bodyEnd);
	}

	public static int findLineNumber(int begin, CharSequence fileContent) {
		int count = 0;
		for (int i = 0; i < begin; i++) {
			if (fileContent.charAt(i) == '\n') {
				count++;
			}
		}
		return count + 1;
	}
	
	public String getFile() {
		return file;
	}

	public void setFile(String file) {
		this.file = file;
	}

	public int getBegin() {
		return begin;
	}

	public void setBegin(int begin) {
		this.begin = begin;
	}

	public int getEnd() {
		return end;
	}

	public void setEnd(int end) {
		this.end = end;
	}

	public int getBodyBegin() {
		return bodyBegin;
	}

	public void setBodyBegin(int bodyBegin) {
		this.bodyBegin = bodyBegin;
	}

	public int getBodyEnd() {
		return bodyEnd;
	}

	public void setBodyEnd(int bodyEnd) {
		this.bodyEnd = bodyEnd;
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

	public String format() {
		return String.format("%s:%d", file, line);
	}

	public int getLine() {
		return line;
	}

	public void setLine(int line) {
		this.line = line;
	}
	
}
