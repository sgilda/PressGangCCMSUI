package org.jboss.pressgangccms.client.local.events;

import com.google.gwt.event.shared.GwtEvent;

public class SearchViewEvent extends GwtEvent<SearchViewEventHandler>
{
	public static Type<SearchViewEventHandler> TYPE = new Type<SearchViewEventHandler>();

	@Override
	public Type<SearchViewEventHandler> getAssociatedType()
	{
		return TYPE;
	}

	@Override
	protected void dispatch(final SearchViewEventHandler handler)
	{
		handler.onSearchViewOpen(this);
	}
}
