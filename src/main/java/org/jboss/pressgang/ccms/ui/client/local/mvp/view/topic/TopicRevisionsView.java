package org.jboss.pressgang.ccms.ui.client.local.mvp.view.topic;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.i18n.shared.DateTimeFormat;
import com.google.gwt.i18n.shared.DateTimeFormat.PredefinedFormat;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.CellTable.Resources;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.*;
import org.jboss.pressgang.ccms.rest.v1.collections.items.RESTTopicCollectionItemV1;
import org.jboss.pressgang.ccms.rest.v1.entities.RESTTopicV1;
import org.jboss.pressgang.ccms.ui.client.local.constants.CSSConstants;
import org.jboss.pressgang.ccms.ui.client.local.constants.Constants;
import org.jboss.pressgang.ccms.ui.client.local.constants.ServiceConstants;
import org.jboss.pressgang.ccms.ui.client.local.mvp.presenter.topic.TopicRevisionsPresenter;
import org.jboss.pressgang.ccms.ui.client.local.mvp.view.base.BaseTemplateView;
import org.jboss.pressgang.ccms.ui.client.local.resources.css.TableResources;
import org.jboss.pressgang.ccms.ui.client.local.resources.strings.PressGangCCMSUI;
import org.jboss.pressgang.ccms.ui.client.local.ui.UIUtilities;
import org.jboss.pressgang.ccms.ui.client.local.utilities.EnhancedAsyncDataProvider;
import org.jboss.pressgang.mergelygwt.client.Mergely;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.logging.Logger;

import static com.google.common.base.Preconditions.checkState;

/**
 * A MVP view for displaying a topic's revision history.
 *
 * @author Matthew Casperson
 */
public class TopicRevisionsView extends BaseTemplateView implements TopicRevisionsPresenter.Display {

    private static final Logger LOGGER = Logger.getLogger(TopicRevisionsView.class.getName());

    /**
     * The height of the button panel in the merge view
     */
    private static final int BUTTON_PANEL_HEIGHT = 44;

    /**
     * A button used when rendering the view button column.
     */
    private final DisableableButtonCell viewButtonCell = new DisableableButtonCell();
    /**
     * A button used when rendering the diff button column.
     */
    private final DisableableButtonCell diffButtonCell = new DisableableButtonCell();
    /**
     * Holds the mergely elements and the ok/cancel buttons
     */
    private final DockLayoutPanel diffPanel = new DockLayoutPanel(Style.Unit.PX);
    /**
     * The parent for the mergely elements. Needs to be a layout panel so resize events are propogated.
     */
    private final SimpleLayoutPanel diffParent = new SimpleLayoutPanel();
    /**
     * The done button.
     */
    private final PushButton done = UIUtilities.createPushButton(PressGangCCMSUI.INSTANCE.Done());
    /**
     * The cancel button.
     */
    private final PushButton cancel = UIUtilities.createPushButton(PressGangCCMSUI.INSTANCE.Cancel());
    /**
     * The current instance of the Mergely ui element.
     */
    private Mergely mergely;
    /**
     * true if we are displaying the revisions, and false if we are displaying the mergely ui element.
     */
    private boolean isDisplayingRevisions = true;
    /**
     * true if the diff and view buttons in the revisions cell table are enabled, and false otherwise.
     */
    private boolean buttonsEnabled = true;

    /**
     * The panel that holds the table and pager.
     */
    private final VerticalPanel searchResultsPanel = new VerticalPanel();
    /**
     * The pager used to page over the table results.
     */
    private final SimplePager pager = UIUtilities.createSimplePager();
    /**
     * The table used to display the revisions.
     */
    private final CellTable<RESTTopicCollectionItemV1> results = new CellTable<RESTTopicCollectionItemV1>(
            Constants.MAX_SEARCH_RESULTS, (Resources) GWT.create(TableResources.class));
    /**
     * The column displaying the revision number.
     */
    private final TextColumn<RESTTopicCollectionItemV1> revisionNumber = new TextColumn<RESTTopicCollectionItemV1>() {
        @Override
        public String getValue(@NotNull final RESTTopicCollectionItemV1 object) {
            return object.getItem().getRevision().toString();
        }
    };
    /**
     * The column displaying the revision date.
     */
    private final TextColumn<RESTTopicCollectionItemV1> revisionDate = new TextColumn<RESTTopicCollectionItemV1>() {
        @Override
        public String getValue(@NotNull final RESTTopicCollectionItemV1 object) {
            return DateTimeFormat.getFormat(PredefinedFormat.DATE_LONG).format(object.getItem().getLastModified());
        }
    };
    /**
     * The column displaying the revision message.
     */
    @NotNull
    private final TextColumn<RESTTopicCollectionItemV1> revisionMessage = new TextColumn<RESTTopicCollectionItemV1>() {
        @Override
        @NotNull
        public String getValue(@Nullable final RESTTopicCollectionItemV1 object) {
            if (object != null && object.getItem() != null && object.getItem().getLogDetails() != null && object.getItem().getLogDetails().getMessage() != null) {
                return object.getItem().getLogDetails().getMessage();
            }
            return "";
        }
    };
    /**
     * The column displaying the user that committed the revision.
     */
    @NotNull
    private final TextColumn<RESTTopicCollectionItemV1> revisionUser = new TextColumn<RESTTopicCollectionItemV1>() {
        @Override
        @NotNull
        public String getValue(@Nullable final RESTTopicCollectionItemV1 object) {
            if (object != null && object.getItem() != null && object.getItem().getLogDetails() != null
                    && object.getItem().getLogDetails().getUser() != null
                    && object.getItem().getLogDetails().getUser().getName() != null) {
                return object.getItem().getLogDetails().getUser().getName();
            }
            return "";
        }
    };
    /**
     * The column displaying the minor revision checkbox.
     */
    @NotNull
    private final Column<RESTTopicCollectionItemV1, Boolean> minorRevisionColumn = new Column<RESTTopicCollectionItemV1, Boolean>(
            new DisableableCheckboxCell(false)) {
        @NotNull
        @Override
        public Boolean getValue(@Nullable final RESTTopicCollectionItemV1 object) {
            if (object != null && object.getItem() != null && object.getItem().getLogDetails() != null && object.getItem().getLogDetails().getFlag() != null) {
                final Integer flag = object.getItem().getLogDetails().getFlag();
                return (flag & ServiceConstants.MINOR_CHANGE) == ServiceConstants.MINOR_CHANGE;
            }
            return false;
        }
    };
    /**
     * The column displaying the major revision checkbox.
     */
    @NotNull
    private final Column<RESTTopicCollectionItemV1, Boolean> majorRevisionColumn = new Column<RESTTopicCollectionItemV1, Boolean>(
            new DisableableCheckboxCell(false)) {
        @NotNull
        @Override
        public Boolean getValue(@Nullable final RESTTopicCollectionItemV1 object) {
            if (object != null && object.getItem() != null && object.getItem().getLogDetails() != null && object.getItem().getLogDetails().getFlag() != null) {
                return (object.getItem().getLogDetails().getFlag() & ServiceConstants.MAJOR_CHANGE) == ServiceConstants.MAJOR_CHANGE;
            }
            return false;
        }
    };
    /**
     * The column that displays the button used to view the revision.
     */
    @NotNull
    private final Column<RESTTopicCollectionItemV1, String> viewButton = new Column<RESTTopicCollectionItemV1, String>(
            viewButtonCell) {
        @NotNull
        @Override
        public String getValue(@Nullable final RESTTopicCollectionItemV1 object) {
            viewButtonCell.setEnabled(buttonsEnabled);

            /*
             * The last revision is the same as the topic in the main database. We indicate that by showing the last revision as
             * editable instead of read only.
             */
            if (mainTopic != null && object != null && object.getItem() != null && object.getItem().getRevision().equals(mainTopic.getRevision())) {
                if (revisionTopic == null || revisionTopic.getRevision().equals(mainTopic.getRevision())) {
                    viewButtonCell.setEnabled(false);
                    return PressGangCCMSUI.INSTANCE.CurrentlyEditing();
                } else {
                    return PressGangCCMSUI.INSTANCE.Edit();
                }
            }

            if (revisionTopic == null || (object != null && object.getItem() != null && !revisionTopic.getRevision().equals(object.getItem().getRevision()))) {
                return PressGangCCMSUI.INSTANCE.View();
            }

            viewButtonCell.setEnabled(false);
            return PressGangCCMSUI.INSTANCE.CurrentlyViewing();
        }
    };
    /**
     * The column that displays the diff button.
     */
    @NotNull
    private final Column<RESTTopicCollectionItemV1, String> diffButton = new Column<RESTTopicCollectionItemV1, String>(
            diffButtonCell) {
        @NotNull
        @Override
        public String getValue(@Nullable final RESTTopicCollectionItemV1 object) {
            diffButtonCell.setEnabled(buttonsEnabled);

            if (mainTopic != null) {
                if (object != null && object.getItem() != null && object.getItem().getRevision().equals(mainTopic.getRevision())) {
                    if (revisionTopic == null || revisionTopic.getRevision().equals(mainTopic.getRevision())) {
                        diffButtonCell.setEnabled(false);
                        return PressGangCCMSUI.INSTANCE.CurrentlyEditing();
                    }
                }

                if (revisionTopic == null || (object != null && object.getItem() != null && !revisionTopic.getRevision().equals(object.getItem().getRevision()))) {

                    final String viewingXML = revisionTopic == null ? mainTopic.getXml() : revisionTopic.getXml();
                    @NotNull final String fixedViewingXML = viewingXML == null ? "" : viewingXML.trim();

                    checkState(object.getItem() != null, "The collection item should reference a valid entity.");

                    if (object.getItem().getXml() == null || object.getItem().getXml().trim().isEmpty()) {
                        /* Diffs don't work if there is no XML to compare to */
                        diffButtonCell.setEnabled(false);
                        return PressGangCCMSUI.INSTANCE.NoXML();
                    } else if (object.getItem().getXml().trim().equals(fixedViewingXML)) {
                        /* The XML is the same */
                        diffButtonCell.setEnabled(false);
                        return PressGangCCMSUI.INSTANCE.SameXML();
                    } else {
                        return PressGangCCMSUI.INSTANCE.Diff();
                    }
                }
            }

            diffButtonCell.setEnabled(false);
            return PressGangCCMSUI.INSTANCE.CurrentlyViewing();
        }
    };
    /**
     * The data provider used to display the revisions.
     */
    private EnhancedAsyncDataProvider<RESTTopicCollectionItemV1> provider;
    /**
     * A reference to the displayed revision topic.
     */
    private RESTTopicV1 revisionTopic;
    /**
     * A reference to the displayed topic.
     */
    private RESTTopicV1 mainTopic;

    /**
     * Builds the UI.
     */
    public TopicRevisionsView() {
        super(PressGangCCMSUI.INSTANCE.PressGangCCMS(), PressGangCCMSUI.INSTANCE.SearchResults() + " - " + PressGangCCMSUI.INSTANCE.Revisions());

        LOGGER.info("ENTER TopicRevisionsView()");

        results.addColumn(viewButton, PressGangCCMSUI.INSTANCE.View() + " / " + PressGangCCMSUI.INSTANCE.Edit());
        results.addColumn(diffButton, PressGangCCMSUI.INSTANCE.Diff());
        results.addColumn(revisionNumber, PressGangCCMSUI.INSTANCE.RevisionNumber());
        results.addColumn(revisionDate, PressGangCCMSUI.INSTANCE.RevisionDate());
        results.addColumn(revisionUser, PressGangCCMSUI.INSTANCE.Username());
        results.addColumn(revisionMessage, PressGangCCMSUI.INSTANCE.RevisionMessage());
        results.addColumn(minorRevisionColumn, PressGangCCMSUI.INSTANCE.MinorChange());
        results.addColumn(majorRevisionColumn, PressGangCCMSUI.INSTANCE.MajorChange());

        /*results.addColumnStyleName(0, CSSConstants.TopicRevisionView.TOPIC_REVISION_NUMBER_COULMN);
        results.addColumnStyleName(1, CSSConstants.TopicRevisionView.TOPIC_REVISION_DATE_COULMN);
        results.addColumnStyleName(2, CSSConstants.TopicRevisionView.TOPIC_REVISION_MINOR_RELEASE_COULMN);
        results.addColumnStyleName(3, CSSConstants.TopicRevisionView.TOPIC_REVISION_MAJOR_RELEASE_COULMN);
        results.addColumnStyleName(4, CSSConstants.TopicRevisionView.TOPIC_REVISION_USER_COULMN);
        results.addColumnStyleName(5, CSSConstants.TopicRevisionView.TOPIC_REVISION_MESSAGE_COULMN);
        results.addColumnStyleName(6, CSSConstants.TopicRevisionView.TOPIC_REVISION_VIEW_COULMN);
        results.addColumnStyleName(7, CSSConstants.TopicRevisionView.TOPIC_REVISION_DIFF_COULMN);*/

        searchResultsPanel.addStyleName(CSSConstants.TopicView.SEARCH_RESULTS_PANEL);

        searchResultsPanel.add(results);
        searchResultsPanel.add(pager);

        pager.setDisplay(results);

        final HorizontalPanel buttonPanel = new HorizontalPanel();
        buttonPanel.addStyleName(CSSConstants.TopicRevisionView.TOPIC_REVISION_DIFF_BUTTON_PANEL);
        buttonPanel.add(done);
        buttonPanel.add(cancel);

        diffPanel.addSouth(buttonPanel, BUTTON_PANEL_HEIGHT);
        diffPanel.add(diffParent);
        diffPanel.addStyleName(CSSConstants.TopicRevisionView.TOPIC_REVISION_DIFF_PANEL);

        this.getPanel().setWidget(searchResultsPanel);
    }

    @NotNull
    @Override
    public Column<RESTTopicCollectionItemV1, String> getDiffButton() {
        return diffButton;
    }

    @Override
    public RESTTopicV1 getRevisionTopic() {
        return revisionTopic;
    }

    @Override
    public void setRevisionTopic(final RESTTopicV1 revisionTopic) {
        this.revisionTopic = revisionTopic;
    }

    @NotNull
    @Override
    public Column<RESTTopicCollectionItemV1, String> getViewButton() {
        return viewButton;
    }

    @Override
    public EnhancedAsyncDataProvider<RESTTopicCollectionItemV1> getProvider() {
        return provider;
    }

    @Override
    public void setProvider(@NotNull final EnhancedAsyncDataProvider<RESTTopicCollectionItemV1> provider) {
        this.provider = provider;
        provider.addDataDisplay(results);
    }

    @NotNull
    @Override
    public CellTable<RESTTopicCollectionItemV1> getResults() {
        return results;
    }

    @Override
    public SimplePager getPager() {
        return pager;
    }

    @Override
    public void display(final RESTTopicV1 topic, final boolean readOnly) {
        this.mainTopic = topic;
    }

    @Override
    public void displayRevisions() {
        this.getPanel().setWidget(searchResultsPanel);
        isDisplayingRevisions = true;
    }

    /**
     * Display a mergely diff viewer.
     * <p/>
     * For mergely to display properly, it has to be attached when the parent element is attached to the DOM. If you
     * attach megely when the parent is detached, it will not resize properly.
     *
     * @param lhs         The text for the left hand side
     * @param lhsReadOnly true if the left hand side is read only, and false otherwise
     * @param rhs         The text for the right hand side
     */
    @Override
    public void displayDiff(@NotNull final String lhs, boolean lhsReadOnly, @NotNull final String rhs) {
        diffParent.setWidget(null);
        mergely = new Mergely(lhs, lhsReadOnly, rhs, true, true, Constants.XML_MIME_TYPE, false);
        this.getPanel().setWidget(diffPanel);
        diffParent.setWidget(mergely);
        isDisplayingRevisions = false;
    }

    @Override
    public PushButton getDone() {
        return done;
    }

    @Override
    @Nullable
    public Mergely getMergely() {
        return mergely;
    }

    @Override
    public PushButton getCancel() {
        return cancel;
    }

    @Override
    public boolean isDisplayingRevisions() {
        return isDisplayingRevisions;
    }

    @Override
    public boolean isButtonsEnabled() {
        return buttonsEnabled;
    }

    @Override
    public void setButtonsEnabled(boolean buttonsEnabled) {
        this.buttonsEnabled = buttonsEnabled;
        results.redraw();
    }
}
