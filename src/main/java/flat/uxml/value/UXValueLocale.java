package flat.uxml.value;

import flat.uxml.UXTheme;
import flat.uxml.UXValue;

import java.util.Objects;

public class UXValueLocale extends UXValue {
    private String text;

    public UXValueLocale(String text) {
        this.text = text;
    }

    @Override
    public String asString(UXTheme theme) {
        return theme.getText(text);
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
}
