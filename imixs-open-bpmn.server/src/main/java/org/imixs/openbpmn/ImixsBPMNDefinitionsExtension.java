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
package org.imixs.openbpmn;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.json.JsonObject;

import org.eclipse.glsp.graph.GModelElement;
import org.openbpmn.bpmn.BPMNModel;
import org.openbpmn.bpmn.BPMNTypes;
import org.openbpmn.bpmn.elements.BPMNProcess;
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
public class ImixsBPMNDefinitionsExtension extends ImixsBPMNExtension {

        private static Logger logger = Logger.getLogger(ImixsBPMNTaskExtension.class.getName());

        public ImixsBPMNDefinitionsExtension() {
                super();
        }

        @Override
        public int getPriority() {
                return 101;
        }

        @Override
        public boolean handlesElementTypeId(final String elementTypeId) {
                return BPMNTypes.PROCESS_TYPE_PUBLIC.equals(elementTypeId);
        }

        /**
         * This Extension is for the default public process only
         */
        @Override
        public boolean handlesBPMNElement(final BPMNElement bpmnElement) {
                if (bpmnElement instanceof BPMNProcess) {
                        return ((BPMNProcess) bpmnElement).isPublicProcess();
                }
                return false;
        }

        /**
         * This Helper Method generates a JSON Object with the BPMNElement properties.
         * <p>
         * This json object is used on the GLSP Client to generate the EMF JsonForms
         */
        @Override
        public void buildPropertiesForm(final BPMNElement bpmnElement, final DataBuilder dataBuilder,
                        final SchemaBuilder schemaBuilder, final UISchemaBuilder uiSchemaBuilder) {

                // find the definitions element
                BPMNModel model = bpmnElement.getModel();
                Element elementNode = model.getDefinitions();
                dataBuilder //
                                .addData("txtworkflowmodelversion",
                                                ImixsExtensionUtil.getItemValueString(model, elementNode,
                                                                "txtworkflowmodelversion"));

                // add Date Objects
                List<String> dateobjects = ImixsExtensionUtil.getItemValueList(model, elementNode,
                                "txttimefieldmapping");
                dataBuilder.addArray("dateobjects");
                for (String _date : dateobjects) {
                        dataBuilder.addObject();
                        String[] dateParts = _date.split("\\|");
                        if (dateParts.length > 1) {
                                dataBuilder.addData("date", dateParts[0].trim());
                                dataBuilder.addData("item", dateParts[1].trim());
                        } else {
                                dataBuilder.addData("item", _date.trim());
                        }

                }
                dataBuilder.closeArrayBuilder();

                // add Field Mapping
                List<String> actors = ImixsExtensionUtil.getItemValueList(model, elementNode, "txtfieldmapping");
                dataBuilder.addArray("actors");
                for (String _actor : actors) {
                        dataBuilder.addObject();
                        String[] actorParts = _actor.split("\\|");
                        if (actorParts.length > 1) {
                                dataBuilder.addData("actor", actorParts[0].trim());
                                dataBuilder.addData("item", actorParts[1].trim());
                        } else {
                                dataBuilder.addData("item", _actor.trim());
                        }

                }
                dataBuilder.closeArrayBuilder();

                // add Plugin list
                List<String> plugins = ImixsExtensionUtil.getItemValueList(model, elementNode, "txtplugins");
                dataBuilder.addArray("plugins");
                for (String _plugin : plugins) {
                        dataBuilder.addObject();
                        dataBuilder.addData("classname",
                                        _plugin);
                }
                dataBuilder.closeArrayBuilder();

                /*
                 * *****************
                 * Schema *
                 *******************/
                schemaBuilder. //
                                addProperty("txtworkflowmodelversion", "string", null);

                schemaBuilder.addArray("dateobjects");
                schemaBuilder.addProperty("date", "string", null, null);
                schemaBuilder.addProperty("item", "string", null, null);

                schemaBuilder.addArray("actors");
                schemaBuilder.addProperty("actor", "string", null, null);
                schemaBuilder.addProperty("item", "string", null, null);

                schemaBuilder.addArray("plugins");
                schemaBuilder.addProperty("classname", "string", null, null);

                Map<String, String> multilineOption = new HashMap<>();
                multilineOption.put("multi", "true");

                /***********
                 * UISchema
                 ***********/
                uiSchemaBuilder. //
                                addCategory("Workflow"). //
                                addLayout(Layout.VERTICAL). //
                                addElement("txtworkflowmodelversion", "Model Version", null). //
                                addElement("dateobjects", "Date Objects", null). //
                                addElement("actors", "Actors", null). //
                                addElement("plugins", "Plugins", null);

        }

        @Override
        public void updatePropertiesData(final JsonObject json, final BPMNElement bpmnElement,
                        final GModelElement gNodeElement) {

                // find the definitions element
                BPMNModel model = bpmnElement.getModel();
                Element elementNode = model.getDefinitions();

                ImixsExtensionUtil.setItemValue(model, elementNode, "txtworkflowmodelversion", "xs:string",
                                json.getString("txtworkflowmodelversion", ""));
                ImixsExtensionUtil.setItemValue(model, elementNode, "txttimefieldmapping", "xs:string",
                                json.getString("txttimefieldmapping", ""));
                ImixsExtensionUtil.setItemValue(model, elementNode, "txtfieldmapping", "xs:string",
                                json.getString("txtfieldmapping", ""));
                ImixsExtensionUtil.setItemValue(model, elementNode, "txtplugins", "xs:string",
                                json.getString("txtplugins", ""));

        }

}