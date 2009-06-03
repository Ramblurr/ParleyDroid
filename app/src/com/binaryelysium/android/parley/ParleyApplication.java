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
package com.binaryelysium.android.parley;

import com.binaryelysium.android.parley.data.VocSet;

import android.app.Application;


public class ParleyApplication extends Application
{
    
    private static ParleyApplication instance;
    private static final String font = "DejaVuSansCondensed.ttf";
    private static final String fontbold = "DejaVuSansCondensed-Bold.ttf";
    
    private VocSet mCurrentVocSet;
    

    public static ParleyApplication getInstance()
    {

        return instance;
    }
    
    public static String getFont()
    {
        return font;
    }
    
    public static String getFontBold()
    {
        return fontbold;
    }
    
    public void onCreate()
    {

        super.onCreate();
        instance = this;
        mCurrentVocSet = null;
    }
    
    public void onTerminate()
    {
        mCurrentVocSet = null;
        instance = null;
        super.onTerminate();
    }
    
    public void setVocSet( VocSet vocset )
    {
        mCurrentVocSet = vocset;
    }

    public VocSet getVocSet()
    {
        return mCurrentVocSet;
    }
}
