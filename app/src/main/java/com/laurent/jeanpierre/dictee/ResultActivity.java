package com.laurent.jeanpierre.dictee;

import android.graphics.Color;
import android.os.Build;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class ResultActivity extends AppCompatActivity {
  String[] words;
  String[] pronunce;
  int[] tries;
  int[] fails;
  private TextToSpeech tts;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_result);

    words = getIntent().getStringArrayExtra("WORDS");
    tries = getIntent().getIntArrayExtra("TRIALS");
    fails = getIntent().getIntArrayExtra("FAILURES");
    pronunce = getIntent().getStringArrayExtra("PRONOUNCE");

    MyAdapter adapter = new MyAdapter();
    RecyclerView list = (RecyclerView) findViewById(R.id.liste);
    list.setHasFixedSize(true);
    list.setLayoutManager(new LinearLayoutManager(this));
    list.setAdapter(adapter);
    adapter.notifyDataSetChanged();

    tts = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
      @Override
      public void onInit(int i) {
        tts.setSpeechRate(0.75f);
      }
    });
  }

  private class MyAdapter extends RecyclerView.Adapter {

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
      View v = getLayoutInflater().inflate(R.layout.word_row, parent, false);
      return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
      ((ViewHolder)holder).setItem(position);
      if (position % 2 == 0)
        holder.itemView.setBackgroundColor(Color.TRANSPARENT);
      else
        holder.itemView.setBackgroundColor(Color.CYAN);
    }

    @Override
    public int getItemCount() {
      return words.length;
    }
  }
  private class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    int idx;
    TextView word;
    TextView trials;
    TextView failures;
    public ViewHolder(View itemView) {
      super(itemView);
      word = (TextView) itemView.findViewById(R.id.word);
      trials = (TextView) itemView.findViewById(R.id.nb_trials);
      failures = (TextView) itemView.findViewById(R.id.nb_fail);
      itemView.setOnClickListener(this);
    }
    void  setItem(int position) {
      word.setText(words[position]);
      trials.setText(String.format("%d",tries[position]));
      failures.setText(String.format("%d",fails[position]));
      idx = position;
    }

    @Override
    public void onClick(View v) {
      if (Build.VERSION.SDK_INT<21)
        tts.speak(pronunce[idx],TextToSpeech.QUEUE_FLUSH,null);
      else
        tts.speak(pronunce[idx],TextToSpeech.QUEUE_FLUSH,null,"res");// does not exist before API 21
    }
  } // class ViewHolder
} // class ResultActivity
