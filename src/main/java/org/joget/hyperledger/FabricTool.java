package org.joget.hyperledger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.hyperledger.fabric.sdk.Channel;
import org.hyperledger.fabric.sdk.HFClient;
import org.hyperledger.fabric_ca.sdk.HFCAClient;
import org.joget.apps.app.lib.JsonTool;
import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.service.AppService;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.form.model.FormRow;
import org.joget.apps.form.model.FormRowSet;
import org.joget.apps.form.service.FormUtil;
import org.joget.commons.util.LogUtil;
import org.joget.commons.util.UuidGenerator;
import org.joget.plugin.property.service.PropertyUtil;
import org.joget.workflow.model.WorkflowAssignment;
import org.joget.workflow.model.service.WorkflowManager;
import org.joget.workflow.util.WorkflowUtil;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.context.ApplicationContext;

/**
 * Tool to perform Hyperledger Fabric queries.
 */
public class FabricTool extends JsonTool {

    @Override
    public String getName() {
        return "Hyperledger Fabric Tool";
    }

    @Override
    public String getVersion() {
        return "7.0.0";
    }

    @Override
    public String getDescription() {
        return "Tool to perform Hyperledger Fabric queries and updates";
    }

    @Override
    public String getLabel() {
        return "Hyperledger Fabric Tool";
    }

    @Override
    public String getClassName() {
        return getClass().getName();
    }

    @Override
    public String getPropertyOptions() {
        AppDefinition appDef = AppUtil.getCurrentAppDefinition();
        String appId = appDef.getId();
        String appVersion = appDef.getVersion().toString();
        Object[] arguments = new Object[]{appId, appVersion, appId, appVersion};
        return AppUtil.readPluginResource(getClass().getName(), "/properties/fabricTool.json", arguments, true, "messages/fabric");
    }

    @Override
    public Object execute(Map properties) {
        boolean status = true;

        // debug mode flag to output additional log messages
        boolean debug = Boolean.parseBoolean((String) properties.get("debugMode"));

        // get workflow assignment to process hash variables
        WorkflowAssignment wfAssignment = (WorkflowAssignment) properties.get("workflowAssignment");

        try {
            // flag to register a new user
            boolean registerNewUser = Boolean.parseBoolean((String) properties.get("registerNewUser"));

            // user credentials
            String userId = WorkflowUtil.processVariable((String) properties.get("userId"), null, wfAssignment);
            String userCert = WorkflowUtil.processVariable((String) properties.get("userCert"), null, wfAssignment);
            String userPrivateKey = WorkflowUtil.processVariable((String) properties.get("userPrivateKey"), null, wfAssignment);
            String affiliation = WorkflowUtil.processVariable((String) properties.get("affiliation"), null, wfAssignment);
            String mspId = WorkflowUtil.processVariable((String) properties.get("mspId"), null, wfAssignment);

            // admin credentials to register a new user
            String adminId = WorkflowUtil.processVariable((String) properties.get("adminId"), null, wfAssignment);
            String adminSecret = WorkflowUtil.processVariable((String) properties.get("adminSecret"), null, wfAssignment);

            // CA, peer, orderer and channel configuration
            Map<String, String> connectionProfileMap = FabricUtil.parseConnectionProfileJson(WorkflowUtil.processVariable((String) properties.get("connectionProfileJson"), null, wfAssignment));
            String caUrl = WorkflowUtil.processVariable((String) connectionProfileMap.get("caUrl"), null, wfAssignment);
            String caCert = WorkflowUtil.processVariable((String) connectionProfileMap.get("caCert"), null, wfAssignment);
            String peerName = WorkflowUtil.processVariable((String) connectionProfileMap.get("peerName"), null, wfAssignment);
            String peerUrl = WorkflowUtil.processVariable((String) connectionProfileMap.get("peerUrl"), null, wfAssignment);
            String peerCert = WorkflowUtil.processVariable((String) connectionProfileMap.get("peerCert"), null, wfAssignment);
            String ordererName = WorkflowUtil.processVariable((String) connectionProfileMap.get("ordererName"), null, wfAssignment);
            String ordererUrl = WorkflowUtil.processVariable((String) connectionProfileMap.get("ordererUrl"), null, wfAssignment);
            String ordererCert = WorkflowUtil.processVariable((String) connectionProfileMap.get("ordererCert"), null, wfAssignment);
            String channelName = WorkflowUtil.processVariable((String) connectionProfileMap.get("channelName"), null, wfAssignment);

            // chaincode and query settings
            String transactionType = (String) properties.get("transactionType");
            boolean isUpdateTransaction = "update".equals(transactionType);
            String chaincodeId = WorkflowUtil.processVariable((String) properties.get("chaincodeId"), null, wfAssignment);
            String functionName = WorkflowUtil.processVariable((String) properties.get("functionName"), null, wfAssignment);
            ArrayList<String> argList = new ArrayList<>();
            Object[] paramsValues = (Object[]) properties.get("functionArgs");
            for (Object o : paramsValues) {
                String args = "";
                if (o instanceof Map) {
                    Map mapping = (HashMap) o;
                    args = mapping.get("functionArgs").toString();
                } else if (o != null) {
                    args = o.toString();
                }
                argList.add(WorkflowUtil.processVariable(args, "", wfAssignment));
            }
            String[] functionArgs = argList.toArray(new String[0]);

            LogUtil.info(getClass().getName(), "Invoking Hyperledger Fabric chaincode " + transactionType + " " + functionName);

            // get user
            FabricUser appUser;
            if (registerNewUser) {
                // create fabric-ca client
                HFCAClient caClient = FabricUtil.getHfCaClient(caUrl, caCert);

                // enroll or load admin
                FabricUser admin = FabricUtil.getAdmin(caClient, adminId, adminSecret, affiliation, mspId);
                LogUtil.info(getClass().getName(), admin.toString());

                // register and enroll new user
                appUser = FabricUtil.getUser(caClient, admin, userId, affiliation, mspId);
                LogUtil.info(getClass().getName(), appUser.toString());
            } else {
                // get user object based on provided cert and private key
                appUser = FabricUtil.getUser(userId, affiliation, mspId, userCert, userPrivateKey);
            }
            if (debug) {
                LogUtil.info(getClass().getName(), appUser.toString());
            }

            // get HFC client instance
            HFClient client = FabricUtil.getHfClient();
            // set user context
            client.setUserContext(appUser);

            // get HFC channel using the client
            Channel channel = FabricUtil.getChannel(client, peerName, peerUrl, peerCert, ordererName, ordererUrl, ordererCert, channelName);
            if (debug) {
                LogUtil.info(getClass().getName(), "Channel: " + channel.getName());
            }

            if (isUpdateTransaction) {
                // update blockchain
                FabricUtil.updateBlockChain(client, channel, chaincodeId, functionName, functionArgs);

            } else {
                // query blockchain
                String queryResponse = FabricUtil.queryBlockChain(client, channelName, chaincodeId, functionName, functionArgs);
                if (debug) {
                    LogUtil.info(getClass().getName(), "Query Response:" + queryResponse);
                }
                // create JSON object from response
                JSONObject jsonObj;
                if (queryResponse.startsWith("[")) {
                    // query response is an array, move it into an object property
                    JSONArray resultArray = new JSONArray(queryResponse);
                    jsonObj = new JSONObject();
                    jsonObj.put("defaultResultArray", resultArray);
                    String multirowBaseObjectName = (String) properties.get("multirowBaseObject");
                    if (multirowBaseObjectName == null || multirowBaseObjectName.isEmpty()) {
                        properties.put("multirowBaseObject", "defaultResultArray");
                        Object[] fieldMapping = (Object[]) properties.get("fieldMapping");
                        if (fieldMapping != null) {
                            for (Object o: fieldMapping) {
                                Map mapping = (HashMap) o;
                                String newJsonObjectName = "defaultResultArray." + mapping.get("jsonObjectName");
                                mapping.put("jsonObjectName", newJsonObjectName);
                            }
                        }
                    }
                } else {
                    jsonObj = new JSONObject(queryResponse);
                }

                // store response
                Map object = getProperties(jsonObj);
                storeToForm(wfAssignment, properties, object);
                storeToWorkflowVariable(wfAssignment, properties, object);
            }

        } catch (Exception e) {
            status = false;
            LogUtil.error(getClass().getName(), e, e.getMessage());
        }
        LogUtil.info(getClass().getName(), "Transaction Status: " + status);

        // update status result
        storeStatusToForm(wfAssignment, properties, Boolean.toString(status));
        storeStatusToWorkflowVariable(wfAssignment, properties, Boolean.toString(status));

        return status;
    }

    @Override
    protected void storeToForm(WorkflowAssignment wfAssignment, Map properties, Map object) {
        super.storeToForm(wfAssignment, properties, object);
        boolean debug = Boolean.parseBoolean((String) properties.get("debugMode"));
        if (debug) {
            String formDefId = (String) properties.get("formDefId");
            String functionName = WorkflowUtil.processVariable((String) properties.get("functionName"), null, wfAssignment);
            LogUtil.info(getClass().getName(), "Transaction response for " + functionName + " saved to form " + formDefId);
        }
    }

    @Override
    protected void storeToWorkflowVariable(WorkflowAssignment wfAssignment, Map properties, Map object) {
        super.storeToWorkflowVariable(wfAssignment, properties, object);
        boolean debug = Boolean.parseBoolean((String) properties.get("debugMode"));
        if (debug) {
            String functionName = WorkflowUtil.processVariable((String) properties.get("functionName"), null, wfAssignment);
            LogUtil.info(getClass().getName(), "Transaction response for " + functionName + " saved to workflow variables");
        }
    }
    
    @Override
    protected FormRow getRow(WorkflowAssignment wfAssignment, String multirowBaseObjectName, Integer rowNumber, Object[] fieldMapping, Map object) {
        return super.getRow(wfAssignment, multirowBaseObjectName, rowNumber, fieldMapping, object);
    }

    @Override
    protected Object getObjectFromMap(String key, Map object) {
        return super.getObjectFromMap(key, object);
    }

    /**
     * Store status value into a mapped form field
     * @param wfAssignment
     * @param properties
     * @param status 
     */
    protected void storeStatusToForm(WorkflowAssignment wfAssignment, Map properties, String status) {
        // set status to mapped field
        String formDefId = (String) properties.get("statusFormDefId");
        String fieldName = (String) properties.get("statusFormMapping");
        if (formDefId != null && !formDefId.isEmpty() && fieldName != null && !fieldName.isEmpty()) {
            ApplicationContext ac = AppUtil.getApplicationContext();
            AppService appService = (AppService) ac.getBean("appService");
            AppDefinition appDef = (AppDefinition) properties.get("appDef");
            FormRow row = new FormRow();
            if (FormUtil.PROPERTY_ID.equals(fieldName)) {
                row.setId(status);
            } else {
                row.put(fieldName, status);
            }
            if (row.getId() == null || row.getId().trim().isEmpty()) {
                if (wfAssignment != null) {
                    row.setId(wfAssignment.getProcessId());
                } else {
                    row.setId(UuidGenerator.getInstance().getUuid());
                }
            }
            // save form data
            FormRowSet rowSet = new FormRowSet();
            rowSet.add(row);
            appService.storeFormData(appDef.getId(), appDef.getVersion().toString(), formDefId, rowSet, null);
            boolean debug = Boolean.parseBoolean((String) properties.get("debugMode"));
            if (debug) {
                LogUtil.info(getClass().getName(), "Transaction status " + status + " saved to form field " + formDefId + "." + fieldName);
            }
        }
    }

    /**
     * Store status value into a mapped workflow variable
     * @param wfAssignment
     * @param properties
     * @param status 
     */
    protected void storeStatusToWorkflowVariable(WorkflowAssignment wfAssignment, Map properties, String status) {
        String statusVariableMapping = (String) properties.get("statusVariableMapping");
        if (statusVariableMapping != null && !statusVariableMapping.isEmpty()) {
            ApplicationContext ac = AppUtil.getApplicationContext();
            WorkflowManager workflowManager = (WorkflowManager) ac.getBean("workflowManager");
            workflowManager.activityVariable(wfAssignment.getActivityId(), statusVariableMapping, status);
            boolean debug = Boolean.parseBoolean((String) properties.get("debugMode"));
            if (debug) {
                LogUtil.info(getClass().getName(), "Transaction status " + status + " saved to workflow variable " + statusVariableMapping);
            }
        }
    }

    /**
     * Convenient method used by system to parse a JSON object into a Map.
     * Copied from org.joget.plugin.property.service.PropertyUtil to avoid OSGI classloading conflicts.
     * @param obj
     * @return 
     */
    protected Map<String, Object> getProperties(JSONObject obj) {
        Map<String, Object> properties = new HashMap<>();
        try {
            if (obj != null) {
                Iterator keys = obj.keys();
                while (keys.hasNext()) {
                    String key = (String) keys.next();
                    if (key.startsWith(PropertyUtil.PROPERTIES_EDITOR_METAS)) {
                        //ignore
                    } else if (!obj.isNull(key)) {
                        Object value = obj.get(key);
                        if (value instanceof JSONArray) {
                            properties.put(key, getProperties((JSONArray) value));
                        } else if (value instanceof JSONObject) {
                            properties.put(key, getProperties((JSONObject) value));
                        } else {
                            String stringValue = obj.getString(key);
                            if ("{}".equals(stringValue)) {
                                properties.put(key, new HashMap<>());
                            } else {
                                properties.put(key, stringValue);
                            }
                        }
                    } else {
                        properties.put(key, "");
                    }
                }
            }
        } catch (Exception e) {
        }
        return properties;
    }

    /**
     * Convenient method used by system to parse a JSON array into a Map array.
     * Copied from org.joget.plugin.property.service.PropertyUtil to avoid OSGI classloading conflicts.
     * @param arr
     * @return 
     * @throws java.lang.Exception 
     */
    protected Object[] getProperties(JSONArray arr) throws Exception {
        Collection<Object> array = new ArrayList<>();
        if (arr != null && arr.length() > 0) {
            for (int i = 0; i < arr.length(); i++) {
                Object value = arr.get(i);
                if (value != null) {
                    if (value instanceof JSONArray) {
                        array.add(getProperties((JSONArray) value));
                    } else if (value instanceof JSONObject) {
                        array.add(getProperties((JSONObject) value));
                    } else if (value instanceof String) {
                        array.add(value);
                    }
                }
            }
        }
        return array.toArray();
    }

}
