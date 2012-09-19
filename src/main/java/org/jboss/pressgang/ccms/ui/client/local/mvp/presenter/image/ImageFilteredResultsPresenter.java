package org.jboss.pressgang.ccms.ui.client.local.mvp.presenter.image;

import java.util.List;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.jboss.pressgang.ccms.rest.v1.collections.RESTImageCollectionV1;
import org.jboss.pressgang.ccms.rest.v1.entities.RESTImageV1;
import org.jboss.pressgang.ccms.ui.client.local.mvp.presenter.base.EditableView;
import org.jboss.pressgang.ccms.ui.client.local.mvp.presenter.base.TemplatePresenter;
import org.jboss.pressgang.ccms.ui.client.local.mvp.view.base.BaseTemplateViewInterface;
import org.jboss.pressgang.ccms.ui.client.local.mvp.view.image.ImageFilteredResultsView;
import org.jboss.pressgang.ccms.ui.client.local.resources.strings.PressGangCCMSUI;
import org.jboss.pressgang.ccms.ui.client.local.restcalls.RESTCalls;
import org.jboss.pressgang.ccms.ui.client.local.utilities.EnhancedAsyncDataProvider;

import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.view.client.HasData;

@Dependent
public class ImageFilteredResultsPresenter extends TemplatePresenter implements EditableView {
    public static final String HISTORY_TOKEN = "ImageFilteredResultsView";
    
    public interface Display extends BaseTemplateViewInterface {
        org.jboss.pressgang.ccms.ui.client.local.utilities.EnhancedAsyncDataProvider<RESTImageV1> getProvider();

        void setProvider(final org.jboss.pressgang.ccms.ui.client.local.utilities.EnhancedAsyncDataProvider<RESTImageV1> provider);

        CellTable<RESTImageV1> getResults();

        SimplePager getPager();

        TextBox getImageIdFilter();

        TextBox getImageDescriptionFilter();

        @Override
        PushButton getSearch();

        TextBox getImageOriginalFileNameFilter();
    }

    @Inject
    private Display display;

    private String queryString;

    /** Keeps a reference to the start row. */
    private Integer tableStartRow;

    /** Keeps a reference to the list of topics being displayed. */
    private List<RESTImageV1> currentList;

    @Override
    public void parseToken(final String searchToken) {
        queryString = searchToken.replace(HISTORY_TOKEN + ";", "");
    }

    @Override
    public void go(final HasWidgets container) {
        container.clear();
        container.add(display.getTopLevelPanel());

        bind();
    }

    private void bind() {
        super.bind(display, this);

        final EnhancedAsyncDataProvider<RESTImageV1> provider = new EnhancedAsyncDataProvider<RESTImageV1>() {
            @Override
            protected void onRangeChanged(final HasData<RESTImageV1> item) {
                final int start = item.getVisibleRange().getStart();
                final int length = item.getVisibleRange().getLength();
                final int end = start + length;

                final RESTCalls.RESTCallback<RESTImageCollectionV1> callback = new RESTCalls.RESTCallback<RESTImageCollectionV1>() {
                    @Override
                    public void begin() {
                        display.addWaitOperation();
                    }

                    @Override
                    public void generalException(final Exception ex) {
                        display.removeWaitOperation();
                    }

                    @Override
                    public void success(final RESTImageCollectionV1 retValue) {
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

                RESTCalls.getImagesFromQuery(callback, queryString, start, end);
            }
        };

        display.setProvider(provider);
    }

    /**
     * @return A provider to be used for the topic display list.
     */
    private EnhancedAsyncDataProvider<RESTImageV1> generateListProvider() {
        final EnhancedAsyncDataProvider<RESTImageV1> provider = new EnhancedAsyncDataProvider<RESTImageV1>() {
            @Override
            protected void onRangeChanged(final HasData<RESTImageV1> item) {
                tableStartRow = item.getVisibleRange().getStart();
                final int length = item.getVisibleRange().getLength();
                final int end = tableStartRow + length;

                final RESTCalls.RESTCallback<RESTImageCollectionV1> callback = new RESTCalls.RESTCallback<RESTImageCollectionV1>() {
                    @Override
                    public void begin() {
                        display.addWaitOperation();
                    }

                    @Override
                    public void generalException(final Exception ex) {
                        Window.alert(PressGangCCMSUI.INSTANCE.ErrorGettingTopics());
                        display.removeWaitOperation();
                    }

                    @Override
                    public void success(final RESTImageCollectionV1 retValue) {
                        try {
                            currentList = retValue.getItems();
                            updateRowData(tableStartRow, currentList);
                            updateRowCount(retValue.getSize(), true);
                        } finally {
                            display.removeWaitOperation();
                        }
                    }

                    @Override
                    public void failed() {
                        display.removeWaitOperation();
                        Window.alert(PressGangCCMSUI.INSTANCE.ConnectionError());
                    }
                };

                RESTCalls.getImagesFromQuery(callback, queryString, tableStartRow, end);
            }
        };
        return provider;
    }

    @Override
    public boolean checkForUnsavedChanges() {
        return true;
    }
}