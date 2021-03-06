package com.laurent.jeanpierre.dictee;


import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Set;
import java.util.TreeSet;

/** The Words database
 * Created by jeanpierre on 07/12/16.
 */
public class WordsDatabase extends SQLiteOpenHelper {
  // Database Name
  private static final String DATABASE_NAME = "words2018.sqlite";
  // words table name
  private static final String TABLE_WORDS = "Words";

  // Database Version
// 2017  private static final int DATABASE_VERSION = 36; // unvariable words a (Ib-Ij)
  private static final int DATABASE_VERSION = 52; // Dictée 7 CM1 v3
  /** Latest letter from database. */
  public static String last_letter = "'D7'";
  /** All letters from database. */
  public static String all_letters = "";
  /** All letters from database. */
  public static LinkedList<String> all_letters_array = new LinkedList<>();

  private Context contex;

  public WordsDatabase(Context context) {
    super(context, DATABASE_NAME, null, DATABASE_VERSION);
    words = new String[20];
    wordsPronounce = new String[20];

    this.contex = context;
    File dbPath = context.getDatabasePath(DATABASE_NAME);
    try { // Have to copy assets database to internal database :((
      if (! dbPath.exists()) { // TODO check for new version to avoid overwriting
        importDB(context, dbPath);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void importDB(Context context, File dbPath) throws IOException {
    if (context.getDatabasePath(DATABASE_NAME).exists()) {
      context.getDatabasePath(DATABASE_NAME).delete();
      File journal = new File(context.getDatabasePath(DATABASE_NAME).getAbsolutePath() + "-journal");
      if (journal.exists())
        journal.delete();
    }
    Toast.makeText(context,"Copying DB ...",Toast.LENGTH_SHORT).show();
    byte buf[] = new byte[1024];
    int len;
    InputStream in = context.getAssets().open(DATABASE_NAME);
    OutputStream out = new FileOutputStream(dbPath,false);
    while ((len = in.read(buf))>0) out.write(buf, 0, len);
    out.flush();
    out.close();
    Toast.makeText(context,"Copy DB ok",Toast.LENGTH_SHORT).show();

    Set<String> prefs = new TreeSet<>();
    String[] letters = last_letter.split(",");
    for (String letter : letters) {
      prefs.add(letter.substring(1,letter.length()-1)); // remove first & last "'"
    }
    PreferenceManager.getDefaultSharedPreferences(context).edit()
        .clear()
        .putStringSet(context.getString(R.string.soundsTitle),prefs)
        .apply();
    Toast.makeText(context,"Preferences applied",Toast.LENGTH_SHORT).show();
  }

  @Override
  public void onCreate(SQLiteDatabase sqLiteDatabase) {
    // Nothing to do ?
  }

  @Override
  public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
    try {
      importDB(contex, contex.getDatabasePath(DATABASE_NAME));
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public String[] words,wordsPronounce;

  public void getAllWords(String filter) {

    LinkedList<String> wordsList = new LinkedList<>();
    LinkedList<String> soundsList = new LinkedList<>();
// Select All Query
    String selectQuery = "SELECT name,pronunce FROM " + TABLE_WORDS;
    if ((filter != null) && !filter.isEmpty())
      selectQuery += " WHERE `set` IN "+filter;
    //Log.i("DB-query",selectQuery);
    SQLiteDatabase db = this.getReadableDatabase();
    Cursor cursor;
    try {
      cursor = db.rawQuery(selectQuery, null);
    } catch(Exception e)
    {
      db.close();
      try {
        importDB(contex,contex.getDatabasePath(DATABASE_NAME));
        getAllWords(filter);
        return;
      } catch (IOException e1) {
        e1.printStackTrace();
        System.exit(1);
      }
      cursor = null;
    }
// looping through all rows and adding to list
    if (cursor.moveToFirst()) {
      do {
        wordsList.add(cursor.getString(0).trim()); // remove extra spaces just-in-case
        String p = cursor.getString(1);
        if ((p == null) || (p.isEmpty()))
          soundsList.add(wordsList.getLast());
        else
          soundsList.add(p);
      } while (cursor.moveToNext());
    }
    cursor.close();
    db.close();
    words = new String[wordsList.size()];
    wordsPronounce = new String[wordsList.size()];
    int i=0;
    for (String w : wordsList) words[i++] = w;
    i=0;
    for (String w : soundsList) wordsPronounce[i++] = w;
    if (wordsList.isEmpty())
      getAllWords(null);
  } // getAllWords()

  /**
   * Fetches all the letters from the database
   */
  public static void computeLetters(Context c) {
    try {
      WordsDatabase base = new WordsDatabase(c);
      SQLiteDatabase db = base.getReadableDatabase();
      //Cursor cursor = db.rawQuery("select `set`, name from Words", null);
      Cursor cursor = db.rawQuery("select distinct `set` from Words", null);
      StringBuilder sb = new StringBuilder();
      if (cursor.moveToFirst()) {
        do {
          String letter = cursor.getString(0);
          if (letter != null)
            letter = letter.trim(); // remove extra spaces just-in-case
//          Log.e("Dictée","word: "+cursor.getString(1) + "  -  "+cursor.getString(0));
          sb.append('\'');
          sb.append(letter);
          sb.append("',");
          all_letters_array.add(letter);
        } while (cursor.moveToNext());
      }
      cursor.close();
      sb.deleteCharAt(sb.length()-1); // remove the last ','
      all_letters = sb.toString();
      if (last_letter == null || last_letter.isEmpty())
        last_letter = all_letters_array.getLast();
      db.close();
      Set<String> prefs = PreferenceManager.getDefaultSharedPreferences(c).getStringSet(c.getString(R.string.soundsTitle),null);
      if (prefs != null) {
        LinkedList<String> toRemove = new LinkedList<>();
        for (String entry : prefs)
          if (!all_letters_array.contains(entry)) {
            toRemove.add(entry);
            Log.d("Removed preference:", entry);
          }
        prefs.removeAll(toRemove);
        PreferenceManager.getDefaultSharedPreferences(c).edit().
            putStringSet(c.getString(R.string.soundsTitle), prefs)
            .apply();
      }
    } catch (Exception e) {
      Log.e("Dictée","Unable to compute letters from database",e);
    }
  }
} // class WordsDatabase
