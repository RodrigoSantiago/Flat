package flat.uxml;

import flat.exception.FlatException;
import flat.resources.ResourceStream;
import flat.uxml.sheet.UXSheetAttribute;
import flat.uxml.sheet.UXSheetParser;
import flat.uxml.sheet.UXSheetStyle;
import flat.uxml.value.*;
import flat.widget.State;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class UXSheet {

    private final HashMap<String, UXStyle> styles = new HashMap<>();
    private final HashMap<String, UXValue> variableInitialValue = new HashMap<>();
    private final List<UXSheetParser.ErroLog> logs = new ArrayList<>();

    public static UXSheet parse(ResourceStream stream) {
        ArrayList<String> includes = new ArrayList<>();
        return parseInclude(stream, includes);
    }

    private static UXSheet parseInclude(ResourceStream stream, ArrayList<String> includes) {
        Object cache = stream.getCache();
        if (cache != null) {
            if (cache instanceof Exception) {
                return null;
            } else if (cache instanceof UXSheet) {
                return (UXSheet) cache;
            } else {
                stream.clearCache();
            }
        }
        try {
            UXSheet sheet = read(stream, includes);
            if (sheet != null) {
                stream.putCache(sheet);
            }
            return sheet;
        } catch (Exception e) {
            stream.putCache(e);
            throw new FlatException(e);
        }
    }

    private static UXSheet read(ResourceStream stream, ArrayList<String> includes) throws IOException {
        UXSheet sheet = new UXSheet();

        readRecursive(sheet, stream, includes);

        for (var style : sheet.styles.values()) {
            String parentName = style.getParentName();
            if (parentName != null) {
                var parent = sheet.styles.get(parentName);
                if (parent == null) {
                    sheet.logs.add(new UXSheetParser.ErroLog(-1, -1
                            , UXSheetParser.ErroLog.PARENT_NOT_FOUND + " '" + style.getName() + " : " + parentName + "'"));
                } else if (!style.setParent(parent)) {
                    sheet.logs.add(new UXSheetParser.ErroLog(-1, -1
                            , UXSheetParser.ErroLog.CYCLIC_PARENT + " '" + style.getName() + " : " + parentName + "'"));
                }
            }
        }

        return sheet;
    }

    private static void readRecursive(UXSheet sheet, ResourceStream stream, ArrayList<String> includes) throws IOException {
        if (stream.isFolder()) {
            for (var st : stream.getFiles()) {
                readRecursive(sheet, st, includes);
            }
        } else {
            byte[] data = stream.readData();
            if (data == null) {
                throw new FlatException("Invalid file " + stream.getResourceName());
            }

            String uxss = new String(stream.readData(), StandardCharsets.UTF_8);
            UXSheetParser reader = new UXSheetParser(uxss);
            reader.parse();

            // Logs
            sheet.logs.addAll(reader.getLogs());

            // Styles
            for (UXSheetStyle sheetStyle : reader.getStyles()) {
                UXStyle style = new UXStyle(sheetStyle.getName(), sheetStyle.getParent());
                for (var attr : sheetStyle.getAttributes().values()) {
                    style.add(UXHash.getHash(attr.getName()), State.ENABLED, attr.getValue());
                }
                for (var pseudo : sheetStyle.getStates().values()) {
                    State state = State.valueOf(pseudo.getName().toUpperCase());
                    for (var attr : pseudo.getAttributes().values()) {
                        style.add(UXHash.getHash(attr.getName()), state, attr.getValue());
                    }
                }
                if (sheet.styles.containsKey(style.getName())) {
                    sheet.logs.add(new UXSheetParser.ErroLog(-1, -1
                            , UXSheetParser.ErroLog.REPEATED_STYLE + " '" + style.getName() + "'"));
                }
                sheet.styles.put(style.getName(), style);
            }

            // Variables
            for (UXSheetAttribute variable : reader.getVariables()) {
                if (sheet.variableInitialValue.containsKey(variable.getName())) {
                    sheet.logs.add(new UXSheetParser.ErroLog(-1, -1
                            , UXSheetParser.ErroLog.REPEATED_VARIABLE + " '" + variable.getName() + "'"));
                }
                sheet.variableInitialValue.put(variable.getName(), variable.getValue());
            }

            // Include
            for (UXSheetAttribute include : reader.getIncludes()) {
                UXValue value = include.getValue();
                ResourceStream relative = stream.getRelative(value.asString(null));
                if (includes.contains(relative.getResourceName())) {
                    sheet.logs.add(new UXSheetParser.ErroLog(-1, -1
                            , UXSheetParser.ErroLog.CYCLIC_INCLUDE + " '" + value.asString(null) + "'"));
                } else {
                    UXSheet includeSheet = parseInclude(relative, includes);

                    for (var set : includeSheet.styles.entrySet()) {
                        if (!sheet.styles.containsKey(set.getKey())) {
                            sheet.styles.put(set.getKey(), set.getValue());
                        }
                    }

                    for (var set : includeSheet.variableInitialValue.entrySet()) {
                        if (!sheet.variableInitialValue.containsKey(set.getKey())) {
                            sheet.variableInitialValue.put(set.getKey(), set.getValue());
                        }
                    }
                }
            }
        }
    }

    public UXTheme instance(float fontScale, UXStringBundle stringBundle, HashMap<String, UXValue> variables) {
        return new UXTheme(this, fontScale, stringBundle, variables);
    }

    public UXTheme instance() {
        return new UXTheme(this, 1f, null, null);
    }

    public UXStyle getStyle(String name) {
        return styles.get(name);
    }

    public UXValue getVariableInitialValue(String name) {
        return variableInitialValue.get(name);
    }

    public List<UXSheetParser.ErroLog> getLogs() {
        return logs;
    }
}