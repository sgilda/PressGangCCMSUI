package org.jboss.pressgangccms.client.local.view.base;

import org.jboss.pressgangccms.client.local.constants.CSSConstants;
import org.jboss.pressgangccms.client.local.resources.css.CSSResources;
import org.jboss.pressgangccms.client.local.resources.images.ImageResources;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.SimpleLayoutPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.ToggleButton;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * This class is used to build the standard page template
 * 
 * @author Matthew Casperson
 */
public abstract class BaseTemplateView implements BaseTemplateViewInterface
{
	/** Defines the top level layout that holds the header and the other content */
	private final DockLayoutPanel topLevelLayoutPanel = new DockLayoutPanel(Unit.PX);
	
	/** Defines the panel that holds the page title and the other content */
	private final DockLayoutPanel secondLevelLayoutPanel = new DockLayoutPanel(Unit.EM);
	
	/** Defines the panel that holds the shortcut bar, content and footer */
	private final DockLayoutPanel thirdLevelLayoutPanel = new DockLayoutPanel(Unit.PX);
	
	
	private final SimplePanel headingBanner = new SimplePanel();
	private final SimpleLayoutPanel pageTitleParentLayoutPanel = new SimpleLayoutPanel();
	private final Label pageTitle = new Label();
	private final VerticalPanel shortcutPanel = new VerticalPanel();
	private SimpleLayoutPanel panel = new SimpleLayoutPanel();

	private final FlexTable topActionPanel = new FlexTable();
	private final HorizontalPanel footerPanel = new HorizontalPanel();
	private final Image spinner = new Image(ImageResources.INSTANCE.spinner());
	private final DialogBox waiting = new DialogBox();

	private final PushButton home;
	private final PushButton search;
	private final PushButton searchTranslations;
	private final PushButton bug;
	private final PushButton reports;
	
	public DockLayoutPanel getTopLevelPanel()
	{
		return topLevelLayoutPanel;
	}
	
	@Override
	public SimpleLayoutPanel getPanel()
	{
		return panel;
	}

	public PushButton getReports()
	{
		return reports;
	}

	public PushButton getSearchTranslations()
	{
		return searchTranslations;
	}

	@Override
	public FlexTable getTopActionPanel()
	{
		return topActionPanel;
	}

	@Override
	public PushButton getBug()
	{
		return bug;
	}

	@Override
	public PushButton getSearch()
	{
		return search;
	}

	@Override
	public void setSpinnerVisible(final boolean enabled)
	{
		if (enabled)
		{
			waiting.center();
			waiting.show();
		}
		else
		{
			waiting.hide();
		}
	}

	public BaseTemplateView(final String applicationName, final String pageName)
	{
		/* Iinitialize the loading spinner */
		waiting.setGlassEnabled(true);
		waiting.setWidget(spinner);
		
		/* Set the heading */
		headingBanner.addStyleName(CSSResources.INSTANCE.App().ApplicationHeadingPanel());
		headingBanner.add(new Image(ImageResources.INSTANCE.headingBanner()));
		
		topLevelLayoutPanel.addStyleName(CSSConstants.TOPLEVELLAYOUTPANEL);
		topLevelLayoutPanel.addNorth(headingBanner, 110);
		
		/* Set the second level layout */
		secondLevelLayoutPanel.addStyleName(CSSConstants.SECONDLEVELLAYOUTPANEL);
		topLevelLayoutPanel.add(secondLevelLayoutPanel);
		
		/* Set the page title */
		pageTitle.setText(" " + pageName);
		pageTitle.addStyleName("PageTitle");
		
		pageTitleParentLayoutPanel.add(pageTitle);
		
		pageTitleParentLayoutPanel.addStyleName(CSSConstants.PAGETITLEPARENTLAYOUTPANEL);
		secondLevelLayoutPanel.addNorth(pageTitleParentLayoutPanel, 3);
		
		/* Set the remaining content */
		thirdLevelLayoutPanel.addStyleName(CSSConstants.THIRDLEVELLAYOUTPANEL);
		secondLevelLayoutPanel.add(thirdLevelLayoutPanel);
		
		/* Set the action bar panel */
		topActionPanel.addStyleName(CSSConstants.TOPACTIONPANEL);
		
		thirdLevelLayoutPanel.addNorth(topActionPanel, 64);
		
		/* Set the shortcut bar */
		shortcutPanel.addStyleName(CSSConstants.SHORTCUTPANEL);
		
		thirdLevelLayoutPanel.addWest(shortcutPanel, 64);
		
		/* Add the content panel */
		panel.addStyleName(CSSConstants.CONTENTLAYOUTPANEL);
		
		thirdLevelLayoutPanel.add(panel);
		
		/* Set the footer panel */
		footerPanel.addStyleName(CSSConstants.FOOTERPANEL);
		
		thirdLevelLayoutPanel.addSouth(footerPanel, 0);	
		
		/* Build the shortcut panel */

		/* Add a spacer */
		final Image spacer = new Image(ImageResources.INSTANCE.transparent48());
		spacer.addStyleName("SpacedButton");
		shortcutPanel.add(spacer);

		home = createPushButton(ImageResources.INSTANCE.home48(), ImageResources.INSTANCE.homeDown48(), ImageResources.INSTANCE.homeHover48(), "SpacedButton");
		shortcutPanel.add(home);

		search = createPushButton(ImageResources.INSTANCE.search48(), ImageResources.INSTANCE.searchDown48(), ImageResources.INSTANCE.searchHover48(), "SpacedButton");
		shortcutPanel.add(search);

		searchTranslations = createPushButton(ImageResources.INSTANCE.searchTranslations48(), ImageResources.INSTANCE.searchTranslationsDown48(), ImageResources.INSTANCE.searchTranslationsHover48(), ImageResources.INSTANCE.searchTranslationsDisabled48(), "SpacedButton");
		searchTranslations.setEnabled(false);
		shortcutPanel.add(searchTranslations);

		reports = createPushButton(ImageResources.INSTANCE.reports48(), ImageResources.INSTANCE.reportsDown48(), ImageResources.INSTANCE.reportsHover48(), ImageResources.INSTANCE.reportsDisabled48(), "SpacedButton");
		reports.setEnabled(false);
		shortcutPanel.add(reports);

		bug = createPushButton(ImageResources.INSTANCE.bug48(), ImageResources.INSTANCE.bugDown48(), ImageResources.INSTANCE.bugHover48(), "SpacedButton");
		shortcutPanel.add(bug);
	}
	
	protected void addRightAlignedActionButtonPaddingPanel()
	{
		final int rows = this.getTopActionPanel().getRowCount();
		int columns = 0;
		if (rows != 0)
		{
			columns = this.getTopActionPanel().getCellCount(0);
		}

		this.getTopActionPanel().setWidget(0, columns, new SimplePanel());
		this.getTopActionPanel().getCellFormatter().addStyleName(0, columns, CSSConstants.RIGHTALIGNEDACTIONBUTTONS);
	}

	protected void addActionButton(final Widget widget)
	{
		final int rows = this.getTopActionPanel().getRowCount();
		int columns = 0;
		if (rows != 0)
		{
			columns = this.getTopActionPanel().getCellCount(0);
		}

		this.getTopActionPanel().setWidget(0, columns, widget);
	}

	protected PushButton createPushButton(final ImageResource up, final ImageResource down, final ImageResource hover)
	{
		final PushButton retvalue = new PushButton(new Image(up), new Image(down));
		//retvalue.getUpHoveringFace().setImage(new Image(hover));
		return retvalue;
	}

	protected PushButton createPushButton(final ImageResource up, final ImageResource down, final ImageResource hover, final String className)
	{
		final PushButton retvalue = new PushButton(new Image(up), new Image(down));
		//retvalue.getUpHoveringFace().setImage(new Image(hover));
		retvalue.addStyleName(className);
		return retvalue;
	}

	protected PushButton createPushButton(final ImageResource up, final ImageResource down, final ImageResource hover, final ImageResource disabled)
	{
		final PushButton retvalue = new PushButton(new Image(up), new Image(down));
		//retvalue.getUpHoveringFace().setImage(new Image(hover));
		retvalue.getUpDisabledFace().setImage(new Image(disabled));
		return retvalue;
	}

	protected PushButton createPushButton(final ImageResource up, final ImageResource down, final ImageResource hover, final ImageResource disabled, final String className)
	{
		final PushButton retvalue = new PushButton(new Image(up), new Image(down));
		//retvalue.getUpHoveringFace().setImage(new Image(hover));
		retvalue.getUpDisabledFace().setImage(new Image(disabled));
		retvalue.addStyleName(className);
		return retvalue;
	}

	protected ToggleButton createToggleButton(final ImageResource up, final ImageResource down, final ImageResource hover)
	{
		final ToggleButton retvalue = new ToggleButton(new Image(up), new Image(down));
		//retvalue.getUpHoveringFace().setImage(new Image(hover));
		return retvalue;
	}

	protected ToggleButton createToggleButton(final ImageResource up, final ImageResource down, final ImageResource hover, final String className)
	{
		final ToggleButton retvalue = new ToggleButton(new Image(up), new Image(down));
		//retvalue.getUpHoveringFace().setImage(new Image(hover));
		retvalue.addStyleName(className);
		return retvalue;
	}
}