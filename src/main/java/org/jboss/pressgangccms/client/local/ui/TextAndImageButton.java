package org.jboss.pressgangccms.client.local.ui;

import com.google.gwt.editor.client.IsEditor;
import com.google.gwt.editor.client.LeafValueEditor;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Image;

/**
 * http://blog.js-development.com/2010/03/gwt-button-with-image-and-text.html
 * 
 * @author Matthew Casperson
 */
public class TextAndImageButton extends Button 
{
	protected static final String DIV_STYLE = "padding-left:3px; vertical-align:middle;";
	private String text;
	private final Element div = DOM.createElement("div");

	public TextAndImageButton()
	{
		super();
		div.setAttribute("style", DIV_STYLE);
		DOM.insertChild(getElement(), div, 0);		
	}

	public TextAndImageButton(final String text, final ImageResource imageResource)
	{
		setText(text);
		setResource(imageResource);
	}

	public void setResource(final ImageResource imageResource)
	{
		final Image img = new Image(imageResource);
		final String definedStyles = img.getElement().getAttribute("style");
		img.getElement().setAttribute("style", definedStyles + "; vertical-align:middle;");
		DOM.insertBefore(getElement(), img.getElement(), DOM.getFirstChild(getElement()));
	}

	@Override
	public void setText(final String text)
	{
		this.text = text;
		div.setInnerText(text);
	}

	@Override
	public String getText()
	{
		return this.text;
	}
}
