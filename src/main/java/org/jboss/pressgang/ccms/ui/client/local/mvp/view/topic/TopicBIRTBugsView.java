package org.jboss.pressgang.ccms.ui.client.local.mvp.view.topic;

import java.util.logging.Logger;

import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.ui.Frame;
import org.jboss.pressgang.ccms.rest.v1.entities.base.RESTBaseTopicV1;
import org.jboss.pressgang.ccms.ui.client.local.constants.Constants;
import org.jboss.pressgang.ccms.ui.client.local.mvp.presenter.topic.TopicBIRTBugsPresenter;
import org.jboss.pressgang.ccms.ui.client.local.mvp.view.base.BaseTemplateView;
import org.jboss.pressgang.ccms.ui.client.local.resources.strings.PressGangCCMSUI;
import org.jboss.pressgang.ccms.ui.client.local.server.ServerDetails;
import org.jboss.pressgang.ccms.ui.client.local.callbacks.ServerDetailsCallback;
import org.jetbrains.annotations.NotNull;

/**
 * A MVP view for displaying a topic's Bugzilla Bugs. This view simply displays an iFrame
 * to a BIRT report.
 *
 * @author Matthew Casperson
 */
public class TopicBIRTBugsView extends BaseTemplateView implements TopicBIRTBugsPresenter.Display {

    @NotNull
    private final Frame iFrame;

    private static final Logger LOGGER = Logger.getLogger(TopicBIRTBugsView.class.getName());

    public TopicBIRTBugsView() {
        super(PressGangCCMSUI.INSTANCE.PressGangCCMS(), PressGangCCMSUI.INSTANCE.Bugs());

        LOGGER.info("ENTER TopicBIRTBugsView()");

        iFrame = new Frame();
        iFrame.setWidth("100%");
        iFrame.setHeight("100%");
        iFrame.getElement().getStyle().setBorderWidth(0, Style.Unit.PX);
        this.getPanel().add(iFrame);
    }

    @Override
    public void display(@NotNull final RESTBaseTopicV1<?, ?, ?> entity, final boolean readonly) {
        if (entity.getId() != null) {
            ServerDetails.getSavedServer(new ServerDetailsCallback() {
                @Override
                public void serverDetailsFound(@NotNull final ServerDetails serverDetails) {
                    iFrame.setUrl(serverDetails.getReportUrl() + Constants.BIRT_RUN_REPORT + Constants.BIRT_TOPIC_BUGZILLA_REPORT + entity.getId());
                }
            });
        }
    }
}
