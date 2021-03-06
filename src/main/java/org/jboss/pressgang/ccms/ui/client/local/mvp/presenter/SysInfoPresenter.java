package org.jboss.pressgang.ccms.ui.client.local.mvp.presenter;

import static org.jboss.pressgang.ccms.ui.client.local.utilities.GWTUtilities.clearContainerAndAddTopLevelPanel;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import com.google.gwt.user.client.ui.HasWidgets;
import org.jboss.pressgang.ccms.rest.v1.elements.RESTSystemStatsV1;
import org.jboss.pressgang.ccms.ui.client.local.mvp.presenter.base.BaseTemplatePresenter;
import org.jboss.pressgang.ccms.ui.client.local.mvp.presenter.base.BaseTemplatePresenterInterface;
import org.jboss.pressgang.ccms.ui.client.local.mvp.view.base.BaseTemplateViewInterface;
import org.jboss.pressgang.ccms.ui.client.local.restcalls.FailOverRESTCallDatabase;
import org.jboss.pressgang.ccms.ui.client.local.restcalls.RESTCallBack;
import org.jetbrains.annotations.NotNull;

/**
 * Presenter to add logic to the system information view
 */
@Dependent
public class SysInfoPresenter extends BaseTemplatePresenter implements BaseTemplatePresenterInterface {

    public static final String HISTORY_TOKEN = "SysInfoView";

    public interface Display extends BaseTemplateViewInterface {
        void display(@NotNull final RESTSystemStatsV1 stats);
    }

    @Inject
    private Display display;

    @Override
    public void parseToken(@NotNull final String historyToken) {

    }

    @Override
    public void bindExtended() {
        super.bind(display);

        getFailOverRESTCall().performRESTCall(
                FailOverRESTCallDatabase.getSysInfo(),
                new RESTCallBack<RESTSystemStatsV1>() {
                    public void success(@NotNull final RESTSystemStatsV1 value) {
                        display.display(value);
                    }
                }, display);
    }

    @Override
    public void go(@NotNull final HasWidgets container) {
        display.setViewShown(true);
        clearContainerAndAddTopLevelPanel(container, display);
        bindExtended();
    }

    @Override
    public void close() {

    }
}
