package org.compiere.report;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.Properties;
import java.util.logging.Level;

import org.adempiere.util.ProcessUtil;
import org.compiere.model.MClient;
import org.compiere.model.MClientInfo;
import org.compiere.model.MImage;
import org.compiere.model.MOrgInfo;
import org.compiere.model.X_AD_PInstance_Para;
import org.compiere.process.ProcessInfo;
import org.compiere.process.ProcessInfoParameter;
import org.compiere.process.SvrProcess;
import org.compiere.util.Env;
import org.compiere.util.Trx;


public class DazzleReportStarter extends SvrProcess {


	//private static CLogger log = CLogger.getCLogger(DazzleReportStarter.class);
	private File imageFile=null;
	private int seqNo = 500;
	private String p_RunNextJob = null;

	public void addProcessParameters(Properties ctx, ProcessInfo pi, String trxName, String paraName, Object paraValue){

		X_AD_PInstance_Para p = new X_AD_PInstance_Para (ctx, 0, trxName);

		if ( p != null ) {
			p.setAD_PInstance_ID(pi.getAD_PInstance_ID());
			p.setParameterName(paraName);
			if ( paraValue instanceof String)
				p.setP_String((String)paraValue);
			else if ( paraValue instanceof BigDecimal )
					p.setP_Number((BigDecimal)paraValue);
			else if ( paraValue instanceof Integer )
					p.setP_Number(new BigDecimal((Integer)paraValue));
			else if ( paraValue instanceof Date)
					p.setP_Date(new Timestamp(((Date)paraValue).getTime()));

			p.setSeqNo(seqNo);
			p.save(trxName);
			seqNo += 10;
		}

	}

    public void addAdditionalParameters(Properties ctx, ProcessInfo pi, String trx){

    	addProcessParameters(ctx, pi, trx, "Client_ID", new BigDecimal(Env.getAD_Client_ID(Env.getCtx())));
    	addProcessParameters(ctx, pi, trx, "Org_ID", new BigDecimal(Env.getAD_Org_ID(Env.getCtx())));

        String BASE_PATH = System.getProperty("java.io.tmpdir") + System.getProperty("file.separator") ;
        if (BASE_PATH != null) {
          	addProcessParameters(ctx, pi, trx, "BASE_PATH", BASE_PATH );
          	addProcessParameters(ctx, pi, trx, "SUBREPORT_DIR", BASE_PATH );
        }

		try
		    {

			  MImage image=null;

		      imageFile =  File.createTempFile("logo", "jpg");
		      //First check org level for logo
		      MOrgInfo orgInfo = MOrgInfo.get( Env.getCtx(), Env.getAD_Org_ID(Env.getCtx()), null );
		      if ( orgInfo != null && orgInfo.getLogo_ID()!= 0){
	              image = new MImage(Env.getCtx(), orgInfo.getLogo_ID(), null);
		      }
			      else {
			    	  MClientInfo clientInfo = MClientInfo.get(Env.getCtx(), new Integer(Env.getAD_Client_ID(Env.getCtx())), null);

					  if ( clientInfo != null && clientInfo.getLogoReport_ID() != 0 ) {
			              image = new MImage(Env.getCtx(), clientInfo.getLogoReport_ID(), null);
					  }
			      }

	          if (image != null ) {
	          	addProcessParameters(ctx, pi, trx, "image_file_name", imageFile.getAbsolutePath() );
		    	  byte [] data = image.getBinaryData();
		    	  DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(imageFile)));
		    	  if ( out != null ) {
		    		  out.write(data);
		    		  out.close();
		    	  }
              }
		    }
            catch(IOException e) {
            	System.err.println("Error Writing/Reading Streams.");
            }
}

	@Override
	protected void prepare() {
		// Martin added to pick up next java program to run 02/09/2013
		ProcessInfoParameter[] para = getParameter();
		for (int i = 0; i < para.length; i++)
		{
			String name = para[i].getParameterName();
			if (para[i].getParameter() == null)
				;
			else if (name.equals("RunNextJob"))
				p_RunNextJob = (String)para[i].getParameter();
			else
				log.log(Level.SEVERE, "Unknown Parameter: " + name);
		}

	}

	@Override
	protected String doIt() throws Exception {

		addAdditionalParameters(getCtx(), getProcessInfo(), get_TrxName());
		// Check and run next process if available Martin added 02/09/2013
		if (p_RunNextJob != null) {
			ProcessInfo pi = getProcessInfo();
			pi.setClassName(p_RunNextJob);
			Trx localTrx = Trx.get(get_TrxName(), false); // Get the transaction
			ProcessUtil.startJavaProcess(getCtx(), pi,localTrx );
		}


		return "";
	}



}