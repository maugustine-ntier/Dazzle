/******************************************************************************
 * Product: Adempiere ERP & CRM Smart Business Solution
 * Copyright (C) 2008 Low Heng Sin  All Rights Reserved.                      *
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
package org.adempiere.webui.component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.zkoss.zul.AbstractListModel;
import org.zkoss.zul.ListModel;
import org.zkoss.zul.ListModelExt;
import org.zkoss.zul.ListSubModel;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.event.ListDataEvent;

/**
 *
 * @author Low Heng Sin
 *
 */
public class SimpleListModelNew extends AbstractListModel
implements ListModelExt, ListSubModel, java.io.Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = -572148106182756840L;

	private ComboboxNew cbox = null;

	protected List list;

	private int[] maxLength;

	public SimpleListModelNew() {
		this(new ArrayList());
	}

	public SimpleListModelNew(List list) {
		this.list = list;
	}

	public Object getElementAt(int index) {
		if (index >= 0 && index < list.size())
			return list.get(index);
		else
			return null;
	}

	public int getSize() {
		return list.size();
	}

	protected StringBuffer truncate(String src, int maxLength) {
		int j = maxLength;
		while (j > 0 && Character.isWhitespace(src.charAt(j - 1)))
			--j;
		return new StringBuffer(j + 3)
			.append(src.substring(0, j)).append("...");
	}

	public void render(Listitem item, Object data) throws Exception {
		if (data instanceof Object[]) {
			renderArray(item, (Object[])data);
		} else if (data instanceof Collection) {
			renderCollection(item, (Collection)data);
		} else {
			String value = data != null ? data.toString() : "";
			renderCell(0, item, value);
		}
	}

	protected void renderCell(int col, Listitem item, String value) {
		String tooltip = null;
		if (maxLength != null && maxLength.length > col && maxLength[col] > 0 && value.length() > maxLength[col]) {
			tooltip = value;
			value = truncate(value, maxLength[col]).toString();
		}
		ListCell listCell = new ListCell(value);
		listCell.setParent(item);
		if (tooltip != null)
			listCell.setTooltiptext(tooltip);
	}

	private void renderCollection(Listitem item, Collection data) {
		int i = 0;
		for (Object col : data) {
			String value = (col != null ? col.toString() : "");
			renderCell(i, item, value);
			i++;
		}
	}

	private void renderArray(Listitem item, Object[] data) {
		int i = 0;
		for (Object col : data) {
			String value = (col != null ? col.toString() : "");
			renderCell(i, item, value);
			i++;
		}
	}

	/*public int getControls() {
		return DETACH_ON_RENDER;
	}*/

	public Listcell newListcell(Listitem item) {
		return null;
	}

	public Listitem newListitem(Listbox listbox) {
		ListItem item = new ListItem();
		item.applyProperties();
		return item;
	}

	public void setMaxLength(int[] maxLength) {
		this.maxLength = maxLength;
	}

	public void addElement(Object obj) {
		list.add(obj);
		int index = list.size() - 1;
		fireEvent(ListDataEvent.INTERVAL_ADDED, index, index);
	}

	public void add(int index, Object obj) {
		list.add(index, obj);
		fireEvent(ListDataEvent.INTERVAL_ADDED, index, index);
	}

	public void removeAllElements() {
		list.clear();
		fireEvent(ListDataEvent.CONTENTS_CHANGED, -1, -1);
	}

	public void removeElement(Object element) {
		int index = list.indexOf(element);
		list.remove(element);
		fireEvent(ListDataEvent.INTERVAL_REMOVED, index, index);
	}

	public void setElementAt(Object element, int index) {
		list.set(index, element);
		fireEvent(ListDataEvent.CONTENTS_CHANGED, index, index);
	}

	public int indexOf(Object value) {
		return list.indexOf(value);
	}

	@Override
	public ListModel getSubModel(Object value, int nRows) {
		if (value != null && cbox.getQueryName() != null && !cbox.getQueryName().equals("")) {
            String nameStartsWith = value.toString();
            List data = cbox.getItems(50, nameStartsWith);
            return ListModelFlyweight.create(data, nameStartsWith, cbox.getQueryName());
        }
        return ListModelFlyweight.create(Collections.emptyList(), cbox.EMPTY_STRING , cbox.getQueryName());
	}

	public ComboboxNew getCbox() {
		return cbox;
	}

	public void setCbox(ComboboxNew cbox) {
		this.cbox = cbox;
	}

	@Override
	public void sort(Comparator cmpr, boolean ascending) {
		// TODO Auto-generated method stub

	}

	@Override
	public String getSortDirection(Comparator cmpr) {
		// TODO Auto-generated method stub
		return null;
	}



}
