package org.jboss.pressgang.ccms.ui.client.local.mvp.component.image;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import org.jboss.pressgang.ccms.rest.v1.collections.RESTImageCollectionV1;
import org.jboss.pressgang.ccms.rest.v1.collections.RESTLanguageImageCollectionV1;
import org.jboss.pressgang.ccms.rest.v1.collections.items.RESTImageCollectionItemV1;
import org.jboss.pressgang.ccms.rest.v1.collections.items.RESTLanguageImageCollectionItemV1;
import org.jboss.pressgang.ccms.rest.v1.components.ComponentImageV1;
import org.jboss.pressgang.ccms.rest.v1.constants.CommonFilterConstants;
import org.jboss.pressgang.ccms.rest.v1.entities.RESTImageV1;
import org.jboss.pressgang.ccms.rest.v1.entities.RESTLanguageImageV1;
import org.jboss.pressgang.ccms.rest.v1.entities.RESTStringConstantV1;
import org.jboss.pressgang.ccms.ui.client.local.constants.Constants;
import org.jboss.pressgang.ccms.ui.client.local.constants.ServiceConstants;
import org.jboss.pressgang.ccms.ui.client.local.mvp.component.base.searchandedit.BaseSearchAndEditComponent;
import org.jboss.pressgang.ccms.ui.client.local.mvp.events.ImagesFilteredResultsAndImageViewEvent;
import org.jboss.pressgang.ccms.ui.client.local.mvp.events.SearchResultsAndTopicViewEvent;
import org.jboss.pressgang.ccms.ui.client.local.mvp.presenter.image.ImageFilteredResultsPresenter;
import org.jboss.pressgang.ccms.ui.client.local.mvp.presenter.image.ImagePresenter;
import org.jboss.pressgang.ccms.ui.client.local.mvp.presenter.image.ImagesFilteredResultsAndImagePresenter;
import org.jboss.pressgang.ccms.ui.client.local.mvp.view.base.BaseTemplateViewInterface;
import org.jboss.pressgang.ccms.ui.client.local.preferences.Preferences;
import org.jboss.pressgang.ccms.ui.client.local.resources.strings.PressGangCCMSUI;
import org.jboss.pressgang.ccms.ui.client.local.restcalls.BaseRestCallback;
import org.jboss.pressgang.ccms.ui.client.local.restcalls.RESTCalls;
import org.jboss.pressgang.ccms.ui.client.local.restcalls.RESTCalls.RESTCallback;
import org.jboss.pressgang.ccms.ui.client.local.ui.editor.image.RESTLanguageImageV1Editor;
import org.jboss.pressgang.ccms.ui.client.local.utilities.GWTUtilities;
import org.vectomatic.file.File;
import org.vectomatic.file.FileReader;
import org.vectomatic.file.events.ErrorHandler;
import org.vectomatic.file.events.LoadEndEvent;
import org.vectomatic.file.events.LoadEndHandler;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.Window;

/**
 * The component used to add logic to the image search and edit view.
 * 
 * Images are a little different to other entities in that one of the properties, imageBase64, is generated by the server from
 * the binary image data. This property is used by the GWT application to display the image, which means that when editing
 * uploading a new image we actually have to save to the server instead of applying all changes locally and then saving them all
 * in one hit.
 * 
 * It also means that when we create a new image we actually create and save a new image, instead of creating an in memory image
 * that is edited and then saved.
 * 
 * @author Matthew Casperson
 * 
 */
public class ImagesFilteredResultsAndImageComponent
        extends
        BaseSearchAndEditComponent<ImageFilteredResultsPresenter.Display, ImagesFilteredResultsAndImagePresenter.Display, RESTImageV1, RESTImageCollectionV1, RESTImageCollectionItemV1, ImagePresenter.Display, ImagePresenter.Display>
        implements ImagesFilteredResultsAndImagePresenter.LogicComponent {

    /**
     * A reference to the StringConstants that holds the locales.
     */
    private String[] locales;

    @Inject
    private HandlerManager eventBus;

    @Override
    public void bind(final int topicId, final String pageId, final ImageFilteredResultsPresenter.Display imageFilteredResultsDisplay,
            final ImageFilteredResultsPresenter.LogicComponent imageFilteredResultsComponent,
            final ImagePresenter.Display imageDisplay, final ImagePresenter.LogicComponent imageComponent,
            final ImagesFilteredResultsAndImagePresenter.Display display, final BaseTemplateViewInterface waitDisplay) {

        super.bind(topicId, pageId, Preferences.IMAGE_VIEW_MAIN_SPLIT_WIDTH, imageDisplay, imageDisplay, imageFilteredResultsDisplay,
                imageFilteredResultsComponent, display, waitDisplay);

        populateLocales();
    }

    /**
     * Here we load the actual language images associated with the image
     */
    @Override
    protected void loadAdditionalDisplayedItemData() {
        final RESTCallback<RESTImageV1> callback = new BaseRestCallback<RESTImageV1, ImagesFilteredResultsAndImagePresenter.Display>(
                display, new BaseRestCallback.SuccessAction<RESTImageV1, ImagesFilteredResultsAndImagePresenter.Display>() {
                    @Override
                    public void doSuccessAction(final RESTImageV1 retValue,
                            final ImagesFilteredResultsAndImagePresenter.Display display) {
                        /*
                         * Do a shallow copy here, because Chrome has issues with System.arraycopy - see
                         * http://code.google.com/p/chromium/issues/detail?id=56588
                         */
                        retValue.cloneInto(filteredResultsComponent.getProviderData().getDisplayedItem().getItem(), false);

                        finishLoading();
                    }
                }) {
        };

        RESTCalls.getImage(callback, filteredResultsComponent.getProviderData().getSelectedItem().getItem().getId());
    }

    private BaseRestCallback.SuccessAction<RESTImageV1, BaseTemplateViewInterface> getDefaultImageRestCallback() {
        return new BaseRestCallback.SuccessAction<RESTImageV1, BaseTemplateViewInterface>() {
            @Override
            public void doSuccessAction(final RESTImageV1 retValue, final BaseTemplateViewInterface display) {
                retValue.cloneInto(filteredResultsComponent.getProviderData().getSelectedItem().getItem(), false);
                retValue.cloneInto(filteredResultsComponent.getProviderData().getDisplayedItem().getItem(), false);
                initializeViews();
                updateDisplayAfterSave(false);
            }
        };
    }

    @Override
    protected void switchView(final ImagePresenter.Display displayedView) {

        super.switchView(displayedView);

        lastDisplayedView = displayedView;
    }

    private List<String> getUnassignedLocales() {
        final List<String> newLocales = new ArrayList<String>(Arrays.asList(locales));

        /* Make it so you can't add a locale if it already exists */
        if (filteredResultsComponent.getProviderData().getDisplayedItem().getItem().getLanguageImages_OTM() != null) {
            for (final RESTLanguageImageCollectionItemV1 langImage : filteredResultsComponent.getProviderData()
                    .getDisplayedItem().getItem().getLanguageImages_OTM().returnExistingAndAddedCollectionItems()) {
                newLocales.remove(langImage.getItem().getLocale());
            }
        }

        return newLocales;
    }

    private void populateLocales() {
        final RESTCalls.RESTCallback<RESTStringConstantV1> callback = new BaseRestCallback<RESTStringConstantV1, BaseTemplateViewInterface>(
                display, new BaseRestCallback.SuccessAction<RESTStringConstantV1, BaseTemplateViewInterface>() {
                    @Override
                    public void doSuccessAction(final RESTStringConstantV1 retValue, final BaseTemplateViewInterface display) {
                        /* Get the list of locales from the StringConstant */
                        locales = retValue.getValue().replaceAll("\\r\\n", "").replaceAll("\\n", "").replaceAll(" ", "")
                                .split(",");

                        finishLoading();
                    }
                }) {
        };

        RESTCalls.getStringConstant(callback, ServiceConstants.LOCALE_STRING_CONSTANT);
    }

    /**
     * Each Language Image has an upload button that needs to be bound to some behaviour.
     * 
     * @param entityPropertiesView The view that displays the image details.
     * @param waitDisplay The view that displays the waiting screen
     */
    private void bindImageUploadButtons() {
        if (entityPropertiesView.getEditor() == null) {
            throw new IllegalStateException("display.getEditor() cannot be null");
        }

        for (final RESTLanguageImageV1Editor editor : entityPropertiesView.getEditor().languageImages_OTMEditor().itemsEditor()
                .getEditors()) {
            editor.getUploadButton().addClickHandler(new ClickHandler() {
                @Override
                public void onClick(final ClickEvent event) {

                    /*
                     * There should only be one file, but use a loop to accommodate any changes that might implement multiple
                     * files
                     */
                    for (final File file : editor.getUpload().getFiles()) {
                        waitDisplay.addWaitOperation();

                        final FileReader reader = new FileReader();

                        reader.addErrorHandler(new ErrorHandler() {
                            @Override
                            public void onError(final org.vectomatic.file.events.ErrorEvent event) {
                                entityPropertiesView.removeWaitOperation();
                            }
                        });

                        reader.addLoadEndHandler(new LoadEndHandler() {
                            @Override
                            public void onLoadEnd(final LoadEndEvent event) {
                                try {
                                    final String result = reader.getStringResult();
                                    final byte[] buffer = GWTUtilities.getByteArray(result, 1);

                                    /* Flush any changes */
                                    entityPropertiesView.getDriver().flush();
                                    
                                    /*
                                     * Create the image to be modified. This is so we don't send off unnecessary data.
                                     */
                                    final RESTImageV1 updateImage = new RESTImageV1();
                                    updateImage.setId(filteredResultsComponent.getProviderData().getDisplayedItem().getItem()
                                            .getId());
                                    updateImage.explicitSetDescription(filteredResultsComponent.getProviderData().getDisplayedItem().getItem().getDescription());

                                    /* Create the language image */
                                    final RESTLanguageImageV1 updatedLanguageImage = new RESTLanguageImageV1();
                                    updatedLanguageImage.setId(editor.self.getItem().getId());
                                    updatedLanguageImage.explicitSetImageData(buffer);
                                    updatedLanguageImage.explicitSetFilename(file.getName());

                                    /* Add the language image */
                                    updateImage.explicitSetLanguageImages_OTM(new RESTLanguageImageCollectionV1());
                                    updateImage.getLanguageImages_OTM().addUpdateItem(updatedLanguageImage);

                                    final RESTCalls.RESTCallback<RESTImageV1> callback = new BaseRestCallback<RESTImageV1, BaseTemplateViewInterface>(
                                            waitDisplay, getDefaultImageRestCallback()) {
                                    };

                                    RESTCalls.updateImage(callback, updateImage);
                                } finally {
                                    waitDisplay.removeWaitOperation();
                                }
                            }
                        });

                        reader.readAsBinaryString(file);
                    }
                }
            });
        }
    }

    @Override
    public boolean hasUnsavedChanges() {
        if (filteredResultsComponent.getProviderData().getDisplayedItem() != null) {

            entityPropertiesView.getDriver().flush();

            return !GWTUtilities.stringEqualsEquatingNullWithEmptyString(filteredResultsComponent.getProviderData()
                    .getSelectedItem().getItem().getDescription(), filteredResultsComponent.getProviderData()
                    .getDisplayedItem().getItem().getDescription());
        }
        return false;
    }

    /**
     * Potentially two REST calls have to finish before we can display the page. This function will be called as each REST call
     * finishes, and when all the information has been gathered, the page will be displayed.
     */
    private void finishLoading() {
        if (locales != null && filteredResultsComponent.getProviderData().getDisplayedItem() != null) {
            initializeViews();
        }
    }

    /**
     * Called once an entity has been saved to refresh the various lists that may have been modified by the edited or created
     * entity.
     * 
     * 
     * @param wasNewEntity true if the entity that was saved was a new entity, and false otherwise
     */
    @Override
    protected void updateDisplayAfterSave(final boolean wasNewEntity) {
        /* refresh the list of tags from the existing list that was modified */
        if (!wasNewEntity) {
            filteredResultsDisplay.getProvider().displayAsynchronousList(filteredResultsComponent.getProviderData().getItems(),
                    filteredResultsComponent.getProviderData().getSize(),
                    filteredResultsComponent.getProviderData().getStartRow());
        }
        /* If we just created a new entity, refresh the list of entities from the database */
        else {
            filteredResultsComponent.bind(ServiceConstants.SEARCH_VIEW_HELP_TOPIC, "", filteredResultsComponent.getQuery(), filteredResultsDisplay, waitDisplay);

            /*
             * reInitialiseView will flush the ui, which will flush the null ID back to the displayed object. To prevent that we
             * need to call edit on the newly saved entity
             */
            entityPropertiesView.getDriver().edit(filteredResultsComponent.getProviderData().getDisplayedItem().getItem());

        }

        initializeViews();
    }

    @Override
    protected void bindActionButtons() {
        entityPropertiesView.getSave().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(final ClickEvent event) {
                if (hasUnsavedChanges()) {

                    /*
                     * Create the image to be modified. This is so we don't send off unnessessary data.
                     */
                    final RESTImageV1 updateImage = new RESTImageV1();
                    updateImage.setId(filteredResultsComponent.getProviderData().getDisplayedItem().getItem().getId());
                    updateImage.explicitSetDescription(filteredResultsComponent.getProviderData().getDisplayedItem().getItem()
                            .getDescription());

                    final RESTCalls.RESTCallback<RESTImageV1> callback = new BaseRestCallback<RESTImageV1, BaseTemplateViewInterface>(
                            waitDisplay, getDefaultImageRestCallback()) {
                    };

                    RESTCalls.updateImage(callback, updateImage);
                } else {
                    Window.alert(PressGangCCMSUI.INSTANCE.NoUnsavedChanges());
                }

            }
        });

        entityPropertiesView.getAddLocale().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(final ClickEvent event) {
                entityPropertiesView.getAddLocaleDialog().getDialogBox().center();
                entityPropertiesView.getAddLocaleDialog().getDialogBox().show();
            }
        });

        entityPropertiesView.getRemoveLocale().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(final ClickEvent event) {
                if (Window.confirm(PressGangCCMSUI.INSTANCE.ConfirmDelete())) {

                    final int selectedTab = entityPropertiesView.getEditor().languageImages_OTMEditor().getSelectedIndex();
                    if (selectedTab != -1) {
                        final RESTLanguageImageCollectionItemV1 selectedImage = entityPropertiesView.getEditor()
                                .languageImages_OTMEditor().itemsEditor().getList().get(selectedTab);

                        /* Adding or removing a locale will save changes to the description */
                        entityPropertiesView.getDriver().flush();

                        /*
                         * Create the image to be modified. This is so we don't send off unnessessary data.
                         */
                        final RESTImageV1 updateImage = new RESTImageV1();
                        updateImage.setId(filteredResultsComponent.getProviderData().getDisplayedItem().getItem().getId());
                        updateImage.explicitSetDescription(filteredResultsComponent.getProviderData().getDisplayedItem()
                                .getItem().getDescription());

                        /* Create the language image */
                        final RESTLanguageImageV1 languageImage = new RESTLanguageImageV1();
                        languageImage.setId(selectedImage.getItem().getId());

                        /* Add the langauge image */
                        updateImage.explicitSetLanguageImages_OTM(new RESTLanguageImageCollectionV1());
                        updateImage.getLanguageImages_OTM().addRemoveItem(languageImage);

                        final RESTCalls.RESTCallback<RESTImageV1> callback = new BaseRestCallback<RESTImageV1, BaseTemplateViewInterface>(
                                waitDisplay, getDefaultImageRestCallback()) {
                        };

                        RESTCalls.updateImage(callback, updateImage);
                    }
                }
            }
        });

        entityPropertiesView.getAddLocaleDialog().getOk().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(final ClickEvent event) {
                entityPropertiesView.getAddLocaleDialog().getDialogBox().hide();

                final String selectedLocale = entityPropertiesView.getAddLocaleDialog().getLocales()
                        .getItemText(entityPropertiesView.getAddLocaleDialog().getLocales().getSelectedIndex());

                /* Don't add locales twice */
                if (filteredResultsComponent.getProviderData().getDisplayedItem().getItem().getLanguageImages_OTM() != null) {
                    for (final RESTLanguageImageCollectionItemV1 langImage : filteredResultsComponent.getProviderData()
                            .getDisplayedItem().getItem().getLanguageImages_OTM().returnExistingAndAddedCollectionItems()) {
                        if (langImage.getItem().getLocale().equals(selectedLocale)) {
                            return;
                        }
                    }
                }

                /* Adding or removing a locate will also save any changes to the description */
                entityPropertiesView.getDriver().flush();

                /*
                 * Create the image to be modified. This is so we don't send off unnessessary data.
                 */
                final RESTImageV1 updateImage = new RESTImageV1();
                updateImage.setId(filteredResultsComponent.getProviderData().getDisplayedItem().getItem().getId());
                updateImage.explicitSetDescription(filteredResultsComponent.getProviderData().getDisplayedItem().getItem()
                        .getDescription());

                /* Create the language image */
                final RESTLanguageImageV1 languageImage = new RESTLanguageImageV1();
                languageImage.explicitSetLocale(selectedLocale);

                /* Add the langauge image */
                updateImage.explicitSetLanguageImages_OTM(new RESTLanguageImageCollectionV1());
                updateImage.getLanguageImages_OTM().addNewItem(languageImage);

                final RESTCalls.RESTCallback<RESTImageV1> callback = new BaseRestCallback<RESTImageV1, BaseTemplateViewInterface>(
                        waitDisplay, getDefaultImageRestCallback()) {
                };

                RESTCalls.updateImage(callback, updateImage);
            }
        });

        entityPropertiesView.getAddLocaleDialog().getCancel().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(final ClickEvent event) {
                entityPropertiesView.getAddLocaleDialog().getDialogBox().hide();
            }
        });

        entityPropertiesView.getViewImage().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(final ClickEvent event) {

                final int selectedTab = entityPropertiesView.getEditor().languageImages_OTMEditor().getSelectedIndex();
                if (selectedTab != -1) {
                    final RESTLanguageImageCollectionItemV1 selectedImage = entityPropertiesView.getEditor()
                            .languageImages_OTMEditor().itemsEditor().getList().get(selectedTab);

                    displayImageInPopup(GWTUtilities.getStringUTF8(selectedImage.getItem().getImageDataBase64()));
                }
            };
        });

        entityPropertiesView.getFindTopics().addClickHandler(new ClickHandler() {

            @Override
            public void onClick(final ClickEvent event) {

                final String docbookFileName = ComponentImageV1.getDocbookFileName(filteredResultsComponent.getProviderData()
                        .getDisplayedItem().getItem());

                if (docbookFileName != null && !docbookFileName.isEmpty() && isOKToProceed()) {

                    eventBus.fireEvent(new SearchResultsAndTopicViewEvent(Constants.QUERY_PATH_SEGMENT_PREFIX
                            + org.jboss.pressgang.ccms.utils.constants.CommonFilterConstants.TOPIC_XML_FILTER_VAR + "=images/" + docbookFileName, event.getNativeEvent()
                            .getKeyCode() == KeyCodes.KEY_CTRL));
                }

            }
        });
    }

    /**
     * Open a popup window that displays the image defined in the base64 parameter
     * 
     * @param base64 The BASE64 representation of the image to be displayed
     */
    native private void displayImageInPopup(final String base64) /*-{
		var win = $wnd.open("data:image/jpeg;base64," + base64, "_blank",
				"width=" + (screen.width - 200) + ", height="
						+ (screen.height - 200) + ", left=100, top=100"); // a window object
    }-*/;

    @Override
    protected void bindFilteredResultsButtons() {
        filteredResultsDisplay.getEntitySearch().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(final ClickEvent event) {
                if (isOKToProceed()) {
                    eventBus.fireEvent(new ImagesFilteredResultsAndImageViewEvent(filteredResultsComponent.getQuery(), event
                            .getNativeEvent().getKeyCode() == KeyCodes.KEY_CTRL));
                }
            }
        });

        filteredResultsDisplay.getCreate().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(final ClickEvent event) {
                if (isOKToProceed()) {

                    /* Start by getting the default locale */
                    final RESTCallback<RESTStringConstantV1> callback = new BaseRestCallback<RESTStringConstantV1, ImagesFilteredResultsAndImagePresenter.Display>(
                            display,
                            new BaseRestCallback.SuccessAction<RESTStringConstantV1, ImagesFilteredResultsAndImagePresenter.Display>() {
                                @Override
                                public void doSuccessAction(final RESTStringConstantV1 retValue,
                                        final ImagesFilteredResultsAndImagePresenter.Display display) {

                                    /* When we have the default locale, create a new image */
                                    final RESTLanguageImageV1 langImage = new RESTLanguageImageV1();
                                    langImage.explicitSetLocale(retValue.getValue());

                                    final RESTImageV1 newImage = new RESTImageV1();
                                    newImage.explicitSetLanguageImages_OTM(new RESTLanguageImageCollectionV1());
                                    newImage.getLanguageImages_OTM().addNewItem(langImage);

                                    final RESTCallback<RESTImageV1> imageCallback = new BaseRestCallback<RESTImageV1, ImagesFilteredResultsAndImagePresenter.Display>(
                                            display,
                                            new BaseRestCallback.SuccessAction<RESTImageV1, ImagesFilteredResultsAndImagePresenter.Display>() {
                                                @Override
                                                public void doSuccessAction(final RESTImageV1 retValue,
                                                        final ImagesFilteredResultsAndImagePresenter.Display display) {

                                                    final RESTImageCollectionItemV1 selectedImageCollectionItem = new RESTImageCollectionItemV1();
                                                    selectedImageCollectionItem.setItem(retValue.clone(false));
                                                    filteredResultsComponent.getProviderData().setSelectedItem(
                                                            selectedImageCollectionItem);

                                                    final RESTImageCollectionItemV1 displayedImageCollectionItem = new RESTImageCollectionItemV1();
                                                    displayedImageCollectionItem.setItem(retValue.clone(false));
                                                    filteredResultsComponent.getProviderData().setDisplayedItem(
                                                            displayedImageCollectionItem);

                                                    initializeViews();

                                                    /* Display the entities property view */
                                                    switchView(entityPropertiesView);

                                                    /* Reload the filtered results view */
                                                    updateDisplayAfterSave(true);
                                                }
                                            }) {
                                    };

                                    RESTCalls.createImage(imageCallback, newImage);
                                }
                            }) {
                    };

                    RESTCalls.getStringConstant(callback, ServiceConstants.DEFAULT_LOCALE_ID);

                }
            }
        });

    }

    @Override
    protected void initializeViews(final List<ImagePresenter.Display> filter) {

        if (viewIsInFilter(filter, entityPropertiesView)) {
            entityPropertiesView.initialize(filteredResultsComponent.getProviderData().getDisplayedItem().getItem(),
                    getUnassignedLocales().toArray(new String[0]));
        }

        bindImageUploadButtons();
    }
}
