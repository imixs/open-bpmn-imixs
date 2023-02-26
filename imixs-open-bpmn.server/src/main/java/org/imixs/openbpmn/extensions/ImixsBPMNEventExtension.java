/********************************************************************************
 * Copyright (c) 2022 Imixs Software Solutions GmbH and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the Eclipse
 * Public License v. 2.0 are satisfied: GNU General Public License, version 2
 * with the GNU Classpath Exception which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 ********************************************************************************/
package org.imixs.openbpmn.extensions;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import javax.json.JsonObject;

import org.eclipse.glsp.graph.GModelElement;
import org.openbpmn.bpmn.BPMNModel;
import org.openbpmn.bpmn.BPMNTypes;
import org.openbpmn.bpmn.elements.Event;
import org.openbpmn.bpmn.elements.core.BPMNElement;
import org.openbpmn.glsp.jsonforms.DataBuilder;
import org.openbpmn.glsp.jsonforms.SchemaBuilder;
import org.openbpmn.glsp.jsonforms.UISchemaBuilder;
import org.openbpmn.glsp.jsonforms.UISchemaBuilder.Layout;
import org.w3c.dom.Element;

/**
 * This is the Default BPMNEvent extension providing the JSONForms shemata.
 *
 * @author rsoika
 *
 */
public class ImixsBPMNEventExtension extends ImixsBPMNExtension {

    private static Logger logger = Logger.getLogger(ImixsBPMNTaskExtension.class.getName());

    public ImixsBPMNEventExtension() {
        super();
    }

    @Override
    public int getPriority() {
        return 101;
    }

    /**
     * The ImixsBPMNTaskExtension can only be applied to a BPMN Task element
     */
    @Override
    public boolean handlesElementTypeId(final String elementTypeId) {
        return BPMNTypes.CATCH_EVENT.equals(elementTypeId);
    }

    /**
     * This Extension is for BPMN Task Elements only
     * <p>
     * The method also verifies if the element has a imixs:processid attribute. This
     * attribute is added in the 'addExtesnion' method call
     */
    @Override
    public boolean handlesBPMNElement(final BPMNElement bpmnElement) {

        if (bpmnElement instanceof Event) {
            Event event = (Event) bpmnElement;
            if (event.getType().equals(BPMNTypes.CATCH_EVENT)) {
                if (event.hasAttribute(getNamespace() + ":activityid")) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * This method adds a unique identifier to the corresponding BPMNElement
     */
    @Override
    public void addExtension(final BPMNElement bpmnElement) {

        if (bpmnElement instanceof Event) {
            bpmnElement.setExtensionAttribute(getNamespace(), "activityid", "10");
        }
    }

    /**
     * This Helper Method generates a JSON Object with the BPMNElement properties.
     * <p>
     * This json object is used on the GLSP Client to generate the EMF JsonForms
     */
    @Override
    public void buildPropertiesForm(final BPMNElement bpmnElement, final DataBuilder dataBuilder,
            final SchemaBuilder schemaBuilder, final UISchemaBuilder uiSchemaBuilder) {

        BPMNModel model = bpmnElement.getModel();
        Element elementNode = bpmnElement.getElementNode();

        /***********
         * Data
         */
        dataBuilder //
                .addData("activityid", bpmnElement.getExtensionAttribute(getNamespace(), "activityid")) //
                .addData("txtactivityresult",
                        ImixsExtensionUtil.getItemValueString(model, elementNode, "txtactivityresult")) //

                .addData("txtbusinessruleengine",
                        ImixsExtensionUtil.getItemValueString(model, elementNode, "txtbusinessruleengine")) //
                .addData("txtbusinessrule",
                        ImixsExtensionUtil.getItemValueString(model, elementNode, "txtbusinessrule")) //

                .addData("rtfresultlog",
                        ImixsExtensionUtil.getItemValueString(model, elementNode, "rtfresultlog")); //

        // set public result
        String keyPublicResult = ImixsExtensionUtil.getItemValueString(model, elementNode, "keypublicresult");
        if ("0".equals(keyPublicResult)) {
            keyPublicResult = "No";
        } else {
            keyPublicResult = "Yes";
        }

        dataBuilder.addData("keypublicresult", keyPublicResult); //

        /***********
         * Schema
         */

        String[] publicEventOptions = { "Yes", "No" };
        schemaBuilder //
                .addProperty("activityid", "string", null) //
                .addProperty("txtactivityresult", "string",
                        "Optional Execution Result. Additional item values can be defined here. ") //
                .addProperty("txtbusinessruleengine", "string", "") //
                .addProperty("txtbusinessrule", "string",
                        "A business rule can also provide an optional result object with new or updated item values. " +
                                "The item values stored in the result object will be applied to the current process instance.") //
                .addProperty("rtfresultlog", "string", "") //
                .addProperty("keypublicresult", "string", "Yes", publicEventOptions);

        /***********
         * UISchema
         */
        Map<String, String> radioOption = new HashMap<>();
        radioOption.put("format", "radio");
        Map<String, String> multilineOption = new HashMap<>();
        multilineOption.put("multi", "true");
        uiSchemaBuilder //
                .addCategory("Workflow") //
                .addLayout(Layout.HORIZONTAL) //
                .addElement("activityid", "Event ID", null) //
                .addElement("keypublicresult", "Pubilc Event", radioOption) //
                .addLayout(Layout.VERTICAL) //
                .addElement("txtactivityresult", "Workflow Result", multilineOption) //
                .addCategory("History") //
                .addElement("rtfresultlog", "Log Entry", multilineOption) //
                .addCategory("Business Rule") //
                .addElement("txtbusinessruleengine", "Engine", null) //
                .addElement("txtbusinessrule", "Rule", multilineOption); //

    }

    /**
     * This method updates the BPMN properties and also the imixs processid.
     * The processID is also updated for the frontend.
     */
    @Override
    public void updatePropertiesData(final JsonObject json, final String category, final BPMNElement bpmnElement,
            final GModelElement gNodeElement) {

        // we are only interested in category Workflow and History
        if (!"Workflow".equals(category) && !"History".equals(category) && !"Business Rule".equals(category)) {
            return;
        }

        BPMNModel model = bpmnElement.getModel();
        Element elementNode = bpmnElement.getElementNode();

        bpmnElement.setExtensionAttribute(getNamespace(), "activityid",
                json.getString("activityid", "0"));
        ImixsExtensionUtil.setItemValue(model, elementNode, "txtactivityresult", "xs:string",
                json.getString("txtactivityresult", ""));

        ImixsExtensionUtil.setItemValue(model, elementNode, "rtfresultlog", "xs:string",
                json.getString("rtfresultlog", ""));

        String keyPublicResult = json.getString("keypublicresult", "Yes");
        if ("Yes".equals(keyPublicResult)) {
            keyPublicResult = "1";
        } else {
            keyPublicResult = "0";
        }
        ImixsExtensionUtil.setItemValue(model, elementNode, "keypublicresult", "xs:string", keyPublicResult);

        // Rules

        ImixsExtensionUtil.setItemValue(model, elementNode, "txtbusinessruleengine", "xs:string",
                json.getString("txtbusinessruleengine", ""));
        ImixsExtensionUtil.setItemValue(model, elementNode, "txtbusinessrule", "xs:string",
                json.getString("txtbusinessrule", ""));
    }

}
