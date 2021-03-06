package org.jboss.pressgang.ccms.ui.client.local.mvp.view.process;

import javax.enterprise.context.Dependent;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.ClosablePopup;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.TextArea;
import org.jboss.pressgang.ccms.ui.client.local.constants.CSSConstants;
import org.jboss.pressgang.ccms.ui.client.local.mvp.presenter.process.ProcessFilteredResultsPresenter;
import org.jboss.pressgang.ccms.ui.client.local.resources.strings.PressGangCCMSUI;
import org.jboss.pressgang.ccms.ui.client.local.ui.UIUtilities;
import org.jetbrains.annotations.NotNull;

@Dependent
public class  ProcessLogsDialogBox extends ClosablePopup implements ProcessFilteredResultsPresenter.LogsDialogBox {
    private final PushButton close = UIUtilities.createPushButton(PressGangCCMSUI.INSTANCE.Close());
    private final FlexTable layout = new FlexTable();
    private final TextArea logs = new TextArea();
    private HandlerRegistration handlerRegistration;

    @Override
    public PushButton getClose() {
        return close;
    }

    @NotNull
    @Override
    public String getText() {
        return logs.getText();
    }

    @NotNull
    @Override
    public void setText(final String text) {
        String cleanedText = text;
        if (text != null) {
            // Tabs won't be rendered in some browsers or will be inconsistent so change it to 4 spaces
            cleanedText = cleanedText.replace("\t", "    ");
        }
        logs.setText(cleanedText);
    }

    @Override
    public ClosablePopup getDialogBox() {
        return this;
    }

    public ProcessLogsDialogBox() {
        super(PressGangCCMSUI.INSTANCE.ProcessLogs(), true);
        setGlassEnabled(true);

        addStyleName(CSSConstants.ProcessLogsDialog.LOGS_DIALOG_BOX);
        logs.addStyleName(CSSConstants.ProcessLogsDialog.LOGS_FIELD);
        logs.setReadOnly(true);

        @NotNull final HorizontalPanel buttonPanel = new HorizontalPanel();
        buttonPanel.addStyleName(CSSConstants.Common.DIALOG_BOX_OK_CANCEL_PANEL);
        buttonPanel.add(close);

        layout.setWidget(0, 0, logs);
        layout.setWidget(1, 0, buttonPanel);

        add(layout);

        // Add the close handler
        close.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                hide();
            }
        });
    }

    @Override
    public void show() {
        // Add a handler to deal with window resizes
        if (handlerRegistration == null) {
            handlerRegistration = Window.addResizeHandler(new ResizeHandler() {
                @Override
                public void onResize(final ResizeEvent event) {
                    center();
                }
            });
        }
        super.show();
    }

    @Override
    public void hide() {
        if (handlerRegistration != null) {
            handlerRegistration.removeHandler();
            handlerRegistration = null;
        }
        super.hide();
    }
}
