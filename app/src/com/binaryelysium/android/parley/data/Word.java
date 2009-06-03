/**
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

import java.util.Hashtable;

/**
 * @author michhof
 *
 */
public class Word {
  private String mText;  
  private String mPronunciation;
  private String mType;
  private String mSpecialWordType;

  //constants for conjugation arrays
  public static final int NUMBER_OF_PRONOUNS = 5; //size of conjugation arrays
  public static final int CONJ_FIRSTPERSON = 0; //index for firstperson conjugations...
  public static final int CONJ_SECONDPERSON = 1;
  public static final int CONJ_THIRDPERSON_MALE = 2;
  public static final int CONJ_THIRDPERSON_FEMALE = 3;
  public static final int CONJ_THIRDPERSON_NEUTRAL = 4;
  //conjugation hashes
  //each hash takes conjugation arrays (string arrays) as values, and tense names (Strings) as keys:
  private Hashtable mConjugationsSingular;
  private Hashtable mConjugationsPlural;
  private Hashtable mConjCountSingular; //key: String, tense name; value: Integer object, the count of conjugations within the array that are actually set (not null or empty)
  private Hashtable mConjCountPlural;
  private int mTotalConjugationCount = 0; //total count of all (non empty) conjugations associated with this word
  private String mTranslationId;
  private VocSet mVocSet;

  public Word(String translationId, VocSet vocSet) {
    mConjugationsSingular = new Hashtable();
    mConjugationsPlural = new Hashtable();
    mConjCountSingular = new Hashtable();
    mConjCountPlural = new Hashtable();
    mTranslationId = translationId;
    mVocSet = vocSet;
  }
  
  public void setText(String text) {
    mText = text;
  }

  public String getText() {
    return mText;
  }

  public void setPronunciation(String pron) {
    mPronunciation = pron;    
  }

  public String getPronunciation() {
    return mPronunciation;
  }
  
  public void setType(String type) {
    mType = type;    
  }

  public String getType() {
    return mType;
  }

  public void setSpecialWordType(String specialWordType) {
    mSpecialWordType = specialWordType;    
  }
  
  public void setConjugations(String tense, String[] conjugationsSingular, String[] conjugationsPlural) {
    mConjugationsSingular.put(tense, conjugationsSingular);
    mConjugationsPlural.put(tense, conjugationsPlural);
    //update the conjugation count for singular
    Integer oldCountSingular = (Integer)mConjCountSingular.get(tense);
    if(oldCountSingular == null)
      oldCountSingular = new Integer(0);
    int newCountSingular = 0;    
    for(int i = 0; i < conjugationsSingular.length; i++) { //look at all array positions
      if( conjugationsSingular[i] != null && conjugationsSingular[i].length() > 0 ) //if they are empty
        newCountSingular++;
    }
    mConjCountSingular.put(tense, new Integer(newCountSingular)); //update for this tense
    mTotalConjugationCount += (newCountSingular - oldCountSingular.intValue()); //update total count
    
    //update the conjugation count for plural    
    Integer oldCountPlural = (Integer)mConjCountPlural.get(tense);
    if(oldCountPlural == null)
      oldCountPlural = new Integer(0);
    int newCountPlural = 0;    
    for(int i = 0; i < conjugationsPlural.length; i++) { //look at all array positions
      if( conjugationsPlural[i] != null && conjugationsPlural[i].length() > 0 ) //if they are empty
        newCountPlural++;
    }
    mConjCountPlural.put(tense, new Integer(newCountPlural)); //update for this tense
    mTotalConjugationCount += (newCountPlural - oldCountPlural.intValue()); //update total count
  }


  public String getConjSingularAsString(String tense) {
    return getConjAsString(tense, "singular", mConjCountSingular, mConjugationsSingular); //FIXME: constants
  }

  public String getConjPluralAsString(String tense) {
    return getConjAsString(tense, "plural", mConjCountPlural, mConjugationsPlural);
  }
  
  private String getConjAsString(String tense, String singPlur, Hashtable conjCount, Hashtable conjugations) {
    Integer count = (Integer)conjCount.get(tense);
    if(count == null || count.intValue() == 0) //no conjugations for this tense...
      return ""; //...return an empty string
    else {
      //so we have at least one conjugation for this tense
      String conjString = "";
      String[] conjArray = (String[])conjugations.get(tense);
      String currConj;
      for( int i = 0; i < conjArray.length; i++ ) {
        currConj = conjArray[i];
        if(currConj != null && currConj.length() > 0) { //this conjugation is non-empty
          currConj = getPersonalPronoun(singPlur, i) + " " + currConj; //prefix the conjugation with the pronoun
          if(conjString.length() > 0) //if we added already a conjugation to the string
            conjString += ", "; //add a separator before the next
          conjString += currConj; //add the conjugation
        }
      }      
      return (tense + ": " + conjString + "\n"); //the string should start with the name of the tense
                                                 //followed by a colon and end with a newline
    }    
  }

  private String getPersonalPronoun(String singPlur, int i) {
    if(singPlur.equals("singular"))
      return mVocSet.getPersonalPronounSingular(mTranslationId, i);
    else
      return mVocSet.getPersonalPronounPlural(mTranslationId, i);
  }
  
  public boolean hasConjugations() {
    return mTotalConjugationCount > 0 ? true : false;
  }

}
