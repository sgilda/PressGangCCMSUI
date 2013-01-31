package org.jboss.pressgang.ccms.ui.client.local.mvp.presenter.topic;

import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.view.client.HasData;
import org.jboss.errai.bus.client.api.Message;
import org.jboss.pressgang.ccms.rest.v1.collections.RESTPropertyTagCollectionV1;
import org.jboss.pressgang.ccms.rest.v1.collections.RESTTagCollectionV1;
import org.jboss.pressgang.ccms.rest.v1.collections.RESTTopicCollectionV1;
import org.jboss.pressgang.ccms.rest.v1.collections.base.RESTBaseCollectionItemV1;
import org.jboss.pressgang.ccms.rest.v1.collections.items.RESTPropertyTagCollectionItemV1;
import org.jboss.pressgang.ccms.rest.v1.collections.items.RESTTagCollectionItemV1;
import org.jboss.pressgang.ccms.rest.v1.collections.items.RESTTopicCollectionItemV1;
import org.jboss.pressgang.ccms.rest.v1.collections.items.join.RESTAssignedPropertyTagCollectionItemV1;
import org.jboss.pressgang.ccms.rest.v1.collections.items.join.RESTTagInCategoryCollectionItemV1;
import org.jboss.pressgang.ccms.rest.v1.collections.join.RESTAssignedPropertyTagCollectionV1;
import org.jboss.pressgang.ccms.rest.v1.entities.RESTPropertyTagV1;
import org.jboss.pressgang.ccms.rest.v1.entities.RESTTopicV1;
import org.jboss.pressgang.ccms.rest.v1.entities.base.RESTBaseEntityV1;
import org.jboss.pressgang.ccms.rest.v1.entities.join.RESTAssignedPropertyTagV1;
import org.jboss.pressgang.ccms.rest.v1.sort.RESTTagCategoryCollectionItemV1SortComparator;
import org.jboss.pressgang.ccms.ui.client.local.constants.ServiceConstants;
import org.jboss.pressgang.ccms.ui.client.local.mvp.presenter.base.TemplatePresenter;
import org.jboss.pressgang.ccms.ui.client.local.mvp.presenter.base.orderedchildren.BaseExtendedChildrenPresenter;
import org.jboss.pressgang.ccms.ui.client.local.mvp.presenter.base.orderedchildren.SetNewChildSortCallback;
import org.jboss.pressgang.ccms.ui.client.local.mvp.presenter.topic.base.BaseTopicViewPresenter;
import org.jboss.pressgang.ccms.ui.client.local.mvp.view.base.BaseTemplateViewInterface;
import org.jboss.pressgang.ccms.ui.client.local.mvp.view.base.orderedchildren.BaseExtendedChildrenViewInterface;
import org.jboss.pressgang.ccms.ui.client.local.mvp.view.topic.BaseTopicViewInterface;
import org.jboss.pressgang.ccms.ui.client.local.preferences.Preferences;
import org.jboss.pressgang.ccms.ui.client.local.resources.strings.PressGangCCMSUI;
import org.jboss.pressgang.ccms.ui.client.local.restcalls.RESTCalls;
import org.jboss.pressgang.ccms.ui.client.local.utilities.EnhancedAsyncDataProvider;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.jboss.pressgang.ccms.ui.client.local.utilities.GWTUtilities.clearContainerAndAddTopLevelPanel;
import static org.jboss.pressgang.ccms.ui.client.local.utilities.GWTUtilities.removeHistoryToken;

/**
    The presenter used to populate the tables of possible and existing topic property tags.
 */
@Dependent
public class TopicPropertyTagsPresenter extends BaseExtendedChildrenPresenter<
            RESTTopicV1, RESTTopicCollectionV1, RESTTopicCollectionItemV1,
            RESTTopicV1,
            RESTPropertyTagV1, RESTPropertyTagCollectionV1, RESTPropertyTagCollectionItemV1,
            RESTAssignedPropertyTagV1, RESTAssignedPropertyTagCollectionV1, RESTAssignedPropertyTagCollectionItemV1>
        implements TemplatePresenter {

    public interface Display extends BaseExtendedChildrenViewInterface<
                RESTTopicV1, RESTTopicCollectionV1, RESTTopicCollectionItemV1,
                RESTTopicV1,
                RESTPropertyTagV1, RESTPropertyTagCollectionV1, RESTPropertyTagCollectionItemV1,
                RESTAssignedPropertyTagV1, RESTAssignedPropertyTagCollectionV1, RESTAssignedPropertyTagCollectionItemV1>,
            BaseTopicViewInterface {
        Column<RESTAssignedPropertyTagCollectionItemV1, String> getPropertyTagRemoveColumn();
    }

    public static final String HISTORY_TOKEN = "TopicPropertyTagsView";

    /**
     * A logger.
     */
    private static final Logger LOGGER = Logger.getLogger(TopicPropertyTagsPresenter.class.getName());
    private Integer topicId;
    @Inject
    private Display display;

    public Display getDisplay() {
        return display;
    }

    @Override
    public void parseToken(final String historyToken) {
        try {
            topicId = Integer.parseInt(removeHistoryToken(historyToken, HISTORY_TOKEN));
        } catch (final NumberFormatException ex) {
            topicId = null;
        }
    }

    @Override
    public void go(final HasWidgets container) {
        clearContainerAndAddTopLevelPanel(container, display);
        process(topicId, ServiceConstants.DEFAULT_HELP_TOPIC, HISTORY_TOKEN);

    }

    public void process(final Integer topicId, final int helpTopicId, final String pageId) {
        super.bind(helpTopicId, pageId, Preferences.TOPIC_PROPERTYTAG_VIEW_MAIN_SPLIT_WIDTH, display);
    }

    @Override
    public EnhancedAsyncDataProvider<RESTAssignedPropertyTagCollectionItemV1> generateExistingProvider(final RESTTopicV1 entity) {
        return new EnhancedAsyncDataProvider<RESTAssignedPropertyTagCollectionItemV1>() {
            @Override
            protected void onRangeChanged(final HasData<RESTAssignedPropertyTagCollectionItemV1> display) {
                try
                {
                    LOGGER.log(Level.INFO, "ENTER TopicPropertyTagsPresenter.generateExistingProvider() EnhancedAsyncDataProvider.onRangeChanged()");

                    getExistingProviderData().setStartRow(display.getVisibleRange().getStart());
                    getExistingProviderData().setItems(new ArrayList<RESTAssignedPropertyTagCollectionItemV1>());

                    /* Zero results can be a null list. Also selecting a new tag will reset getProviderData(). */
                    if (entity != null && entity.getProperties() != null) {
                        LOGGER.log(Level.INFO, "Found " + entity.getProperties().getItems().size() + " Property Tags.");
                        /* Don't display removed tags */
                        for (final RESTAssignedPropertyTagCollectionItemV1 propertyTagInTopic : entity.getProperties()
                                .returnExistingAddedAndUpdatedCollectionItems()) {
                            getExistingProviderData().getItems().add(propertyTagInTopic);
                        }
                    }
                    else
                    {
                        LOGGER.log(Level.WARNING, "entity == null: " + (entity == null) + " entity.getProperties() == null: " + (entity.getProperties() == null));
                    }

                    //Collections.sort(getExistingProviderData().getItems(), new RESTTagCategoryCollectionItemV1SortComparator());

                    displayNewFixedList(getExistingProviderData().getItems());
                } finally {
                    LOGGER.log(Level.INFO, "EXIT TopicPropertyTagsPresenter.generateExistingProvider() EnhancedAsyncDataProvider.onRangeChanged()");
                }
            }
        };
    }

    @Override
    public EnhancedAsyncDataProvider<RESTPropertyTagCollectionItemV1> generatePossibleChildrenProvider() {
        return new EnhancedAsyncDataProvider<RESTPropertyTagCollectionItemV1>() {
            @Override
            protected void onRangeChanged(final HasData<RESTPropertyTagCollectionItemV1> display) {

                providerData.setStartRow(display.getVisibleRange().getStart());

                if (providerData.getItems() != null) {
                    displayNewFixedList(providerData.getItems());
                } else {
                    resetProvider();
                }

            }
        };
    }

    @Override
    public void refreshPossibleChildrenDataAndList() {
        try {
            LOGGER.log(Level.INFO, "ENTER TopicPropertyTagsPresenter.refreshPossibleChildrenDataAndList()");

            final RESTCalls.RESTCallback<RESTPropertyTagCollectionV1> callback = new RESTCalls.RESTCallback<RESTPropertyTagCollectionV1>() {
                @Override
                public void begin() {
                    getDisplay().addWaitOperation();
                }

                @Override
                public void generalException(final Exception ex) {
                    LOGGER.log(Level.SEVERE, "RESTCallback.generalException()\n\tException: " + ex.toString());
                    Window.alert(PressGangCCMSUI.INSTANCE.ConnectionError());
                    getDisplay().removeWaitOperation();
                }

                @Override
                public void success(final RESTPropertyTagCollectionV1 retValue) {
                    try {
                        LOGGER.log(Level.INFO, "RESTCallback.success(). retValue.getSize(): " + retValue.getSize() + " retValue.getItems().size(): " + retValue.getItems().size());
                        /* Zero results can be a null list */
                        providerData.setStartRow(0);
                        providerData.setItems(retValue.getItems());
                        providerData.setSize(retValue.getItems().size());

                        /* Refresh the list */
                        getDisplay().getPossibleChildrenProvider().displayNewFixedList(providerData.getItems());

                    } finally {
                        getDisplay().removeWaitOperation();
                    }
                }

                @Override
                public void failed(final Message message, final Throwable throwable) {
                    getDisplay().removeWaitOperation();
                    LOGGER.log(Level.SEVERE, "RESTCallback.failed()\n\tMessage: " + message.toString() + "\n\t Throwable: " + throwable.toString());
                    Window.alert(PressGangCCMSUI.INSTANCE.ConnectionError());
                }
            };

            /* Redisplay the loading widget. updateRowCount(0, false) is used to display the cell table loading widget. */
            providerData.reset();
            this.getDisplay().getPossibleChildrenProvider().resetProvider();

            RESTCalls.getPropertyTags(callback);
        } finally {
            LOGGER.log(Level.INFO, "EXIT TopicPropertyTagsPresenter.refreshPossibleChildrenDataAndList()");
        }
    }
}