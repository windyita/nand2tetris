/**
 * Created by yangyan on 11/26/14.
 */

import java.util.*;



public class SymbolTable {

    private HashMap<String, SymbolProperties> class_symbols;
    private HashMap<String, SymbolProperties> subroutine_symbols;
    private int argIndex = 0;
    private int varIndex = 0;
    private int fieldIndex = 0;
    private int staticIndex = 0;

    public SymbolTable() {
        class_symbols = new HashMap<String, SymbolProperties>();
        subroutine_symbols = new HashMap<String, SymbolProperties>();
    }

    public void startSubroutine() {
        subroutine_symbols.clear();
        argIndex = 0;
        varIndex = 0;
    }

    public void Define(String name, String type, Kind kind) {
        switch (kind) {
            case STATIC:
                class_symbols.put(name, new SymbolProperties(type, kind, staticIndex) );
                staticIndex++;
                break;
            case FIELD:
                class_symbols.put(name, new SymbolProperties(type, kind, fieldIndex) );
                fieldIndex++;
                break;
            case ARG:
                subroutine_symbols.put(name, new SymbolProperties(type, kind, argIndex) );
                argIndex++;
                break;
            case VAR:
                subroutine_symbols.put(name, new SymbolProperties(type, kind, varIndex) );
                varIndex++;
                break;
            default:
                break;
        }
    }

    public int VarCount(Kind kind) {
        int cnt = 0;
        switch (kind) {
            case STATIC:
                cnt = staticIndex;
                break;
            case FIELD:
                cnt = fieldIndex;
                break;
            case ARG:
                cnt = argIndex;
                break;
            case VAR:
                cnt = varIndex;
                break;
            default:
                break;
        }
        return cnt;
    }

    public Kind KindOf(String name) {
        SymbolProperties symbolProperties;

        if (class_symbols.containsKey(name)) {
            symbolProperties = class_symbols.get(name);
            return symbolProperties.getKind();
        }
        else if (subroutine_symbols.containsKey(name)) {
            symbolProperties = subroutine_symbols.get(name);
            return symbolProperties.getKind();
        }
        else {
            return Kind.NONE;
        }
    }

    public Segment SegmentOf(String name) {
        Segment varSegment = Segment.LOCAL;

        switch (this.KindOf(name)) {
            case STATIC:
                varSegment = Segment.STATIC;
                break;
            case FIELD:
                varSegment = Segment.THIS;
                break;
            case ARG:
                varSegment = Segment.ARGUMENT;
                break;
            case VAR:
                varSegment = Segment.LOCAL;
                break;
            default:
                break;
        }
        return varSegment;
    }

    public String TypeOf(String name) {

        SymbolProperties symbolProperties;

        if (class_symbols.containsKey(name)) {
            symbolProperties = class_symbols.get(name);
            return symbolProperties.getType();
        }
        else if (subroutine_symbols.containsKey(name)) {
            symbolProperties = subroutine_symbols.get(name);
            return symbolProperties.getType();
        }
        else {
            return "";
        }
    }

    public int IndexOf(String name) {
        SymbolProperties symbolProperties;

        if (class_symbols.containsKey(name)) {
            symbolProperties = class_symbols.get(name);
            return symbolProperties.getIndex();
        }
        else if (subroutine_symbols.containsKey(name)) {
            symbolProperties = subroutine_symbols.get(name);
            return symbolProperties.getIndex();
        }
        else {
            return -1;
        }
    }

    class SymbolProperties {
        SymbolProperties(String type, Kind kind, int index) {
            this.type = type;
            this.kind = kind;
            this.index = index;
        }

        private String type;
        private Kind kind;
        private int index;

        public String getType() {
            return type;
        }

        public Kind getKind() {
            return kind;
        }

        public int getIndex() {
            return index;
        }
    }
}