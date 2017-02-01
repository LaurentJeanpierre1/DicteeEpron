package com.laurent.jeanpierre.dictee;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class ResultActivity extends AppCompatActivity {
String[] words;
  int[] tries;
  int[] fails;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_result);

    words = getIntent().getStringArrayExtra("WORDS");
    tries = getIntent().getIntArrayExtra("TRIALS");
    fails = getIntent().getIntArrayExtra("FAILURES");

    MyAdapter adapter = new MyAdapter();
    RecyclerView list = (RecyclerView) findViewById(R.id.liste);
    list.setHasFixedSize(true);
    list.setLayoutManager(new LinearLayoutManager(this));
    list.setAdapter(adapter);
    adapter.notifyDataSetChanged();
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
    }

    @Override
    public int getItemCount() {
      return words.length;
    }
  }
  private class ViewHolder extends RecyclerView.ViewHolder {
    TextView word;
    TextView trials;
    TextView failures;
    public ViewHolder(View itemView) {
      super(itemView);
      word = (TextView) itemView.findViewById(R.id.word);
      trials = (TextView) itemView.findViewById(R.id.nb_trials);
      failures = (TextView) itemView.findViewById(R.id.nb_fail);
    }
    void  setItem(int position) {
      word.setText(words[position]);
      trials.setText(String.format("%d",tries[position]));
      failures.setText(String.format("%d",fails[position]));
    }
  }
}
