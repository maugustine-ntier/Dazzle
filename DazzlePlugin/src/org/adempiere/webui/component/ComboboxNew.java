package org.adempiere.webui.component;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.adempiere.exceptions.DBException;
import org.adempiere.webui.apps.form.WObjRet;
import org.compiere.util.DB;
import org.zkoss.zul.Comboitem;

//import com.sun.xml.internal.ws.util.StringUtils;

public class ComboboxNew extends org.zkoss.zul.Combobox {


	private String queryName = "";
	protected static final String EMPTY_STRING = "";

	public String getQueryName() {
		return queryName;
	}

	public void setQueryName(String queryName) {
		this.queryName = queryName;
	}

	public List getItems(int itemsNumber, String nameStartsWith) {
	    PreparedStatement pstmt = null;
        ResultSet rs = null;
        ArrayList<WObjRet> list = new ArrayList<WObjRet>();

        try
        {
            pstmt = DB.prepareStatement(queryName, null);
            pstmt.setString(1, (nameStartsWith.toUpperCase()) + "%");
           /* int count = 0;
    		for(int i =0; i < queryName.length(); i++)
    		    if(queryName.charAt(i) == '?')
    		        count++; // This is to accomadate bar codes and prod values
            if (count > 1) {
            	pstmt.setString(2, (nameStartsWith.toUpperCase()));
            }*/
            pstmt.setMaxRows(itemsNumber);
            rs = pstmt.executeQuery();
            while (rs.next())
            {
            	WObjRet objRet = new WObjRet();
            	objRet.setValue(rs.getString(1));
            	objRet.setIDOfRecord(rs.getInt(2));
            	list.add(objRet);
               // list.add(rs.getString(1));
            }
        }
        catch (SQLException e)
        {
            throw new DBException(e, queryName);
        }
        finally
        {
        	DB.close(rs, pstmt);
            rs = null; pstmt = null;
        }
		return list;
	}

	/*@Override
	public void setModel(ListModel model) {
		super.setModel(new SimpleListModel(Collections.emptyList()) {


			public ListModel getSubModel(Object value, int nRows) {
		        if (value != null && queryName != null && !queryName.equals("")) {
		            String nameStartsWith = value.toString();
		            List data = getItems(100, nameStartsWith);
		            return ListModelFlyweight.create(data, nameStartsWith, queryName);
		        }
		        return ListModelFlyweight.create(Collections.emptyList(), EMPTY_STRING , queryName);
		    }
		}
		);
	}*/

	public Comboitem getItemByValue(String value) throws IllegalArgumentException{

		for(Comboitem item : getItems()) {
			if((item.getValue().toString()).equals(value))
				return item;
		}

		throw new IllegalArgumentException(value+" wasn't found in available values");
	}



}
