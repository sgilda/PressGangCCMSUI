package org.jboss.pressgangccms.client.local.mvp.presenter.tag;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.jboss.pressgangccms.client.local.mvp.presenter.base.EditableView;
import org.jboss.pressgangccms.client.local.mvp.presenter.base.TemplatePresenter;
import org.jboss.pressgangccms.client.local.mvp.view.tag.TagView;
import org.jboss.pressgangccms.client.local.mvp.view.tag.TagViewInterface;
import org.jboss.pressgangccms.client.local.restcalls.RESTCalls;
import org.jboss.pressgangccms.client.local.ui.editor.tagview.RESTTagV1BasicDetailsEditor;
import org.jboss.pressgangccms.rest.v1.entities.RESTTagV1;

import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.user.client.ui.HasWidgets;

@Dependent
public class TagPresenter extends TemplatePresenter implements EditableView {
   
    public static final String HISTORY_TOKEN = "TagView";
    
    // Empty interface declaration, similar to UiBinder
    public interface TagPresenterDriver extends SimpleBeanEditorDriver<RESTTagV1, RESTTagV1BasicDetailsEditor> {
    }

    public interface Display extends TagViewInterface {
        @SuppressWarnings("rawtypes")
        SimpleBeanEditorDriver getDriver();
    }

    private String tagId;

    @Inject
    private Display display;

    @Override
    public void parseToken(final String searchToken) {
        tagId = searchToken.replace(HISTORY_TOKEN + ";", "");
    }

    @Override
    public void go(final HasWidgets container) {
        container.clear();
        container.add(display.getTopLevelPanel());

        getTag();

        bind();
    }

    private void getTag() {
        final RESTCalls.RESTCallback<RESTTagV1> callback = new RESTCalls.RESTCallback<RESTTagV1>() {
            @Override
            public void begin() {
                display.addWaitOperation();
            }

            @Override
            public void generalException(final Exception ex) {
                display.removeWaitOperation();
            }

            @Override
            public void success(final RESTTagV1 retValue) {
                try {
                    display.initialize(retValue, false);
                } finally {
                    display.removeWaitOperation();
                }
            }

            @Override
            public void failed() {
                display.removeWaitOperation();
            }
        };

        try {
            RESTCalls.getTag(callback, Integer.parseInt(tagId));
        } catch (final NumberFormatException ex) {
            display.removeWaitOperation();
        }
    }

    private void bind() {
        super.bind(display, this);
    }

    @Override
    public boolean checkForUnsavedChanges() {
        return true;
    }
}
