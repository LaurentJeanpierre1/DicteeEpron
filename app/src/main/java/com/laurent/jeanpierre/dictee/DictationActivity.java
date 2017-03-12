package com.laurent.jeanpierre.dictee;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabaseCorruptException;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StrikethroughSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.MessageFormat;
import java.util.Random;
import java.util.Set;

public class DictationActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

  static String[] words;
  static String[] wordsPronounce;

  private int[] wordCount;
  private int nbWords;
  private int score;

  int[] trials;
  int[] failures;

  private String solution;
  private int solutionIdx;
  private TextToSpeech tts;
  boolean isReset;

  //For open keyboard
  public void OpenKeyBoard(Context mContext){
    InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
    imm.toggleSoftInput(InputMethodManager.SHOW_FORCED,0);
  }
  //For close keyboard
  public void CloseKeyBoard(Context mContext){
    InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
    imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY,0);
  }
  @Override
  protected void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_dictation);
    Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);
    setVolumeControlStream(AudioManager.STREAM_MUSIC);

    if (savedInstanceState == null) {
      try {
        new WordsDatabase(this).getReadableDatabase().close(); // in case of update
      } catch (SQLiteDatabaseCorruptException ex) {
        Log.d("Dictée", "Database updated?", ex);
      }
      WordsDatabase.computeLetters(this);
    }
    resetWords("(\"" + WordsDatabase.last_letter + "\")");
    FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
    fab.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
            .setAction("Action", null).show();
      }
    });
    fab.setVisibility(ImageView.INVISIBLE);
    tts = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
    @Override
    public void onInit(int i) {
        tts.setSpeechRate(0.75f);
        if (savedInstanceState == null)
          selectWord();
      }
    });

    if (savedInstanceState != null) {
      score = savedInstanceState.getInt("score", 0);
      ((TextView) findViewById(R.id.score)).setText(getString(R.string.textScore, Integer.toString(score)));
      nbWords = savedInstanceState.getInt("length", words.length);
      wordCount = savedInstanceState.getIntArray("wordCount");
      if (wordCount == null)
        wordCount = new int[nbWords];
      trials = savedInstanceState.getIntArray("trials");
      if (trials == null)
        trials = new int[nbWords];
      failures = savedInstanceState.getIntArray("failures");
      if (failures == null)
        failures = new int[nbWords];
      solutionIdx = savedInstanceState.getInt("idx",-1);
      if (solutionIdx != -1) {
        solution = words[solutionIdx];
        ((TextView) findViewById(R.id.solution)).setText(String.format("%d", wordCount[solutionIdx])); //solution
        onRepeatPressed(null);
        isReset = false;
      } else
        selectWord();
    }
  } // onCreate(savedInstanceState)

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    outState.putInt("score",score);
    outState.putInt("length",nbWords);
    outState.putIntArray("wordCount",wordCount);
    outState.putIntArray("trials",trials);
    outState.putIntArray("failures",failures);
    outState.putInt("idx",solutionIdx);
  }

  private void resetWords(String filter) {
    if (filter != null) {
      WordsDatabase db = new WordsDatabase(this);
      db.getAllWords(filter);
      words = db.words;
      wordsPronounce = db.wordsPronounce;
    }

    score = 0;
    nbWords = words.length;
    wordCount = new int[nbWords];
    trials = new int[nbWords];
    failures = new int[nbWords];
    for (int i=0; i<nbWords; ++i) {
      wordCount[i] = 3;
      trials[i] = 0;
      failures[i] = 0;
    }
    nbWords = 3*words.length;
    isReset = true;
  }

  @Override
  protected void onDestroy() {
    tts.shutdown();
    super.onDestroy();
  }

  private void selectWord() {
    ((TextView) findViewById(R.id.score)).setText(getString(R.string.textScore, Integer.toString(score)));
    String newOne = null;
    //noinspection StringEquality // solution is drawn from this same array
    do {
      int tot = new Random().nextInt(nbWords);
      for (int i=0; i<words.length; ++i) {
        tot -= wordCount[i];
        if (tot <= 0) {
          newOne = words[i];
          solutionIdx = i;
          break;
        }
      } // for i
    } while ((newOne == null) || (newOne == solution));
    solution = newOne;
    ((TextView) findViewById(R.id.solution)).setText(String.format("%d", wordCount[solutionIdx])); //solution
    String fmt = getString(R.string.spokenInviteWord);
    String sentence = MessageFormat.format(getString(R.string.spokenInviteWord), wordsPronounce[solutionIdx]);
    if (Build.VERSION.SDK_INT<21)
      tts.speak(sentence, TextToSpeech.QUEUE_FLUSH, null);
    else
      tts.speak(sentence, TextToSpeech.QUEUE_FLUSH, null, solution); // does not exist before API 21
    findViewById(R.id.validate).setEnabled(true);
    OpenKeyBoard(getApplicationContext());
    findViewById(R.id.fab).setVisibility(ImageView.INVISIBLE);
    findViewById(R.id.editAnswer).setEnabled(true);
    isReset = false;
  } // selectWord()

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.menu_dictation, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.
    int id = item.getItemId();

    //noinspection SimplifiableIfStatement
    if (id == R.id.action_settings) {
      Intent intent = new Intent();
      intent.setClassName(this, "com.laurent.jeanpierre.dictee.SettingsActivity");
      startActivity(intent);
      return true;
    } else if (id == R.id.action_results) {
      new AlertDialog.Builder(this).setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
        }
      }).setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
          showResults();
          resetWords(null);
        }
      }).setMessage(R.string.prompt_restart).show();
      return true;
    }

    return super.onOptionsItemSelected(item);
  }

  public void onValidatePressed(View v) {
    int msg, dur;
    CloseKeyBoard(getApplicationContext());
    final EditText answer = (EditText) findViewById(R.id.editAnswer);
    String answerText = answer.getText().toString().trim().replaceAll(" {2,}"," ");
    ++trials[solutionIdx];
    if (answerText.equalsIgnoreCase(solution)) {
      msg = R.string.bravo;
      dur = Snackbar.LENGTH_SHORT;
      if (wordCount[solutionIdx] > 1) {
        --wordCount[solutionIdx];
        --nbWords;
      }
      ++score;
      SpannableString text = new SpannableString(answerText);
      text.setSpan(new ForegroundColorSpan(Color.GREEN),0, answerText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
      answer.setText(text, TextView.BufferType.SPANNABLE);
    } else {
      MediaPlayer.create(this, R.raw.buzz2).start();
      msg = R.string.failure;
      dur = Snackbar.LENGTH_INDEFINITE;
      ++wordCount[solutionIdx];
      ++nbWords;
      --score;
      ++failures[solutionIdx];
      findViewById(R.id.fab).setVisibility(ImageView.VISIBLE);
      SpannableStringBuilder text = new SpannableStringBuilder();
      StrDiff dif;
      try {
        dif = new StrDiff(solution, answerText.trim());
      } catch (Throwable t) {
        System.gc();
        dif = new StrDiff(solution, ""); // matching simple
      }
      for (int i=0, p1 = 0, p2=0; i<dif.best.op.length(); ++i) {
        switch(dif.best.op.charAt(i)) {
          case '=' : text.append(answerText.charAt(p2)); text.setSpan(new ForegroundColorSpan(Color.GREEN),i,i+1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);++p1; ++p2; break;
          case '+' : text.append(answerText.charAt(p2)); text.setSpan(new StrikethroughSpan(),i,i+1,Spanned.SPAN_EXCLUSIVE_EXCLUSIVE); ++p2; break;
          case '-' : text.append(solution.charAt(p1)); text.setSpan(new ForegroundColorSpan(Color.RED),i,i+1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);++p1; break;
          case '#' : text.append(answerText.charAt(p2)); text.setSpan(new BackgroundColorSpan(Color.RED),i,i+1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);++p1; ++p2; break;
        }
      }
      answer.setText(text, TextView.BufferType.SPANNABLE);
    }
    findViewById(R.id.validate).setEnabled(false);
    answer.setEnabled(false);
    ((TextView) findViewById(R.id.score)).setText(getString(R.string.textScore, Integer.toString(score)));
    String msg2 = getString(msg, solution);
    Snackbar.make(v, msg2, dur).setAction("Action", null).addCallback(new Snackbar.Callback() {
      @Override
      public void onDismissed(Snackbar snackbar, int event) {
        super.onDismissed(snackbar, event);
        answer.setText("", TextView.BufferType.EDITABLE);
        findViewById(R.id.editAnswer).requestFocus();
        selectWord();
      }
    }).show();
  }

  public void onRepeatPressed(View view) {
    String sentence = MessageFormat.format(getString(R.string.spokenInviteWord), wordsPronounce[solutionIdx]);
    if (Build.VERSION.SDK_INT<21)
      tts.speak(sentence, TextToSpeech.QUEUE_FLUSH, null);
    else
      tts.speak(sentence, TextToSpeech.QUEUE_FLUSH, null, solution);
  }


  @Override
  protected void onResume() {
    super.onResume();
    // Set up a listener whenever a key changes
    PreferenceManager.getDefaultSharedPreferences(this)
        .registerOnSharedPreferenceChangeListener(this);
    if (isReset) {
      selectWord();
    }
  }

  @Override
  protected void onPause() {
    super.onPause();
    isReset = false;
    // Unregister the listener whenever a key changes
//    PreferenceManager.getDefaultSharedPreferences(this)
//        .unregisterOnSharedPreferenceChangeListener(this);
  }

  @Override
  public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {

    Set<String> ss = sharedPreferences.getStringSet(s,null);
    StringBuilder sb = new StringBuilder("(");
    if (ss != null)
      for(String v : ss) {
        if (sb.length() != 1)
          sb.append(", ");
        sb.append('"');
        sb.append(v);
        sb.append('"');
      }
    sb.append(')');
    resetWords(sb.toString());
  }

  public void showResults() {
    Intent intent = new Intent(this, ResultActivity.class);
    intent.putExtra("WORDS", words);
    intent.putExtra("TRIALS", trials);
    intent.putExtra("FAILURES", failures);
    intent.putExtra("PRONOUNCE", wordsPronounce);
    isReset = true;
    startActivityForResult(intent, 213);
  }

  @Override
  public void onBackPressed() {
    super.onBackPressed();
    showResults();
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (requestCode == 213) {
      selectWord();
      ((TextView) findViewById(R.id.editAnswer)).setText("");
    }
    super.onActivityResult(requestCode, resultCode, data);
  }
} // class DictationActivity
