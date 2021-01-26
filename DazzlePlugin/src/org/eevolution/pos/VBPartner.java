/******************************************************************************
 * Product: Adempiere ERP & CRM Smart Business Solution                       *
 * This program is free software; you can redistribute it and/or modify it    *
 * under the terms version 2 of the GNU General Public License as published   *
 * by the Free Software Foundation. This program is distributed in the hope   *
 * that it will be useful, but WITHOUT ANY WARRANTY; without even the implied *
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.           *
 * See the GNU General Public License for more details.                       *
 * You should have received a copy of the GNU General Public License along    *
 * with this program; if not, write to the Free Software Foundation, Inc.,    *
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.                     *
 * For the text or an alternative of this public license, you may reach us    *
 * Copyright (C) 2003-2009 e-Evolution,SC. All Rights Reserved.               *
 * Contributor(s): Antonio Ca�averal www.e-evolution.com                      *
 * Contributor(s): Victor P�rez www.e-evolution.com                           *
 *****************************************************************************/

package org.eevolution.pos;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.DefaultComboBoxModel;
import javax.swing.table.DefaultTableModel;

import org.compiere.model.MBPGroup;
import org.compiere.model.MBPartner;
import org.compiere.model.MBPartnerLocation;
import org.compiere.model.MContactInterest;
import org.compiere.model.MCountry;
import org.compiere.model.MInterestArea;
import org.compiere.model.MLocation;
import org.compiere.model.MUser;
import org.compiere.util.DB;
import org.compiere.util.Env;
import org.compiere.util.Msg;
import org.compiere.util.Trx;

/**
 * 
 * @author antonio.canaveral@e-evolution.com, www.e-evolution.com
 * @author victor.perez@e-evolution.com, www.e-evolution.com
 */
public class VBPartner {

	private MBPartner bpLoaded;

	private boolean isInBlakList=false;
	private String name;
	private String lastName;
	private String value;//cedula-RUC
	private char gender;
	private String address1;
	private String address2;
	private String address3;
	private String address4;
	private String region;
	private String city;
	private String phone1;
	private String phone2;
	private String email;
	private String postalCode;  // Martin added

	private char type;
	private int groupid;
	private String strOut;
	private String locationName;
	private BigDecimal creditlimit;
	private BigDecimal creditused;
	private int C_BPartner_ID;
	private int C_BPartner_Location_ID;
	private int AD_USER_ID=0;
	private Trx trx;
	//private VComboBoxLoaderInt loader;
	// Martin changed - to South Africa
	//private final int ID_COUNTRY=171;
	private final int ID_COUNTRY=VUtil.getParameterAsInt("C_Country_ID");
	private boolean needUpdate=false;
	private boolean isCustomer;
	// Martin added VAT
	private String vatNo;


	public String getVatNo() {
		return vatNo;
	}

	public void setVatNo(String vatNo) {
		this.vatNo = vatNo;
	}

	public static javax.swing.table.DefaultTableModel InterestAreas(MBPartner bp)
	{
		DefaultTableModel modelo = new DefaultTableModel(){
			boolean[] canEdit = new boolean [] {false, false,true};
			public boolean isCellEditable(int rowIndex, int columnIndex) {
				return canEdit [columnIndex];
			}
			public Class getColumnClass(int c) {
				return getValueAt(0, c).getClass();
			}
		};	
		modelo.addColumn("Id");
		modelo.addColumn(Msg.translate(Env.getCtx(),"R_InterestArea_ID"));
		modelo.addColumn(Msg.translate(Env.getCtx(),"isActive"));
		String Iname;
		boolean isActive;
		int user_id;
		//lets get the user of the bp.
		MUser[] usr= MUser.getOfBPartner(Env.getCtx(),bp.getC_BPartner_ID());
		if(usr.length!=0)
		{
			user_id=usr[0].getAD_User_ID();

			int ids[] = MInterestArea.getAllIDs(MInterestArea.Table_Name, " AD_Client_ID="+Env.getAD_Client_ID(Env.getCtx()), null);
			for(int id:ids)
			{
				MInterestArea interest = MInterestArea.get(Env.getCtx(),id);
				Iname=interest.getName();
				//MContactInterest mci = new MContactInterest(Env.getCtx(),id,user_id,true,null);
				interest.setAD_User_ID(user_id);
				interest.setSubscriptionInfo(user_id);
				if(interest.isSubscribed())
					isActive=true;
				else
					isActive=false;
				Vector<Object> line = new Vector<Object>();
				line.add(interest.getR_InterestArea_ID());
				line.add(Iname);
				line.add(isActive);
				modelo.addRow(line);
			}
		}else
		{
			//VUtil.setMsg("Ingrese el e-mail del Cliente, para acceder a las áreas de interés.", null, false);
		}
		return modelo;
	}

	public static javax.swing.DefaultComboBoxModel getBPGroupCombo()
	{
		/*int [] bpId;
		String groups=VUtil.getParameter("C_BP_Group_ID");
		String def=null;
		if(groups.contains(";"))
		{
			def=groups.substring(groups.indexOf(";")+1,groups.length());
			groups=groups.substring(0, groups.indexOf(";"));
		}
		// Martin added the client
		int ad_client_ID = Env.getAD_Client_ID(Env.getCtx());
		//bpId=MBPGroup.getAllIDs("C_BP_Group","C_BP_Group_ID IN("+groups+")"
		//		+ " and AD_Client_ID = " + ad_client_ID,null);
		
		bpId=MBPGroup.getAllIDs("C_BP_Group","AD_Client_ID = " + ad_client_ID,null);
		Vector vector = new Vector(bpId.length);
		VComboBoxLoaderInt selected = null;
		for(int i=0;i < bpId.length;i++)
		{
			MBPGroup bpg = new MBPGroup(Env.getCtx(),bpId[i],null);       
			VComboBoxLoaderInt loader = new VComboBoxLoaderInt();
			loader.setIndex(bpId[i]);
			loader.setText(bpg.getName());
			vector.add(loader);
			if(def!=null&& bpId[i]==Integer.parseInt(def))
			{
				selected =loader;
			}

		}
		javax.swing.DefaultComboBoxModel comboVector = new javax.swing.DefaultComboBoxModel(vector);
		if(selected!=null)
			comboVector.setSelectedItem(selected);
		return comboVector;*/
		return null;
	}

	public javax.swing.DefaultComboBoxModel getBPLocationNameCombo(VDocument curr)
	{
		/*MBPartnerLocation[] bpl = MBPartnerLocation.getForBPartner(Env.getCtx(),C_BPartner_ID);
		Vector vector = new Vector();
		VComboBoxLoaderInt selected = null;
		for(int i=0;i<bpl.length;i++)
		{
			if(bpl[i].isActive())
			{
				VComboBoxLoaderInt load = new VComboBoxLoaderInt();
				load.setIndex(bpl[i].getC_BPartner_Location_ID());
				load.setText(bpl[i].getName());
				vector.add(load);
				if(curr!=null && curr.getC_BPartner_Location_ID()>0)
				{
					if(bpl[i].getC_BPartner_Location_ID()==curr.getC_BPartner_Location_ID())
					{
						selected =load;
					}
				}
			}
		}
		javax.swing.DefaultComboBoxModel comboVector = new javax.swing.DefaultComboBoxModel(vector);
		if(selected!=null)
			comboVector.setSelectedItem(selected);
		return comboVector;*/
		return null;
	}

	public static javax.swing.DefaultComboBoxModel getBPType()
	{
/*		try {
			String sql =" select rl.value,rl.name" 
				+" from AD_Ref_List rl" 
				+" inner join ad_reference r on  r.AD_Reference_ID=rl.AD_Reference_ID" 
				+" and r.AD_Reference_ID= ? "
				+" order by rl.name";  
			PreparedStatement pstmt = DB.prepareStatement(sql, null);
			pstmt.setInt(1, VUtil.getParameterAsInt("BP_Type"));
			ResultSet rs = pstmt.executeQuery();
			Vector vector = new Vector();
			while(rs.next())
			{
				VComboBoxLoaderChar loader = new VComboBoxLoaderChar();
				loader.setIndex(rs.getString("value").charAt(0));
				loader.setText(rs.getString("name"));
				vector.add(loader);
			}
			DB.close(rs, pstmt);
			javax.swing.DefaultComboBoxModel comboVector = new javax.swing.DefaultComboBoxModel(vector);
			return comboVector;
		}
		catch (Exception e)
		{
			return null;
		}	*/
		return null;
	}


	public static javax.swing.DefaultComboBoxModel getCities()
	{
		/*try {
			String sql =" SELECT c_city_id,name " 
				+ " FROM c_city "
				+ " WHERE c_country_id="+VUtil.getParameterAsInt("C_Country_ID")
				+ " ORDER BY name";
			PreparedStatement pstmt = DB.prepareStatement(sql, null);
			ResultSet rs = pstmt.executeQuery();
			Vector vector = new Vector();
			sql=" SELECT c_city_id"
				+" FROM c_city" 
				+" WHERE c_country_id="+VUtil.getParameterAsInt("C_Country_ID")
				+" and upper(name) in ("
				+"	 select upper(city)"
				+"	 from AD_OrgInfo oi"
				+"	 join c_location l on l.c_location_id=oi.c_location_id"
				+"	 WHERE oi.AD_Org_ID=?)";
			int selected=DB.getSQLValue(null, sql,Env.getAD_Org_ID(Env.getCtx()));
			VComboBoxLoaderInt sel=null;
			while(rs.next())
			{
				VComboBoxLoaderInt loader = new VComboBoxLoaderInt();
				loader.setIndex(rs.getInt("c_city_id"));
				loader.setText(rs.getString("name"));
				vector.add(loader);
				if(rs.getInt("c_city_id")==selected)
					sel=loader;
			}
			DB.close(rs, pstmt);
			javax.swing.DefaultComboBoxModel comboVector = new javax.swing.DefaultComboBoxModel(vector);
			if(sel!=null)
				comboVector.setSelectedItem(sel);
			return comboVector;
		}
		catch (Exception e)
		{
			return null;
		}*/
		return null;
	}


	public static String getVendorName(String _code)
	{
		String res = "";
		try {
			String sql =" SELECT substring(COALESCE(C_BPARTNER.name2,ad_user.name),0,28) as Nombre "+
			" FROM ad_user,C_BPARTNER"+
			" WHERE C_BPARTNER.c_bpartner_id=ad_user.c_bpartner_id"+
			" AND ad_user.userpin='" + _code + "'";
			PreparedStatement pstmt = DB.prepareStatement(sql, null);
			ResultSet rs = pstmt.executeQuery();
			while(rs.next())
			{
				res=rs.getString("Nombre");
			}
			DB.close(rs, pstmt);
		} catch (SQLException ex) {
			Logger.getLogger(VBPartner.class.getName()).log(Level.SEVERE, null, ex);
		}
		return res;
	}



	public static int getVendor(String _code)
	{
		int res = 0;
		try {
			String sql =" SELECT ad_user_id as ID "+
			" FROM ad_user,C_BPARTNER"+
			" WHERE C_BPARTNER.c_bpartner_id=ad_user.c_bpartner_id"+
			" AND ad_user.userpin='" + _code + "'";
			//duns
			PreparedStatement pstmt = DB.prepareStatement(sql, null);
			ResultSet rs = pstmt.executeQuery();
			while(rs.next())
			{
				res=rs.getInt("ID");
			}
			DB.close(rs, pstmt);
		} catch (SQLException ex) {
			Logger.getLogger(VBPartner.class.getName()).log(Level.SEVERE, null, ex);
		}
		return res;
	}

	public void setEmail(String value)
	{
		email=value;
	}
	public String getEmail()
	{
		String mail="";
		try{
			MUser[] usr= MUser.getOfBPartner(Env.getCtx(),C_BPartner_ID);
			AD_USER_ID=usr[0].getAD_User_ID();
			mail=usr[0].getEMail();
		}catch(Exception e)
		{
			System.out.println(e.getMessage());
		}
		return mail;
	}
	public void setLocationName(String value)
	{
		locationName=value;
	}
	public String getLocationName()
	{
		return locationName;
	}
	public String getBPGroupValue()
	{
		return bpLoaded.getBPGroup().getValue();
	}
	public String getBPGroupText()
	{
		return bpLoaded.getBPGroup().getName();
	}
	public String getBPMesage()
	{
		return bpLoaded.getDescription();
	}
	public String getOut()
	{
		return strOut;
	}
	public boolean getIsBlakList()
	{
		return isInBlakList;
	}
	/*public VComboBoxLoaderInt getGroup()
	{
		//return new VComboBoxLoaderInt(getBPGroupText(),groupid);
	}*/
	public void setGroupId(int value)
	{
		groupid=value;
	}

	public boolean getNeedUpdate()
	{
		return needUpdate;
	}

	public void setType(char value)
	{
		type=value;
	}

	/*public VComboBoxLoaderChar getTypeCombo()
	{
		try {
			
			// Martin removed the translation
			String sql =" select rl.value,trl.name" 
				+" from AD_Ref_List rl" 
				+" inner join ad_reference r on  r.AD_Reference_ID=rl.AD_Reference_ID" 
				+" and r.AD_Reference_ID= ?"
				+" inner join AD_Ref_List_Trl trl on  rl.AD_Ref_List_id=trl.AD_Ref_List_id"
				+" and trl.AD_Language='"+Env.getCtx().getProperty("#AD_Language")+"'"
				+" and rl.value='"+type+"'"
				+" order by rl.name";  
			
			String sql =" select rl.value,rl.name" 
				+" from AD_Ref_List rl" 
				+" inner join ad_reference r on  r.AD_Reference_ID=rl.AD_Reference_ID" 
				+" and r.AD_Reference_ID= ?"
				//+" inner join AD_Ref_List_Trl trl on  rl.AD_Ref_List_id=trl.AD_Ref_List_id"
				//+" and trl.AD_Language='"+Env.getCtx().getProperty("#AD_Language")+"'"
				+" and rl.value='"+type+"'"
				+" order by rl.name";  

			PreparedStatement pstmt = DB.prepareStatement(sql, null);
			pstmt.setInt(1, VUtil.getParameterAsInt("BP_Type"));
			ResultSet rs = pstmt.executeQuery();
			VComboBoxLoaderChar loader = new VComboBoxLoaderChar();
			while(rs.next())
			{
				loader.setIndex(rs.getString("value").charAt(0));
				loader.setText(rs.getString("name"));
			}
			DB.close(rs, pstmt);
			return loader;
		}
		catch (Exception e)
		{
			return null;
		}	
		return null;
	}*/
	
	// Martin added - so user does not choose the BP validation type
	/*public char getTypeDefault()
	{
		try {
			
					
			String sql =" select rl.value,rl.name" 
				+" from AD_Ref_List rl" 
				+" inner join ad_reference r on  r.AD_Reference_ID=rl.AD_Reference_ID" 
				+" and r.AD_Reference_ID= ?"
				//+" inner join AD_Ref_List_Trl trl on  rl.AD_Ref_List_id=trl.AD_Ref_List_id"
				//+" and trl.AD_Language='"+Env.getCtx().getProperty("#AD_Language")+"'"
				+" and rl.value='"+type+"'"
				+" order by rl.name";  

			PreparedStatement pstmt = DB.prepareStatement(sql, null);
			pstmt.setInt(1, VUtil.getParameterAsInt("BP_Type"));
			ResultSet rs = pstmt.executeQuery();
			VComboBoxLoaderChar loader = new VComboBoxLoaderChar();
			char type = 'P';
			while(rs.next())
			{
				type = rs.getString(1).charAt(0);
			}
			DB.close(rs, pstmt);
			return type;
		}
		catch (Exception e)
		{
			return 'P';
		}	
	}*/
	
	
	public char getType()
	{
		return type;
	}

	public void setName(String value)
	{
		name=value;
	}
	public String getName()
	{
		return name;
	}

	public void setLastName(String value)
	{
		lastName=value;
	}

	public String getLastName()
	{
		return lastName;
	}

	public void setValue(String _value)
	{
		value=_value;
	}
	public String getValue()
	{
		return value;
	}

	public static DefaultComboBoxModel getGenderModel()
	{
		/*try {
			String sql =" select rl.value,trl.name" 
				+" from AD_Ref_List rl" 
				+" inner join ad_reference r on  r.AD_Reference_ID=rl.AD_Reference_ID" 
				+" and r.AD_Reference_ID="+VUtil.getParameter("Gender")
				+" inner join AD_Ref_List_Trl trl on  rl.AD_Ref_List_id=trl.AD_Ref_List_id"
				+" and trl.AD_Language='"+Env.getCtx().getProperty("#AD_Language")+"'"
				+" order by rl.name";  

			PreparedStatement pstmt = DB.prepareStatement(sql, null);
			ResultSet rs = pstmt.executeQuery();
			Vector vector = new Vector();
			while(rs.next())
			{
				VComboBoxLoaderChar loader = new VComboBoxLoaderChar();
				loader.setIndex(rs.getString("value").charAt(0));
				loader.setText(rs.getString("name"));
				vector.add(loader);
			}
			DB.close(rs, pstmt);
			javax.swing.DefaultComboBoxModel comboVector = new javax.swing.DefaultComboBoxModel(vector);
			return comboVector;
		}
		catch (Exception e)
		{
			return null;
		}	*/
		return null;
	}

	/*public void setGender(VComboBoxLoaderChar value)
	{
		if(value!=null)
			gender=value.getIndex();
		else
			gender='N';
	}
	public VComboBoxLoaderChar getGender()
	{
		try {
			String sql =" select rl.value,trl.name" 
				+" from AD_Ref_List rl" 
				+" inner join ad_reference r on  r.AD_Reference_ID=rl.AD_Reference_ID" 
				+" and r.AD_Reference_ID="+VUtil.getParameter("Gender")
				+" inner join AD_Ref_List_Trl trl on  rl.AD_Ref_List_id=trl.AD_Ref_List_id"
				+" and trl.AD_Language='"+Env.getCtx().getProperty("#AD_Language")+"'";
				if(String.valueOf(gender).trim().length() > 0)
					sql += "and rl.value='"+gender+"'";
				sql += " order by rl.name";  

			PreparedStatement pstmt = DB.prepareStatement(sql, null);
			ResultSet rs = pstmt.executeQuery();
			VComboBoxLoaderChar loader = new VComboBoxLoaderChar();
			while(rs.next())
			{
				loader.setIndex(rs.getString("value").charAt(0));
				loader.setText(rs.getString("name"));
			}
			DB.close(rs, pstmt);
			return loader;
		}
		catch (Exception e)
		{
			return null;
		}	
	}*/

	public String getAddress1()
	{
		return address1;
	}
	public String getAddress2()
	{
		return address2;
	}
	public String getAddress3()
	{
		return address3;
	}
	public String getAddress4()
	{
		return address4;
	}
	public void setAddress1(String value)
	{
		address1=value;
	}

	public void setAddress2(String value)
	{
		address2=value;
	}
	public void setAddress3(String value)
	{
		address3=value;
	}
	public void setAddress4(String value)
	{
		address4=value;
	}
	public void setPhone(String value)
	{
		phone1=value;
	}
	public void setPhone2(String value)
	{
		phone2=value;
	}

	public String getPhone()
	{
		return phone1;
	}
	public String getPhone2()
	{
		return phone2;
	}
	public void setRegion(String value)
	{
		region=value;
	}
	public String getRegion()
	{
		return region;
	}
	public void setCity(String value)
	{
		city=value;
	}
	public String getCity()
	{
		return city;
	}

	public BigDecimal getCreditLimit()
	{
		return creditlimit;
	}
	public void setCreditLimit(BigDecimal cl)
	{
		creditlimit=cl;
	}

	public BigDecimal getCreditUsed()
	{
		return creditused;
	}
	public void setCreditUsed(BigDecimal cu)
	{
		creditused=cu;
	}

	public MBPartner getMBPartner()
	{
		return bpLoaded;
	}

	/*public VComboBoxLoaderInt getComboCity()
	{
		try {
			String sql =" SELECT c_city_id,name " 
				+ " FROM c_city "
				+ " WHERE c_country_id=" + VUtil.getParameterAsInt("C_Country_ID")
				+ " AND name = '" + city + "'";
			PreparedStatement pstmt = DB.prepareStatement(sql, null);
			ResultSet rs = pstmt.executeQuery();
			VComboBoxLoaderInt loader = new VComboBoxLoaderInt();
			while(rs.next())
			{
				loader.setIndex(rs.getInt("c_city_id"));
				loader.setText(rs.getString("name"));
			}
			DB.close(rs, pstmt);
			return loader;
		}
		catch (Exception e)
		{
			return null;
		}
	}
*/
	public int getBpartner_ID()
	{
		return C_BPartner_ID;
	}

	public boolean getIsCustomer()
	{
		return isCustomer;
	}

	public String getPostalCode() {
		return postalCode;
	}

	public void setPostalCode(String postalCode) {
		this.postalCode = postalCode;
	}

	public boolean loadBpByValue(String _value)
	{
		boolean res=false;
		bpLoaded =  MBPartner.get(Env.getCtx(), _value);
		if(bpLoaded!=null)
		{
			this.isCustomer=bpLoaded.isCustomer();
			this.C_BPartner_ID=bpLoaded.getC_BPartner_ID();
			this.value=_value;
			this.name=bpLoaded.getName();
			this.lastName=bpLoaded.getName2();
			this.creditlimit=bpLoaded.getSO_CreditLimit();
			this.creditused=bpLoaded.getSO_CreditUsed();

			if(bpLoaded.getSOCreditStatus()!=null && bpLoaded.getSOCreditStatus().equals(MBPartner.SOCREDITSTATUS_CreditHold))
				isInBlakList=true;
			System.out.println(bpLoaded.get_Value("gender"));
			if(bpLoaded.get_Value("typeid")!=null)
				this.type=bpLoaded.get_Value("typeid").toString().charAt(0);
			if(bpLoaded.get_Value("gender")!=null)
				this.gender=bpLoaded.get_Value("gender").toString().charAt(0);
			this.groupid=bpLoaded.getBPGroup().get_ID();
			// Martin added
			this.vatNo=bpLoaded.getTaxID();
			res=true;
		}
		//bpLoaded.get_Value("type");
		return res;
	}

	public boolean exists(String _value)
	{
		boolean res=false;
		bpLoaded =  MBPartner.get(Env.getCtx(), _value);
		if(bpLoaded!=null)
		{
			this.C_BPartner_ID=bpLoaded.getC_BPartner_ID();
			res=true;
		}
		return res;
	}

	public boolean loadBpByLocation(String _value)
	{
		MBPartnerLocation[] bpl = MBPartnerLocation.getForBPartner(Env.getCtx(),C_BPartner_ID);
		boolean finded=false;
		try
		{
			for(int i=0;i<bpl.length;i++)
			{
				if(bpl[i].getName().compareTo(_value)==0)
				{
					this.phone1=bpl[i].getPhone();
					this.phone2=bpl[i].getPhone2();
					this.locationName=bpl[i].getName();
					this.C_BPartner_Location_ID = bpl[i].getC_BPartner_Location_ID();
					loadLocationByBpL();
					finded=true;
				}
			}
		}catch (Exception e)
		{
			System.out.println("ERROR: " + e.getMessage());
		}
		return finded;
	}

	public boolean existsLocation(String _value)
	{
		MBPartnerLocation[] bpl = MBPartnerLocation.getForBPartner(Env.getCtx(),C_BPartner_ID);
		boolean finded=false;
		try
		{
			for(int i=0;i<bpl.length;i++)
			{
				if(bpl[i].getName().toUpperCase().compareTo(_value)==0)
				{
					this.locationName=bpl[i].getName();
					this.C_BPartner_Location_ID = bpl[i].getC_BPartner_Location_ID();
					finded=true;
				}
			}
		}catch (Exception e)
		{
			System.out.println("ERROR: " + e.getMessage());
		}
		return finded;
	}

	private void loadLocationByBpL()
	{
		MLocation loc = MLocation.getBPLocation(Env.getCtx(), C_BPartner_Location_ID, null);
		this.region=loc.getRegionName();
		this.city=loc.getCity();
		this.address1=loc.getAddress1();
		this.address2=loc.getAddress2();
		this.address3=loc.getAddress3();
		this.address4=loc.getAddress4();
		this.postalCode=loc.getPostal();  // Martin added
		System.out.println("loadLocationByBpL->"+address1);
	}

	public boolean Save(String _value)
	{
		boolean res=false;
		trx= Trx.get(Trx.createTrxName(), true);
		if(exists(_value))
			res=Update();
		else
			res=SaveNew();
		if(res)
			trx.commit();
		else
			trx.rollback();

		trx.close();
		trx =null;
		return res;
	}

	private boolean Update()
	{
		boolean res=false;
		bpLoaded.setValue(value);
		bpLoaded.setAD_Org_ID(0);
		if(name!=null&name.length()!=0)
			bpLoaded.setName(name);
		if(lastName!=null&name.length()!=0)
			bpLoaded.setName2(lastName);
		// Martin Changed
		//bpLoaded.set_ValueOfColumn("Gender", String.valueOf(gender));
		bpLoaded.set_ValueOfColumn("Gender", "M");
		//bpLoaded.set_ValueOfColumn("Type", String.valueOf(type));
		// Martin Changed
		//bpLoaded.set_ValueOfColumn("TypeID", String.valueOf(type));
		bpLoaded.set_ValueOfColumn("typeid", String.valueOf(type));
		bpLoaded.setBPGroup(MBPGroup.get(Env.getCtx(),groupid));
		// Martin added
		bpLoaded.setTaxID(vatNo);
		if(bpLoaded.save())
			res=true;
		VerifLocation();
		if(email.length()!=0)
			VerifMail();

		return res;    
	}

	private boolean SaveNew()
	{
		boolean res=false;
		try{
			MBPartner bp = new MBPartner(Env.getCtx(),0,trx.getTrxName());
			//if (LEC_RUCValidador.validaIdentificacion(String.valueOf(type),value))
			{
				bp.setAD_Org_ID(0);
				bp.setValue(value);
				if(name!=null&name.length()!=0)
					bp.setName(name);
				if(lastName!=null&lastName.length()!=0)
					bp.setName2(lastName);
				// Martin changed
				//bp.set_ValueOfColumn("Gender", String.valueOf(gender));
				bp.set_ValueOfColumn("Gender", "M");
				//bp.set_ValueOfColumn("Type", String.valueOf(type));
				bp.setIsCustomer(true);
				bp.setBPGroup(new MBPGroup(Env.getCtx(),groupid,trx.getTrxName()));
				// Martin CHanged
				//bp.set_ValueOfColumn("TypeID", String.valueOf(type));
				bp.set_ValueOfColumn("typeid", String.valueOf(type));
				// Martin added
				bp.setTaxID(vatNo);
				strOut+="Saved";
				if(!bp.save())
					strOut+="Can't save";
				else
				{
					this.C_BPartner_ID=bp.getC_BPartner_ID();
					this.bpLoaded=bp;
					VerifLocation();
					if(email.length()!=0)
						VerifMail();
					res=true;
				}
			}
			//else
				//strOut+="Not valid value";
		}catch (Exception e)
		{
			//Logger.getLogger(VloadInfo.class.getName()).log(Level.SEVERE, null, e);
		}
		return res;
	}

	private void VerifLocation()
	{
		MBPartnerLocation bpl;
		if(!existsLocation(locationName))
		{
			needUpdate=true;
			bpl = new MBPartnerLocation(Env.getCtx(),0,trx.getTrxName());
			MLocation loc = new MLocation(Env.getCtx(),0,trx.getTrxName());
			if(address1!=null&&address1.length()!=0){
				loc.setAddress1(address1);
				if (locationName == null) {
					locationName = address1;
				}
			}
			if(address2!=null&&address2.length()!=0){
				loc.setAddress2(address2);
				if (locationName == null) {
					locationName = address2;
				}
			}
			if(address3!=null&&address3.length()!=0) {
				loc.setAddress3(address3);
				if (locationName == null) {
					locationName = address3;
				}
			}
			if(address4!=null&&address4.length()!=0){
				loc.setAddress4(address4);
				if (locationName == null) {
					locationName = address4;
				}
			}
			if (postalCode!=null && postalCode.length() != 0)
				loc.setPostal(postalCode);  // Martin added
			loc.setRegionName(region);
			loc.setCity(city);
			loc.setCountry(MCountry.get(Env.getCtx(), ID_COUNTRY));
			loc.saveEx();
			bpl.setC_Location_ID(loc.getC_Location_ID());
			bpl.setC_BPartner_ID(C_BPartner_ID);
		}
		else
		{
			bpl = new MBPartnerLocation(Env.getCtx(),C_BPartner_Location_ID,trx.getTrxName());
			MLocation loc = new MLocation(Env.getCtx(),bpl.getC_Location_ID(),trx.getTrxName());
			if(address1!=null&&address1.length()!=0){
				loc.setAddress1(address1);
				if (locationName == null) {
					locationName = address1;
				}
			}
			if(address2!=null&&address2.length()!=0) {
				loc.setAddress2(address2);
				if (locationName == null) {
					locationName = address2;
				}
			}
			if(address3!=null&&address3.length()!=0) {
				loc.setAddress3(address3);
				if (locationName == null) {
					locationName = address3;
				}
			}
			if(address4!=null&&address4.length()!=0) {
				loc.setAddress4(address4);
				if (locationName == null) {
					locationName = address4;
				}
			}
			if (postalCode!=null && postalCode.length() != 0)
				loc.setPostal(postalCode);  // Martin added
			loc.setRegionName(region);
			loc.setCity(city);
			loc.setCountry(MCountry.get(Env.getCtx(), ID_COUNTRY));
			loc.saveEx();
		}
		bpl.setName(locationName);
		if(phone1!=null&&phone1.length()!=0)
			bpl.setPhone(phone1);
		if(phone2!=null&&phone2.length()!=0)
			bpl.setPhone2(phone2);
		if (!bpl.save())
		{
			strOut+="Cant Save Location";
		}
	}

	private void VerifMail()
	{
		MUser usr;
		String buffMail = getEmail();
		usr = new MUser(Env.getCtx(),AD_USER_ID,trx.getTrxName());
		if(AD_USER_ID==0)
			usr.setName(name);
		usr.setEMail(email);
		usr.setC_BPartner_ID(C_BPartner_ID);
		usr.setC_BPartner_Location_ID(C_BPartner_Location_ID);
		usr.saveEx();    
	}

	public static void saveInterestArea(boolean _cheked,String Interest_ID,int bpid)
	{
		MUser[] usr= MUser.getOfBPartner(Env.getCtx(),bpid);
		if(usr.length!=0)
		{
			int user_id=usr[0].getAD_User_ID();
			MContactInterest cin = MContactInterest.get(Env.getCtx(), Integer.valueOf(Interest_ID), user_id, true, null);
			if(_cheked)
				cin.subscribe();
			else
				cin.unsubscribe();
			cin.saveEx();
		}
	}

}