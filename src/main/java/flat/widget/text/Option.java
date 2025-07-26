package flat.widget.text;

public class Option {
    private final String locale;
    private final String value;
    
    public Option(String value) {
        this.locale = null;
        this.value = value;
    }
    
    public Option(String value, String locale) {
        this.value = value;
        this.locale = locale;
    }
    
    public String getValue() {
        return value;
    }
    
    public String getLocale() {
        return locale;
    }
}
