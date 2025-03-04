package flat.uxml.value;

import flat.uxml.UXTheme;

import java.util.Objects;

public class UXValueLocale extends UXValue {
    private final String text;

    public UXValueLocale(String text) {
        this.text = text;
    }

    @Override
    public String asString(UXTheme theme) {
        return theme == null ? text : theme.getText(text);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UXValueLocale that = (UXValueLocale) o;
        return Objects.equals(text, that.text);
    }

    @Override
    public int hashCode() {
        return Objects.hash(text);
    }

    @Override
    public String toString() {
        return "Locale : " + text;
    }
}
