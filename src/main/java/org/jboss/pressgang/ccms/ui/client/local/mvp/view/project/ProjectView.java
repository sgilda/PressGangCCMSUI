package org.jboss.pressgang.ccms.ui.client.local.mvp.view.project;

import com.google.gwt.core.client.GWT;
import org.jboss.pressgang.ccms.rest.v1.entities.RESTProjectV1;
import org.jboss.pressgang.ccms.ui.client.local.mvp.presenter.project.ProjectPresenter;
import org.jboss.pressgang.ccms.ui.client.local.mvp.presenter.project.ProjectPresenter.ProjectPresenterDriver;
import org.jboss.pressgang.ccms.ui.client.local.mvp.view.base.BaseTemplateView;
import org.jboss.pressgang.ccms.ui.client.local.resources.strings.PressGangCCMSUI;
import org.jboss.pressgang.ccms.ui.client.local.ui.editor.projectview.RESTProjectV1BasicDetailsEditor;
import org.jetbrains.annotations.NotNull;

public class ProjectView extends BaseTemplateView implements ProjectPresenter.Display {


    /**
     * The GWT Editor Driver
     */
    private final ProjectPresenterDriver driver = GWT.create(ProjectPresenterDriver.class);

    private boolean readOnly = false;

    @Override
    public ProjectPresenterDriver getDriver() {
        return driver;
    }

    public ProjectView() {
        super(PressGangCCMSUI.INSTANCE.PressGangCCMS(), PressGangCCMSUI.INSTANCE.ProjectDetails());
    }

    @Override
    public void display(final RESTProjectV1 entity, final boolean readOnly) {
        this.readOnly = readOnly;

        /* SearchUIProjectsEditor is a grid */
        @NotNull final RESTProjectV1BasicDetailsEditor editor = new RESTProjectV1BasicDetailsEditor(this.readOnly);
        /* Initialize the driver with the top-level editor */
        driver.initialize(editor);
        /* Copy the data in the object into the UI */
        driver.edit(entity);
        /* Add the projects */
        this.getPanel().setWidget(editor);
    }


}