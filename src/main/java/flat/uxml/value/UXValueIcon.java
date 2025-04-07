package flat.uxml.value;

import flat.graphics.image.Drawable;
import flat.graphics.symbols.IconsManager;
import flat.uxml.UXTheme;

import java.util.Objects;

public class UXValueIcon extends UXValue {
    private final UXValue bundle;
    private final UXValue name;

    public UXValueIcon(UXValue bundle, UXValue name) {
        this.bundle = bundle;
        this.name = name;
    }

    @Override
    public Drawable asDrawable(UXTheme theme) {
        if (bundle == null) {
            return IconsManager.getIcon(name.asString(theme));
        } else {
            return IconsManager.getIcon(bundle.asString(theme), name.asString(theme));
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UXValueIcon that = (UXValueIcon) o;
        return Objects.equals(bundle, that.bundle) && Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(bundle, name);
    }

    @Override
    public String toString() {
        return "Icon : " + bundle + " " + name;
    }
}
