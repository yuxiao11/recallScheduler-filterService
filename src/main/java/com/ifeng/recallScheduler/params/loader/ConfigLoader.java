package com.ifeng.recallScheduler.params.loader;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 加载配置文件的工具类,定义配置文件的加载和数据获取的方法
 * Created by jibin on 2017/6/26.
 */
public class ConfigLoader {
    private Log log = LogFactory.getLog(ConfigLoader.class);
    //	private PropertiesConfiguration propertyConfig;
    private PropertiesConfiguration propertyConfig;


    public ConfigLoader(String configFile) {
        propertyConfig = initConfig(configFile);
    }

    private PropertiesConfiguration initConfig(String configFile) {
        // 加载配置文件
        try {
//			propertyConfig=new Properties();
            propertyConfig=new PropertiesConfiguration();
//			propertyConfig.load(new FileInputStream(configFile));
            //中文读入处理:先设置编码为UTF-8,再load
            propertyConfig.setEncoding("UTF-8");
            //加载配置文件
            propertyConfig.load(configFile);
            // 设置配置发生变化的时候自动载入
            propertyConfig.setReloadingStrategy(new FileChangedReloadingStrategy());
        } catch (Exception e) {
            log.error(e);
        }
        return propertyConfig;
    }

    public Object getValueByKey(String key) {
        Object value = null;
        try {
            value = propertyConfig.getProperty(key);

        } catch (Exception e) {
            log.warn("get " + key + " error:" + e.getMessage());
        }

        return value;
    }

	/*public PropertiesConfiguration getPropertyConfig() {
		return propertyConfig;
	}*/

    public String getString(String key) {
        String value=(String) propertyConfig.getProperty(key);
        return StringUtils.isNotBlank(value)?value.trim():"";
        //return propertyConfig.getString(key, "");
    }

    public int getInt(String key) {
        String value=(String) propertyConfig.getProperty(key);
        return StringUtils.isNotBlank(value)?Integer.parseInt(value.trim()):-1;
        //return propertyConfig.getInt(key, -1);
    }

    public float getFloat(String key) {
        String value=(String) propertyConfig.getProperty(key);
        return StringUtils.isNotBlank(value)?Float.parseFloat(value.trim()):-1;
        //return propertyConfig.getFloat(key, -1);
    }

    public long getLong(String key) {
        String value=(String) propertyConfig.getProperty(key);
        return StringUtils.isNotBlank(value)?Long.parseLong(value.trim()):-1;
        //return propertyConfig.getLong(key, -1);
    }

    public double getDouble(String key) {
        String value=(String) propertyConfig.getProperty(key);
        return StringUtils.isNotBlank(value)?Double.parseDouble(value.trim()):-1;
        //return propertyConfig.getDouble(key, -1);
    }

    public boolean getBoolean(String key) {
        String value=(String) propertyConfig.getProperty(key);
//		return StringUtils.isNotBlank(value)?Boolean.getBoolean(value.trim()):false;
        return propertyConfig.getBoolean(key, false);
    }

	/*public List<Object> getList(String key) {
		return propertyConfig.getList(key,null);
	}*/

    public List<Integer> getIntegerList(String key) {
        List<Integer> intList = new ArrayList<Integer>();
//		intList = (List<Integer>) propertyConfig.getProperty(key);
        try {
            List<Object> value = propertyConfig.getList(key);
            if(value==null){
                return null;
            }
            for(Object o:value){
                intList.add(Integer.parseInt(o.toString()));
            }
//			String listStr=(String) propertyConfig.getProperty(key);
//			if (StringUtils.isNotBlank(listStr)){
//				String[] values=listStr.trim().split(",");
//				for (String o : values) {
//					intList.add(Integer.parseInt(o.toString()));
//				}
//			}
			/*List<Object> value = propertyConfig.getList(key,null);
			if (value == null) {
				return intList;
			}

			for (Object o : value) {
				intList.add(Integer.parseInt(o.toString()));
			}*/
        } catch (Exception e) {
            log.debug("get " + key + " failed:" + e.getMessage());
        }
        return intList;
    }

    public String getStringList(String key){
        String intList=null;

        try{
            Object value = propertyConfig.getList(key);
            if(value==null){
                return null;
            }
            intList=value.toString();
        }catch(Exception e){
            log.error("get "+key+" error:"+e.getMessage());
        }
        return intList;
    }
    public List<String> getStringListForList(String key){
        List<String> intList=new ArrayList<String>();

        try{
            List<Object> value = propertyConfig.getList(key);
            if(value==null){
                return null;
            }
            for(Object o:value){
                intList.add(o.toString());
            }
        }catch(Exception e){
            log.error("get "+key+" error:"+e.getMessage());
        }
        return intList;
    }

    public String getStringListToString(String key){
        StringBuffer sb = new StringBuffer();
        int i=0;
        try{
            List<Object> value = propertyConfig.getList(key);
            if(value==null){
                return null;
            }
            for(Object o:value){
                if(i>0)sb.append(",");
                sb.append(o);
                i++;
            }
        }catch(Exception e){
            log.error("get "+key+" error:"+e.getMessage());
        }
        return sb.toString();
    }

    public String getMutiInfoString(String key){
        String value=null;
        try{
            value = Arrays.toString(propertyConfig.getStringArray(key));

            value=value.substring(1,value.length()-1);
        }catch(Exception e){
            log.error("get "+key+" error:"+e.getMessage());
        }

        return value;
    }
}
