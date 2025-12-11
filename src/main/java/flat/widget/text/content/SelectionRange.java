package flat.widget.text.content;

import java.util.Objects;

public class SelectionRange {
    private final int startLine;
    private final int startCharacter;
    private final int endLine;
    private final int endCharacter;
    
    public SelectionRange(int startLine, int startCharacter, int endLine, int endCharacter) {
        this.startLine = startLine;
        this.startCharacter = startCharacter;
        this.endLine = endLine;
        this.endCharacter = endCharacter;
    }
    
    public int getStartLine() {
        return startLine;
    }
    
    public int getStartCharacter() {
        return startCharacter;
    }
    
    public int getEndLine() {
        return endLine;
    }
    
    public int getEndCharacter() {
        return endCharacter;
    }
    
    public boolean isEmpty() {
        return startLine == endLine && startCharacter == endCharacter;
    }
    
    public boolean contains(int line, int character) {
        return (line > startLine || (line == startLine && character >= startCharacter)) &&
                    (line < endLine || (line == endLine && character < endCharacter));
    }
    
    @Override
    public boolean equals(Object object) {
        if (object == null || getClass() != object.getClass()) return false;
        SelectionRange that = (SelectionRange) object;
        return startLine == that.startLine && startCharacter == that.startCharacter &&
                       endLine == that.endLine && endCharacter == that.endCharacter;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(startLine, startCharacter, endLine, endCharacter);
    }
}
