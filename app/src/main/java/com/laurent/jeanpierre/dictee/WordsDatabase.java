package com.laurent.jeanpierre.dictee;


import android.app.Application;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedList;
/**
 * Created by jeanpierre on 07/12/16.
 */
public class WordsDatabase extends SQLiteOpenHelper {
  // Database Version
  private static final int DATABASE_VERSION = 6; // Lettre F
  // Database Name
  private static final String DATABASE_NAME = "words.sqlite";
  // words table name
  private static final String TABLE_WORDS = "Words";

  private Context contex;

  public WordsDatabase(Context context) {
    super(context, DATABASE_NAME, null, DATABASE_VERSION);
    words = new String[10];
    wordsPronounce = new String[10];

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
    Toast.makeText(context,"Copying DB ...",Toast.LENGTH_LONG).show();
    byte buf[] = new byte[1024];
    int len;
    InputStream in = context.getAssets().open(DATABASE_NAME);
    OutputStream out = new FileOutputStream(dbPath,false);
    while ((len = in.read(buf))>0) out.write(buf, 0, len);
    out.flush();
    out.close();
    Toast.makeText(context,"Copy DB ok",Toast.LENGTH_LONG).show();
  }

  @Override
  public void onCreate(SQLiteDatabase sqLiteDatabase) {
    // Nothing to do ?
  }

  @Override
  public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
    // Nothing to do ?
    try {
      importDB(contex, contex.getDatabasePath(DATABASE_NAME));
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public String[] words,wordsPronounce;

  public void getAllWords(String filter) {

    LinkedList<String> wordsList = new LinkedList<String>();
    LinkedList<String> soundsList = new LinkedList<String>();
// Select All Query
    String selectQuery = "SELECT name,pronunce FROM " + TABLE_WORDS;
    if (filter != null)
      selectQuery += " WHERE `set` IN "+filter;
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
        wordsList.add(cursor.getString(0));
        String p = cursor.getString(1);
        if (p == null)
          soundsList.add(wordsList.getLast());
        else
          soundsList.add(p);
      } while (cursor.moveToNext());
    }
    words = new String[wordsList.size()];
    wordsPronounce = new String[wordsList.size()];
    int i=0;
    for (String w : wordsList) words[i++] = w;
    i=0;
    for (String w : soundsList) wordsPronounce[i++] = w;
  } // getAllWords()
} // class WordsDatabase
