/******************************************************************************
 * Copyright (C) 2013 Heng Sin Low                                            *
 * Copyright (C) 2013 Trek Global                 							  *
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

import org.adempiere.webui.apps.form.WAllocationDazzle;
import org.adempiere.webui.apps.form.WDazzleCreditNote;
import org.adempiere.webui.apps.form.WDazzleDebitNote;
import org.adempiere.webui.apps.form.WDazzleMovement;
import org.adempiere.webui.apps.form.WDazzlePOPos;
import org.adempiere.webui.apps.form.WDazzlePos;
import org.adempiere.webui.apps.form.WDazzleQuotePos;
import org.adempiere.webui.apps.form.WDazzleSupplierInvoice;
import org.adempiere.webui.factory.IFormFactory;
import org.adempiere.webui.panel.ADForm;
import org.adempiere.webui.panel.IFormController;
import org.compiere.util.CLogger;

/**
 * @author NCG
 *
 */
public class DazzleFormFactory implements IFormFactory {

	private static final CLogger log = CLogger.getCLogger(DazzleFormFactory.class); 
			
	/**
	 * default constructor
	 */
	public DazzleFormFactory() {
	}

	/* (non-Javadoc)
	 * @see org.adempiere.webui.factory.IFormFactory#newFormInstance(java.lang.String)
	 */
	@Override
	public ADForm newFormInstance(String formName) {
		Object form = null;
		if (formName.endsWith("VDazzleMovement")) {
			form = new WDazzleMovement();
		} else if (formName.endsWith("VAllocationDazzle")) {
			form = new WAllocationDazzle();
		} else if (formName.endsWith("VDazzleCreditNote")) {
			form = new WDazzleCreditNote();
		} else if (formName.endsWith("VDazzleDebitNote")) {
			form = new WDazzleDebitNote();
		} else if (formName.endsWith("VDazzlePOPos")) {
			form = new WDazzlePOPos();
		} else if (formName.endsWith("VDazzlePos")) {
			form = new WDazzlePos();
		} else if (formName.endsWith("VDazzleQuotePos")) {
			form = new WDazzleQuotePos();
		} else if (formName.endsWith("VDazzleSupplierInvoice")) {
			form = new WDazzleSupplierInvoice();
		}
//		Object form = EquinoxExtensionLocator.instance().locate(Object.class, "org.adempiere.webui.Form", formName, null).getExtension();
//		if (form == null) {
//			//static lookup
//    		String webClassName = ADClassNameMap.get(formName);
//    		//fallback to dynamic translation
//    		if (webClassName == null || webClassName.trim().length() == 0)
//    		{
//    			webClassName = translateFormClassName(formName);
//    		}
//
//    		if (webClassName == null)
//    		{
//    			log.warning("Web UI form not implemented for the swing form " + formName);
//    		}
//    		else
//    		{
//    			ClassLoader loader = Thread.currentThread().getContextClassLoader();
//    			Class<?> clazz = null; 
//    			if (loader != null) {
//		    		try
//		    		{
//		        		clazz = loader.loadClass(webClassName);
//		    		}
//		    		catch (Exception e)
//		    		{
//		    			if (log.isLoggable(Level.INFO))
//		    				log.log(Level.INFO, e.getLocalizedMessage(), e);
//		    		}
//    			}
//    			if (clazz == null) {
//    				loader = this.getClass().getClassLoader();
//    				try
//		    		{
//		    			//	Create instance w/o parameters
//		        		clazz = loader.loadClass(webClassName);
//		    		}
//		    		catch (Exception e)
//		    		{
//		    			if (log.isLoggable(Level.INFO))
//		    				log.log(Level.INFO, e.getLocalizedMessage(), e);
//		    		}
//    			}
//    			if (clazz != null) {
//    				try
//		    		{
//		    			form = clazz.newInstance();
//		    		}
//		    		catch (Exception e)
//		    		{
//		    			if (log.isLoggable(Level.WARNING))
//		    				log.log(Level.WARNING, e.getLocalizedMessage(), e);
//		    		}
//    			}
//    		}
//		}		
		
		if (form != null) {
			if (form instanceof ADForm) {
				return (ADForm)form;
			} else if (form instanceof IFormController) {
				IFormController controller = (IFormController) form;
				ADForm adForm = controller.getForm();
				adForm.setICustomForm(controller);
				return adForm;
			}
		}
		
//		if (log.isLoggable(Level.INFO))
//			log.info(formName + " not found at extension registry and classpath");
		return null;
	}


}
