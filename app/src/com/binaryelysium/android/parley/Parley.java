/***************************************************************************
*   Copyright (C) 2009  Casey Link <unnamedrambler@gmail.com>             *
*   Copyright (C) 2008 Michael Hofer <mobvoc@unglaublich.priv.at>         *
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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Vector;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.view.View.OnCreateContextMenuListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.TableLayout.LayoutParams;

import com.binaryelysium.android.parley.R;
import com.binaryelysium.android.parley.data.Entry;
import com.binaryelysium.android.parley.data.VocSet;
import com.binaryelysium.android.parley.data.Word;
import com.binaryelysium.android.parley.filepicker.FilePicker;
import com.binaryelysium.android.parley.utils.ArabicReshaper;
import com.binaryelysium.android.parley.utils.UserTask;

import org.kxml2.io.KXmlParser;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class Parley extends Activity
{
    private TextView mStatus;
    private Button mStartButton;
    private ListView mLessonsList;
    private ArrayAdapter<String> mLessonsAdapter;
    private TableLayout mWordsList;
    private ViewFlipper mViewFlipper;
    private VocSet mVocSet; // will take the vocabulary data

    Animation mPushRightIn;
    Animation mPushRightOut;
    Animation mPushLeftIn;
    Animation mPushLeftOut;

    private static final int LESSONLIST_ID = 0;
    private static final int WORDLIST_ID = 1;
    private static final int BROWSE_ID = 2;

    private static final int MENU_LOAD = 0;

    /** Called when the activity is first created. */
    @Override
    public void onCreate( Bundle savedInstanceState )
    {

        super.onCreate( savedInstanceState );
        setContentView( R.layout.main );

        mStatus = ( TextView ) findViewById( R.id.status );
        mStartButton = ( Button ) findViewById( R.id.start_practice );
        mStartButton.setEnabled( false );
        mStartButton.setOnClickListener( mStartButtonOnClickListener );
        mLessonsList = ( ListView ) findViewById( R.id.lessons_list_view );
        mWordsList = ( TableLayout ) findViewById( R.id.words_table );
        mViewFlipper = ( ViewFlipper ) findViewById( R.id.ViewFlipper );
        mViewFlipper.setAnimateFirstView( false );
        mViewFlipper.setAnimationCacheEnabled( false );

        mPushLeftIn = AnimationUtils.loadAnimation( this, R.anim.push_left_in );
        mPushLeftOut = AnimationUtils
                .loadAnimation( this, R.anim.push_left_out );
        mPushRightIn = AnimationUtils
                .loadAnimation( this, R.anim.push_right_in );
        mPushRightOut = AnimationUtils.loadAnimation( this,
                R.anim.push_right_out );

        // mStatus.setText( "Loading File.." );
        // new ParleyKvtmlParser( "/sdcard/parley/arabic_test.kvtml"
        // ).execute();
    }

    /* Creates the menu items */
    public boolean onCreateOptionsMenu( Menu menu )
    {

        menu.add( 0, MENU_LOAD, 0, "Load Collection" );
        return true;
    }

    public boolean onOptionsItemSelected( MenuItem item )
    {

        switch ( item.getItemId() )
        {
        case MENU_LOAD:
            Intent i = new Intent( this, FilePicker.class );
            startActivityForResult( i, 0 );
            return true;
        }
        return false;
    }

    @Override
    protected void onActivityResult( int requestCode, int resultCode,
            Intent data )
    {

        super.onActivityResult( requestCode, resultCode, data );

        if ( requestCode == 0 )
        {
            if ( resultCode == RESULT_OK )
            {
//                String name = data.getStringExtra( FilePicker.FILE_NAME );
                String path = data.getStringExtra( FilePicker.ABSOLUTE_PATH );
                mStatus.setText( "Loading File.." );
                new ParleyKvtmlParser( path ).execute();
            }
        }
    }

    private void loadingComplete( VocSet vocset )
    {

        mVocSet = vocset;
        if ( mVocSet == null )
        {
            mStatus.setText( "Loading Complete.. ERROR!" );
            return;
        }
        mStatus.setText( "Loading Complete.. OK!" );

        String[] lessons = mVocSet.getLessonNames();
        System.out.println( "TOTAL LESSONS: " + lessons.length );
        // mLessonsAdapter = new ListAdapter(Parley.this, lessons);
        mLessonsAdapter = new ArrayAdapter<String>( this,
                android.R.layout.simple_list_item_multiple_choice, lessons );
        mLessonsList.setAdapter( mLessonsAdapter );
        mLessonsList.setChoiceMode( ListView.CHOICE_MODE_MULTIPLE );
        mLessonsList.setFocusable( false );
        mLessonsList
                .setOnCreateContextMenuListener( mLessonsContextMenuListener );
        mStartButton.setEnabled( true );
    }

    private OnClickListener mStartButtonOnClickListener = new OnClickListener()
    {

        public void onClick( View v )
        {

            SparseBooleanArray items = mLessonsList.getCheckedItemPositions();
            int count = 0;
            for ( int i = 0; i < items.size(); i++ )
            {
                System.out.println( "CHECKINGLESSON: " + i );
                if ( items.get( i, false ) )
                {
                    mVocSet.activateLesson( i, true );
                    count++;
                }
                else
                    mVocSet.activateLesson( i, false );

            }
            if ( count <= 0 )
                return;
            ParleyApplication.getInstance().setVocSet( mVocSet );

            String langname1 = mVocSet.getLanguageNames()[1];
            String langname2 = mVocSet.getLanguageNames()[0];

            Intent i = new Intent( Parley.this, FlashcardSession.class );
            System.out.println( "langnames: " + langname1 + " " + langname2 );
            i.putExtra( "langname1", langname1 );
            i.putExtra( "langname2", langname2 );
            startActivity( i );
            Toast.makeText( Parley.this,
                    "Practice Started for " + count + " lessons",
                    Toast.LENGTH_SHORT ).show();

        }

    };
    private OnCreateContextMenuListener mLessonsContextMenuListener = new OnCreateContextMenuListener()
    {

        public void onCreateContextMenu( ContextMenu menu, View v,
                ContextMenuInfo menuInfo )
        {

            menu.add( 0, BROWSE_ID, 0, "Browse Lesson" );

        }

    };

    public boolean onContextItemSelected( MenuItem item )
    {

        AdapterContextMenuInfo info = ( AdapterContextMenuInfo ) item
                .getMenuInfo();
        switch ( item.getItemId() )
        {
        case BROWSE_ID:
            // setNextAnimation();
            String langid = mVocSet
                    .getLanguageId( mVocSet.getLanguageNames()[1] );
            String langid2 = mVocSet
                    .getLanguageId( mVocSet.getLanguageNames()[0] );
            mVocSet.activateLesson( info.position, true );
            Entry[] entries = mVocSet.createSession( 0 );

            LayoutParams tlp = new LayoutParams();
            tlp.width = LayoutParams.FILL_PARENT;
            for ( int i = 0; i < entries.length; i++ )
            {
                if ( entries[i] != null )
                {
                    String w1 = entries[i].getWord( langid ).getText();
                    String w2 = entries[i].getWord( langid2 ).getText();

                    TableRow row = new TableRow( this );
                    TextView v1 = new TextView( this );
                    v1.setText( w1 );
                    row.addView( v1 );

                    TextView v2 = new TextView( this );
                    v2.setTypeface( Typeface.createFromAsset( getAssets(),
                            ParleyApplication.getFont() ) );
                    v2.setText( ArabicReshaper.Reshaper( w2 ) );
                    row.addView( v2 );
                    mWordsList.addView( row, tlp );
                }
            }
            mViewFlipper.setDisplayedChild( WORDLIST_ID );
            return true;
        default:
            return super.onContextItemSelected( item );
        }
    }

    public boolean onKeyDown( int keyCode, KeyEvent event )
    {

        if ( keyCode == KeyEvent.KEYCODE_BACK )
        {
            if ( mViewFlipper.getDisplayedChild() == WORDLIST_ID )
            {

                mViewFlipper.setDisplayedChild( LESSONLIST_ID );
                return true;
            }
            if ( event.getRepeatCount() == 0 )
            {
                finish();
                return true;
            }
        }
        return false;
    }

    
    /**
     * Parses a kvtml file into useable datastructures.
     * 
     * Internals of the implementation taken from MobVoc
     * by Michael Hofer <mobvoc@unglaublich.priv.at>.
     * http://sourceforge.net/projects/mobvoc
     *
     */
  
    private class ParleyKvtmlParser extends UserTask<String, Integer, VocSet>
    {

        private String mXMLResource; // jar-filename, local FS file or URL
        private InputStream mXMLInputStream;

        private VocSet mVocSet; // will take the vocabulary data

        private volatile boolean mCancelLoading = false; // for stopping the

        // loading

        // (cancel by user)

        public ParleyKvtmlParser( String xmlResource )
        {

            mXMLResource = xmlResource;
        }

        @Override
        public VocSet doInBackground( String... params )
        {

            try
            {
                // TODO method to cancel the parser

                // initialize xmlparser and open input stream
                KXmlParser parser = new KXmlParser();
                this.createXMLInputStream();
                parser.setInput( mXMLInputStream, "UTF-8" );

                // parse xml file
                parser.nextTag();
                parser.require( XmlPullParser.START_TAG, null, "kvtml" );

                // check for correct kvtml version
                String kvtmlVersion = parser
                        .getAttributeValue( null, "version" );
                if ( kvtmlVersion == null || !kvtmlVersion.equals( "2.0" ) )
                    throw new Exception(
                            "Wrong KVTML version. Open in Parley and resave to convert to KVTML2." );

                mVocSet = new VocSet(); // will take the vocabulary data
                while ( !mCancelLoading
                        && ( parser.nextTag() != XmlPullParser.END_TAG ) )
                {
                    // now we are inside the kvtml-tags; walk through the
                    // sections:
                    readDataSection( parser );
                }
                if ( !mCancelLoading )
                {
                    // finish walkthrough:
                    parser.require( XmlPullParser.END_TAG, null, "kvtml" );
                    parser.next();
                    parser.require( XmlPullParser.END_DOCUMENT, null, null );
                    parser = null;
                }
                else
                { // loading was cancelled by user
                    // throw new Exception( "Loading cancelled by user." );
                    return null;
                }

            }
            catch ( Exception e )
            {
                // something went wrong
                mVocSet = null; // don't deliver an incomplete VocSet
                e.printStackTrace();
                return null;
            }

            // HERE mVocSet is ready for return, or is null
            return mVocSet;
        }

        public void onPostExecute( VocSet result )
        {

            loadingComplete( result );
        }

        /**
         * Sets mInputStream for the set mXMLResource. Resource can be an XML
         * file in the jar ("jar://"), in the local FS ("file://"), or specified
         * as URL ("http://")
         * 
         * @throws IOException
         */
        private void createXMLInputStream() throws IOException
        {

            FileInputStream in = null;
            try
            {
                in = new FileInputStream( mXMLResource );
            }
            catch ( FileNotFoundException e )
            {
                throw new IOException( "File does not exist: " );
            }
            mXMLInputStream = in;
        }

        /**
         * Reads in relevant vocabulary data directly from the parser. Expects
         * the parser to be in the xml-document, on the first tag after the
         * kvtml-start-tag. Reads one section (sub element of kvtml) - should be
         * called repeatedly for further sections.
         * 
         * @param parser
         * @throws IOException
         * @throws XmlPullParserException
         */
        private void readDataSection( KXmlParser parser )
                throws XmlPullParserException, IOException
        {

            // we are looking for one of the following sections:
            // information, identifiers, tenses, usages, entries, lessons,
            // wordtypes
            parser.require( XmlPullParser.START_TAG, null, null );
            String sectionName = parser.getName(); // found section name
            // mAlert.setString( "Reading '" + sectionName + "' section..." );
            // //
            // update
            // alert
            int count = 0; // for counting elements in the subsections

            while ( !mCancelLoading
                    && parser.nextTag() != XmlPullParser.END_TAG )
            { // while in the found section
                // initiateGarbageCollection(); //call the garbage collector
                // before
                // loading each element
                if ( sectionName.equals( "information" ) )
                {
                    readInformationSection( parser );
                }
                else if ( sectionName.equals( "identifiers" ) )
                {
                    readIdentifiersSection( parser );
                }
                else if ( sectionName.equals( "tenses" ) )
                {
                    readTensesSection( parser );
                }
                else if ( sectionName.equals( "usages" ) )
                {
                    parser.skipSubTree();
                }
                else if ( sectionName.equals( "entries" ) )
                {
                    // update display
                    if ( ++count % 10 == 0 )
                    { // don't update display for every entry
                        // mAlert.setString( "Loaded " + count + " entries..."
                        // );
                    }
                    // read entries subsection:
                    readEntriesSection( parser );
                }
                else if ( sectionName.equals( "lessons" ) )
                {
                    readLessonsSection( parser );
                }
                else if ( sectionName.equals( "wordtypes" ) )
                {
                    readWordtypesSection( parser, null );
                }
                else
                {
                    // unknown section - just ignore it
                    parser.skipSubTree();
                }
            }
            // the last tag (END_TAG) should have the same name es the START_TAG
            if ( !mCancelLoading ) // only look at the end tag if the loop
                // wasn't
                // aborted prematurely
                parser.require( XmlPullParser.END_TAG, null, sectionName );
        }

        private void readInformationSection( KXmlParser parser )
                throws XmlPullParserException, IOException
        {

            // information-section contains: title, generator, license, category
            parser.require( XmlPullParser.START_TAG, null, null );
            String tagName = parser.getName(); // found tag name

            String text = parser.nextText(); // reads tag-content; positions
            // parser
            // on END_TAG

            if ( tagName.equals( "title" ) )
            {
                mVocSet.setTitle( text );
            }
            else
            {
                // unknown tag - ignore
            }

            parser.require( XmlPullParser.END_TAG, null, tagName );
        }

        private void readTensesSection( KXmlParser parser )
                throws XmlPullParserException, IOException
        {

            // tenses section should contain only "tense" tags
            parser.require( XmlPullParser.START_TAG, null, null );
            String tagName = parser.getName(); // found tag name

            String text = parser.nextText(); // reads tag-content; positions
            // parser
            // on END_TAG

            if ( tagName.equals( "tense" ) )
            {
                mVocSet.addTense( text );
            }
            else
            {
                // unknown tag - ignore
            }

            parser.require( XmlPullParser.END_TAG, null, tagName );
        }

        private void readLessonsSection( KXmlParser parser )
                throws XmlPullParserException, IOException
        {

            // lessons-section contains "lesson" subsections (or "container"? -
            // TODO: ask parley devs)
            String lessonSectionName = "lesson";
            try
            {
                parser.require( XmlPullParser.START_TAG, null, "lesson" ); // according
                // to
                // kvtml2.dtd...
            }
            catch ( XmlPullParserException e )
            {
                parser.require( XmlPullParser.START_TAG, null, "container" ); // ...but
                // parley
                // seems
                // to
                // use
                // container
                // instead
                lessonSectionName = "container";
            }
            // lesson (or container) found; we create it later (after parsing
            // the
            // important lesson data)
            String lName = null;
            Vector<String> lIds = new Vector<String>(); // contains entry-ids of this lesson (as
            // String)

            while ( parser.nextTag() != XmlPullParser.END_TAG )
            { // while in the lesson section
                // read in the content tags
                parser.require( XmlPullParser.START_TAG, null, null );
                String tagName = parser.getName(); // found tag name

                if ( tagName.equals( "name" ) )
                {
                    lName = parser.nextText();
                }
                else if ( tagName.equals( "entryid" ) )
                {// old format (id as text)
                    lIds.addElement( parser.nextText() );
                }
                else if ( tagName.equals( "entry" ) )
                { // new format (id as attribute)
                    lIds.addElement( parser.getAttributeValue( null, "id" ) );
                    parser.skipSubTree(); // don't look at the text
                }
                else
                {
                    parser.skipSubTree(); // unknown tag - skip
                }

                parser.require( XmlPullParser.END_TAG, null, tagName );
                // initiateGarbageCollection();
            }
            // all lesson data was read - create the lesson:
            mVocSet.createLesson( lName, lIds );

            parser.require( XmlPullParser.END_TAG, null, lessonSectionName ); // end
            // of
            // lesson/container
        }

        private void readIdentifiersSection( KXmlParser parser )
                throws XmlPullParserException, IOException
        {

            // contains identifier subsections (identifier = language)
            parser.require( XmlPullParser.START_TAG, null, "identifier" );
            // identifier (language) found; create it:
            String id = parser.getAttributeValue( null, "id" ); // id is
            // attribute
            // of identifier tag
            mVocSet.createLanguage( id );

            while ( parser.nextTag() != XmlPullParser.END_TAG )
            { // while in the identifier section
                // read in the content tags
                parser.require( XmlPullParser.START_TAG, null, null );
                String tagName = parser.getName(); // found tag name

                if ( tagName.equals( "article" ) )
                {
                    parser.skipSubTree(); // skip subsections; positions parser
                    // on
                    // END_TAG
                }
                else if ( tagName.equals( "personalpronouns" ) )
                { // subsection defining the personal pronouns
                    readPersonalPronounsDefinitions( parser, id );
                }
                else
                { // content-tag
                    String text = parser.nextText(); // reads tag-content;
                    // positions
                    // parser on END_TAG
                    if ( tagName.equals( "name" ) )
                    {
                        mVocSet.setLanguageName( id, text );
                    }
                    else
                    {
                        // unknown tag - ignore
                    }
                }

                parser.require( XmlPullParser.END_TAG, null, tagName );
            }
            parser.require( XmlPullParser.END_TAG, null, "identifier" );
        }

        private void readPersonalPronounsDefinitions( KXmlParser parser,
                String translationId ) throws XmlPullParserException,
                IOException
        {

            while ( parser.nextTag() != XmlPullParser.END_TAG )
            { // while in the personalpronouns section
                // contains singular and plural subsections
                parser.require( XmlPullParser.START_TAG, null, null );
                String tagName = parser.getName(); // found tag name
                String[] pronounArray = null;

                if ( tagName.equals( "singular" ) || tagName.equals( "plural" ) )
                {
                    pronounArray = new String[Word.NUMBER_OF_PRONOUNS];
                    // read in the pronouns
                    while ( parser.nextTag() != XmlPullParser.END_TAG )
                    { // while in the singular/plural subsection
                        parser.require( XmlPullParser.START_TAG, null, null );
                        String pronounName = parser.getName(); // found pronoun
                        String text = parser.nextText(); // reads tag-content;
                        // positions parser on
                        // END_TAG

                        if ( pronounName.equals( "firstperson" ) )
                            pronounArray[Word.CONJ_FIRSTPERSON] = text;
                        else if ( pronounName.equals( "secondperson" ) )
                            pronounArray[Word.CONJ_SECONDPERSON] = text;
                        else if ( pronounName.equals( "thirdpersonmale" ) )
                            pronounArray[Word.CONJ_THIRDPERSON_MALE] = text;
                        else if ( pronounName.equals( "thirdpersonfemale" ) )
                            pronounArray[Word.CONJ_THIRDPERSON_FEMALE] = text;
                        else if ( pronounName
                                .equals( "thirdpersonneutralcommon" ) )
                            pronounArray[Word.CONJ_THIRDPERSON_NEUTRAL] = text;

                        parser.require( XmlPullParser.END_TAG, null,
                                pronounName );
                    }
                }
                else
                {
                    parser.skipSubTree();
                }
                parser.require( XmlPullParser.END_TAG, null, tagName );

                // save pronounArray
                if ( tagName.equals( "singular" ) && pronounArray != null )
                    mVocSet.setPronounsSingular( translationId, pronounArray );
                else if ( tagName.equals( "plural" ) && pronounArray != null )
                    mVocSet.setPronounsPlural( translationId, pronounArray );
            }
        }

        // precreate stringbuffer for type concatenation:

        private void readWordtypesSection( KXmlParser parser, String parentType )
                throws XmlPullParserException, IOException
        {

            // contains containers (with nested sub containers) which contain
            // wordtype assignments to the entries/translations
            // an entry may appear only in a sub container; for recursive calls
            // we
            // give the parentType (type concatenation)
            // wordtypes must appear after all entries (can't assign types to
            // non-existing entries)
            parser.require( XmlPullParser.START_TAG, null, "container" );
            // container found
            StringBuffer type = new StringBuffer( 25 ); // should be big enough
            // for
            // usual type combinations
            // type-tag, occurs once per container; we assume that this tag
            // appears
            // always before the entries
            if ( parentType != null ) // recursive call
                type.append( parentType ).append( ", " ); // separator between
            // existing and next type
            // part
            String specialType = ""; // specialwordtype-tag, like type - doesn't
            // exist for all types/containers
            while ( parser.nextTag() != XmlPullParser.END_TAG )
            { // while in the container section
                // read in the content tags
                parser.require( XmlPullParser.START_TAG, null, null );
                String tagName = parser.getName(); // found tag name

                if ( tagName.equals( "container" ) )
                { // sub-container (should appear after all entries, or a more
                    // general type may overwrite the specialized type from the
                    // sub-container)
                    readWordtypesSection( parser, type.toString() ); // recursive
                    // call for
                    // nested
                    // subcontainers
                }
                else if ( tagName.equals( "entry" ) )
                { // subsection
                    readWordtypesEntrySubsection( parser, type.toString(),
                            specialType ); // assigns entry/translations to the
                    // given type
                }
                else
                { // content-tags: name or specialwordtype; we assume that both
                    // occur for the first entry in this container
                    if ( tagName.equals( "name" ) )
                    {
                        type.append( parser.nextText() ); // type for all
                        // entries in
                        // this container
                        // (concatenated with type
                        // from prev. recursion)
                    }
                    else if ( tagName.equals( "specialwordtype" ) )
                    { // thats the one we store with the entries
                        specialType = parser.nextText(); // specialwordtype for
                        // all
                        // entries in this
                        // container
                    }
                    else
                    {
                        parser.skipSubTree(); // unknown tagName - skip
                    }
                }
                parser.require( XmlPullParser.END_TAG, null, tagName );
            }
            parser.require( XmlPullParser.END_TAG, null, "container" );
        }

        private void readWordtypesEntrySubsection( KXmlParser parser,
                String type, String specialType )
                throws XmlPullParserException, IOException
        {

            // read entry id (attribute):
            String entryId = parser.getAttributeValue( null, "id" );

            while ( parser.nextTag() != XmlPullParser.END_TAG )
            { // while in the entry subsection
                // read in the content tags:
                parser.require( XmlPullParser.START_TAG, null, null );
                String tagName = parser.getName(); // found tag name

                // empty tranlsation tags with the translation id
                if ( tagName.equals( "translation" ) )
                {
                    String translationId = parser
                            .getAttributeValue( null, "id" );
                    parser.nextText(); // only to position parser on END_TAG
                    // assign type to translation:
                    Entry e = mVocSet.getEntry( entryId );
                    if ( e != null )
                    { // if the entry exists (if it appeared before the
                        // wordtypes
                        // section in xml)
                        Word w = e.getWord( translationId );
                        if ( w != null )
                        {
                            w.setType( type );
                            w.setSpecialWordType( specialType );
                        }
                    }
                }

                parser.require( XmlPullParser.END_TAG, null, tagName );
                // initiateGarbageCollection();
            } // end of translation subsection
        }

        private void readEntriesSection( KXmlParser parser )
                throws XmlPullParserException, IOException
        {

            // entries-section contains entry subsections
            parser.require( XmlPullParser.START_TAG, null, "entry" );
            // entry found
            String id = parser.getAttributeValue( null, "id" ); // read in id
            // attribute...
            Entry e = mVocSet.createEntry( id ); // ...and create entry

            while ( parser.nextTag() != XmlPullParser.END_TAG )
            { // while in the entry section
                // read in the content tags
                parser.require( XmlPullParser.START_TAG, null, null );
                String tagName = parser.getName(); // found tag name

                if ( tagName.equals( "translation" ) )
                { // subsection (word in a specific language)
                    readTranslationSubsection( parser, e );
                }
                else
                { // content-tag (inquery, inactive, sizehint)
                    if ( tagName.equals( "inactive" ) )
                    {
                        String text = parser.nextText(); // reads tag-content;
                        // positions parser on
                        // END_TAG
                        if ( text.equals( "true" ) )
                        {
                            // TODO: e.setInactive(true);
                        }
                    }
                    else
                    {
                        parser.skipSubTree(); // unknown tag - skip
                    }
                }

                parser.require( XmlPullParser.END_TAG, null, tagName );
            }
            parser.require( XmlPullParser.END_TAG, null, "entry" );
        }

        private void readTranslationSubsection( KXmlParser parser, Entry e )
                throws XmlPullParserException, IOException
        {

            // read translation id (attribute):
            Word w = e.addTranslation( parser.getAttributeValue( null, "id" ) );

            while ( parser.nextTag() != XmlPullParser.END_TAG )
            { // while in the translation subsection)
                // read in the content tags:
                parser.require( XmlPullParser.START_TAG, null, null );
                String tagName = parser.getName(); // found tag name

                // possible subsections: grade, wordtype, comparison,
                // conjugation,
                // multiplechoice
                // possible tags: text, inquery, comment, pronunciation,
                // falsefriend, antonym, synonym, example,
                // usage, paraphrase, image, sound
                if ( tagName.equals( "grade" ) || tagName.equals( "wordtype" )
                        || tagName.equals( "comparison" )
                        || tagName.equals( "multiplechoice" ) )
                {
                    parser.skipSubTree(); // skip subsections; positions parser
                    // on
                    // END_TAG
                }
                else if ( tagName.equals( "conjugation" ) )
                { // subsection
                    readConjugationSubsection( parser, w );
                }
                else
                { // content-tag
                    if ( tagName.equals( "text" ) )
                    {
                        w.setText( parser.nextText() ); // reads tag-content;
                        // positions parser on
                        // END_TAG
                    }
                    else if ( tagName.equals( "pronunciation" ) )
                    {
                        w.setPronunciation( parser.nextText() );
                    }
                    else
                    {
                        parser.skipSubTree(); // unknown tag - skip
                    }
                }

                parser.require( XmlPullParser.END_TAG, null, tagName );
            } // end of translation subsection
        }

        private void readConjugationSubsection( KXmlParser parser, Word w )
                throws XmlPullParserException, IOException
        {

            String tense = null; // tense name for this conjugation
            String[] conjugationsSingular = new String[Word.NUMBER_OF_PRONOUNS];
            String[] conjugationsPlural = new String[Word.NUMBER_OF_PRONOUNS];
            int personFoundIndex; // should be set to one of Word.CONJ_...
            // (index
            // for conjugation array)
            boolean tenseIsEmpty = true; // empty conjugations sections consist
            // only
            // of the tense name - dont save it if it's
            // empty

            while ( parser.nextTag() != XmlPullParser.END_TAG )
            { // while in the conjugation subsection)
                // read in the content tags:
                parser.require( XmlPullParser.START_TAG, null, null );
                String tagName = parser.getName(); // found tag name

                if ( tagName.equals( "tense" ) )
                { // content tag naming the tense
                    tense = parser.nextText(); // reads tag-content; positions
                    // parser on END_TAG
                }
                else if ( tagName.equals( "singular" )
                        || tagName.equals( "plural" ) )
                { // subsections
                    tenseIsEmpty = false; // if there is such a subsection,
                    // there is
                    // usually at least one person entry
                    while ( parser.nextTag() != XmlPullParser.END_TAG )
                    { // while in the singular/plural subsection
                        parser.require( XmlPullParser.START_TAG, null, null );
                        String person = parser.getName(); // tagnames like
                        // "firstperson",
                        // "secondperson",...
                        if ( person.equals( "thirdperson" ) )
                        { // old format, subsection
                            parser.skipSubTree(); // skip old format
                        }
                        else
                        { // all other persons should contain only
                            // string-contents
                            String personText = parser.nextText();
                            if ( personText.length() > 0 )
                            { // only if this person text is set
                                // set the array index for the type of person
                                // found
                                personFoundIndex = -1;
                                if ( person.equals( "firstperson" ) )
                                    personFoundIndex = Word.CONJ_FIRSTPERSON;
                                else if ( person.equals( "secondperson" ) )
                                    personFoundIndex = Word.CONJ_SECONDPERSON;
                                else if ( person.equals( "thirdpersonmale" ) )
                                    personFoundIndex = Word.CONJ_THIRDPERSON_MALE;
                                else if ( person.equals( "thirdpersonfemale" ) )
                                    personFoundIndex = Word.CONJ_THIRDPERSON_FEMALE;
                                else if ( person
                                        .equals( "thirdpersonneutralcommon" ) )
                                    personFoundIndex = Word.CONJ_THIRDPERSON_NEUTRAL;
                                // save the found person in the according
                                // conjugations array
                                if ( tagName.equals( "singular" )
                                        && personFoundIndex != -1 ) // if it is
                                    // a
                                    // known
                                    // index/conjugation
                                    conjugationsSingular[personFoundIndex] = personText;
                                else if ( tagName.equals( "plural" )
                                        && personFoundIndex != -1 )
                                    conjugationsPlural[personFoundIndex] = personText;
                            }
                        }
                        parser.require( XmlPullParser.END_TAG, null, person );
                    }

                }
                else
                { // unknown
                    parser.skipSubTree(); // skip it (whether it's an entry or a
                    // subsection)
                }

                parser.require( XmlPullParser.END_TAG, null, tagName );
            } // end of conjugation subsection

            // save the found conjugations with the word
            if ( !tenseIsEmpty ) // only if there where conjuation forms in this
                // conjugation subsection
                w.setConjugations( tense, conjugationsSingular,
                        conjugationsPlural );
        }
    }

}
