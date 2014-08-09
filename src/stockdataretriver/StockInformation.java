/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package stockdataretriver;

import java.util.Objects;

/**
 *
 * @author ZhipingJiang
 */
public class StockInformation {
    private String code;
    private String name;
    private String ifengURL;
    IFengStockListRetriver.ifengUrlType type;
    IFengStockListRetriver.ifengUrlClass cls;

    public StockInformation(String code, String name, String ifengURL, IFengStockListRetriver.ifengUrlType type, IFengStockListRetriver.ifengUrlClass cls) {
        this.code = code;
        this.name = name;
        this.ifengURL = ifengURL;
        this.type = type;
        this.cls = cls;
    }

    public IFengStockListRetriver.ifengUrlType getType() {
        return type;
    }

    public void setType(IFengStockListRetriver.ifengUrlType type) {
        this.type = type;
    }

    public IFengStockListRetriver.ifengUrlClass getCls() {
        return cls;
    }

    public void setCls(IFengStockListRetriver.ifengUrlClass cls) {
        this.cls = cls;
    }

    

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIfengURL() {
        return ifengURL;
    }

    public void setIfengURL(String ifengURL) {
        this.ifengURL = ifengURL;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 29 * hash + Objects.hashCode(this.code);
        hash = 29 * hash + Objects.hashCode(this.name);
        hash = 29 * hash + Objects.hashCode(this.ifengURL);
        hash = 29 * hash + Objects.hashCode(this.type);
        hash = 29 * hash + Objects.hashCode(this.cls);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final StockInformation other = (StockInformation) obj;
        if (!Objects.equals(this.code, other.code)) {
            return false;
        }
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
        if (!Objects.equals(this.ifengURL, other.ifengURL)) {
            return false;
        }
        if (this.type != other.type) {
            return false;
        }
        if (this.cls != other.cls) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "StockInformation{" + "code=" + code + ", name=" + name + ", ifengURL=" + ifengURL + ", type=" + type + ", cls=" + cls + '}';
    }
    
    
    
    
}
