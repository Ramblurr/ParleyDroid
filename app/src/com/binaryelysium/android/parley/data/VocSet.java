/**
 * Copyright (C) 2009 Casey Link <unnamedrambler@gmail.com>
 * Copyright (C) 2008 Michael Hofer (mobvoc [at] unglaublich.priv.at)

 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301, USA.
 */
package com.binaryelysium.android.parley.data;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Random;
import java.util.Vector;

/**
 * @author michhof
 *
 */
public class VocSet {
  private String mTitle;

  private Vector mTenses; //contains the available tenses for this vocset (ordered)
  private Hashtable mLanguages; //key: language id (String); value: Language object
  private Vector mLessons; //contains lesson objects (ordered)
  private Hashtable mAllEntries; //contains all entries in this vocset; key: entry id (String); value: Entry object
  
  private Hashtable mPronounsSingular; //key: translationId (String); value: String array containig the pronouns
  private Hashtable mPronounsPlural;


  public VocSet() {
    mTenses = new Vector();
    mLanguages = new Hashtable();
    mLessons = new Vector();
    mAllEntries = new Hashtable();
    mPronounsSingular = new Hashtable();
    mPronounsPlural = new Hashtable();
  }
  
  public void setTitle(String title) {
    mTitle = title;    
  }
  
  public String getTitle() {
    return mTitle;
  }

  public void createLanguage(String id) {
    mLanguages.put(id, new Language(id));
  }

  public void setLanguageName(String id, String text) {
    ((Language) mLanguages.get(id)).setName(text);    
  }

  public void createLesson(String name, Vector ids) {
    mLessons.addElement(new Lesson(name, ids));    
  }
  
  public String[] getLanguageNames() {
    int lCount = mLanguages.size();
    if( lCount == 0 )
      return null;
    else {
      String[] langs = new String[lCount];
      int i = 0;
      for(Enumeration e = mLanguages.elements(); e.hasMoreElements(); ) {
        langs[i++] = ((Language)e.nextElement()).getName();
      }
      return langs;
    }
  }

  public String[] getLessonNames() {
    int lCount = mLessons.size();
    if( lCount == 0 )
      return null;
    else {
      String[] lessons = new String[lCount];
      int i = 0;
      for(Enumeration e = mLessons.elements(); e.hasMoreElements(); ) {
        lessons[i++] = ((Lesson)e.nextElement()).getName();
      }
      return lessons;
    }    
  }

  public void activateLesson(int lessonIndex, boolean active) {
    Lesson l = (Lesson)mLessons.elementAt(lessonIndex);
    l.setActive(active);
  }

  public String getLanguageId(String langString) { 
    String k = null;
    for (Enumeration e = mLanguages.keys() ; e.hasMoreElements() ;) { //enumerate all keys in the hash
      k = (String)e.nextElement();
      Language l = (Language)mLanguages.get(k);
      if( l.getName().equals(langString) ) //FIXME: doesn't work on duplicate values (languages)
                                            //better remember the ids in the first place (when adding the languages to the screen)
        return k; //k is the key of the language
    }
    return k;
  }

  public Entry createEntry(String entryId) {
    Entry e = new Entry(entryId, this);
    mAllEntries.put(entryId, e);
    return e;
  }
  
  public Entry[] createSession(int level) {    
    Vector<Entry> entryVector = new Vector<Entry>();
    //run through all entries associated with active lessons and append them to the vector if they meet the conditions
    for(Enumeration<Lesson> en = mLessons.elements(); en.hasMoreElements(); ) {
      Lesson l = (Lesson)en.nextElement();
      if( l.isActive() ) {
        for( int i=0; l.hasEntryId(i); i++) {
          String id = l.getEntryId(i);
          //the entry with the given id is in this active lesson, and therefore should be in the vector
          Entry e = (Entry)mAllEntries.get(id);
          if(e.getLevel() == level) //only if in the specified level
            entryVector.addElement(e);
        }
      }
    }
    return (Entry[]) entryVector.toArray( new Entry[entryVector.size()] );
  }
  
  public Entry[] createRandomSession(int level) 
  {
      //now we have all entries in the vector - randomize them and return the resulting array:
      return randomizeEntryVector( createSession( level ) );
  }
  
  /**
   * Returns only entries which have conjugations in there words for the specified language
   * @return
   */
  public Entry[] createRandomConjugationSession(String langId) {
    Vector entryVector = new Vector();
    //run through all entries associated with active lessons and append them to the vector if they meet the conditions
    for(Enumeration en = mLessons.elements(); en.hasMoreElements(); ) {
      Lesson l = (Lesson)en.nextElement();
      if( l.isActive() ) {
        for( int i=0; l.hasEntryId(i); i++) {
          String id = l.getEntryId(i);
          //the entry with the given id is in this active lesson, and therefore should be in the vector
          Entry e = (Entry)mAllEntries.get(id);
          Word w = e.getWord(langId);
          if(w.hasConjugations()) //only if the word has conjugations
            entryVector.addElement(e);
        }
      }
    }
    //now we have all entries in the vector - randomize them and return the resulting array:
    return randomizeEntryVector( (Entry[]) entryVector.toArray( new Entry[entryVector.size()] ));
  }
  

  private Entry[] randomizeEntryVector(Entry[] entryArray) {
    Entry[] sessionEntries = new Entry[entryArray.length]; //result array will take the random references
    Random rand = new Random(); //initialize random numbers
    Entry e;
    for( int i=entryArray.length-1; i >= 0; i-- ) { //run through all elements, starting from the last
      e = (Entry)entryArray[i];
      putEntryOnRandomArrayPos(e, sessionEntries, rand); //finds a free random place for the entry
      entryArray[i] = null;
    }
    return sessionEntries;
  }

  
  private boolean putEntryOnRandomArrayPos(Entry e, Entry[] sessionEntries, Random rand) {
    int pos = rand.nextInt(sessionEntries.length);
    boolean posFound = false;
    if( sessionEntries[pos] == null ) { //if this position is still free...
      sessionEntries[pos] = e; //put our entry there
      posFound = true;
    }
    else { //position is already occupied
      //simply look for the next free position in both directions
      //choose direction randomly:
      int shift;
      if( rand.nextInt(2) == 0 )
        shift = -1; //go left first
      else
        shift = +1; //go right first      
      for( int j = pos + shift; j >= 0 && j < sessionEntries.length; j += shift ) {
        if( sessionEntries[j] == null ) {
          sessionEntries[j] = e;
          posFound = true;
          break;
        }
      }
      if( !posFound) { //no free position in the first direction
        shift = shift * (-1); //...try the other direction
        for( int j = pos + shift; j >= 0 && j < sessionEntries.length; j += shift ) {
          if( sessionEntries[j] == null ) {
            sessionEntries[j] = e;
            posFound = true;
            break;
          }
        }
      }
    }
    return posFound;
  }

  public Entry getEntry(String entryId) {
    return (Entry)mAllEntries.get(entryId);
  }

  public void addTense(String tense) {
    mTenses.addElement(tense);
  }

  public int getTenseCount() {
    return mTenses.size();
  }

  public String getTense(int i) {
    return (String)mTenses.elementAt(i);
  }

  public String getPersonalPronounSingular(String translationId, int pronounId) {
   String[] pronounArray =  (String[])mPronounsSingular.get(translationId);
   if(pronounArray == null)
     return "";
   else {
     return pronounArray[pronounId];
   }        
  }

  public String getPersonalPronounPlural(String translationId, int pronounId) {
    String[] pronounArray =  (String[])mPronounsPlural.get(translationId);
    if(pronounArray == null)
      return "";
    else {
      return pronounArray[pronounId];
    }        
   }

  public void setPronounsSingular(String translationId, String[] pronounArray) {
    mPronounsSingular.put(translationId, pronounArray);
  }

  public void setPronounsPlural(String translationId, String[] pronounArray) {
    mPronounsPlural.put(translationId, pronounArray);
  }

}
