package org.jboss.pressgang.ccms.ui.client.local.ui.editor.topicview;

import com.google.gwt.editor.client.Editor;
import com.google.gwt.user.client.ui.SimplePanel;
import edu.ycp.cs.dh.acegwt.client.ace.AceEditor;
import edu.ycp.cs.dh.acegwt.client.ace.AceEditorData;
import edu.ycp.cs.dh.acegwt.client.ace.AceEditorMode;
import edu.ycp.cs.dh.acegwt.client.tagdb.XMLElementDB;
import edu.ycp.cs.dh.acegwt.client.typo.TypoJS;
import org.jboss.pressgang.ccms.rest.v1.entities.RESTTranslatedTopicV1;
import org.jboss.pressgang.ccms.ui.client.local.constants.CSSConstants;
import org.jboss.pressgang.ccms.ui.client.local.constants.Constants;
import org.jboss.pressgang.ccms.ui.client.local.data.XMLElementDBLoader;

public final class RESTTranslatedTopicV1AdditionalXMLEditor extends SimplePanel implements Editor<RESTTranslatedTopicV1> {
    /**
     * Ace builds from the 17th December 2012 and prior use absolute positioning, and require
     * that the AceEditor being constructed with true. After the 17th December 2012 the ACE
     * editor uses relative positioning, and the AceEditor needs to be constructed with false.
     */
    public final AceEditor translatedAdditionalXML;


    /**
     * @param readOnly           true if the UI created by this editor should be readonly, and false otherwise
     * @param positiveDictionary a reference to the dictionary used by the ACE editor spell checking
     */
    public RESTTranslatedTopicV1AdditionalXMLEditor(final boolean readOnly, final TypoJS positiveDictionary,
            final TypoJS negativeDictionary, final TypoJS negativePhraseDictionary, final XMLElementDBLoader XMLElementDBLoader) {
        final XMLElementDB xmlElementDB = XMLElementDBLoader.getXMLElementDB();
        final AceEditorData data = new AceEditorData();
        data.setXMLElementDB(xmlElementDB);
        data.setNegativeDictionary(negativeDictionary);
        data.setPositiveDictionary(positiveDictionary);
        data.setNegativePhraseDictionary(negativePhraseDictionary);
        data.setRestUrl(xmlElementDB.getRestEndpoint());
        this.translatedAdditionalXML = new AceEditor(false, data, true, false);

        this.addStyleName(CSSConstants.TopicView.TOPIC_XML_VIEW_ACE_PANEL);
        translatedAdditionalXML.addStyleName(CSSConstants.TopicView.TOPIC_XML_VIEW_XML_FIELD);

        translatedAdditionalXML.setReadOnly(readOnly);
        translatedAdditionalXML.setMode(AceEditorMode.DOCBOOK_45);
        translatedAdditionalXML.setThemeByName(Constants.DEFAULT_THEME);

        this.setWidget(translatedAdditionalXML);
    }
}
