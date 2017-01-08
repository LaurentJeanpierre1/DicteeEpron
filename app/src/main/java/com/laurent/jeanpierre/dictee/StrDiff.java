package com.laurent.jeanpierre.dictee;


import android.support.annotation.NonNull;

import java.util.PriorityQueue;

class State implements  Comparable<State>{
  String op;
  int pos1, pos2;
  int score;
  int heuristique;

  @Override
  public String toString() {
    return String.format("%s,%d,%d:%d,%d",op,pos1,pos2,score,heuristique);
  }

  @Override
  public int compareTo(@NonNull State t1) {
    int s = score+heuristique;
    int os = t1.score+t1.heuristique;
    if (s > os) return -1;
    if (s < os) return +1;
    if (pos1+pos2 > t1.pos1+t1.pos2) return -1; // plus de cars consommés => meilleur
    if (pos1+pos2 < t1.pos1+t1.pos2) return +1; // plus de cars consommés => meilleur
    if (score > t1.score) return -1;
    if (score < t1.score) return +1;
    return 0;
  }

  public State() {
    op = "";
    pos1 = 0;
    pos2 = 0;
    score = 0;
  }

  public State(State other) {
    op = other.op;
    pos1 = other.pos1;
    pos2 = other.pos2;
    score = other.score;
  }

  public void next(char n, int len1, int len2) {
    op += n;
    switch (n) {
      case '+':
        --score;
        ++pos2;
        break;
      case '-':
        --score;
        ++pos1;
        break;
      case '#':
        --score;
        ++pos1;
        ++pos2;
        break;
      case '=': /*++score;*/
        ++pos1;
        ++pos2;
        break;
    } // switch
    int h1 = len1 - pos1;
    int h2 = len2 - pos2;
    heuristique = h1 - h2;
    if (heuristique > 0)
      heuristique = - heuristique; // s'il reste plus de caractères d'un côté que de l'autre, il faudra ajouter au moins un + ou un - pour ajuster...
  } // next(...)
} // class State

class StrDiff {

  String s1, s2;
  State best = null;

  public StrDiff(String str1, String str2) {
    s1 = str1;
    s2 = str2;
    int[][] partials = new int[s1.length()+1][s2.length()+1];
    PriorityQueue<State> status = new PriorityQueue<>();
    int bestScore = -100000;
    status.add(new State());
    while (!status.isEmpty()) {
      State s = status.poll();
      while ((s.pos1 < s1.length()) &&
          (s.pos2 < s2.length()) &&
          (s1.charAt(s.pos1) == s2.charAt(s.pos2)))
        s.next('=', s1.length(), s2.length());
      if (s.pos1 == s1.length()) {
        if (s.pos2 == s2.length()) {
          // deux chaines terminées, c'est donc le meilleur score (PriorityQueue)
          if (s.score >= bestScore) {
            //bestScore = s.score; //inutile, on break...
            best = s;
            break;
          }
        } else {
          // s1 fini, pas s2...
          while (s.pos2 < s2.length())
            s.next('+', s1.length(), s2.length());
          if (s.score > bestScore) {
            bestScore = s.score;
            best = s;
            status.add(s);
          }
        }
      } else if (s.pos2 == s2.length()) {
        // s2 fini, pas s1...
        while (s.pos1 < s1.length())
          s.next('-', s1.length(), s2.length());
        if (s.score > bestScore) {
          bestScore = s.score;
          best = s;
          status.add(s);
        }
      } else {
        // aucune des chaînes terminées => caractère !=
        // Trois possibles : Ajout d'un char; Supp d'un char, Rempl du char
        State sbis = new State(s);
        sbis.next('+', s1.length(), s2.length());
        State ster = new State(s);
        ster.next('-', s1.length(), s2.length());
        s.next('#', s1.length(), s2.length());
        ajouteOuRemplace(partials, status, s);
        ajouteOuRemplace(partials, status, sbis);
        ajouteOuRemplace(partials, status, ster);
      }
    } // while status != empty
  } // StrDiff

  private void ajouteOuRemplace(int[][] partials, PriorityQueue<State> status, State s) {
    if (partials[s.pos1][s.pos2] == 0) { // jamais arrivé là
      status.add(s);
      partials[s.pos1][s.pos2] = s.score;
    } else if (partials[s.pos1][s.pos2]<s.score) { // mieux qu'avant
      for (State ss : status)
        if ((ss.pos1 == s.pos1) && (ss.pos2 == s.pos2)) { // même point, donc moins bon
          System.out.println("meilleur");
          status.remove(ss);
        }
      status.add(s);
      partials[s.pos1][s.pos2] = s.score;
    } else {
      System.out.println("pire");
    }
  }

  public static void main(String[] args) {
    StrDiff d = new StrDiff("abcdef", "abCdef");
    System.out.println(d.best.op);
    d = new StrDiff("abcdef", "abdef");
    System.out.println(d.best.op);
    d = new StrDiff("abdef", "abcdef");
    System.out.println(d.best.op);

    d = new StrDiff("abcdef", "njkskd");
    System.out.println(d.best.op);

    d = new StrDiff("abcdef", "wsbfvldef");
    System.out.println(d.best.op);

  }

}
