/***************************************************************************
*   Copyright (C) 2009  Casey Link <unnamedrambler@gmail.com>             *
*                                                                         *
*   This program is free software; you can redistribute it and/or modify  *
*   it under the terms of the GNU General Public License as published by  *
*   the Free Software Foundation; either version 3 of the License, or     *
*   (at your option) any later version.                                   *
*                                                                         *
*   This program is distributed in the hope that it will be useful,       *
*   but WITHOUT ANY WARRANTY; without even the implied warranty of        *
*   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the         *
*   GNU General Public License for more details.                          *
*                                                                         *
*   You should have received a copy of the GNU General Public License     *
*   along with this program.  If not, see <http://www.gnu.org/licenses/>. *
***************************************************************************/

package com.binaryelysium.android.parley.filepicker;

import java.util.ArrayList;

import com.binaryelysium.android.parley.R;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class FileAdapter extends BaseAdapter
{

    protected Activity mContext;

    private ArrayList<FileEntry> mList;

    public FileAdapter( Activity context )
    {

        mContext = context;
        mList = new ArrayList<FileEntry>();
    }

    public int getCount()
    {
        if(mList != null)
            return mList.size();
        else
            return 0;
    }

    public Object getItem( int position )
    {
        return mList.get(position).value;
    }

    public long getItemId( int position )
    {
        return position;
    }

    public View getView( int position, View convertView, ViewGroup parent )
    {

        View row = convertView;

        ViewHolder holder;

        if ( row == null )
        {
            LayoutInflater inflater = mContext.getLayoutInflater();
            row = inflater.inflate( R.layout.file_item, null );

            holder = new ViewHolder();
            holder.label = ( TextView ) row.findViewById( R.id.file_name );
            holder.label2 = ( TextView ) row.findViewById( R.id.file_details );
            holder.image = ( ImageView ) row.findViewById( R.id.file_icon );

            row.setTag( holder );
        }
        else
        {
            holder = ( ViewHolder ) row.getTag();
        }

        holder.label.setText( mList.get( position ).text );
        if ( mList.get( position ).text2 != null )
        {
            holder.label2.setText( mList.get( position ).text2 );
            holder.label2.setVisibility( View.VISIBLE );
        }
        else
        {
            holder.label2.setVisibility( View.GONE );
        }
        if ( mList.get( position ).icon_id == -1 )
            holder.image.setVisibility( View.GONE );
        else
            holder.image.setVisibility( View.VISIBLE );
        
            if( mList.get(position).icon_id >= 0 )
                holder.image.setImageResource(mList.get(position).icon_id);
            
            return row;
    }
    
    /**
     * Sets list of Iconified Entires as a source for the adapter
     * 
     * @param list
     */
    public void setSource(ArrayList<FileEntry> list) {
        mList = list;
    }

    /**
     * Holder pattern implementation
     * 
     * @author Casey Link
     */
    static class ViewHolder
    {

        TextView label;
        TextView label2;
        ImageView image;
    }

}
