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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class UXSheet {

    private final HashMap<String, UXStyle> styles = new HashMap<>();
    private final HashMap<String, UXValue> variableInitialValue = new HashMap<>();
    private List<UXSheetParser.ErroLog> logs = new ArrayList<>();
    private final List<UXSheet> imports = new ArrayList<>();

    public static UXSheet parse(ResourceStream stream) {
        Object cache = stream.getCache();
        if (cache != null) {
            if (cache instanceof UXSheet) {
                return (UXSheet) cache;
            } else {
                throw new FlatException("Invalid UXSheet at: " + stream.getResourceName());
            }
        }
        try {
            UXSheet sheet = read(stream);
            stream.putCache(sheet);
            return sheet;
        } catch (IOException e) {
            throw new FlatException(e);
        }
    }

    private static UXSheet read(ResourceStream stream) throws IOException {
        UXSheet sheet = new UXSheet();

        readRecursive(sheet, stream);

        for (var style : sheet.styles.values()) {
            String parentName = style.getParentName();
            if (parentName != null) {
                var parent = sheet.styles.get(parentName);
                if (!style.setParent(parent)) {
                    sheet.logs.add(new UXSheetParser.ErroLog(-1, -1
                            , UXSheetParser.ErroLog.CYCLIC_PARENT + " '" + style.getName() + " : " + parentName + "'"));
                }
            }
        }

        return sheet;
    }

    private static void readRecursive(UXSheet sheet, ResourceStream stream) throws IOException {
        if (stream.isFolder()) {
            for (var st : stream.getFiles()) {
                readRecursive(sheet, st);
            }
        } else {
            String data = new String(stream.getStream().readAllBytes(), StandardCharsets.UTF_8);
            UXSheetParser reader = new UXSheetParser(data);
            reader.parse();

            // Logs
            sheet.logs.addAll(reader.getLogs());

            // Styles
            for (UXSheetStyle sheetStyle : reader.getStyles().values()) {
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
            for (UXSheetAttribute variable : reader.getVariables().values()) {
                if (sheet.variableInitialValue.containsKey(variable.getName())) {
                    sheet.logs.add(new UXSheetParser.ErroLog(-1, -1
                            , UXSheetParser.ErroLog.REPEATED_VARIABLE + " '" + variable.getName() + "'"));
                }
                sheet.variableInitialValue.put(variable.getName(), variable.getValue());
            }
        }
    }

    public UXTheme instance() {
        return new UXTheme(this, 1f, null);
    }

    public UXStyle getStyle(String name) {
        return styles.get(name);
    }
}