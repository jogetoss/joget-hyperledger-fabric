package org.joget.hyperledger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.ArrayUtils;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.form.model.Element;
import org.joget.apps.form.model.FormBinder;
import org.joget.apps.form.model.FormData;
import org.joget.apps.form.model.FormLoadBinder;
import org.joget.apps.form.model.FormLoadElementBinder;
import org.joget.apps.form.model.FormLoadMultiRowElementBinder;
import org.joget.apps.form.model.FormRow;
import org.joget.apps.form.model.FormRowSet;
import org.joget.commons.util.LogUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Form binder to retrieve JSON results from a Hyperledger Fabric query.
 */
public class FabricFormBinder extends FormBinder implements FormLoadBinder, FormLoadElementBinder, FormLoadMultiRowElementBinder {

    @Override
    public String getName() {
        return "Hyperledger Fabric Form Binder";
    }

    @Override
    public String getVersion() {
        return "6.2.2";
    }

    @Override
    public String getDescription() {
        return "Form Binder for Hyperledger Fabric queries";
    }

    @Override
    public String getLabel() {
        return "Hyperledger Fabric Form Binder";
    }

    @Override
    public String getClassName() {
        return getClass().getName();
    }

    @Override
    public String getPropertyOptions() {
        return AppUtil.readPluginResource(getClass().getName(), "/properties/fabricFormBinder.json", null, true, "messages/fabric");
    }

    @Override
    public FormRowSet load(Element element, String primaryKey, FormData formData) {
        FormRowSet rowSet = new FormRowSet();
        try {
            JSONArray jsonArray = FabricUtil.executeQueryFromProperties(getProperties());
            for (int i = 0; i < jsonArray.length(); i++) {
                FormRow row = new FormRow();
                JSONObject jsonObj = jsonArray.getJSONObject(i);
                String[] propertyKeys = getNestedPropertyKeys();
                for (Iterator k = jsonObj.keys(); k.hasNext();) {
                    String key = (String) k.next();
                    if (ArrayUtils.contains(propertyKeys, key)) {
                        JSONObject propertyObj = jsonObj.getJSONObject(key);
                        for (Iterator p = propertyObj.keys(); p.hasNext();) {
                            String propKey = (String) p.next();
                            row.put(propKey, propertyObj.getString(propKey));
                        }
                    } else {
                        row.put(key, jsonObj.getString(key));
                    }
                }
                rowSet.add(row);
                if (i > 0) {
                    rowSet.setMultiRow(true);
                }
            }
        } catch (JSONException e) {
            LogUtil.error(getClass().getName(), e, e.getMessage());
        }
        return rowSet;
    }

    protected String[] getNestedPropertyKeys() {
        // get nested properties
        Map<String, Object> properties = getProperties();
        Object[] paramsValues = (Object[]) properties.get("nestedProperty");
        List<String> nestedProps = new ArrayList<>();
        for (Object o : paramsValues) {
            Map mapping = (HashMap) o;
            String prop = mapping.get("nestedProperty").toString();
            nestedProps.add(prop);
        }
        String[] propertyKeys = nestedProps.toArray(new String[0]);
        return propertyKeys;
    }

}
