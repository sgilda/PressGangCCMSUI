package org.jboss.pressgang.ccms.ui.client.local.mvp.presenter.topic.search;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterables;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.view.client.HasData;
import com.google.gwt.xml.client.XMLParser;
import com.google.gwt.xml.client.impl.DOMParseException;
import org.jboss.errai.bus.client.api.Message;
import org.jboss.pressgang.ccms.rest.v1.collections.RESTTagCollectionV1;
import org.jboss.pressgang.ccms.rest.v1.collections.RESTTopicCollectionV1;
import org.jboss.pressgang.ccms.rest.v1.collections.base.RESTBaseCollectionItemV1;
import org.jboss.pressgang.ccms.rest.v1.collections.items.RESTBugzillaBugCollectionItemV1;
import org.jboss.pressgang.ccms.rest.v1.collections.items.RESTTagCollectionItemV1;
import org.jboss.pressgang.ccms.rest.v1.collections.items.RESTTopicCollectionItemV1;
import org.jboss.pressgang.ccms.rest.v1.collections.items.join.RESTCategoryInTagCollectionItemV1;
import org.jboss.pressgang.ccms.rest.v1.collections.join.RESTAssignedPropertyTagCollectionV1;
import org.jboss.pressgang.ccms.rest.v1.entities.RESTTagV1;
import org.jboss.pressgang.ccms.rest.v1.entities.RESTTopicV1;
import org.jboss.pressgang.ccms.ui.client.local.constants.Constants;
import org.jboss.pressgang.ccms.ui.client.local.constants.ServiceConstants;
import org.jboss.pressgang.ccms.ui.client.local.mvp.events.dataevents.TopicListReceivedHandler;
import org.jboss.pressgang.ccms.ui.client.local.mvp.presenter.base.searchandedit.BaseSearchAndEditComponent;
import org.jboss.pressgang.ccms.ui.client.local.mvp.presenter.base.searchandedit.DisplayNewEntityCallback;
import org.jboss.pressgang.ccms.ui.client.local.mvp.presenter.base.searchandedit.GetNewEntityCallback;
import org.jboss.pressgang.ccms.ui.client.local.mvp.presenter.topic.base.BaseTopicCombinedViewPresenter;
import org.jboss.pressgang.ccms.ui.client.local.mvp.presenter.topic.base.GetCurrentTopic;
import org.jboss.pressgang.ccms.ui.client.local.mvp.presenter.topic.base.StringListLoaded;
import org.jboss.pressgang.ccms.ui.client.local.mvp.events.viewevents.SearchResultsAndTopicViewEvent;
import org.jboss.pressgang.ccms.ui.client.local.mvp.presenter.base.TemplatePresenter;
import org.jboss.pressgang.ccms.ui.client.local.mvp.presenter.topic.*;
import org.jboss.pressgang.ccms.ui.client.local.mvp.view.base.BaseTemplateViewInterface;
import org.jboss.pressgang.ccms.ui.client.local.mvp.view.base.searchandedit.BaseSearchAndEditViewInterface;
import org.jboss.pressgang.ccms.ui.client.local.mvp.view.topic.BaseTopicViewInterface;
import org.jboss.pressgang.ccms.ui.client.local.preferences.Preferences;
import org.jboss.pressgang.ccms.ui.client.local.resources.strings.PressGangCCMSUI;
import org.jboss.pressgang.ccms.ui.client.local.restcalls.BaseRestCallback;
import org.jboss.pressgang.ccms.ui.client.local.restcalls.RESTCalls;
import org.jboss.pressgang.ccms.ui.client.local.restcalls.RESTCalls.RESTCallback;
import org.jboss.pressgang.ccms.ui.client.local.sort.RESTTopicCollectionItemV1RevisionSort;
import org.jboss.pressgang.ccms.ui.client.local.ui.SplitType;
import org.jboss.pressgang.ccms.ui.client.local.ui.editor.topicview.RESTTopicV1BasicDetailsEditor;
import org.jboss.pressgang.ccms.ui.client.local.ui.editor.topicview.assignedtags.TopicTagViewCategoryEditor;
import org.jboss.pressgang.ccms.ui.client.local.ui.editor.topicview.assignedtags.TopicTagViewProjectEditor;
import org.jboss.pressgang.ccms.ui.client.local.ui.editor.topicview.assignedtags.TopicTagViewTagEditor;
import org.jboss.pressgang.ccms.ui.client.local.utilities.EnhancedAsyncDataProvider;
import org.jboss.pressgang.ccms.ui.client.local.utilities.GWTUtilities;

import javax.annotation.Nullable;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.jboss.pressgang.ccms.ui.client.local.utilities.GWTUtilities.clearContainerAndAddTopLevelPanel;
import static org.jboss.pressgang.ccms.ui.client.local.utilities.GWTUtilities.removeHistoryToken;

@Dependent
public class SearchResultsAndTopicPresenter
        extends BaseSearchAndEditComponent<
            SearchResultsPresenter.Display,
            RESTTopicV1,
            RESTTopicCollectionV1,
            RESTTopicCollectionItemV1,
            BaseTopicViewInterface,
            TopicPresenter.Display,
            RESTTopicV1BasicDetailsEditor>
        implements TemplatePresenter {

    public static final String HISTORY_TOKEN = "SearchResultsAndTopicView";
    /**
     * false to indicate that the topic views should display action buttons applicable to established topics (as opposed to new
     * topics)
     */
    private static final boolean NEW_TOPIC = false;
    private static final Logger logger = Logger.getLogger(SearchResultsAndTopicPresenter.class.getName());
    /**
     * Setup automatic flushing and rendering.
     */
    final Timer timer = new Timer() {
        @Override
        public void run() {
            if (lastDisplayedView == topicXMLComponent.getDisplay()) {
                refreshRenderedView(false);
            }
        }
    };
    @Inject private HandlerManager eventBus;
    /**
     * The last xml that was rendered
     */
    private String lastXML;
    /**
     * How long it has been since the xml changes
     */
    private long lastXMLChange;
    /**
     * False if we are not displaying external images in the current rendered view, and true otherwise
     */
    private boolean isDisplayingImage;
    private String queryString;
    /**
     * A list of locales retrieved from the server
     */
    private List<String> locales;

    @Inject private Display display;

    @Inject private TopicPresenter topicViewComponent;

    @Inject private TopicXMLPresenter topicXMLComponent;
    /**
     * The rendered topic view display in a split panel
     */
    @Inject private TopicRenderedPresenter.Display topicSplitPanelRenderedDisplay;
    @Inject private SearchResultsPresenter searchResultsComponent;
    @Inject private TopicXMLErrorsPresenter topicXMLErrorsPresenter;
    @Inject private TopicTagsPresenter topicTagsComponent;
    @Inject private TopicRevisionsPresenter topicRevisionsComponent;
    @Inject private TopicBIRTBugsPresenter topicBugsPresenter;
    @Inject private TopicRenderedPresenter topicRenderedPresenter;
    @Inject private TopicPropertyTagsPresenter topicPropertyTagPresenter;
    /**
     * How the rendering panel is displayed
     */
    private SplitType split = SplitType.NONE;

    @Override
    public void go(final HasWidgets container) {

        try {
            logger.log(Level.INFO, "ENTER SearchResultsAndTopicPresenter.go()");


            /* A call back used to get a fresh copy of the entity that was selected */
            final GetNewEntityCallback<RESTTopicV1> getNewEntityCallback = new GetNewEntityCallback<RESTTopicV1>() {

                @Override
                public void getNewEntity(final Integer id, final DisplayNewEntityCallback<RESTTopicV1> displayCallback) {

                    try {
                        logger.log(Level.INFO, "ENTER SearchResultsAndTopicPresenter.bind() GetNewEntityCallback.getNewEntity()");

                        final RESTCallback<RESTTopicV1> callback = new BaseRestCallback<RESTTopicV1, BaseTemplateViewInterface>(
                                display, new BaseRestCallback.SuccessAction<RESTTopicV1, BaseTemplateViewInterface>() {
                            @Override
                            public void doSuccessAction(final RESTTopicV1 retValue, final BaseTemplateViewInterface display) {
                                try {
                                    logger.log(Level.INFO,
                                            "ENTER SearchResultsAndTopicPresenter.bind() RESTCallback.doSuccessAction()");

                                    logger.log(Level.INFO, "retValue.getProperties().getItems().size(): " + retValue.getProperties().getItems().size());

                                    displayCallback.displayNewEntity(retValue);
                                } finally {
                                    logger.log(Level.INFO,
                                            "EXIT SearchResultsAndTopicPresenter.bind() RESTCallback.doSuccessAction()");
                                }
                            }
                        });
                        RESTCalls.getTopic(callback, id);
                    } finally {
                        logger.log(Level.INFO, "EXIT SearchResultsAndTopicPresenter.bind() GetNewEntityCallback.getNewEntity()");
                    }
                }
            };

            clearContainerAndAddTopLevelPanel(container, display);
            this.display.getViewActionButtonsPanel().setWidget(display.getTopActionParentPanel());

            /* Initialize the other presenters we have pulled in */
            searchResultsComponent.process(ServiceConstants.SEARCH_VIEW_HELP_TOPIC, HISTORY_TOKEN, queryString);
            topicTagsComponent.process(null, ServiceConstants.DEFAULT_HELP_TOPIC, HISTORY_TOKEN);
            topicPropertyTagPresenter.process(null, ServiceConstants.DEFAULT_HELP_TOPIC, HISTORY_TOKEN);

            /* When the topics have been loaded, display the first one */
            searchResultsComponent.addTopicListReceivedHandler(new TopicListReceivedHandler(){
                @Override
                public void onTopicsRecieved(final RESTTopicCollectionV1 topics) {
                    if (topics.getItems() != null && topics.getItems().size() == 1) {
                        loadNewEntity(getNewEntityCallback, topics.getItems().get(0));
                    }
                }
            });

            super.bind(ServiceConstants.TOPIC_EDIT_VIEW_CONTENT_TOPIC, HISTORY_TOKEN, Preferences.TOPIC_VIEW_MAIN_SPLIT_WIDTH, topicXMLComponent.getDisplay(), topicViewComponent.getDisplay(),
                    searchResultsComponent.getDisplay(), searchResultsComponent, display, display, getNewEntityCallback);

            /* Display the split panes */
            initializeDisplay();

            // the birt bugs presenter is just an iframe, and doesn't need any providers
            // this.topicBugsPresenter.getDisplay().setProvider(generateTopicBugListProvider());
            this.topicRevisionsComponent.getDisplay().setProvider(generateTopicRevisionsListProvider());

            bindViewTopicRevisionButton();
            bindSplitPanelResize();
            bindTagEditingButtons();
            loadSplitPanelSize();

            this.topicTagsComponent.bindNewTagListBoxes(new AddTagClickhandler());

            BaseTopicCombinedViewPresenter.populateLocales(display, new StringListLoaded() {
                @Override
                public void stringListLoaded(final List<String> locales) {
                    SearchResultsAndTopicPresenter.this.locales = locales;
                }
            });

            BaseTopicCombinedViewPresenter.addKeyboardShortcutEventHandler(this.topicXMLComponent.getDisplay(), this.display, new GetCurrentTopic() {

                @Override
                public RESTTopicV1 getCurrentlyEditedTopic() {
                    return searchResultsComponent.getProviderData().getDisplayedItem().getItem();
                }
            });
        } finally {
            logger.log(Level.INFO, "EXIT SearchResultsAndTopicPresenter.go()");
        }
    }

    @Override
    public void parseToken(final String historyToken) {

        queryString = removeHistoryToken(historyToken, SearchResultsAndTopicPresenter.HISTORY_TOKEN);

        /* Make sure that the query string has at least the prefix */
        if (!queryString.startsWith(Constants.QUERY_PATH_SEGMENT_PREFIX)) {
            queryString = Constants.QUERY_PATH_SEGMENT_PREFIX;
        }
    }

    /**
     * Refresh the split panel rendered view
     *
     * @param forceExternalImages true if external images should be displayed, false if they should only be displayed
     *                            after the topics has not been edited after a period of time
     */
    private void refreshRenderedView(final boolean forceExternalImages) {
        topicXMLComponent.getDisplay().getDriver().flush();

        final boolean xmlHasChanges = lastXML == null || !lastXML.equals(getTopicOrRevisionTopic().getItem().getXml());

        if (xmlHasChanges) {
            lastXMLChange = System.currentTimeMillis();
        }

        final Boolean timeToDisplayImage = forceExternalImages
                || System.currentTimeMillis() - lastXMLChange >= Constants.REFRESH_RATE_WTH_IMAGES;

        if (xmlHasChanges || (!isDisplayingImage && timeToDisplayImage)) {
            isDisplayingImage = timeToDisplayImage;
            topicSplitPanelRenderedDisplay.initialize(getTopicOrRevisionTopic().getItem(), isReadOnlyMode(), NEW_TOPIC,
                    display.getSplitType(), locales, isDisplayingImage);
        }

        lastXML = getTopicOrRevisionTopic().getItem().getXml();
    }

    /**
     * Reflect the state of the editor with the XML editor toggle buttons
     */
    private void setXMLEditorButtonsToEditorState() {
        try {
            logger.log(Level.INFO, "ENTER SearchResultsAndTopicPresenter.setXMLEditorButtonsToEditorState()");

            topicXMLComponent.getDisplay().getLineWrap().setDown(topicXMLComponent.getDisplay().getEditor().getUserWrapMode());
            topicXMLComponent.getDisplay().getShowInvisibles().setDown(topicXMLComponent.getDisplay().getEditor().getShowInvisibles());
        } finally {
            logger.log(Level.INFO, "EXIT SearchResultsAndTopicPresenter.setXMLEditorButtonsToEditorState()");
        }
    }

    /**
     * (Re)Initialize the main display with the rendered view split pane (if selected).
     */
    private void initializeDisplay() {
        final String savedSplit = Preferences.INSTANCE.getString(Preferences.TOPIC_RENDERED_VIEW_SPLIT_TYPE, "");
        if (Preferences.TOPIC_RENDERED_VIEW_SPLIT_NONE.equals(savedSplit)) {
            split = SplitType.NONE;
        } else if (Preferences.TOPIC_RENDERED_VIEW_SPLIT_VERTICAL.equals(savedSplit)) {
            split = SplitType.VERTICAL;
        } else {
            split = SplitType.HORIZONTAL;
        }

        /* Have to do this after the parseToken method has been called */
        display.initialize(false, false, split, topicSplitPanelRenderedDisplay.getPanel());

        loadSplitPanelSize();
    }

    /**
     * Sync any changes back to the underlying object
     */
    private void flushChanges() {
        if (lastDisplayedView == null || lastDisplayedView.getDriver() == null) {
            return;
        }

        /* These are read only views */
        if (lastDisplayedView == topicXMLErrorsPresenter.getDisplay() || lastDisplayedView == topicTagsComponent.getDisplay()) {
            return;
        }

        lastDisplayedView.getDriver().flush();
    }

    /**
     * The currently displayed topic is topicRevisionsComponent.getDisplay().getRevisionTopic() if it is not null, or
     * searchResultsComponent.getProviderData().getDisplayedItem() otherwise.
     *
     * @return The currently displayed topic
     */
    private RESTTopicCollectionItemV1 getTopicOrRevisionTopic() {
        final RESTTopicCollectionItemV1 sourceTopic = topicRevisionsComponent.getDisplay().getRevisionTopic() == null ? searchResultsComponent
                .getProviderData().getDisplayedItem() : topicRevisionsComponent.getDisplay().getRevisionTopic();

        if (sourceTopic == null)
            throw new NullPointerException("sourceTopic cannot be null");

        return sourceTopic;
    }

    /**
     * The UI is in a readonly mode if viewing a topic revision
     *
     * @return true if the UI is in readonly mode, and false otherwise
     */
    private boolean isReadOnlyMode() {
        return topicRevisionsComponent.getDisplay().getRevisionTopic() != null;
    }

    private void showRegularMenu() {
        display.getViewActionButtonsPanel().clear();
        display.getViewActionButtonsPanel().add(lastDisplayedView.getTopActionPanel());
    }

    private void showRenderedSplitPanelMenu() {
        display.getViewActionButtonsPanel().clear();
        display.getViewActionButtonsPanel().add(display.getRenderedSplitViewMenu());
    }

    /**
     * Load the split panel sizes
     */
    private void loadSplitPanelSize() {
        try {
            logger.log(Level.INFO, "ENTER SearchResultsAndTopicPresenter.loadSplitPanelSize()");

            if (split == SplitType.HORIZONTAL) {
                display.getSplitPanel().setSplitPosition(
                        topicSplitPanelRenderedDisplay.getPanel().getParent(),
                        Preferences.INSTANCE.getInt(Preferences.TOPIC_VIEW_RENDERED_HORIZONTAL_SPLIT_WIDTH,
                                Constants.SPLIT_PANEL_SIZE), false);
            } else if (split == SplitType.VERTICAL) {
                display.getSplitPanel().setSplitPosition(
                        topicSplitPanelRenderedDisplay.getPanel().getParent(),
                        Preferences.INSTANCE.getInt(Preferences.TOPIC_VIEW_RENDERED_VERTICAL_SPLIT_WIDTH,
                                Constants.SPLIT_PANEL_SIZE), false);
            }
        }
        finally {
            logger.log(Level.INFO, "EXIT SearchResultsAndTopicPresenter.loadSplitPanelSize()");
        }
    }

    /**
     * @return A provider to be used for the topic revisions display list
     */
    private EnhancedAsyncDataProvider<RESTTopicCollectionItemV1> generateTopicRevisionsListProvider() {
        final EnhancedAsyncDataProvider<RESTTopicCollectionItemV1> provider = new EnhancedAsyncDataProvider<RESTTopicCollectionItemV1>() {
            @Override
            protected void onRangeChanged(final HasData<RESTTopicCollectionItemV1> display) {
                if (searchResultsComponent.getProviderData().getDisplayedItem() != null
                        && searchResultsComponent.getProviderData().getDisplayedItem().getItem().getRevisions() != null
                        && searchResultsComponent.getProviderData().getDisplayedItem().getItem().getRevisions().getItems() != null) {
                    displayNewFixedList(searchResultsComponent.getProviderData().getDisplayedItem().getItem().getRevisions()
                            .getItems());
                } else {
                    resetProvider();
                }
            }
        };
        return provider;
    }

    /**
     * @return A provider to be used for the topic display list
     */
    private EnhancedAsyncDataProvider<RESTBugzillaBugCollectionItemV1> generateTopicBugListProvider() {
        final EnhancedAsyncDataProvider<RESTBugzillaBugCollectionItemV1> provider = new EnhancedAsyncDataProvider<RESTBugzillaBugCollectionItemV1>() {
            @Override
            protected void onRangeChanged(final HasData<RESTBugzillaBugCollectionItemV1> display) {
                if (searchResultsComponent.getProviderData().getDisplayedItem() != null
                        && searchResultsComponent.getProviderData().getDisplayedItem().getItem().getBugzillaBugs_OTM() != null) {
                    displayNewFixedList(searchResultsComponent.getProviderData().getDisplayedItem().getItem()
                            .getBugzillaBugs_OTM().getItems());
                } else {
                    resetProvider();
                }
            }
        };
        return provider;
    }

    /**
     * Respond to the split panel resizing by redisplaying the ACE editor component
     */
    private void bindSplitPanelResize() {

        try {
            logger.log(Level.INFO, "ENTER SearchResultsAndTopicPresenter.bindSplitPanelResize()");

            display.getSplitPanel().addResizeHandler(new ResizeHandler() {
                @Override
                public void onResize(final ResizeEvent event) {
                    if (topicXMLComponent.getDisplay().getEditor() != null) {
                        topicXMLComponent.getDisplay().getEditor().redisplay();
                    }

                    /*
                     * Saves the width of the split screen
                     */
                    if (split == SplitType.HORIZONTAL) {
                        Preferences.INSTANCE.saveSetting(Preferences.TOPIC_VIEW_RENDERED_HORIZONTAL_SPLIT_WIDTH, display
                                .getSplitPanel().getSplitPosition(topicSplitPanelRenderedDisplay.getPanel().getParent()) + "");
                    } else if (split == SplitType.VERTICAL) {
                        Preferences.INSTANCE.saveSetting(Preferences.TOPIC_VIEW_RENDERED_VERTICAL_SPLIT_WIDTH, display
                                .getSplitPanel().getSplitPosition(topicSplitPanelRenderedDisplay.getPanel().getParent()) + "");
                    }
                }
            });
        }
        finally {
            logger.log(Level.INFO, "EXIT SearchResultsAndTopicPresenter.bindSplitPanelResize()");
        }
    }

    /**
     * Add behaviour to the tag delete buttons
     */
    private void bindTagEditingButtons() {

        try {
            logger.log(Level.INFO, "ENTER SearchResultsAndTopicPresenter.bindTagEditingButtons()");

            /* This will be null if the tags have not been downloaded */
            if (topicTagsComponent.getDisplay().getEditor() != null) {
                for (final TopicTagViewProjectEditor topicTagViewProjectEditor : topicTagsComponent.getDisplay().getEditor().projects
                        .getEditors()) {
                    for (final TopicTagViewCategoryEditor topicTagViewCategoryEditor : topicTagViewProjectEditor.categories
                            .getEditors()) {
                        for (final TopicTagViewTagEditor topicTagViewTagEditor : topicTagViewCategoryEditor.myTags.getEditors()) {
                            topicTagViewTagEditor.getDelete().addClickHandler(
                                    new DeleteTagClickHandler(topicTagViewTagEditor.getTag().getTag()));
                        }
                    }
                }
            }
        } finally {
            logger.log(Level.INFO, "EXIT SearchResultsAndTopicPresenter.bindTagEditingButtons()");
        }
    }

    /**
     * Bind behaviour to the view buttons in the topic revisions cell table
     */
    private void bindViewTopicRevisionButton() {
        topicRevisionsComponent.getDisplay().getDiffButton().setFieldUpdater(new FieldUpdater<RESTTopicCollectionItemV1, String>() {
            @Override
            public void update(final int index, final RESTTopicCollectionItemV1 revisionTopic, final String value) {
                final RESTCalls.RESTCallback<RESTTopicV1> callback = new BaseRestCallback<RESTTopicV1, TopicRevisionsPresenter.Display>(
                        topicRevisionsComponent.getDisplay(),
                        new BaseRestCallback.SuccessAction<RESTTopicV1, TopicRevisionsPresenter.Display>() {
                            @Override
                            public void doSuccessAction(final RESTTopicV1 retValue,
                                                        final TopicRevisionsPresenter.Display display) {
                                final RESTTopicCollectionItemV1 sourceTopic = getTopicOrRevisionTopic();
                                final String retValueLabel = PressGangCCMSUI.INSTANCE.TopicID()
                                        + ": "
                                        + retValue.getId()
                                        + " "
                                        + PressGangCCMSUI.INSTANCE.TopicRevision()
                                        + ": "
                                        + retValue.getRevision().toString()
                                        + " "
                                        + PressGangCCMSUI.INSTANCE.RevisionDate()
                                        + ": "
                                        + DateTimeFormat.getFormat(PredefinedFormat.DATE_FULL).format(
                                        retValue.getLastModified());

                                final String sourceTopicLabel = PressGangCCMSUI.INSTANCE.TopicID()
                                        + ": "
                                        + sourceTopic.getItem().getId()
                                        + " "
                                        + PressGangCCMSUI.INSTANCE.TopicRevision()
                                        + ": "
                                        + sourceTopic.getItem().getRevision().toString()
                                        + " "
                                        + PressGangCCMSUI.INSTANCE.RevisionDate()
                                        + ": "
                                        + DateTimeFormat.getFormat(PredefinedFormat.DATE_FULL).format(
                                        sourceTopic.getItem().getLastModified());

                                /* See if the topic contains valid XML or not */
                                boolean isXML = true;
                                try {
                                    XMLParser.parse(sourceTopic.getItem().getXml());
                                } catch (final DOMParseException ex) {
                                    isXML = false;
                                }

                                topicRevisionsComponent.displayDiff(retValue.getXml(), retValueLabel, sourceTopic.getItem()
                                        .getXml(), sourceTopicLabel, isXML);
                            }
                        }) {

                };
                RESTCalls.getTopicRevision(callback, revisionTopic.getItem().getId(), revisionTopic.getItem().getRevision());
            }
        });

        topicRevisionsComponent.getDisplay().getViewButton().setFieldUpdater(new FieldUpdater<RESTTopicCollectionItemV1, String>() {
            @Override
            public void update(final int index, final RESTTopicCollectionItemV1 revisionTopic, final String value) {

                /* Reset the reference to the revision topic */
                topicRevisionsComponent.getDisplay().setRevisionTopic(null);

                if (!revisionTopic.getItem().getRevision()
                        .equals(searchResultsComponent.getProviderData().getDisplayedItem().getItem().getRevision())) {
                    /* Reset the reference to the revision topic */
                    topicRevisionsComponent.getDisplay().setRevisionTopic(revisionTopic);
                }

                /* Load the tags and bugs */
                loadTagsAndBugs();

                initializeViews();
                topicRevisionsComponent.getDisplay().setProvider(generateTopicRevisionsListProvider());
                switchView(topicRevisionsComponent.getDisplay());
            }
        });
    }

    @Override
    public boolean hasUnsavedChanges() {

        /* No topic selected, so no changes need to be saved */
        if (this.searchResultsComponent.getProviderData().getDisplayedItem() == null)  {
            return false;
        }

        /* if there is no selected item, we are trying to save a new topic */
        if (this.searchResultsComponent.getProviderData().getSelectedItem() == null) {
            return true;
        }

        /* Save any pending changes */
        flushChanges();

        final RESTTopicV1 displayedTopic = this.searchResultsComponent.getProviderData().getDisplayedItem().getItem();
        final RESTTopicV1 selectedTopic = this.searchResultsComponent.getProviderData().getSelectedItem().getItem();

        boolean unsavedChanges = false;

        /* If there are any modified tags in newTopic, we have unsaved changes */
        if (!displayedTopic.getTags().returnDeletedAddedAndUpdatedCollectionItems().isEmpty()) {
            unsavedChanges = true;
        }

        /*
         * If any values in selectedTopic don't match displayedTopic, we have unsaved changes
         */
        if (!GWTUtilities.stringEqualsEquatingNullWithEmptyString(selectedTopic.getTitle(), displayedTopic.getTitle()))
            unsavedChanges = true;
        if (!GWTUtilities.stringEqualsEquatingNullWithEmptyString(selectedTopic.getLocale(), displayedTopic.getLocale()))
            unsavedChanges = true;
        if (!GWTUtilities.stringEqualsEquatingNullWithEmptyString(selectedTopic.getDescription(),
                displayedTopic.getDescription()))
            unsavedChanges = true;
        if (!GWTUtilities.stringEqualsEquatingNullWithEmptyString(selectedTopic.getXml(), displayedTopic.getXml()))
            unsavedChanges = true;

        return unsavedChanges;
    }

    @Override
    protected void loadAdditionalDisplayedItemData() {

        try {
            logger.log(Level.INFO, "ENTER SearchResultsAndTopicPresenter.loadAdditionalDisplayedItemData()");

            /* Display the tags that are added to the category */
            topicPropertyTagPresenter.refreshExistingChildList(searchResultsComponent.getProviderData().getDisplayedItem().getItem());

            /* Get a new collection of tags */
            //topicPropertyTagPresenter.refreshPossibleChildrenDataAndList();

            /* reset the topic review view */
            topicRevisionsComponent.getDisplay().setRevisionTopic(null);

            /* set the revisions to show the loading widget */
            if (topicRevisionsComponent.getDisplay().getProvider() != null) {
                topicRevisionsComponent.getDisplay().getProvider().resetProvider();
            }

            /* if searchResultsComponent.getProviderData().getSelectedItem() == null, then we are displaying a new topic */
            if (searchResultsComponent.getProviderData().getSelectedItem() != null)
            {
                /* A callback to respond to a request for a topic with the revisions expanded */
                final RESTCalls.RESTCallback<RESTTopicV1> topicWithRevisionsCallback = new BaseRestCallback<RESTTopicV1, TopicRevisionsPresenter.Display>(
                        topicRevisionsComponent.getDisplay(), new BaseRestCallback.SuccessAction<RESTTopicV1, TopicRevisionsPresenter.Display>() {
                    @Override
                    public void doSuccessAction(final RESTTopicV1 retValue, final TopicRevisionsPresenter.Display display) {
                        searchResultsComponent.getProviderData().getDisplayedItem().getItem()
                                .setRevisions(retValue.getRevisions());

                                /* refresh the list */
                        topicRevisionsComponent.getDisplay().getProvider().displayNewFixedList(retValue.getRevisions().getItems());
                    }
                });

                RESTCalls.getTopicWithRevisions(topicWithRevisionsCallback, searchResultsComponent.getProviderData()
                        .getSelectedItem().getItem().getId());

                /* got on to load the tags and bugs */
                loadTagsAndBugs();
            }



            /* fix the rendered view buttons */
            initializeSplitViewButtons();
        } finally {
            logger.log(Level.INFO, "EXIT SearchResultsAndTopicPresenter.loadAdditionalDisplayedItemData()");
        }

    }

    /**
     * The tags and bugs for a topic are loaded as separate operations to minimize the amount of data initially sent when a
     * topic is displayed.
     *
     * We pull down the extended collections from a revision, just to make sure that the collections we are getting are for
     * the entity we are viewing, since there is a slight chance that a new revision could be saved in between us loading
     * the empty entity and then loading the collections.
     */
    private void loadTagsAndBugs() {
        /* set the bugs to show the loading widget */
        /*if (topicBugsPresenter.getDisplay().getProvider() != null) {
            topicBugsPresenter.getDisplay().getProvider().resetProvider();
        }*/

        /* clear the tags display */
        initializeViews(Arrays.asList(new BaseTopicViewInterface[]{topicTagsComponent.getDisplay()}));

        /* A callback to respond to a request for a topic with the tags expanded */
        final RESTCalls.RESTCallback<RESTTopicV1> topicWithTagsCallback = new BaseRestCallback<RESTTopicV1, TopicTagsPresenter.Display>(
                topicTagsComponent.getDisplay(), new BaseRestCallback.SuccessAction<RESTTopicV1, TopicTagsPresenter.Display>() {
            @Override
            public void doSuccessAction(final RESTTopicV1 retValue, final TopicTagsPresenter.Display display) {

                /* copy the revisions into the displayed Topic */
                getTopicOrRevisionTopic().getItem().setTags(retValue.getTags());

                /* update the view */
                initializeViews(Arrays.asList(new BaseTopicViewInterface[]{topicTagsComponent.getDisplay()}));
            }
        });

        /* A callback to respond to a request for a topic with the bugzilla bugs expanded */
        /*final RESTCalls.RESTCallback<RESTTopicV1> topicWithBugsCallback = new BaseRestCallback<RESTTopicV1, TopicBugsPresenter.Display>(
                topicBugsPresenter.getDisplay(), new BaseRestCallback.SuccessAction<RESTTopicV1, TopicBugsPresenter.Display>() {
            @Override
            public void doSuccessAction(final RESTTopicV1 retValue, final TopicBugsPresenter.Display display) {
                final RESTBugzillaBugCollectionV1 collection = retValue.getBugzillaBugs_OTM();
                // copy the revisions into the displayed Topic
                getTopicOrRevisionTopic().getItem().setBugzillaBugs_OTM(collection);
                // refresh the celltable
                topicBugsPresenter.getDisplay().getProvider().displayNewFixedList(collection.getItems());
            }
        }) {

        };*/

        /* Initiate the REST calls */
        final Integer id = getTopicOrRevisionTopic().getItem().getId();
        final Integer revision = getTopicOrRevisionTopic().getItem().getRevision();

        //RESTCalls.getTopicRevisionWithBugs(topicWithBugsCallback, id, revision);
        RESTCalls.getTopicRevisionWithTags(topicWithTagsCallback, id, revision);
    }

    @Override
    protected void switchView(final BaseTopicViewInterface displayedView) {
        try {
            logger.log(Level.INFO, "ENTER SearchResultsAndTopicPresenter.switchView(final TopicViewInterface displayedView)");

            super.switchView(displayedView);

            /* Save any changes to the xml editor */
            if (lastDisplayedView == this.topicXMLComponent.getDisplay()) {
                this.topicXMLComponent.getDisplay().getDriver().flush();
            }

            /* Refresh the rendered view (when there is no page splitting) */
            if (displayedView == this.topicRenderedPresenter.getDisplay()) {
                topicRenderedPresenter.getDisplay().initialize(getTopicOrRevisionTopic().getItem(), isReadOnlyMode(), NEW_TOPIC,
                        display.getSplitType(), locales, true);
            }
            /* Set the projects combo box as the focsed element */
            else if (displayedView == this.topicTagsComponent.getDisplay()) {
                if (topicTagsComponent.getDisplay().getProjectsList().isAttached()) {
                    topicTagsComponent.getDisplay().getProjectsList().getElement().focus();
                }
            }

            /* Update the page name */
            final StringBuilder title = new StringBuilder(displayedView.getPageName());
            if (this.searchResultsComponent.getProviderData().getDisplayedItem() != null) {
                title.append(": [" + searchResultsComponent.getProviderData().getDisplayedItem().getItem().getId() + "] "
                        + searchResultsComponent.getProviderData().getDisplayedItem().getItem().getTitle());
            }
            display.getPageTitle().setText(title.toString());

            /* While editing the XML, we need to setup a refresh of the rendered view */
            if (displayedView == topicXMLComponent.getDisplay() && display.getSplitType() != SplitType.NONE && !isReadOnlyMode()) {
                timer.scheduleRepeating(Constants.REFRESH_RATE);
            } else {
                timer.cancel();
                refreshRenderedView(true);
            }

            BaseTopicCombinedViewPresenter.setHelpTopicForView(this, displayedView);

            lastDisplayedView = displayedView;
        } finally {
            logger.log(Level.INFO, "EXIT SearchResultsAndTopicPresenter.switchView(final TopicViewInterface displayedView)");
        }
    }

    @Override
    protected void bindActionButtons() {
        final ClickHandler createClickHanlder = new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {

                /* make sure there are no unsaved changes, or that the user is happy to continue without saving */
                if (!isOKToProceed()) {
                    return;
                }

                // Create the topic wrapper
                final RESTTopicCollectionItemV1 topicCollectionItem = new RESTTopicCollectionItemV1();
                topicCollectionItem.setState(RESTBaseCollectionItemV1.ADD_STATE);

                // create the topic, and add to the wrapper
                final RESTTopicV1 restTopic = new RESTTopicV1();
                restTopic.setProperties(new RESTAssignedPropertyTagCollectionV1());
                restTopic.setTags(new RESTTagCollectionV1());
                restTopic.setRevisions(new RESTTopicCollectionV1());
                topicCollectionItem.setItem(restTopic);

                // the topic won't show up in the list of topics until it is saved, so the
                // selected item is null
                searchResultsComponent.getProviderData().setSelectedItem(null);

                // the new topic is being displayed though, so we set the displayed item
                searchResultsComponent.getProviderData().setDisplayedItem(topicCollectionItem);

                updateViewsAfterNewEntityLoaded();
            }
        };

        /* Build up a click handler to save the topic */
        final ClickHandler saveClickHandler = new ClickHandler() {
            @Override
            public void onClick(final ClickEvent event) {

                try {
                    logger.log(Level.INFO,
                            "ENTER SearchResultsAndTopicPresenter.bindActionButtons() saveClickHandler.onClick()");

                    if (hasUnsavedChanges()) {
                        display.getMessageLogDialog().getDialogBox().center();
                        display.getMessageLogDialog().getDialogBox().show();
                    } else {
                        Window.alert(PressGangCCMSUI.INSTANCE.NoUnsavedChanges());
                    }
                } finally {
                    logger.log(Level.INFO, "EXIT SearchResultsAndTopicPresenter.bindActionButtons() saveClickHandler.onClick()");
                }
            }
        };

        /* Hook up the dialog box buttons */
        display.getMessageLogDialog().getOk().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(final ClickEvent event) {

                if (searchResultsComponent.getProviderData().getDisplayedItem() != null) {

                    if (searchResultsComponent.getProviderData().getDisplayedItem().returnIsAddItem())
                    {
                        final BaseRestCallback<RESTTopicV1, Display> addCallback = new BaseRestCallback<RESTTopicV1, Display>(
                                display,
                                new BaseRestCallback.SuccessAction<RESTTopicV1, Display>() {
                                    @Override
                                    public void doSuccessAction(final RESTTopicV1 retValue, final Display display) {
                                        try {
                                            // Create the topic wrapper
                                            final RESTTopicCollectionItemV1 topicCollectionItem = new RESTTopicCollectionItemV1();
                                            topicCollectionItem.setState(RESTBaseCollectionItemV1.UNCHANGED_STATE);

                                            // create the topic, and add to the wrapper
                                            final RESTTopicV1 restTopic = new RESTTopicV1();
                                            topicCollectionItem.setItem(retValue);

                                            /* Update the displayed topic */
                                            searchResultsComponent.getProviderData().setDisplayedItem(topicCollectionItem.clone(true));
                                            /* Update the selected topic */
                                            searchResultsComponent.getProviderData().setSelectedItem(topicCollectionItem);

                                            lastXML = null;

                                            initializeViews();

                                            updateDisplayAfterSave(true);

                                            Window.alert(PressGangCCMSUI.INSTANCE.TopicSaveSuccessWithID() + " " + retValue.getId());
                                        } finally {
                                            if (topicXMLComponent.getDisplay().getEditor() != null) {
                                                topicXMLComponent.getDisplay().getEditor().redisplay();
                                            }
                                        }
                                    }
                                }, new BaseRestCallback.FailureAction<Display>() {
                            @Override
                            public void doFailureAction(final Display display) {
                                topicXMLComponent.getDisplay().getEditor().redisplay();
                            }
                        }
                        );

                        /* Sync any changes back to the underlying object */
                        flushChanges();

                        /*
                         * Create a new instance of the topic, with all the properties set to explicitly update
                         */
                        final RESTTopicV1 addedTopic = searchResultsComponent.getProviderData().getDisplayedItem().getItem()
                                .clone(true);

                        addedTopic.explicitSetProperties(addedTopic.getProperties());
                        addedTopic.explicitSetSourceUrls_OTM(addedTopic.getSourceUrls_OTM());
                        addedTopic.explicitSetTags(addedTopic.getTags());
                        addedTopic.explicitSetDescription(addedTopic.getDescription());
                        addedTopic.explicitSetLocale(addedTopic.getLocale());
                        addedTopic.explicitSetTitle(addedTopic.getTitle());
                        addedTopic.explicitSetXml(addedTopic.getXml());

                        final String message = display.getMessageLogDialog().getMessage().getText();
                        RESTCalls.createTopic(addCallback, addedTopic, message, (int) ServiceConstants.MAJOR_CHANGE, ServiceConstants.NULL_USER_ID.toString());
                    }
                    else
                    {
                        final BaseRestCallback<RESTTopicV1, Display> updateCallback = new BaseRestCallback<RESTTopicV1, Display>(
                                display,
                                new BaseRestCallback.SuccessAction<RESTTopicV1, Display>() {
                                    @Override
                                    public void doSuccessAction(final RESTTopicV1 retValue, final Display display) {
                                        try {

                                            boolean overwroteChanges = false;

                                            if (retValue.getRevisions() != null && retValue.getRevisions().getItems() != null
                                                    && retValue.getRevisions().getItems().size() >= 2) {
                                                Collections.sort(retValue.getRevisions().getItems(),
                                                        new RESTTopicCollectionItemV1RevisionSort());
                                                /* Get the second last revision (the last one is the current one) */
                                                final Integer overwriteRevision = retValue.getRevisions().getItems()
                                                        .get(retValue.getRevisions().getItems().size() - 2).getItem().getRevision();
                                                /*
                                                 * if the second last revision doesn't match the revision of the topic when editing was
                                                 * started, then we have overwritten someone elses changes
                                                 */
                                                overwroteChanges = !(searchResultsComponent.getProviderData().getSelectedItem().getItem()
                                                        .getRevision().equals(overwriteRevision));
                                            }

                                            /* Update the displayed topic */
                                            retValue.cloneInto(searchResultsComponent.getProviderData().getDisplayedItem().getItem(),
                                                    true);
                                            /* Update the selected topic */
                                            retValue.cloneInto(searchResultsComponent.getProviderData().getSelectedItem().getItem(), true);

                                            lastXML = null;

                                            initializeViews();

                                            updateDisplayAfterSave(false);

                                            if (overwroteChanges) {
                                                Window.alert(PressGangCCMSUI.INSTANCE.OverwriteSuccess());
                                            } else {
                                                Window.alert(PressGangCCMSUI.INSTANCE.SaveSuccess());
                                            }
                                        } finally {
                                            if (topicXMLComponent.getDisplay().getEditor() != null) {
                                                topicXMLComponent.getDisplay().getEditor().redisplay();
                                            }
                                        }
                                    }
                                }, new BaseRestCallback.FailureAction<Display>() {
                                    @Override
                                    public void doFailureAction(final Display display) {
                                        topicXMLComponent.getDisplay().getEditor().redisplay();
                                    }
                                }
                        );

                        /* Sync any changes back to the underlying object */
                        flushChanges();

                        /*
                         * Create a new instance of the topic, with all the properties set to explicitly update
                         */
                        final RESTTopicV1 updateTopic = searchResultsComponent.getProviderData().getDisplayedItem().getItem()
                                .clone(true);

                        updateTopic.explicitSetProperties(updateTopic.getProperties());
                        updateTopic.explicitSetSourceUrls_OTM(updateTopic.getSourceUrls_OTM());
                        updateTopic.explicitSetTags(updateTopic.getTags());
                        updateTopic.explicitSetDescription(updateTopic.getDescription());
                        updateTopic.explicitSetLocale(updateTopic.getLocale());
                        updateTopic.explicitSetTitle(updateTopic.getTitle());
                        updateTopic.explicitSetXml(updateTopic.getXml());

                        final String message = display.getMessageLogDialog().getMessage().getText();
                        final Integer flag = (int) (display.getMessageLogDialog().getMinorChange().getValue() ? ServiceConstants.MINOR_CHANGE
                                : ServiceConstants.MAJOR_CHANGE);
                        RESTCalls.saveTopic(updateCallback, updateTopic, message, flag, ServiceConstants.NULL_USER_ID.toString());
                    }
                }

                display.getMessageLogDialog().reset();
                display.getMessageLogDialog().getDialogBox().hide();
            }
        });

        display.getMessageLogDialog().getCancel().addClickHandler(new ClickHandler() {

            @Override
            public void onClick(final ClickEvent event) {
                display.getMessageLogDialog().reset();
                display.getMessageLogDialog().getDialogBox().hide();
            }
        });

        final ClickHandler topicViewClickHandler = new ClickHandler() {
            @Override
            public void onClick(final ClickEvent event) {
                /* Sync any changes back to the underlying object */
                flushChanges();

                if (searchResultsComponent.getProviderData().getDisplayedItem() != null) {
                    switchView(topicViewComponent.getDisplay());
                }
            }
        };

        final ClickHandler topicPropertyTagsClickHandler = new ClickHandler() {
            @Override
            public void onClick(final ClickEvent event) {
                /* Sync any changes back to the underlying object */
                flushChanges();

                if (searchResultsComponent.getProviderData().getDisplayedItem() != null) {
                    switchView(topicPropertyTagPresenter.getDisplay());
                }
            }
        };

        final ClickHandler topicXMLClickHandler = new ClickHandler() {
            @Override
            public void onClick(final ClickEvent event) {
                /* Sync any changes back to the underlying object */
                flushChanges();

                if (searchResultsComponent.getProviderData().getDisplayedItem() != null) {
                    switchView(topicXMLComponent.getDisplay());

                }
            }
        };

        final ClickHandler topicRenderedClickHandler = new ClickHandler() {
            @Override
            public void onClick(final ClickEvent event) {
                /* Sync any changes back to the underlying object */
                flushChanges();

                if (searchResultsComponent.getProviderData().getDisplayedItem() != null) {
                    switchView(topicRenderedPresenter.getDisplay());
                }
            }
        };

        final ClickHandler topicXMLErrorsClickHandler = new ClickHandler() {
            @Override
            public void onClick(final ClickEvent event) {
                /* Sync any changes back to the underlying object */
                flushChanges();

                if (searchResultsComponent.getProviderData().getDisplayedItem() != null) {
                    switchView(topicXMLErrorsPresenter.getDisplay());
                }
            }
        };

        final ClickHandler topicTagsClickHandler = new ClickHandler() {
            @Override
            public void onClick(final ClickEvent event) {
                /* Sync any changes back to the underlying object */
                flushChanges();

                if (searchResultsComponent.getProviderData().getDisplayedItem() != null) {
                    switchView(topicTagsComponent.getDisplay());
                }
            }
        };

        final ClickHandler topicBugsClickHandler = new ClickHandler() {
            @Override
            public void onClick(final ClickEvent event) {
                /* Sync any changes back to the underlying object */
                flushChanges();

                if (searchResultsComponent.getProviderData().getDisplayedItem() != null) {
                    switchView(topicBugsPresenter.getDisplay());
                }
            }
        };

        final ClickHandler topicRevisionsClickHanlder = new ClickHandler() {
            @Override
            public void onClick(final ClickEvent event) {
                /* Sync any changes back to the underlying object */
                flushChanges();

                if (searchResultsComponent.getProviderData().getDisplayedItem() != null) {
                    switchView(topicRevisionsComponent.getDisplay());
                }
            }
        };

        final ClickHandler splitMenuHandler = new ClickHandler() {
            @Override
            public void onClick(final ClickEvent event) {
                /* Sync any changes back to the underlying object */
                flushChanges();

                showRenderedSplitPanelMenu();
            }
        };

        final ClickHandler splitMenuCloseHandler = new ClickHandler() {
            @Override
            public void onClick(final ClickEvent event) {
                /* Sync any changes back to the underlying object */
                flushChanges();

                showRegularMenu();
            }
        };

        final ClickHandler splitMenuNoSplitHandler = new ClickHandler() {
            @Override
            public void onClick(final ClickEvent event) {
                /* Sync any changes back to the underlying object */
                flushChanges();

                Preferences.INSTANCE.saveSetting(Preferences.TOPIC_RENDERED_VIEW_SPLIT_TYPE,
                        Preferences.TOPIC_RENDERED_VIEW_SPLIT_NONE);

                timer.cancel();

                initializeDisplay();
                initializeSplitViewButtons();
            }
        };

        final ClickHandler splitMenuVSplitHandler = new ClickHandler() {
            @Override
            public void onClick(final ClickEvent event) {
                /* Sync any changes back to the underlying object */
                flushChanges();

                Preferences.INSTANCE.saveSetting(Preferences.TOPIC_RENDERED_VIEW_SPLIT_TYPE,
                        Preferences.TOPIC_RENDERED_VIEW_SPLIT_VERTICAL);

                timer.scheduleRepeating(Constants.REFRESH_RATE);

                initializeDisplay();
                initializeSplitViewButtons();

                if (lastDisplayedView == topicRenderedPresenter.getDisplay()) {
                    switchView(topicXMLComponent.getDisplay());
                    showRenderedSplitPanelMenu();
                }
            }
        };

        final ClickHandler splitMenuHSplitHandler = new ClickHandler() {
            @Override
            public void onClick(final ClickEvent event) {
                /* Sync any changes back to the underlying object */
                flushChanges();

                Preferences.INSTANCE.saveSetting(Preferences.TOPIC_RENDERED_VIEW_SPLIT_TYPE,
                        Preferences.TOPIC_RENDERED_VIEW_SPLIT_HOIRZONTAL);

                timer.scheduleRepeating(Constants.REFRESH_RATE);

                initializeDisplay();
                initializeSplitViewButtons();

                if (lastDisplayedView == topicRenderedPresenter.getDisplay()) {
                    switchView(topicXMLComponent.getDisplay());
                    showRenderedSplitPanelMenu();
                }
            }
        };

        final ClickHandler cspsHandler = new ClickHandler() {

            @Override
            public void onClick(final ClickEvent event) {

                if (searchResultsComponent.getProviderData().getDisplayedItem() != null && isOKToProceed()) {

                    final RESTTopicV1 topic = searchResultsComponent.getProviderData().getDisplayedItem().getItem();

                    eventBus.fireEvent(new SearchResultsAndTopicViewEvent(Constants.QUERY_PATH_SEGMENT_PREFIX
                            + org.jboss.pressgang.ccms.utils.constants.CommonFilterConstants.TOPIC_XML_FILTER_VAR + "="
                            + topic.getTitle() + " [" + topic.getId() + "];tag" + ServiceConstants.CSP_TAG_ID + "=1;logic=AND",
                            GWTUtilities.isEventToOpenNewWindow(event)));
                }

            }
        };

        /* Hook up the click listeners */
        display.getRenderedSplit().addClickHandler(splitMenuHandler);
        display.getFields().addClickHandler(topicViewClickHandler);
        display.getExtendedProperties().addClickHandler(topicPropertyTagsClickHandler);
        display.getXml().addClickHandler(topicXMLClickHandler);
        display.getRendered().addClickHandler(topicRenderedClickHandler);
        display.getSave().addClickHandler(saveClickHandler);
        display.getXmlErrors().addClickHandler(topicXMLErrorsClickHandler);
        display.getTopicTags().addClickHandler(topicTagsClickHandler);
        display.getBugs().addClickHandler(topicBugsClickHandler);
        display.getHistory().addClickHandler(topicRevisionsClickHanlder);
        display.getCsps().addClickHandler(cspsHandler);

        display.getRenderedSplitOpen().addClickHandler(splitMenuCloseHandler);
        display.getRenderedSplitClose().addClickHandler(splitMenuCloseHandler);
        display.getRenderedNoSplit().addClickHandler(splitMenuNoSplitHandler);
        display.getRenderedVerticalSplit().addClickHandler(splitMenuVSplitHandler);
        display.getRenderedHorizontalSplit().addClickHandler(splitMenuHSplitHandler);

        searchResultsComponent.getDisplay().getCreate().addClickHandler(createClickHanlder);

        /* Hook up the xml editor buttons */
        topicXMLComponent.getDisplay().getLineWrap().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(final ClickEvent event) {
                topicXMLComponent.getDisplay().getEditor().setUseWrapMode(topicXMLComponent.getDisplay().getLineWrap().isDown());
            }
        });

        topicXMLComponent.getDisplay().getShowInvisibles().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(final ClickEvent event) {
                topicXMLComponent.getDisplay().getEditor().setShowInvisibles(topicXMLComponent.getDisplay().getShowInvisibles().isDown());
            }
        });

        BaseTopicCombinedViewPresenter.addKeyboardShortcutEvents(topicXMLComponent.getDisplay(), display);
    }

    @Override
    protected void bindFilteredResultsButtons() {
        // TODO Auto-generated method stub

    }

    @Override
    protected void initializeViews(final List<BaseTopicViewInterface> filter) {

        try {
            logger.log(Level.INFO,
                    "ENTER SearchResultsAndTopicPresenter.initializeViews(final List<TopicViewInterface> filter)");

            logger.log(Level.INFO, "\tInitializing topic views");

            for (final BaseTopicViewInterface view : new BaseTopicViewInterface[]{topicViewComponent.getDisplay(), topicXMLComponent.getDisplay(),
                    topicRenderedPresenter.getDisplay(), topicXMLErrorsPresenter.getDisplay(), topicTagsComponent.getDisplay(), topicBugsPresenter.getDisplay(),
                    topicSplitPanelRenderedDisplay, topicPropertyTagPresenter.getDisplay()}) {
                if (viewIsInFilter(filter, view)) {

                    final RESTTopicCollectionItemV1 topicToDisplay = getTopicOrRevisionTopic();

                    view.initialize(topicToDisplay.getItem(), isReadOnlyMode(), NEW_TOPIC, this.split, locales, false);
                }
            }

            if (viewIsInFilter(filter, topicRevisionsComponent.getDisplay())) {
                logger.log(Level.INFO, "\tInitializing topic revisions view");
                topicRevisionsComponent.getDisplay().initialize(searchResultsComponent.getProviderData().getDisplayedItem().getItem(),
                        isReadOnlyMode(), NEW_TOPIC, display.getSplitType(), locales, false);
            }

            if (viewIsInFilter(filter, topicTagsComponent.getDisplay())) {
                bindTagEditingButtons();
            }

            /* Redisplay the editor. topicXMLComponent.getDisplay().getEditor() will be not null after the initialize method was called above */
            if (viewIsInFilter(filter, topicXMLComponent.getDisplay())) {
                logger.log(Level.INFO, "\tSetting topic XML edit button state and redisplaying ACE editor");
                setXMLEditorButtonsToEditorState();
                topicXMLComponent.getDisplay().getEditor().redisplay();
            }

        } finally {
            logger.log(Level.INFO, "EXIT SearchResultsAndTopicPresenter.initializeViews(final List<TopicViewInterface> filter)");
        }

    }

    private void initializeSplitViewButtons() {
        /* fix the rendered view button */
        display.buildSplitViewButtons(split);
    }

    /**
     * The interface that defines the top level topic list and edit view
     */
    public interface Display extends
            BaseSearchAndEditViewInterface<RESTTopicV1, RESTTopicCollectionV1, RESTTopicCollectionItemV1> {

        FlexTable getRenderedSplitViewMenu();

        PushButton getRenderedSplitOpen();

        PushButton getRenderedHorizontalSplit();

        PushButton getRenderedSplitClose();

        PushButton getRenderedVerticalSplit();

        PushButton getRenderedNoSplit();

        PushButton getRenderedSplit();

        /**
         * @return The button that is used to switch to the history view
         */
        PushButton getHistory();

        /**
         * @return The button that is used to switch to the rendered view
         */
        PushButton getRendered();

        /**
         *
         * @return The button that is used to switch to the XML view
         */
        PushButton getXml();

        /**
         *
         * @return The button that is used to switch to the topic fields view
         */
        PushButton getFields();

        /**
         *
         * @return The button that is used to switch to the topic property tags view
         */
        PushButton getExtendedProperties();

        /**
         *
         * @return The button that is used to save the topic
         */
        PushButton getSave();

        /**
         *
         * @return The button that is used to switch to the XML errors view
         */
        PushButton getXmlErrors();

        /**
         *
         * @return The button that is used to switch to the tags view
         */
        PushButton getTopicTags();

        /**
         *
         * @return The button that is used to switch to the bugs view
         */
        PushButton getBugs();

        /**
         *
         * @return The button this is used to match topics to csps
         */
        PushButton getCsps();

        /** Show the rendered split view menu */
        void showSplitViewButtons();

        /**
         * Rebuild the split view buttons
         * @param splitType The screen split
         */
        void buildSplitViewButtons(final SplitType splitType);

        LogMessageInterface getMessageLogDialog();

        SplitType getSplitType();

        void initialize(final boolean readOnly, final boolean hasErrors, final SplitType splitType, final Panel panel);
    }

    /**
     * A click handler to add a tag to a topic
     *
     * @author Matthew Casperson
     */
    private class AddTagClickhandler implements ClickHandler {

        @Override
        public void onClick(final ClickEvent event) {
            final RESTTagV1 selectedTag = topicTagsComponent.getDisplay().getMyTags().getValue().getTag().getItem();

            /* Need to deal with re-adding removed tags */
            RESTTagCollectionItemV1 deletedTag = null;
            for (final RESTTagCollectionItemV1 tag : searchResultsComponent.getProviderData().getDisplayedItem().getItem()
                    .getTags().getItems()) {
                if (tag.getItem().getId().equals(selectedTag.getId())) {
                    if (RESTBaseCollectionItemV1.REMOVE_STATE.equals(tag.getState())) {
                        deletedTag = tag;
                        break;
                    } else {
                        /* Don't add tags twice */
                        Window.alert(PressGangCCMSUI.INSTANCE.TagAlreadyExists());
                        return;
                    }
                }
            }

            /*
             * If we get this far we are adding a tag to the topic. However, some categories are mutually exclusive, so we need
             * to remove any conflicting tags.
             */

            /* Find the mutually exclusive categories that the new tag belongs to */
            final Collection<RESTCategoryInTagCollectionItemV1> mutuiallyExclusiveCategories = Collections2.filter(selectedTag
                    .getCategories().getItems(), new Predicate<RESTCategoryInTagCollectionItemV1>() {

                @Override
                public boolean apply(final @Nullable RESTCategoryInTagCollectionItemV1 arg0) {
                    if (arg0 == null || arg0.getItem() == null)
                        return false;
                    return arg0.getItem().getMutuallyExclusive();
                }
            });

            /* Find existing tags that belong to any of the mutually exclusive categories */
            final Collection<RESTTagCollectionItemV1> conflictingTags = Collections2.filter(searchResultsComponent
                    .getProviderData().getDisplayedItem().getItem().getTags().getItems(),
                    new Predicate<RESTTagCollectionItemV1>() {

                        @Override
                        public boolean apply(final @Nullable RESTTagCollectionItemV1 existingTag) {

                            /* there is no match if the tag has already been removed */
                            if (existingTag == null || existingTag.getItem() == null
                                    || RESTBaseCollectionItemV1.REMOVE_STATE.equals(existingTag.getState())) {
                                return false;
                            }

                            /* loop over the categories that the tag belongs to */
                            return Iterables.any(existingTag.getItem().getCategories().getItems(),
                                    new Predicate<RESTCategoryInTagCollectionItemV1>() {

                                        @Override
                                        public boolean apply(
                                                final @Nullable RESTCategoryInTagCollectionItemV1 existingTagCategory) {
                                            if (existingTagCategory == null || existingTagCategory.getItem() == null)
                                                return false;

                                            /*
                                             * match any categories that the tag belongs to with any of the mutually exclusive
                                             * categories
                                             */
                                            return Iterables.any(mutuiallyExclusiveCategories,
                                                    new Predicate<RESTCategoryInTagCollectionItemV1>() {

                                                        @Override
                                                        public boolean apply(
                                                                final @Nullable RESTCategoryInTagCollectionItemV1 mutuallyExclusiveCategory) {
                                                            return mutuallyExclusiveCategory.getItem().getId()
                                                                    .equals(existingTagCategory.getItem().getId());
                                                        }
                                                    });

                                        }
                                    });
                        }
                    });

            if (!conflictingTags.isEmpty()) {
                final StringBuilder tags = new StringBuilder("\n");
                for (final RESTTagCollectionItemV1 tag : conflictingTags) {
                    tags.append("\n* " + tag.getItem().getName());
                }

                /* make sure the user is happy to remove the conflicting tags */
                if (!Window.confirm(PressGangCCMSUI.INSTANCE.RemoveConflictingTags() + tags.toString()))
                    return;

                for (final RESTTagCollectionItemV1 tag : conflictingTags) {
                    tag.setState(RESTBaseCollectionItemV1.REMOVE_STATE);
                }
            }

            if (deletedTag == null) {
                /* Get the selected tag, and clone it */
                final RESTTagV1 selectedTagClone = selectedTag.clone(true);
                /* Add the tag to the topic */
                searchResultsComponent.getProviderData().getDisplayedItem().getItem().getTags().addNewItem(selectedTagClone);
            } else {
                deletedTag.setState(RESTBaseCollectionItemV1.UNCHANGED_STATE);
            }

            /* Redisplay the view */
            initializeViews(Arrays.asList(new BaseTopicViewInterface[]{topicTagsComponent.getDisplay()}));
        }
    }

    /**
     * A click handler to remove a tag from a topic
     *
     * @author Matthew Casperson
     */
    private class DeleteTagClickHandler implements ClickHandler {
        private final RESTTagCollectionItemV1 tag;

        public DeleteTagClickHandler(final RESTTagCollectionItemV1 tag) {
            if (tag == null) {
                throw new IllegalArgumentException("tag cannot be null");
            }

            this.tag = tag;
        }

        @Override
        public void onClick(final ClickEvent event) {
            if (searchResultsComponent.getProviderData().getDisplayedItem() == null) {
                throw new IllegalStateException("searchResultsComponent.getProviderData().getDisplayedItem() cannot be null");
            }

            if (RESTBaseCollectionItemV1.ADD_STATE.equals(tag.getState())) {
                /* Tag was added and then removed, so we just delete the tag */
                searchResultsComponent.getProviderData().getDisplayedItem().getItem().getTags().getItems().remove(tag);
            } else {
                /* Otherwise we set the tag as removed */
                tag.setState(RESTBaseCollectionItemV1.REMOVE_STATE);
            }

            initializeViews(Arrays.asList(new BaseTopicViewInterface[]{topicTagsComponent.getDisplay()}));
        }
    }
}
