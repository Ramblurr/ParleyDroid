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
public class Entry {
  private String mId;  
  private Hashtable mTranslations; //key: Translation.id; value: Word object
  private int mLevel; //counts how often this entry was known
  private VocSet mVocSet;

  public Entry(String entryId, VocSet vocSet) {
    mId = entryId;
    mTranslations = new Hashtable();
    mLevel = 0; //start in level 0 (never known)
    mVocSet = vocSet;
  }

  public Word addTranslation(String id) {
    Word w = new Word(id, mVocSet);
    mTranslations.put(id, w);
    return w;
  }

  public Word getWord(String translationId) {
    return (Word)mTranslations.get(translationId);
  }

  public void setLevel(int level) {
    mLevel = level;    
  }

  public int getLevel() {
    return mLevel;
  }
}
