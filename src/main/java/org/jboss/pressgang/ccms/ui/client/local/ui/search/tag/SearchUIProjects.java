package org.jboss.pressgang.ccms.ui.client.local.ui.search.tag;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.jboss.pressgang.ccms.rest.v1.collections.RESTTagCollectionV1;
import org.jboss.pressgang.ccms.rest.v1.collections.items.RESTProjectCollectionItemV1;
import org.jboss.pressgang.ccms.rest.v1.collections.items.RESTTagCollectionItemV1;
import org.jboss.pressgang.ccms.ui.client.local.constants.Constants;
import org.jboss.pressgang.ccms.ui.client.local.resources.strings.PressGangCCMSUI;
import org.jboss.pressgang.ccms.ui.client.local.sort.SearchUINameSort;
import org.jboss.pressgang.ccms.ui.client.local.ui.search.SearchViewBase;

import com.google.gwt.user.client.ui.TriStateSelectionState;

/**
 * The REST interface does not define a hierarchy or projects->categories->tags. Instead, tags belong to both categories and
 * projects, but the projects and categories don't have any direct relationship.
 * 
 * When being viewed however tags are displayed in the projects->categories->tags hierarchy. This class defines the top level
 * collection of projects.
 * 
 * @author Matthew Casperson
 */
public class SearchUIProjects implements SearchViewBase {
    /** The string that appears in the query to indicate the presence or absence of a tag */
    private static final String TAG_PREFIX = "tag";
    /** Indicates that a tag should be present in the returned topics */
    private static final int TAG_INCLUDED = 1;
    /** Indicates that a tag should be absent in the returned topics */
    private static final int TAG_EXCLUDED = 0;

    private final LinkedList<SearchUIProject> projects = new LinkedList<SearchUIProject>();

    public List<SearchUIProject> getProjects() {
        return projects;
    }

    public SearchUIProjects() {

    }

    public SearchUIProjects(final RESTTagCollectionV1 tags) {
        initialize(tags);
    }

    final public void initialize(final RESTTagCollectionV1 tags) {
        if (tags == null) {
            throw new IllegalArgumentException("tags parameter cannot be null");
        }

        for (final RESTTagCollectionItemV1 tag : tags.returnExistingAndAddedCollectionItems()) {
            if (tag.getItem().getProjects() == null) {
                throw new IllegalArgumentException("tag.getItem().getProjects() cannot be null");
            }

            /* Tags to be removed should not show up */
            for (final RESTProjectCollectionItemV1 project : tag.getItem().getProjects().returnExistingCollectionItems()) {
                final SearchUIProject searchUIProject = new SearchUIProject(project);
                if (!projects.contains(searchUIProject)) {
                    searchUIProject.populateCategories(project, tags);
                    projects.add(searchUIProject);
                }
            }
        }

        Collections.sort(projects, new SearchUINameSort());

        /*
         * Add the common project to the start of the list. Do this after all the projects have been added, so it won't get
         * confused with a project that might be called common.
         */
        final SearchUIProject common = new SearchUIProject(PressGangCCMSUI.INSTANCE.Common());
        common.populateCategoriesWithoutProject(tags);
        if (common.getChildCount() != 0) {
            projects.addFirst(common);
        }
    }

    @Override
    public String getSearchQuery(final boolean includeQueryPrefix) {

        final StringBuilder builder = new StringBuilder(includeQueryPrefix ? Constants.QUERY_PATH_SEGMENT_PREFIX_WO_SEMICOLON
                : "");

        for (final SearchUIProject project : projects) {
            for (final SearchUICategory category : project.getCategories()) {
                for (final SearchUITag tag : category.getMyTags()) {
                    if (tag.getState() != TriStateSelectionState.NONE) {
                        builder.append(";");

                        if (tag.getState() == TriStateSelectionState.SELECTED) {
                            builder.append(TAG_PREFIX + tag.getTag().getItem().getId() + "=" + TAG_INCLUDED);
                        } else if (tag.getState() == TriStateSelectionState.UNSELECTED) {
                            builder.append(TAG_PREFIX + tag.getTag().getItem().getId() + "=" + TAG_EXCLUDED);
                        }
                    }
                }
            }
        }

        return builder.toString();
    }
}
