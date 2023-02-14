package org.joget.hyperledger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.ArrayUtils;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.datalist.model.DataList;
import org.joget.apps.datalist.model.DataListBinderDefault;
import org.joget.apps.datalist.model.DataListCollection;
import org.joget.apps.datalist.model.DataListColumn;
import org.joget.apps.datalist.model.DataListFilterQueryObject;
import org.joget.commons.util.LogUtil;
import org.joget.commons.util.PagingUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Datalist binder to retrieve JSON results from a Hyperledger Fabric query.
 */
public class FabricListBinder extends DataListBinderDefault {

    @Override
    public String getName() {
        return "Hyperledger Fabric Datalist Binder";
    }

    @Override
    public String getVersion() {
        return "7.0.0";
    }

    @Override
    public String getDescription() {
        return "Datalist Binder for Hyperledger Fabric queries";
    }

    @Override
    public String getLabel() {
        return "Hyperledger Fabric Datalist Binder";        
    }

    @Override
    public String getClassName() {
        return getClass().getName();
    }

    @Override
    public String getPropertyOptions() {
        return AppUtil.readPluginResource(getClass().getName(), "/properties/fabricListBinder.json", null, true, "messages/fabric");        
    }

    @Override
    public DataListColumn[] getColumns() {
        if (columns == null) {
            if (loadedData == null) {
                loadedData = FabricUtil.executeQueryFromProperties(getProperties());
            }
            try {
                String[] propertyKeys = getNestedPropertyKeys();
                
                // get columns
                Collection<DataListColumn> list = new ArrayList<>();
                JSONObject jsonObj = (JSONObject)loadedData.get(0);
                for (Iterator k=jsonObj.keys(); k.hasNext();) {
                    String key = (String)k.next();
                    if (ArrayUtils.contains(propertyKeys, key)) {
                        JSONObject propertyObj = jsonObj.getJSONObject(key);
                        for (Iterator p=propertyObj.keys(); p.hasNext();) {
                            String propKey = (String)p.next();
                            list.add(new DataListColumn(propKey, propKey, false));
                        }
                    } else {
                        list.add(new DataListColumn(key, key, false));
                    }
                }
                columns = list.toArray(new DataListColumn[]{});
            } catch (JSONException ex) {
                LogUtil.error(getClass().getName(), ex, ex.getMessage());
            }
        }
        return columns;
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

    @Override
    public String getPrimaryKeyColumnName() {
        String primaryKeyColumnName = getPropertyString("primaryKey");
        if (primaryKeyColumnName == null || primaryKeyColumnName.isEmpty()) {
            primaryKeyColumnName = "id";
        }
        return primaryKeyColumnName;
    }

    @Override
    public DataListCollection getData(DataList dl, Map map, DataListFilterQueryObject[] filterQueryObjects, String sort, Boolean desc, Integer start, Integer rows) {
        // TODO: handle filterQueryObjects
        // NOTE: Use CouchDB as the state DB for Hyperledger Fabric for better queries https://hyperledger-fabric.readthedocs.io/en/release-1.3/couchdb_tutorial.html
        if (loadedData == null) {
            loadedData = FabricUtil.executeQueryFromProperties(getProperties());
        }
        List<Map<String,String>> dataList = new ArrayList<>();
        try {
            for (int i=0; i<loadedData.length(); i++) {
                Map<String,String> row = new HashMap<>();
                JSONObject jsonObj = (JSONObject)loadedData.get(i);
                String[] propertyKeys = getNestedPropertyKeys();
                for (Iterator k=jsonObj.keys(); k.hasNext();) {
                    String key = (String)k.next();
                    if (ArrayUtils.contains(propertyKeys, key)) {
                        JSONObject propertyObj = jsonObj.getJSONObject(key);
                        for (Iterator p=propertyObj.keys(); p.hasNext();) {
                            String propKey = (String)p.next();
                            row.put(propKey, propertyObj.getString(propKey));
                        }
                    } else {
                        row.put(key, jsonObj.getString(key));
                    }
                }
                dataList.add(row);
            }
        } catch (JSONException ex) {
            LogUtil.error(getClass().getName(), ex, ex.getMessage());
        }            
        
        List list = PagingUtils.sortAndPage(dataList, sort, desc, start, rows);
        DataListCollection data = new DataListCollection();
        data.addAll(list);
        return data;
    }

    @Override
    public int getDataTotalRowCount(DataList dl, Map map, DataListFilterQueryObject[] filterQueryObjects) {
        // TODO: handle filterQueryObjects
        if (loadedData == null) {
            loadedData = FabricUtil.executeQueryFromProperties(getProperties());
        }
        int count = loadedData.length();
        return count;
    }

    protected JSONArray loadedData = null;
    protected DataListColumn[] columns = null;
    
}
