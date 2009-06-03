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

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

/**
 * @author michhof
 *
 */
public class Lesson {
  private String mName;  
  private Vector mIds; //ids of the entries associated with this lesson (as Strings)
  private boolean mActive;
  
  public Lesson(String name, Vector ids) {
    mName = name;
    mIds = ids;
    mActive = false; //by default a lesson is deactivated
  }

  public String getName() {
    return mName;
  }

  public boolean isActive() {
    return mActive;
  }
  
  public void setActive(boolean active) {
    mActive = active;    
  }

  public int getEntryCount() {
    return mIds.size();
  }

  public boolean hasEntryId(int i) {
    if( i >= 0 && i < mIds.size() )
      return true;
    else
      return false;        
  }

  public String getEntryId(int i) {
    return (String)mIds.elementAt(i);
  }

  /**
   * Returns the count of all associated Entries that are in the specified level
   * @param level
   * @param entries Hashtable containing the Entry objects for this lesson (since they are not stored in the lesson)
   * @return
   */
  public int getEntryCountForLevel(int level, Hashtable entries) {
    //FIXME: entries should probably already be stored in the lesson (connectToEntries method)
    int count = 0;
    for(Enumeration e = mIds.elements(); e.hasMoreElements(); ) { //run through all entries
      String id = (String)e.nextElement();
      Entry ent = (Entry)entries.get(id);
      if(ent != null && ent.getLevel() == level) //count only the ones in the specified level
        count++;
    }
    return count;
  }

}
