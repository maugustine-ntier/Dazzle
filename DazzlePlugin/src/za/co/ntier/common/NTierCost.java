package za.co.ntier.common;

import java.math.BigDecimal;
import java.util.Properties;
import java.util.logging.Level;

import org.adempiere.exceptions.AdempiereException;
import org.compiere.model.MAcctSchema;
import org.compiere.model.MCost;
import org.compiere.model.MCostElement;
import org.compiere.model.MOrg;
import org.compiere.model.MProduct;
import org.compiere.util.CLogger;

/**
 * @author NCG
 * Date: 15 Jun 2015
 * Description:	
 */
public class NTierCost extends BaseSupport {

	/** Logger */
	private static CLogger log = CLogger.getCLogger(NTierCost.class);
	
	protected MProduct m_product;
	
	public NTierCost(Properties ctx, String trxName, MProduct product) {
		super(ctx, trxName);
		m_product = product;
	}
	
	/**
	 * Create a cost record
	 *  true - cost record created
	 *  false - not created (existed already)
	 */
	public static boolean create (MProduct product, BigDecimal costPrice) {
		return ! createOrCheckIfExists(product, costPrice, false);
	}
	
	/**
	 */
	public static boolean isCostRecordExists (MProduct product) {
		return createOrCheckIfExists(product, null, true);
	}
	
	/**
	 */
	public static boolean isCostRecordRequired (MProduct product) {
		return MProduct.PRODUCTTYPE_Item.equals(product.getProductType());
	}
	
	/**
	 * Create the cost record if it does not exist, or check if cost record exists
	 * 	Return: true if cost record exists already, or false if it does not
	 * Adapted from: MCost.create(product)
	 */
	private static boolean createOrCheckIfExists (MProduct product, BigDecimal costPrice, boolean checkOnly)
	{
		log.config(product.getName());

		MAcctSchema[] mass = MAcctSchema.getClientAcctSchema(product.getCtx(),
			product.getAD_Client_ID(), product.get_TrxName());
		MOrg[] orgs = null;

		int M_ASI_ID = 0;		//	No Attribute
		for (MAcctSchema as : mass)
		{
			
			/////////////////////////////////////////////////////////////
			// Get first costing element corresponding with the accounting schema
			/////////////////////////////////////////////////////////////
			MCostElement[] ces = MCostElement.getCostingMethods(product);
			MCostElement ce = null;
			for (MCostElement element : ces) {
				if (as.getCostingMethod().equals(element.getCostingMethod()))
//				if (X_M_CostElement.COSTINGMETHOD_StandardCosting.equals(element.getCostingMethod()))
				{
					ce = element;
					break;
				}
			}
			if (ce == null)
			{
				String msg = "No MCostElement element in the system, corresponding with " +
						" accounting schema costing method: " + as.getCostingMethod();
				throw new AdempiereException( msg );
//				log.warning("No MCostElement element in the system, corresponding with " +
//						" accounting schema costing method: " + as.getCostingMethod());
////				s_log.fine("No Standard Costing in System");
//				return;
			}
			/////////////////// New ///////////////////////////
			
			String cl = product.getCostingLevel(as);
			//	Create Std Costing
			if (MAcctSchema.COSTINGLEVEL_Client.equals(cl))
				{
					MCost cost = MCost.get (product, M_ASI_ID,
						as, 0, ce.getM_CostElement_ID(), product.get_TrxName());
					if ( checkOnly ) {
						return ! cost.is_new();
					}
					if (cost.is_new())
					{
						//NCG: Set the cost
						cost.setCurrentCostPrice(costPrice);
						if (cost.save()) {
							if (log.isLoggable(Level.CONFIG)) log.config("Cost for " + product.getName()
								+ " - " + as.getName());
						} else {
							log.warning("Not created: Cost for " + product.getName()
									+ " - " + as.getName());
						}
						return false;
					}
				}
			else if (MAcctSchema.COSTINGLEVEL_Organization.equals(cl))
			{
				if (orgs == null)
					orgs = MOrg.getOfClient(product);
				for (MOrg o : orgs)
				{
						MCost cost = MCost.get (product, M_ASI_ID,
							as, o.getAD_Org_ID(), ce.getM_CostElement_ID(), product.get_TrxName());
						if ( checkOnly ) {
							return ! cost.is_new();
						}
						if (cost.is_new())
						{
							//NCG: Set the cost
							cost.setCurrentCostPrice(costPrice);
							if (cost.save()) {
								if (log.isLoggable(Level.CONFIG)) log.config("Cost for " + product.getName()
									+ " - " + o.getName()
									+ " - " + as.getName());
							} else {
								log.warning("Not created: Cost for " + product.getName()
										+ " - " + o.getName()
										+ " - " + as.getName());
							}
							return false;
					}
				}	//	for all orgs
			}
			else
			{
//				log.warning("Not created: Cost for " + product.getName()
//					+ " - Costing Level on Batch/Lot");
			}
		}	//	accounting schema loop
		return true;
	}	//	create

}
