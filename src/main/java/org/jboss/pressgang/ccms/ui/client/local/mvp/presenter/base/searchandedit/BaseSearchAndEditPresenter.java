package org.jboss.pressgang.ccms.ui.client.local.mvp.presenter.base.searchandedit;

import com.google.gwt.editor.client.Editor;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.view.client.CellPreviewEvent;
import com.google.gwt.view.client.CellPreviewEvent.Handler;
import org.jboss.pressgang.ccms.rest.v1.collections.base.RESTBaseCollectionItemV1;
import org.jboss.pressgang.ccms.rest.v1.collections.base.RESTBaseCollectionV1;
import org.jboss.pressgang.ccms.rest.v1.entities.base.RESTBaseEntityV1;
import org.jboss.pressgang.ccms.ui.client.local.constants.Constants;
import org.jboss.pressgang.ccms.ui.client.local.constants.ServiceConstants;
import org.jboss.pressgang.ccms.ui.client.local.mvp.presenter.base.BaseTemplatePresenter;
import org.jboss.pressgang.ccms.ui.client.local.mvp.presenter.base.filteredresults.BaseFilteredResultsPresenterInterface;
import org.jboss.pressgang.ccms.ui.client.local.mvp.view.base.BasePopulatedEditorViewInterface;
import org.jboss.pressgang.ccms.ui.client.local.mvp.view.base.BaseTemplateViewInterface;
import org.jboss.pressgang.ccms.ui.client.local.mvp.view.base.filteredresults.BaseFilteredResultsViewInterface;
import org.jboss.pressgang.ccms.ui.client.local.mvp.view.base.searchandedit.BaseSearchAndEditViewInterface;
import org.jboss.pressgang.ccms.ui.client.local.preferences.Preferences;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The base class for all components adding logic to search and edit views. This view has a split screen with a filtered results
 * list on the left, and the entity details on the right, with at least one of those views being a "properties view", which
 * shows the fields associated with an entity (especially the ID).
 *
 * @param <T> The entity type
 * @param <U> The entity collection type of T
 * @param <V> The entity collection item type of T
 * @param <Y> The type of the Editor that is displayed by this component
 * @author Matthew Casperson
 */
abstract public class BaseSearchAndEditPresenter<
        T extends RESTBaseEntityV1<T, U, V>,
        U extends RESTBaseCollectionV1<T, U, V>,
        V extends RESTBaseCollectionItemV1<T, U, V>,
        Y extends Editor<T>>
        extends BaseTemplatePresenter implements BaseSearchAndEditPresenterInterface {

    /**
     * A logger
     */
    private static final Logger LOGGER = Logger.getLogger(BaseSearchAndEditPresenter.class.getName());
    /**
     * The last displayed view
     */
    protected BaseTemplateViewInterface lastDisplayedView;
    /**
     * The default view to display when an entity is selected for the first time
     */
    private BaseTemplateViewInterface firstDisplayedView;
    /**
     * The view that displays the entity properties (namely the id)
     */
    private BasePopulatedEditorViewInterface<T, T, Y> entityPropertiesView;
    /**
     * The view that displays the list of results
     */
    private BaseFilteredResultsViewInterface<V> filteredResultsDisplay;
    /**
     * The component that adds logic to the filtered results view
     */
    private BaseFilteredResultsPresenterInterface<V> filteredResultsComponent;
    /**
     * The top level display
     */
    private BaseSearchAndEditViewInterface display;

    /**
     * Unsupported method. Call bindSearchAndEdit() instead.
     *
     * @param topicId the help topic for the page
     * @param pageId  The history token of the page
     */
    public void bindExtended(final int topicId, @NotNull final String pageId) {
        throw new UnsupportedOperationException("bindExtended() is not supported. Use bindSearchAndEdit() instead.");
    }

    /**
     * @param topicId                    The topic ID used for this views help link
     * @param pageId                     The id of the page, used when submitting feedback
     * @param mainSplitSizePreferenceKey The storage key that holds the position of the split panel
     * @param firstDisplayedView         The view to display first when an entity is first selected
     * @param entityPropertiesView       The view that displays the entity fields
     * @param filteredResultsDisplay     The view that displays the search results
     * @param filteredResultsComponent   The component that backs the filtered results view
     * @param display                    The view that this component adds logic to
     * @param waitDisplay                The view that displays the wait dialog
     */
    protected final void bindSearchAndEdit(
            final int topicId,
            @NotNull final String pageId,
            @NotNull final String mainSplitSizePreferenceKey,
            @NotNull final BaseTemplateViewInterface firstDisplayedView,
            @NotNull final BasePopulatedEditorViewInterface<T, T, Y> entityPropertiesView,
            @NotNull final BaseFilteredResultsViewInterface<V> filteredResultsDisplay,
            @NotNull final BaseFilteredResultsPresenterInterface<V> filteredResultsComponent,
            @NotNull final BaseSearchAndEditViewInterface display,
            @NotNull final BaseTemplateViewInterface waitDisplay,
            @NotNull final GetNewEntityCallback<T> getNewEntityCallback) {

        super.bind(topicId, pageId, display);

        this.entityPropertiesView = entityPropertiesView;
        this.filteredResultsDisplay = filteredResultsDisplay;
        this.filteredResultsComponent = filteredResultsComponent;
        this.firstDisplayedView = firstDisplayedView;
        this.display = display;

        filteredResultsDisplay.setViewShown(true);
        display.setViewShown(true);

        display.displaySearchResultsView(filteredResultsDisplay);

        loadMainSplitResize(mainSplitSizePreferenceKey);
        bindMainSplitResize(mainSplitSizePreferenceKey);
        bindResultsListRowClicks(getNewEntityCallback);
        bindActionButtons();
        bindFilteredResultsButtons();
    }

    /**
     * Called once an entity has been saved to refresh the various lists that may have been modified by the edited or created
     * entity.
     *
     * @param wasNewEntity true if the entity that was saved was a new entity, and false otherwise
     */
    public final void updateDisplayAfterSave(final boolean wasNewEntity) {
        try {

            LOGGER.log(Level.INFO, "ENTER BaseSearchAndEditPresenter.updateDisplayAfterSave()");

            refreshFilteredResults(wasNewEntity);

            if (lastDisplayedView != null) {
                /* refresh the display */
                initializeViews();

                /* Load the data that is loaded when a new entity is selected */
                loadAdditionalDisplayedItemData();
            }
        } finally {
            LOGGER.log(Level.INFO, "EXIT BaseSearchAndEditPresenter.updateDisplayAfterSave()");
        }
    }

    /**
     * Update the list of results
     *
     * @param wasNewEntity true if the update happened after a entity was created, false otherwise
     */
    private void refreshFilteredResults(final boolean wasNewEntity) {
        try {
            LOGGER.log(Level.INFO, "ENTER BaseSearchAndEditPresenter.refreshFilteredResults()");

            if (!wasNewEntity) {
                    /* refresh the list of tags from the existing list that was modified */

                filteredResultsDisplay.getProvider().displayAsynchronousList(filteredResultsComponent.getProviderData().getItems(),
                        filteredResultsComponent.getProviderData().getSize(),
                        filteredResultsComponent.getProviderData().getStartRow());
            } else {
                    /* If we just created a new entity, refresh the list of entities from the database */

                filteredResultsComponent.bindExtendedFilteredResults(ServiceConstants.SEARCH_VIEW_HELP_TOPIC, "", filteredResultsComponent.getQuery());

                /*
                 * reInitialiseView will flush the ui, which will flush the null ID back to the displayed object. To prevent that we
                 * need to call edit on the newly saved entity.
                 *
                 * lastDisplayedView might be null if we have never viewed any entity (like when creating a filter). If so,
                 * don't edit the new entity because there is no view open to display it.
                 */
                if (lastDisplayedView != null) {
                    entityPropertiesView.getDriver().edit(filteredResultsComponent.getProviderData().getDisplayedItem().getItem());
                }
            }
        } finally {
            LOGGER.log(Level.INFO, "EXIT BaseSearchAndEditPresenter.refreshFilteredResults()");
        }
    }

    /**
     * Given an entity, load a fresh copy from the database and display it on the screen
     *
     * @param getNewEntityCallback The callback used to load a fresh entity
     * @param selectedItem         The item that was selected from a collection of entities
     */
    protected final void loadNewEntity(@NotNull final GetNewEntityCallback<T> getNewEntityCallback, @NotNull final V selectedItem) {
        try {
            LOGGER.log(Level.INFO, "ENTER BaseSearchAndEditPresenter.loadNewEntity()");

            /*
             * When an entity is selected we will get afresh copy from the database. This is to deal with a
             * situation where the search screen has been left for a while and the entity has been edited by someone
             * else in the mean time.
             */
            getNewEntityCallback.getNewEntity(selectedItem.getItem(), new DisplayNewEntityCallback<T>() {

                @Override
                public void displayNewEntity(final T entity) {

                    try {
                        LOGGER.log(Level.INFO, "ENTER BaseSearchAndEditPresenter.loadNewEntity() DisplayNewEntityCallback.displayNewEntity(final T entity)");

                        // update the filtered list with the new entity
                        selectedItem.setItem(entity);

                        /*
                         * The selected item will be the category from the list. This is the unedited, unexpanded
                         * copy of the category
                         */
                        filteredResultsComponent.getProviderData().setSelectedItem(selectedItem);

                        /*
                         * All editing is done in a clone of the selected category. Any expanded collections will be
                         * copied into this category
                         */
                        filteredResultsComponent.getProviderData().setDisplayedItem(selectedItem.clone(true));

                        updateViewsAfterNewEntityLoaded();
                    } finally {
                        LOGGER.log(Level.INFO, "EXIT BaseSearchAndEditPresenter.loadNewEntity() DisplayNewEntityCallback.displayNewEntity(final T entity)");
                    }

                }
            });
        } finally {
            LOGGER.log(Level.INFO, "EXIT BaseSearchAndEditPresenter.loadNewEntity()");
        }
    }

    /**
     * When a new entity is selected ore created, this method will update the views
     */
    protected final void updateViewsAfterNewEntityLoaded() {
        /* Allow overriding classes to display any additional details */
        loadAdditionalDisplayedItemData();

        /* Initialize the views */
        initializeViews();

        /* Refresh the view, or display the properties view if none is shown */
        switchView(lastDisplayedView == null ? firstDisplayedView : lastDisplayedView);
    }

    /**
     * Binds logic to the search results list row click event
     */
    protected final void bindResultsListRowClicks(@NotNull final GetNewEntityCallback<T> getNewEntityCallback) {
        filteredResultsDisplay.getResults().addCellPreviewHandler(new Handler<V>() {
            @Override
            public void onCellPreview(@NotNull final CellPreviewEvent<V> event) {

                /* Check to see if this was a click event */
                final boolean isClick = Constants.JAVASCRIPT_CLICK_EVENT.equals(event.getNativeEvent().getType());

                if (isClick) {
                    try {
                        LOGGER.log(Level.INFO, "ENTER BaseSearchAndEditPresenter.bindResultsListRowClicks() Anonymous.onCellPreview(final CellPreviewEvent<V> event)");

                        if (!isOKToProceed()) {
                            return;
                        }

                        final V selectedItem = event.getValue();

                        loadNewEntity(getNewEntityCallback, selectedItem);

                    } finally {
                        LOGGER.log(Level.INFO, "EXIT BaseSearchAndEditPresenter.bindResultsListRowClicks() Anonymous.onCellPreview(final CellPreviewEvent<V> event)");
                    }
                }
            }
        });

    }

    /**
     * Called once a new entity has been selected from the filtered results view. This method
     * is expected to be used to make additional calls to the REST service to load data that
     * was not included when the selected entity was fetched, and then call initializeViews()
     * to display that data.
     */
    abstract protected void loadAdditionalDisplayedItemData();

    /**
     * Called when a new entity is selected. This method is used to display any data that is contained
     * in the selected object when it is initially loaded.
     * <p/>
     * Typically, this method is initially called when the entity is loaded with no filter (i.e. filter = null), meaning
     * that the displayBlah() methods on the views are called.
     * <p/>
     * Additional data (usually child collections) is expected to be loaded and displayed in the
     * loadAdditionalDisplayedItemData() method. The loadAdditionalDisplayedItemData() will then
     * call initializeViews() with a filter set to the view that needs to be updated with the new data.
     *
     * @param filter null if all views are to be initialized, or includes a list of views to be initialized
     */
    abstract protected void initializeViews(@Nullable final List<BaseTemplateViewInterface> filter);

    /**
     * Binds logic to the action buttons. This is called by the bind() method, and so should not
     * be called from an extending class.
     */
    abstract protected void bindActionButtons();

    /**
     * Bind logic to the filtered results buttons. This is called by the bind() method, and so should not
     * be called from an extending class.
     */
    abstract protected void bindFilteredResultsButtons();

    /**
     * Saves the position of the main split
     */
    private void bindMainSplitResize(@NotNull final String saveKey) {
        try {
            LOGGER.log(Level.INFO, "ENTER BaseSearchAndEditPresenter.bindMainSplitResize()");

            display.getSplitPanel().addResizeHandler(new ResizeHandler() {

                @Override
                public void onResize(final ResizeEvent event) {
                    Preferences.INSTANCE.saveSetting(saveKey,
                            display.getSplitPanel().getSplitPosition(display.getResultsViewLayoutPanel()) + "");
                }
            });
        } finally {
            LOGGER.log(Level.INFO, "EXIT BaseSearchAndEditPresenter.bindMainSplitResize()");
        }
    }

    /**
     * Restores the size of the main split screen
     *
     * @param preferencesKey The key against which the previous size was saved
     */
    private void loadMainSplitResize(@NotNull final String preferencesKey) {
        try {
            LOGGER.log(Level.INFO, "ENTER BaseSearchAndEditPresenter.loadMainSplitResize()");

            display.getSplitPanel().setSplitPosition(display.getResultsViewLayoutPanel(),
                    Preferences.INSTANCE.getInt(preferencesKey, Constants.SPLIT_PANEL_SIZE), false);
        } finally {
            LOGGER.log(Level.INFO, "EXIT BaseSearchAndEditPresenter.loadMainSplitResize()");
        }
    }

    /**
     * Called when a new entity is selected
     */
    protected final void initializeViews() {
        initializeViews(null);
    }


    /**
     * Used by the initializeViews method
     *
     * @param filter The filter containing the list of views, or null if all views are to be initialized
     * @param view   The view to test against the filter
     * @return true if the filter is null or if it contains the view, and false otherwise
     */
    protected final boolean viewIsInFilter(@Nullable final List<BaseTemplateViewInterface> filter, @NotNull final BaseTemplateViewInterface view) {
        if (filter == null) {
            return true;
        }

        return filter.contains(view);
    }

    /**
     * Called when displaying changes to a entity or when changing views
     */
    protected final void switchView(@NotNull final BaseTemplateViewInterface displayedView) {

        try {
            LOGGER.log(Level.INFO, "ENTER BaseSearchAndEditPresenter.switchView(final W displayedView)");

            beforeSwitchView(displayedView);

            /* Show/Hide any localised loading dialogs */
            if (lastDisplayedView != null) {
                LOGGER.log(Level.INFO, "\tSetting old view to not shown");
                lastDisplayedView.setViewShown(false);
            }

            /* update the new view */
            if (displayedView != null) {
                LOGGER.log(Level.INFO, "\tSetting new view to shown");
                displayedView.setViewShown(true);
            }

            /* update the display widgets if we have changed displays */
            if (lastDisplayedView != displayedView) {
                LOGGER.log(Level.INFO, "\tAdding new display to canvas");
                display.displayChildView(displayedView);
            }

            /* copy any changes from the property view into the underlying object */
            if (lastDisplayedView == this.entityPropertiesView) {
                LOGGER.log(Level.INFO, "\tFlushing data from properties view");
                this.entityPropertiesView.getDriver().flush();
            }

            afterSwitchView(displayedView);

            lastDisplayedView = displayedView;


        } finally {
            LOGGER.log(Level.INFO, "EXIT BaseSearchAndEditPresenter.switchView(final W displayedView)");
        }
    }

    /**
     * Called once switchView has completed. Override this method to perform some
     * additional logic after a new screen has been displayed.
     *
     * @param displayedView The newly displayed screen.
     */
    protected void afterSwitchView(@NotNull final BaseTemplateViewInterface displayedView) {
    }

    /**
     * Called when switchView has been called. Override this method to perform some
     * additional logic before a new screen has been displayed.
     *
     * @param displayedView The newly displayed screen.
     */
    protected void beforeSwitchView(@NotNull final BaseTemplateViewInterface displayedView) {
    }
}