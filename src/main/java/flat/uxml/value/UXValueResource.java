package flat.uxml.value;

import flat.resources.ResourceStream;
import flat.uxml.UXTheme;
import flat.uxml.UXValue;
import flat.window.Application;

import java.util.Objects;

public class UXValueResource extends UXValue {
    private String url;

    public UXValueResource(String url) {
        this.url = url;
    }

    @Override
    public ResourceStream asResource(UXTheme theme) {
        return Application.getResourcesManager().getResource(url);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UXValueResource that = (UXValueResource) o;
        return Objects.equals(url, that.url);
    }

    @Override
    public int hashCode() {
        return Objects.hash(url);
    }
}
