package net.protocol;

import java.util.HashMap;
import java.util.Map;

import utils.JsonHelper;

public class ObjectBundle 
{
	private Map<String, Object> objectMap = new HashMap<>();

    public ObjectBundle(){}

    public Integer getInt(String key){
        return (Integer)this.objectMap.get(key);
    }
    public String getString(String key){
        return (String)this.objectMap.get(key);
    }
    public ObjectBundle getObjectBundle(String key){
        return (ObjectBundle)this.objectMap.get(key);
    }

    public void setInt(String key, Integer value){
        this.objectMap.put(key, value);
    }
    public void setString(String key, String value){
        this.objectMap.put(key, value);
    }
    public void setObjectBundle(String key, ObjectBundle value){
        this.objectMap.put(key, value);
    }
    
    public String serialize(){
    	return JsonHelper.map2json( this.objectMap );
    }
}
