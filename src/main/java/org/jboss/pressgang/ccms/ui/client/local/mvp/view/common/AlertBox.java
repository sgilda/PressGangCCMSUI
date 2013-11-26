package org.jboss.pressgang.ccms.ui.client.local.mvp.view.common;

import static com.google.common.base.Preconditions.checkState;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.VerticalPanel;
import org.jboss.pressgang.ccms.ui.client.local.constants.CSSConstants;
import org.jboss.pressgang.ccms.ui.client.local.resources.strings.PressGangCCMSUI;
import org.jboss.pressgang.ccms.ui.client.local.ui.UIUtilities;
import org.jetbrains.annotations.NotNull;

/**
 * A dialog box to replace the browser alert.
 */
public class AlertBox extends DialogBox {
    private static final AlertBox INSTANCE = new AlertBox();
    private static final List<HandlerRegistration> handlers = new ArrayList<HandlerRegistration>();
    private static final Event.NativePreviewHandler keyboardShortcutPreviewHandler = new Event.NativePreviewHandler() {
        @Override
        public void onPreviewNativeEvent(@NotNull final Event.NativePreviewEvent event) {
            final NativeEvent ne = event.getNativeEvent();

            if (ne.getKeyCode() == KeyCodes.KEY_ESCAPE || ne.getKeyCode() == KeyCodes.KEY_ENTER) {
                Scheduler.get().scheduleDeferred(new Command() {
                    @Override
                    public void execute() {
                        close();
                    }
                });
            }
        }
    };
    private static HandlerRegistration keyboardEventHandler = null;

    private final PushButton ok = UIUtilities.createPushButton(PressGangCCMSUI.INSTANCE.OK());
    private final HTML message = new HTML();
    private final VerticalPanel verticalPanel = new VerticalPanel();

    private AlertBox() {
        this.setModal(true);
        this.setGlassEnabled(true);
        this.setHTML(new SafeHtmlBuilder().appendEscaped(PressGangCCMSUI.INSTANCE.Alert()).toSafeHtml());
        this.add(verticalPanel);

        verticalPanel.add(this.message);
        verticalPanel.add(ok);

        this.ok.addStyleName(CSSConstants.AlertBox.ALERT_BOX_OK);
        this.message.addStyleName(CSSConstants.AlertBox.ALERT_BOX_MESSAGE);
        verticalPanel.addStyleName(CSSConstants.AlertBox.ALERT_BOX_PANEL);

        // this logic should not really be in a view, but as we are replacing
        // the alert box, this functionality is not going to change
        ok.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(@NotNull final ClickEvent clickEvent) {
                close();
            }
        });
    }

    public static boolean isDisplayed() {
        return INSTANCE.isShowing();
    }

    private static void close() {
        INSTANCE.hide();
        for (final HandlerRegistration handlerRegistration : handlers) {
            handlerRegistration.removeHandler();
        }

        if (keyboardEventHandler != null) {
            keyboardEventHandler.removeHandler();
            keyboardEventHandler = null;
        }
    }

    /**
     * Display a message in a dialog box with an OK button.
     *
     * @param message      The message to display
     * @param closeHandler A handler that is called when the alert box is closed. Once closed, the handler is removed.
     */
    public static void setMessageAndDisplay(@NotNull final String message, @NotNull final CloseHandler<PopupPanel> closeHandler) {
        handlers.add(INSTANCE.addCloseHandler(closeHandler));
        setMessageAndDisplay(message);
    }

    /**
     * Display a message in a dialog box with an OK button.
     *
     * @param message      The message to display
     * @param clickHandler A handler that is called when the ok button is clicked. Once clicked, the handler is removed.
     */
    public static void setMessageAndDisplay(@NotNull final String message, @NotNull final ClickHandler clickHandler) {
        handlers.add(INSTANCE.ok.addClickHandler(clickHandler));
        setMessageAndDisplay(message);
    }

    /**
     * Display a message in a dialog box with an OK button.
     *
     * @param message The message to display
     */
    public static void setMessageAndDisplay(@NotNull final String message) {
        checkState(!INSTANCE.isShowing(), "setMessageAndDisplay() should not be called when the alert is already displayed.");
        INSTANCE.setMessage(message);
        INSTANCE.center();
        keyboardEventHandler = Event.addNativePreviewHandler(keyboardShortcutPreviewHandler);
    }

    public static void setMessage(@NotNull final String message) {
        INSTANCE.message.setHTML(new SafeHtmlBuilder().appendEscapedLines(message).toSafeHtml());
    }
}
