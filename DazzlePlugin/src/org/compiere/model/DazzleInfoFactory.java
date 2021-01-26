/******************************************************************************
 * Product: Adempiere ERP & CRM Smart Business Solution                       *
 * Copyright (C) 2010 Heng Sin Low                							  *
 * This program is free software; you can redistribute it and/or modify it    *
 * under the terms version 2 of the GNU General Public License as published   *
 * by the Free Software Foundation. This program is distributed in the hope   *
 * that it will be useful, but WITHOUT ANY WARRANTY; without even the implied *
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.           *
 * See the GNU General Public License for more details.                       *
 * You should have received a copy of the GNU General Public License along    *
 * with this program; if not, write to the Free Software Foundation, Inc.,    *
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.                     *
 *****************************************************************************/
package org.compiere.model;

import org.adempiere.webui.factory.IInfoFactory;
import org.adempiere.webui.info.InfoWindow;
import org.adempiere.webui.panel.InfoPanel;
import org.adempiere.webui.panel.InfoProductPanel;
import org.compiere.util.Env;

import za.co.ntier.dazzle.zk.info.DazzleInfoProductWindow;

/**
 *
 * @author NCG
 * 	#20150416_RQ100001_nTier_NCG: Feature for adding and viewing an image relating to a product
 */
public class DazzleInfoFactory implements IInfoFactory {

	@Override
	public InfoPanel create(int WindowNo, String tableName, String keyColumn,
			String value, boolean multiSelection, String whereClause, int AD_InfoWindow_ID, boolean lookup) {
		
		InfoPanel info = null;
		
		if (tableName.equals("M_Product")) {
        	info = new DazzleInfoProductWindow(WindowNo, tableName, keyColumn, value, multiSelection, whereClause, AD_InfoWindow_ID, lookup);
    		if (!info.loadedOK()) {
	            info = new InfoProductPanel ( WindowNo,
	            		Env.getContextAsInt(Env.getCtx(), WindowNo, "M_Warehouse_ID"),
	    				Env.getContextAsInt(Env.getCtx(), WindowNo, "M_PriceList_ID"),
	                    multiSelection, value,whereClause, lookup);
    		}
		}
		return info;
//		InfoPanel info = null;
//
//		if (NTierCommon.isAdvancedSearch(tableName)) {
//        	info = new ZZ_InfoWindow(WindowNo, tableName, keyColumn, value, multiSelection, whereClause, AD_InfoWindow_ID, lookup);
//        	if (!info.loadedOK()) {
//	            info = new InfoGeneralPanel (value, WindowNo,
//	                tableName, keyColumn,
//	                multiSelection, whereClause, lookup);
//	        	if (!info.loadedOK()) {
//	        		info.dispose(false);
//	        		info = null;
//	        	}
//        	}
//        } 
//        //
//        return info;
	}

	@Override
	public InfoPanel create(Lookup lookup, GridField field, String tableName,
			String keyColumn, String queryValue, boolean multiSelection,
			String whereClause, int AD_InfoWindow_ID) {
		return null;
//		InfoPanel info = null;
//		if (NTierCommon.isAdvancedSearch(tableName)) {
//			info = create(lookup.getWindowNo(), tableName, keyColumn, queryValue, false, whereClause, AD_InfoWindow_ID, true);
//		}
//		return info;
	}

	/* 
	 * NB: This method must return an InfoWindow, and not an InfoPanel.
	 *  Please note, that the ZZ_InfoWindow class, does not descend from InfoWindow, but rather from
	 *  InfoPanel. Therefore, this factory method cannot be used when creating a ZZ_InfoWindow.
	 *  See classes: ZZ_InfoWindow and DPViews
	 */
	@Override
	public InfoWindow create(int AD_InfoWindow_ID) {
		return null;
		//TODO: Implementation here
//		MInfoWindow infoWindow = new MInfoWindow(Env.getCtx(), AD_InfoWindow_ID, (String)null);
//		String tableName = infoWindow.getAD_Table().getTableName();
//		String keyColumn = tableName + "_ID";
//		InfoPanel info = create(-1, tableName, keyColumn, null, false, null, AD_InfoWindow_ID, false);
//		if (info instanceof InfoWindow)
//			return (InfoWindow) info;
//		else
//			return null;

	}

}
