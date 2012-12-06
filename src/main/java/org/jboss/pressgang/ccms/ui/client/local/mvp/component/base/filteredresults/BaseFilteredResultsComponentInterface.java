package org.jboss.pressgang.ccms.ui.client.local.mvp.component.base.filteredresults;

import org.jboss.pressgang.ccms.rest.v1.collections.base.RESTBaseCollectionItemV1;
import org.jboss.pressgang.ccms.rest.v1.collections.base.RESTBaseCollectionV1;
import org.jboss.pressgang.ccms.rest.v1.entities.base.RESTBaseEntityV1;
import org.jboss.pressgang.ccms.ui.client.local.mvp.component.base.Component;
import org.jboss.pressgang.ccms.ui.client.local.mvp.view.base.BaseTemplateViewInterface;
import org.jboss.pressgang.ccms.ui.client.local.mvp.view.base.filteredresults.BaseFilteredResultsViewInterface;
import org.jboss.pressgang.ccms.ui.client.local.ui.ProviderUpdateData;

/**
 * 
 * @author Matthew Casperson
 * 
 * @param <S> The filtered results view type
 * @param <T> The entity type
 * @param <U> The collection type for entity T
 * @param <V> The collection item type for entity T
 */
public interface BaseFilteredResultsComponentInterface<S extends BaseFilteredResultsViewInterface<T, U, V>, T extends RESTBaseEntityV1<T, U, V>, U extends RESTBaseCollectionV1<T, U, V>, V extends RESTBaseCollectionItemV1<T, U, V>>
        extends Component<S> {

    /**
     * @return The query string that represents the current state of the filters
     */
    String getQuery();

    /**
     * @return The provider data used to populate the celltable
     */
    ProviderUpdateData<V> getProviderData();

    /**
     * @param providerData The provider data used to populate the celltable
     */
    void setTagProviderData(final ProviderUpdateData<V> providerData);

    /**
     * @param topicId The ID of the help topic associated with this view
     * @param pageId The history token associated with this view
     * @param queryString The query that defines the results to be displayed
     * @param display The filtered results view
     * @param waitDisplay The view that displays the waiting dialog
     */
    void bind(final int topicId, final String pageId, final String queryString, final S display,
            final BaseTemplateViewInterface waitDisplay);
}
