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
*   along with this program; if not, write to the                         *
*   Free Software Foundation, Inc.,                                       *
*   51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.         *
***************************************************************************/

package com.binaryelysium.android.parley;

import com.binaryelysium.android.parley.data.Entry;
import com.binaryelysium.android.parley.data.VocSet;
import com.binaryelysium.android.parley.utils.ArabicReshaper;

import android.app.Activity;
import android.app.Dialog;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

public class FlashcardSession extends Activity
{

    private TextView mWord1;
    private TextView mWord2;
    private ViewFlipper mViewFlipper;

    private Entry[] mSessionEntries;
    private int mTotalEntryCount;
    private int mCurrentId;

    private VocSet mVocSet; // will take the vocabulary data
    private String mLangId1; // language ids and names to exercise
    private String mLangId2;
    private String mLangName1;
    private String mLangName2;
    
    Animation mAnimLeftIn;
    Animation mAnimLeftOut;
    Animation mAnimRightIn;
    Animation mAnimRightOut;


    private static final int MENU_END = 0;
    private static final int MENU_SKIP = 1;
    private static final int MENU_SHOW = 2;
    private static final int MENU_PREV = 3;
    private static final int MENU_KNEW = 4;
    private static final int MENU_NEXT = 5;

    private static final int DIALOG_ENDSESSION = 0;

    /** Called when the activity is first created. */
    @Override
    public void onCreate( Bundle savedInstanceState )
    {

        super.onCreate( savedInstanceState );
        setContentView( R.layout.flashcard );

        mWord1 = ( TextView ) findViewById( R.id.word1 );
        mWord2 = ( TextView ) findViewById( R.id.word2 );
        mViewFlipper = ( ViewFlipper ) findViewById( R.id.FCViewFlipper );
        mViewFlipper.setAnimateFirstView( false );
        mViewFlipper.setAnimationCacheEnabled( false );
        mViewFlipper.setLongClickable( true );
        mViewFlipper.setFocusable( false );
        
        mAnimLeftIn = AnimationUtils.loadAnimation(this, R.anim.push_left_in);
        mAnimLeftOut = AnimationUtils.loadAnimation(this, R.anim.push_left_out);
        mAnimRightIn = AnimationUtils.loadAnimation(this, R.anim.push_right_in);
        mAnimRightOut = AnimationUtils.loadAnimation(this, R.anim.push_right_out);
        
        mVocSet = ParleyApplication.getInstance().getVocSet();

        Intent i = getIntent();
        mLangName1 = i.getStringExtra( "langname1" );
        mLangName2 = i.getStringExtra( "langname2" );
        mLangId1 = mVocSet.getLanguageId( mLangName1 );
        mLangId2 = mVocSet.getLanguageId( mLangName2 );

        mSessionEntries = createSession();
        mTotalEntryCount = mSessionEntries.length;
        mCurrentId = -1;

        final GestureDetector detect = new GestureDetector(
                new GestureDetector.SimpleOnGestureListener()
                {

                    public boolean onFling( MotionEvent e1, MotionEvent e2,
                            float velocityX, float velocityY )
                    {

                        float x = e2.getRawX() - e1.getRawX();
                        float y = e2.getRawY() - e1.getRawY();
                        int goal = mViewFlipper.getWidth() / 2;

                        // check for horizontal movement and at least 50% of the
                        // screen crossed
                        if ( Math.abs( y ) < 100 )
                        {
                            if ( x > goal )
                            {
                                showPrevEntry();
                                return true;
                            }
                            if ( x < -goal )
                            {
                                showNextEntry();
                                return true;
                            }
                        }
                        return false;
                    }
                    
                    @Override
                    public boolean onSingleTapUp( MotionEvent e )
                    {
                    
                        flipCard();
                        return true;
                    }
                    
                    @Override
                    public void onLongPress( MotionEvent e )
                    {
                    
                        markKnown();
                        Toast.makeText( FlashcardSession.this, "Card Marked Known",
                                Toast.LENGTH_SHORT ).show();
                        
                    }

                } );

        mViewFlipper.setOnTouchListener( new OnTouchListener()
        {
            public boolean onTouch( View v, MotionEvent event )
            {
                // pass any touch events back to detector
                return detect.onTouchEvent( event );
            }
        } );

        showNextEntry(); // start the session
    } // end onCreate

    @Override
    public boolean onPrepareOptionsMenu( Menu menu )
    {

        menu.clear();
        if ( mViewFlipper.getDisplayedChild() == 0 )
        {
            menu.add( 0, MENU_SKIP, 0, "Skip" );
            menu.add( 0, MENU_SHOW, 0, "Show" );
            menu.add( 0, MENU_PREV, 0, "Previous" );
            menu.add( 0, MENU_END, 0, "End Exercise" );

        }
        else
        {
            menu.add( 0, MENU_KNEW, 0, "Knew it" );
            menu.add( 0, MENU_NEXT, 0, "Next" );
            menu.add( 0, MENU_PREV, 0, "Previous" );
            menu.add( 0, MENU_END, 0, "End Exercise" );
        }
        return true;
    }

    /* Creates the menu items */
    public boolean onCreateOptionsMenu( Menu menu )
    {

        return true;
    }

    /* Handles item selections */
    public boolean onOptionsItemSelected( MenuItem item )
    {

        switch ( item.getItemId() )
        {
        case MENU_END:
            showDialog( DIALOG_ENDSESSION );
            return true;
        case MENU_SKIP:
        case MENU_NEXT:
            showNextEntry();
            return true;
        case MENU_PREV:
            showPrevEntry();
            return true;
        case MENU_KNEW:
            markKnown();
            return true;
        case MENU_SHOW:
            flipCard();
            return true;
        }
        return false;
    }

    @Override
    protected Dialog onCreateDialog( int id )
    {

        switch ( id )
        {
        case DIALOG_ENDSESSION:
            return new AlertDialog.Builder( FlashcardSession.this ).setTitle(
                    "End the Exercise Session" ).setPositiveButton( "Yes",
                    new DialogInterface.OnClickListener()
                    {

                        public void onClick( DialogInterface dialog,
                                int whichButton )
                        {

                            finish();
                        }
                    } ).setNegativeButton( "No",
                    new DialogInterface.OnClickListener()
                    {

                        public void onClick( DialogInterface dialog,
                                int whichButton )
                        {

                        }
                    } ).create();
        }
        return null;
    }

    private void flipCard()
    {

        clearAnimation();
        mViewFlipper.setDisplayedChild( mViewFlipper.getDisplayedChild() * -1
                + 1 );
    }
    
    private void clearAnimation()
    {
        mViewFlipper.setInAnimation( null );
        mViewFlipper.setOutAnimation( null );
    }
    
    private void markKnown()
    {
        mSessionEntries[mCurrentId].setLevel( 1 );
        showNextEntry();
    }

    private void showNextEntry()
    {

        if ( mCurrentId + 1 < mSessionEntries.length )
        {
            Entry e = mSessionEntries[++mCurrentId];
            if ( e.getLevel() > 0 )
            { // entry is on a higher level (was already known)
                showNextEntry(); // don't show it, but go to the next one
                return;
            }
            mViewFlipper.setInAnimation(mAnimLeftIn);
            mViewFlipper.setOutAnimation(mAnimLeftOut);
            updateEntryDisplays( e );
        }
        else
        { // all entries displayed
            createFinishedAlert();
        }
    }

    private void createFinishedAlert()
    {

        Toast.makeText( FlashcardSession.this, "No More Words",
                Toast.LENGTH_LONG ).show();
    }

    private void showPrevEntry()
    {

        if ( mCurrentId > 0 )
        {
            Entry e = mSessionEntries[--mCurrentId];
            if ( e.getLevel() > 0 )
            { // entry is on a higher level (was already known)
                showPrevEntry(); // don't show it, but go to the previous one
                return;
            }
            mViewFlipper.setInAnimation(mAnimRightIn);
            mViewFlipper.setOutAnimation(mAnimRightOut);
            updateEntryDisplays( e );
            
        }
        else
        {
            Toast.makeText( FlashcardSession.this, "No Previous Entry",
                    Toast.LENGTH_LONG ).show();
        }
    }

    private void updateEntryDisplays( Entry e )
    {

        setTitle( "Exercise (" + ( mCurrentId + 1 ) + "/" + mTotalEntryCount
                + ")" );

        String word1 = e.getWord( mLangId1 ).getText();
        String word2 = e.getWord( mLangId2 ).getText();

        mWord2.setTypeface( Typeface.createFromAsset( getAssets(),
                ParleyApplication.getFont() ) );

        mWord1.setText( word1 );
        mWord2.setText( ArabicReshaper.Reshaper( word2 ) );
        mViewFlipper.setDisplayedChild( 0 );
    }

    private Entry[] createSession()
    {

        return mVocSet.createRandomSession( 0 ); // level 0 (all entries never
        // marked as known)
    }
}
