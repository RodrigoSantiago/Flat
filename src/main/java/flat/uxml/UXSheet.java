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
    private List<UXSheetParser.ErroLog> logs;
    private final List<UXSheet> imports = new ArrayList<>();

    public static UXSheet parse(ResourceStream stream) {
        Object cache = stream.getCache();
        if (cache != null) {
            if (cache instanceof UXSheet) {
                return (UXSheet) cache;
            } else {
                throw new FlatException("Invalid UXSheet at: " + stream.getStream());
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
        String data = new String(stream.getStream().readAllBytes(), StandardCharsets.UTF_8);

        UXSheetParser reader = new UXSheetParser(data);
        reader.parse();

        UXSheet sheet = new UXSheet();
        sheet.logs = reader.getLogs();

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
            sheet.styles.put(style.name, style);
        }

        for (var style : sheet.styles.values()) {
            if (style.parentName != null) {
                var parent = sheet.styles.get(style.parentName);
                if (!style.setParent(parent)) {
                    sheet.logs.add(new UXSheetParser.ErroLog(-1, -1, UXSheetParser.ErroLog.CYCLIC_PARENT));
                }
            }
        }

        for (UXSheetAttribute variable : reader.getVariables().values()) {
            sheet.variableInitialValue.put(variable.getName(), variable.getValue());
        }

        return sheet;
    }

    public UXTheme instance() {
        return new UXTheme(this, 160f, 1f, null);
    }

    public UXStyle getStyle(String name) {
        return styles.get(name);
    }
}