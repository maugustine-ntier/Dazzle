package org.adempiere.webui.component;

import java.util.Comparator;
import java.util.List;

import org.compiere.minigrid.IDColumn;
import org.compiere.util.Util;
import org.zkoss.zk.ui.Component;

public class WListItemRenderDazzle extends WListItemRendererMA {


	public WListItemRenderDazzle() {
		super();
		// TODO Auto-generated constructor stub
	}

	public WListItemRenderDazzle(List<? extends String> columnNames,
			List<? extends Integer> columnWidths) {
		super(columnNames, columnWidths);
		// TODO Auto-generated constructor stub
	}

	public WListItemRenderDazzle(List<? extends String> columnNames) {
		super(columnNames);
		// TODO Auto-generated constructor stub
	}




	/**
	 * Create a ListHeader using the given <code>headerValue</code> to
	 * generate the header text.
	 * The <code>toString</code> method of the <code>headerValue</code>
	 * is used to set the header text.
	 *
	 * @param headerValue	The object to use for generating the header text.
	 * @param tooltipText
     * @param headerIndex   The column index of the header
	 * @param classType
	 * @return The generated ListHeader
	 * @see #renderListHead(ListHead)
	 */
	@Override
	public Component getListHeaderComponent(Object headerValue, String tooltipText, int headerIndex, Class<?> classType)
	{
        ListHeader header = null;

        String headerText = headerValue.toString();
        if (m_headers.size() <= headerIndex || m_headers.get(headerIndex) == null)
        {
        	if (classType != null && classType.isAssignableFrom(IDColumn.class))
        	{
        		header = new ListHeader("");
        		header.setWidth("30px");
        	}
        	else
        	{
	            Comparator<Object> ascComparator =  getColumnComparator(true, headerIndex);
	            Comparator<Object> dscComparator =  getColumnComparator(false, headerIndex);

	            header = new ListHeader(headerText);
	            if (!Util.isEmpty(tooltipText))
	            {
	            	header.setTooltiptext(tooltipText);
	            }

	            header.setSort("auto");
	            header.setSortAscending(ascComparator);
	            header.setSortDescending(dscComparator);

	            // Martin added
	            int width = 0;
	            WTableColumn column = getM_tableColumns().get(headerIndex);
	            if (column.getPreferredWidth() > 0) {
	            	width = column.getPreferredWidth();
	            } else {
	            	width = headerText.trim().length() * 9;
	            }

	            /*int width = headerText.trim().length() * 9;
	            if (width > 300)
	            	width = 300;
	            else if (classType != null)
	            {
	            	if (classType.equals(String.class))
	            	{
	            		if (width > 0 && width < 180)
	            			width = 180;
	            	}
	            	else if (classType.equals(IDColumn.class))
	            	{
	            		header.setSort("none");
	            		if (width < 30)
	            			width = 30;
	            	}
	            	else if (classType.isAssignableFrom(Boolean.class))
	            	{
	            		if (width > 0 && width < 30)
	            			width = 30;
	            	}
		            else if (width > 0 && width < 100)
	            		width = 100;
	            }
	            else if (width > 0 && width < 100)
	            	width = 100;*/

	            //header.setStyle("min-width: " + width + "px");
	           header.setWidth(width + "px");
        	}
            m_headers.add(header);
        }
        else
        {
            header = m_headers.get(headerIndex);

            if (!header.getLabel().equals(headerText))
            {
                header.setLabel(headerText);
            }
        }

		return header;
	}
	public Component getListHeaderComponent(Object headerValue,
			int headerIndex, Class<?> classType) {
		 ListHeader header = null;

	        String headerText = headerValue.toString();
	        if (m_headers.size() <= headerIndex || m_headers.get(headerIndex) == null)
	        {
	        	if (classType != null && classType.isAssignableFrom(IDColumn.class))
	        	{
	        		header = new ListHeader("");
	        		header.setWidth("20px");
	        	}
	        	else
	        	{
		            Comparator<Object> ascComparator =  getColumnComparator(true, headerIndex);
		            Comparator<Object> dscComparator =  getColumnComparator(false, headerIndex);

		            header = new ListHeader(headerText);

		            header.setSort("auto");
		            header.setSortAscending(ascComparator);
		            header.setSortDescending(dscComparator);

		            // Martin added
		            int width = 0;
		            WTableColumn column = getM_tableColumns().get(headerIndex);
		            if (column.getPreferredWidth() > 0) {
		            	width = column.getPreferredWidth();
		            } else {
		            	width = headerText.trim().length() * 9;
		            }

		           //int width = headerText.trim().length() * 9;
		           // if (width > 300)
		            	//width = 300;
		           // else
		           // 	if (classType != null)
		         /*   {
		            	if (classType.equals(String.class))
		            	{
		            		if (width > 0 && width < 180)
		            			width = 180;
		            	}
		            	else if (classType.equals(IDColumn.class))
		            	{
		            		header.setSort("none");
		            		if (width == 0)
		            			width = 30;
		            	}
			            else if (width > 0 && width < 100 && (classType == null || !classType.isAssignableFrom(Boolean.class)))
		            		width = 100;
		            }
		            else if (width > 0 && width < 100)
		            	width = 100;*/
		            header.setStyle("min-width: " + width + "px");
		           // header.setWidth(width + "px");
	        	}
	            m_headers.add(header);
	        }
	        else
	        {
	            header = m_headers.get(headerIndex);

	            if (!header.getLabel().equals(headerText))
	            {
	                header.setLabel(headerText);
	            }
	        }

			return header;
	}



}
