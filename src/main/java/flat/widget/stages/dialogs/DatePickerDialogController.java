package flat.widget.stages.dialogs;

import flat.Flat;
import flat.events.*;
import flat.math.Vector2;
import flat.uxml.Controller;
import flat.uxml.UXListener;
import flat.uxml.UXWidgetRangeValueListener;
import flat.widget.enums.DropdownAlign;
import flat.widget.enums.Visibility;
import flat.widget.layout.Grid;
import flat.widget.stages.Dialog;
import flat.widget.stages.Menu;
import flat.widget.stages.MenuItem;
import flat.widget.text.Button;
import flat.widget.text.Label;
import flat.widget.text.TextInputField;
import flat.window.Activity;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

class DatePickerDialogController extends DefaultDialogController {

    private final String title;
    private final boolean cancelable;
    private final boolean ranged;
    private final LocalDate initialDate;
    private final UXListener<Dialog> onShowListener;
    private final UXListener<Dialog> onHideListener;
    private final UXWidgetRangeValueListener<Dialog, LocalDate> onDatePickListener;

    private Menu monthsMenu;
    private Button[] btns;
    private MenuItem[] menuItems;
    private LocalDate visibleDate;
    private LocalDate firstDate;
    private LocalDate date;
    private LocalDate dateOut;
    private boolean dayIn = true;

    private String[] months = {
            "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"
    };
    private String[] weekdays = {
            "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"
    };

    DatePickerDialogController(Dialog dialog, DatePickerDialogBuilder builder) {
        super(dialog);
        this.title = builder.title;
        this.cancelable = builder.cancelable;
        this.ranged = builder.ranged;
        this.initialDate = builder.initialDate == null ? LocalDate.now() : builder.initialDate;
        this.onShowListener = builder.onShowListener;
        this.onHideListener = builder.onHideListener;
        this.onDatePickListener = builder.onDatePickListener;
    }

    @Flat
    public Label titleLabel;

    @Flat
    public Button cancelButton;

    @Flat
    public Grid calendarGrid;

    @Flat
    public Button monthButton;

    @Flat
    public TextInputField textDateIn;

    @Flat
    public TextInputField textDateOut;

    @Flat
    public Label labelLine;
    @Flat
    public Label labelWeekDay1;
    @Flat
    public Label labelWeekDay2;
    @Flat
    public Label labelWeekDay3;
    @Flat
    public Label labelWeekDay4;
    @Flat
    public Label labelWeekDay5;
    @Flat
    public Label labelWeekDay6;
    @Flat
    public Label labelWeekDay7;
    public Label[] labelWeekDay;
    
    @Flat
    public void onTextInputFilter(TextEvent event) {
        event.setText(event.getText().replaceAll("\\D", ""));
    }

    @Flat
    public void onTextFilter(TextEvent event) {
        String numbers = event.getText().replaceAll("\\D", "");
        if (numbers.length() <= 2) {
            event.setText(numbers);
        } else if (numbers.length() <= 4) {
            event.setText(numbers.substring(0, 2) + "/" + numbers.substring(2));
        } else {
            event.setText(numbers.substring(0, 2) + "/" + numbers.substring(2, 4) + "/" + numbers.substring(4));
        }
    }

    @Flat
    public void onTextKey(KeyEvent event) {
        if ((event.getKeycode() == KeyCode.KEY_ENTER || event.getKeycode() ==  KeyCode.KEY_KP_ENTER)
                && event.getType() == KeyEvent.RELEASED) {
            event.getSource().requestFocus(false);
        }
    }

    @Flat
    public void onTextInputFocus(FocusEvent focusEvent) {
        if (focusEvent.getType() == FocusEvent.LOST) {
            var text = (TextInputField) focusEvent.getSource();
            String numbers = text.getText().replaceAll("\\D", "");

            int day = date.getMonthValue();
            int month = date.getMonthValue();
            int year = date.getYear();
            try {
                day = Integer.parseInt(numbers.substring(0, Math.min(2, numbers.length())));
            } catch (Exception ignored) {
            }
            if (numbers.length() > 2) {
                try {
                    month = Integer.parseInt(numbers.substring(2,  Math.min(4, numbers.length())));
                } catch (Exception ignored) {
                }
            }
            if (numbers.length() > 4) {
                try {
                    year = Integer.parseInt(numbers.substring(4));
                } catch (Exception ignored) {
                }
            }

            // TODO - Crazy US Date
            if (true) {
                int change = day;
                day = month;
                month = change;
            }

            if (year < 1) year = 1;

            if (month < 1) month = 1;
            if (month > 12) month = 12;

            int maxDay = LocalDate.of(year, month, 1).plusMonths(1).plusDays(-1).getDayOfMonth();
            if (day < 1) day = 1;
            if (day > maxDay) day = maxDay;

            if (!ranged) {
                dateOut = LocalDate.of(year, month, day);
                setDate(dateOut);
            } else {
                if (text == textDateIn) {
                    setDate(LocalDate.of(year, month, day));
                } else {
                    setDateOut(LocalDate.of(year, month, day));
                }
            }
        }
    }

    @Flat
    public void hide(ActionEvent event) {
        dialog.smoothHide();
    }

    @Flat
    public void onOk(ActionEvent event) {
        if (date.isBefore(dateOut)) {
            UXWidgetRangeValueListener.safeHandle(onDatePickListener, dialog, date, dateOut);
        } else {
            UXWidgetRangeValueListener.safeHandle(onDatePickListener, dialog, dateOut, date);
        }
        dialog.smoothHide();
    }

    @Flat
    public void onCancel(ActionEvent event) {
        dialog.smoothHide();
    }

    @Override
    public void onShow() {
        labelWeekDay = new Label[]{labelWeekDay1, labelWeekDay2, labelWeekDay3, labelWeekDay4,
                labelWeekDay5, labelWeekDay6, labelWeekDay7};
        var theme = dialog.getTheme();
        if (theme != null) {
            for (int i = 0; i < months.length; i++) {
                months[i] = theme.getText(months[i], months[i]);
            }
            for (int i = 0; i < weekdays.length; i++) {
                weekdays[i] = theme.getText(weekdays[i], weekdays[i]);
                if (weekdays[i].length() > 1) {
                    weekdays[i] = new String(Character.toChars(weekdays[i].codePointAt(0)));
                }
                labelWeekDay[i].setText(weekdays[i]);
            }
        }
        
        if (titleLabel != null) {
            titleLabel.setText(title);
        }
        if (!cancelable && cancelButton != null) {
            cancelButton.setVisibility(Visibility.GONE);
        }

        if (!ranged) {
            if (labelLine != null) labelLine.setVisibility(Visibility.GONE);
            if (textDateOut != null) textDateOut.setVisibility(Visibility.GONE);
        }

        date = initialDate;
        dateOut = initialDate;
        if (btns == null) {
            btns = new Button[42];
            for (int i = 0; i < 7; i++) {
                for (int j = 0; j < 6; j++) {
                    Button btnDay = new Button();
                    btns[j * 7 + i] = btnDay;
                    btnDay.addStyle("text-primary");
                    btnDay.addStyle("dialog-datepicker-day");
                    btnDay.setPointerListener(this::onDayClick);
                    if ((j * 7 + i + 1) < 3) btnDay.setEnabled(false);

                    if (calendarGrid != null) calendarGrid.add(btnDay, i, j + 1);
                }
            }
            if (getActivity() != null) {
                dialog.moveTo(getActivity().getWidth() / 2, getActivity().getHeight() / 2);
            }
        }

        if (monthsMenu == null) {
            monthsMenu = new Menu();
            menuItems = new MenuItem[12];
            for (int i = 0; i < 12; i++) {
                int month = i + 1;
                menuItems[i] = new MenuItem();
                menuItems[i].setText(months[i]);
                menuItems[i].setActionListener((event) -> setMonth(month));
                monthsMenu.addMenuItem(menuItems[i]);
            }
        }

        setDate(date);
        setDateOut(dateOut);
        UXListener.safeHandle(onShowListener, dialog);
    }

    private void setDate(LocalDate date) {
        this.date = date;
        visibleDate = date;
        setMonth(visibleDate);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
        if (textDateIn != null) textDateIn.setText(date.format(formatter));
    }

    private void setDateOut(LocalDate date) {
        this.dateOut = date;
        visibleDate = date;
        setMonth(visibleDate);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
        if (textDateOut != null) textDateOut.setText(date.format(formatter));
    }

    private void setMonth(LocalDate date) {
        int month = date.getMonthValue();
        var firstDay = date.withDayOfMonth(1);
        int off = firstDay.getDayOfWeek().getValue() % 7;
        if (off == 0) off = 7;
        firstDate = firstDay.plusDays(-off);
        var start = firstDate;
        for (int i = 0; i < 42; i++) {
            btns[i].setText(start.getDayOfMonth() + "");
            btns[i].setEnabled(start.getMonthValue() == month);
            btns[i].removeStyle("dialog-datepicker-day-out");
            btns[i].removeStyle("dialog-datepicker-day-in");
            btns[i].removeStyle("dialog-datepicker-day-in-between");
            btns[i].removeStyle("dialog-datepicker-day-selected");
            if (start.equals(this.date)) {
                btns[i].addStyle("dialog-datepicker-day-selected");
                if (this.date.isAfter(this.dateOut)) {
                    btns[i].addStyle("dialog-datepicker-day-out");
                } else if (this.date.isBefore(this.dateOut)) {
                    btns[i].addStyle("dialog-datepicker-day-in");
                }
            } else if (start.equals(this.dateOut)) {
                btns[i].addStyle("dialog-datepicker-day-selected");
                if (this.dateOut.isAfter(this.date)) {
                    btns[i].addStyle("dialog-datepicker-day-out");
                } else if (this.dateOut.isBefore(this.date)) {
                    btns[i].addStyle("dialog-datepicker-day-in");
                }
            }
            btns[i].setActivated(start.equals(this.date) || start.equals(this.dateOut));

            if ((start.isAfter(this.date) && start.isBefore(dateOut)) ||
                    (start.isAfter(this.dateOut) && start.isBefore(this.date))) {
                btns[i].addStyle("dialog-datepicker-day-in-between");
            }
            start = start.plusDays(1);
        }
        if (monthButton != null) monthButton.setText(months[month - 1] + " " + date.getYear());
    }

    @Flat
    public void onDayClick(PointerEvent event) {
        if (!event.isConsumed() && event.getType() == PointerEvent.RELEASED) {
            event.consume();
            int index = 0;
            for (int i = 0; i < btns.length; i++) {
                if (btns[i] == event.getSource()) {
                    index = i;
                    break;
                }
            }
            if (!ranged) {
                dateOut = firstDate.plusDays(index);
                setDate(dateOut);
            } else {
                if (dayIn) {
                    setDate(firstDate.plusDays(index));
                    setDateOut(firstDate.plusDays(index));
                } else {
                    setDateOut(firstDate.plusDays(index));
                }
                dayIn = !dayIn;
            }
        }
    }

    @Flat
    public void onMonthsClick(PointerEvent event) {
        if (!event.isConsumed() && event.getType() == PointerEvent.RELEASED) {
            event.consume();
            Activity act = getActivity();
            if (act != null && monthButton != null) {
                float x = monthButton.getOutX();
                float y = monthButton.getOutY();
                float width = monthButton.getOutWidth();
                float height = monthButton.getOutHeight();
                Vector2 screen1 = monthButton.localToScreen(x, y);
                Vector2 screen2 = monthButton.localToScreen(x + width, y + height);
                monthsMenu.setFollowStyleProperty("width", false);
                monthsMenu.setPrefWidth(monthButton.getWidth());
                monthsMenu.show(act, screen2.x, screen2.y, DropdownAlign.TOP_RIGHT);
            }
        }
    }

    @Flat
    public void setMonth(int month) {
        visibleDate = visibleDate.withMonth(month);
        setMonth(visibleDate);
    }

    @Flat
    public void onNextMonth(ActionEvent event) {
        visibleDate = visibleDate.plusMonths(1);
        setMonth(visibleDate);
    }

    @Flat
    public void onPrevMonth(ActionEvent event) {
        visibleDate = visibleDate.plusMonths(-1);
        setMonth(visibleDate);
    }

    @Override
    public void onHide() {
        UXListener.safeHandle(onHideListener, dialog);
    }
}
