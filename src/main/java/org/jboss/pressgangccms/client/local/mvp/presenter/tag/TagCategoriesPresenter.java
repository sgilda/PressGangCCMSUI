package org.jboss.pressgangccms.client.local.mvp.presenter.tag;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.jboss.pressgangccms.client.local.mvp.presenter.base.EditableView;
import org.jboss.pressgangccms.client.local.mvp.presenter.base.TemplatePresenter;
import org.jboss.pressgangccms.client.local.mvp.view.tag.TagProjectsView;
import org.jboss.pressgangccms.client.local.mvp.view.tag.TagViewInterface;
import org.jboss.pressgangccms.client.local.restcalls.RESTCalls;
import org.jboss.pressgangccms.client.local.utilities.EnhancedAsyncDataProvider;
import org.jboss.pressgangccms.rest.v1.collections.RESTCategoryCollectionV1;
import org.jboss.pressgangccms.rest.v1.entities.RESTCategoryV1;
import org.jboss.pressgangccms.rest.v1.entities.RESTTagV1;

import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.SplitLayoutPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.view.client.HasData;

/**
 * The presenter that provides the logic for the tag category relationships.
 * 
 * @author matthew
 * 
 */
@Dependent
public class TagCategoriesPresenter extends TemplatePresenter implements EditableView {
    public static final String HISTORY_TOKEN = "TagCategoriesView";
    
    public interface Display extends TagViewInterface {
        EnhancedAsyncDataProvider<RESTCategoryV1> getProvider();

        void setProvider(final EnhancedAsyncDataProvider<RESTCategoryV1> provider);

        CellTable<RESTCategoryV1> getResults();

        SimplePager getPager();

        Column<RESTCategoryV1, String> getButtonColumn();

        Column<RESTTagV1, String> getTagDownButtonColumn();

        Column<RESTTagV1, String> getTagUpButtonColumn();

        EnhancedAsyncDataProvider<RESTTagV1> getTagsProvider();

        void setTagsProvider(EnhancedAsyncDataProvider<RESTTagV1> tagsProvider);

        VerticalPanel getTagsResultsPanel();

        SplitLayoutPanel getSplit();
    }

    @Inject
    private Display display;

    private String queryString;

    @Override
    public void parseToken(final String searchToken) {
        queryString = searchToken.replace(TagProjectsView.HISTORY_TOKEN + ";", "");
    }

    @Override
    public void go(final HasWidgets container) {
        container.clear();
        container.add(display.getTopLevelPanel());

        bind();
    }

    private void bind() {
        super.bind(display, this);

        final EnhancedAsyncDataProvider<RESTCategoryV1> provider = new EnhancedAsyncDataProvider<RESTCategoryV1>() {
            @Override
            protected void onRangeChanged(final HasData<RESTCategoryV1> item) {
                final int start = item.getVisibleRange().getStart();
                final int length = item.getVisibleRange().getLength();
                final int end = start + length;

                final RESTCalls.RESTCallback<RESTCategoryCollectionV1> callback = new RESTCalls.RESTCallback<RESTCategoryCollectionV1>() {
                    @Override
                    public void begin() {
                        display.addWaitOperation();
                    }

                    @Override
                    public void generalException(final Exception ex) {
                        display.removeWaitOperation();
                    }

                    @Override
                    public void success(final RESTCategoryCollectionV1 retValue) {
                        try {
                            updateRowData(start, retValue.getItems());
                            updateRowCount(retValue.getSize(), true);
                        } finally {
                            display.removeWaitOperation();
                        }
                    }

                    @Override
                    public void failed() {
                        display.removeWaitOperation();
                    }
                };

                RESTCalls.getCategoriesFromQuery(callback, queryString, start, end);
            }
        };

        display.setProvider(provider);
    }

    @Override
    public boolean checkForUnsavedChanges() {
        return true;
    }
}
