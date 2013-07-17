package org.jboss.pressgang.ccms.ui.client.local.data;

import com.google.gwt.http.client.*;
import org.jetbrains.annotations.NotNull;

/**
 * Holds a copy of the flattened Docbook DTD. This is used for validation, and also to inject the
 * entities (like nbsp) into the XML when rendered. This has to be inline for the XML rendering to
 * work in Firefox, as Firefox will not load external entity files.
 */
public final class DocbookDTD {
    private static String dtd = "";
    private static String ent = "";

    public static String getDtd() {
        return dtd;
    }

    public static String getDtdDoctype() {
        if (ent.isEmpty()) {
            return ent;
        }
        return "<!DOCTYPE section [" + ent + "]>";
    }

    /**
     * Load the DTD file.
     */
    public static void loadDtd() {
        try {
            new RequestBuilder(RequestBuilder.GET, "javascript/xmllint/docbook.dtd").sendRequest("", new RequestCallback() {
                @Override
                public void onResponseReceived(@NotNull final Request req, @NotNull final Response resp) {
                    dtd = resp.getText();
                }

                @Override
                public void onError(@NotNull final Request res, @NotNull final Throwable throwable) {
                    dtd = "";
                }
            });
        } catch (@NotNull final RequestException e) {
            dtd = "";
        }

        try {
            new RequestBuilder(RequestBuilder.GET, "javascript/xmllint/docbook.ent").sendRequest("", new RequestCallback() {
                @Override
                public void onResponseReceived(@NotNull final Request req, @NotNull final Response resp) {
                    ent = resp.getText();
                }

                @Override
                public void onError(@NotNull final Request res, @NotNull final Throwable throwable) {
                    ent = "";
                }
            });
        } catch (@NotNull final RequestException e) {
            ent = "";
        }
    }
}