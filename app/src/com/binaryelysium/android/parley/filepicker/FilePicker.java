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

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;

import com.binaryelysium.android.parley.R;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

class NameFilter implements FilenameFilter
{

    public boolean accept( File dir, String name )
    {

        // in this app we are filtering for kvtml files
        return ( name.endsWith( ".kvtml" ) || dir.isDirectory() );
    }
}

/**
 * A simple list based sdcard file picker.
 * 
 * It returns the extras: ABSOLUTE_PATH, and FILE_NAME
 * 
 * @author Casey Link
 * 
 */
public class FilePicker extends ListActivity
{

    public static final String ABSOLUTE_PATH = "ABSOLUTE_PATH";
    public static final String FILE_NAME = "FILE_NAME";
    private static final String DATA_PATH = new String( "/sdcard" );
    private NameFilter mFilter = new NameFilter();
    private File mCurrDir;
    private FileAdapter mAdapter;

    public void onCreate( Bundle icicle )
    {
        super.onCreate( icicle );
        setContentView( R.layout.file_listing );
        mCurrDir = new File( DATA_PATH );
        refreshFileList();
    }

    public void refreshFileList()
    {

        try
        {
            setTitle( mCurrDir.getPath() );
            if ( mCurrDir.listFiles( mFilter ).length > 0 )
            {
                ArrayList<FileEntry> entries = new ArrayList<FileEntry>();

                // Create the '..' entry.
                // We don't want to add the '..' dir at the /sdcard level
                if ( !mCurrDir.getPath().equals( DATA_PATH ) )
                {
                    File parent = mCurrDir.getParentFile();
                    FileEntry parentEntry = new FileEntry( "..", parent
                            .list( mFilter ).length
                            + " Items", parent, R.drawable.ic_launcher_folder );
                    entries.add( parentEntry );
                }
                for ( File file : mCurrDir.listFiles( mFilter ) )
                {
                    String label2;
                    int iconid;
                    if ( file.isDirectory() )
                    {
                        label2 = file.list( mFilter ).length + " Items";
                        iconid = R.drawable.ic_launcher_folder;
                    }
                    else
                    // isFile
                    {
                        label2 = file.length() + " Bytes";
                        iconid = R.drawable.icon_file;
                    }
                    FileEntry e = new FileEntry( file.getName(), label2, file,
                            iconid );
                    entries.add( e );
                }
                mAdapter = new FileAdapter( this );
                mAdapter.setSource( entries );
                setListAdapter( mAdapter );
            }
        }
        catch ( Exception e )
        {
            Toast.makeText( FilePicker.this, "No SD Card Found",
                    Toast.LENGTH_LONG ).show();
            Intent mIntent = new Intent();
            setResult( RESULT_CANCELED, mIntent );
            finish();
        }

    }

    @Override
    protected void onListItemClick( ListView l, View v, int position, long id )
    {

        File item = ( File ) mAdapter.getItem( position );
        if ( item.isDirectory() )
        {
            mCurrDir = item;
            refreshFileList();
        }
        else
        {
            // File Picked
            Intent mIntent = new Intent();
            mIntent.putExtra( ABSOLUTE_PATH, item.getAbsolutePath() );
            mIntent.putExtra( FILE_NAME, item.getName() );
            setResult( RESULT_OK, mIntent );
            finish();
        }
    }

    public boolean onKeyDown( int keyCode, KeyEvent event )
    {

        // We want to intercept BACK presses so we can
        // navigate back up the directory tree.
        if ( keyCode == KeyEvent.KEYCODE_BACK )
        {
            if ( mCurrDir.getPath().equals( DATA_PATH ) )
            {
                Intent mIntent = new Intent();
                setResult( RESULT_CANCELED, mIntent );
                finish();
                return true;
            }
            mCurrDir = mCurrDir.getParentFile();
            refreshFileList();
            return true;
        }
        return false;
    }

}
