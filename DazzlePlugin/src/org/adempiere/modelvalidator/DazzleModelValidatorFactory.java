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
package org.adempiere.modelvalidator;

import java.util.logging.Level;

import org.adempiere.base.IModelValidatorFactory;
import org.compiere.model.ModelValidator;
import org.compiere.util.CLogger;

/**
 * @author hengsin
 *
 */
public class DazzleModelValidatorFactory implements IModelValidatorFactory {

	/** Logger */
	private static CLogger log = CLogger
			.getCLogger(DazzleModelValidatorFactory.class);
	
	/**
	 * default constructor
	 */
	public DazzleModelValidatorFactory() {
	}

	/* (non-Javadoc)
	 * @see org.adempiere.base.IModelValidatorFactory#newModelValidatorInstance(java.lang.String)
	 */
	@Override
	public ModelValidator newModelValidatorInstance(String className) {
		
		if (log.isLoggable(Level.CONFIG))
			log.config("");
		
		ModelValidator validator = null;
		if ( "org.adempiere.modelvalidator.DazzleValidator".equals(className)) {
			validator = new DazzleValidator();
		}
//		ModelValidator validator = EquinoxExtensionLocator.instance().locate(ModelValidator.class, "org.adempiere.base.ModelValidator", className, null).getExtension();
//		if (validator == null) {
//			Class<?> clazz = null;
//			
//			//use context classloader if available
//			ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
//			if (classLoader != null) {
//				try {
//					clazz = classLoader.loadClass(className);
//				}
//				catch (ClassNotFoundException ex) {
//				}
//			}
//			if (clazz == null) {
//				classLoader = this.getClass().getClassLoader();
//				try {
//					clazz = classLoader.loadClass(className);
//				}
//				catch (ClassNotFoundException ex) {
//				}
//			}
//			if (clazz != null) {
//				try {
//					validator = (ModelValidator)clazz.newInstance();
//				} catch (Exception e) {
//					e.printStackTrace();
//				} 
//			} else {
//				new Exception("Failed to load model validator class " + className).printStackTrace();
//			}
//		}
//		
		return validator;
	}

}
