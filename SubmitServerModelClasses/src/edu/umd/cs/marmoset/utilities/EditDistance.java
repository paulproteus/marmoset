package edu.umd.cs.marmoset.utilities;

import java.util.BitSet;
import java.util.List;
import java.util.PriorityQueue;

public class EditDistance<T> {

  private final DistanceMetric<T> metric;

  /**
   * @param metric
   */
  public EditDistance(DistanceMetric<T> metric) {
    super();
    this.metric = metric;
  }

  public interface DistanceMetric<T> {
    int costToInsert(T newValue);
    int minCostToInsert();
    int costToChange(T oldValue, T newValue);
    int costToRemove(T oldValue);
    int minCostToRemove();
  }

  public enum Change {
    REMOVE, CHANGE, KEEP, INSERT
  }

  public static class Solution implements Comparable<Solution>{
    @Override
    public String toString() {
      return "[cost=" + cost + ", edit=" + edit + "]";
    }



    final ImmutableList<Change> edit;
    final int cost;
    final int costBound;
    final int firstPos, secondPos;

    public Solution(int deltaCost, Change change, Solution prev, DistanceMetric<?> metric) {
      this.cost = deltaCost + prev.cost;

      this.edit = ImmutableList.cons(change, prev.edit);
      int f,s;
      switch (change) {
      case KEEP:
      case CHANGE:
          f = prev.firstPos-1;
          s = prev.secondPos-1;
          break;
      case REMOVE:
          f = prev.firstPos-1;
                s = prev.secondPos;
                break;
        case INSERT:
                f = prev.firstPos;
                s = prev.secondPos-1;
                break;
                default:
                    throw new IllegalArgumentException();

      }
      this.firstPos = f;
      this.secondPos = s;
      int additionalCost = 0;

      if (firstPos > secondPos)
          additionalCost = (firstPos - secondPos) * metric.minCostToRemove();
      else if (firstPos < secondPos)
          additionalCost = (secondPos - firstPos) * metric.minCostToInsert();
      this.costBound = cost + additionalCost;

    }

    public Solution(int cost, int firstPos, int secondPos, DistanceMetric<?> metric) {
      this.cost = cost;
      this.edit = ImmutableList.empty();
      this.firstPos = firstPos;
      this.secondPos = secondPos;
      int additionalCost = 0;

            if (firstPos > secondPos)
                additionalCost = (firstPos - secondPos) * metric.minCostToRemove();
            else if (firstPos < secondPos)
                additionalCost = (secondPos - firstPos) * metric.minCostToInsert();
            this.costBound = cost + additionalCost;
    }


        @Override
        public int compareTo(Solution that) {
            int result = this.costBound - that.costBound;
            if (result != 0)
                return result;
            result = this.firstPos + this.secondPos - that.firstPos - that.secondPos;
            return result;

        }
  }

  static final boolean DEBUG = false;

  class Computation {
      PriorityQueue<Solution> queue = new PriorityQueue<Solution>();

    final int[][] explored;
    final List<T> first;
    final List<T> second;
    int positionsExplored;

    Computation(List<T> first, List<T> second) {
        this.first = first;
        this.second = second;
        Solution start = new Solution(0, first.size(), second.size(), metric);
        explored = new int[first.size()+1][ second.size()+1];
          add(start);
    }

    private void add(Solution s) {
        queue.add(s);
    }
    public Solution compute() {
        int iteration = 1;
        while (true) {
            Solution sol = queue.remove();
            if (explored[sol.firstPos][sol.secondPos] > 0)
                  continue;
              explored[sol.firstPos][sol.secondPos] = iteration++;
            positionsExplored++;
            if (sol.firstPos == 0 && sol.secondPos == 0)
                return sol;

            if (sol.firstPos > 0 && sol.secondPos > 0) {
                T f = first.get(sol.firstPos-1);
                T s = second.get(sol.secondPos-1);
                if (f.equals(s)) {
                    // exact match
                    add(new Solution(0, Change.KEEP, sol, metric));
                    continue;
                }
                int costToChange = metric.costToChange(f,  s);
                add(new Solution(costToChange, costToChange <= 1 ? Change.KEEP : Change.CHANGE, sol, metric));
            }
            if (sol.firstPos > 0) {
                    T f = first.get(sol.firstPos-1);
                    int costToChange = metric.costToRemove(f);
                    add(new Solution(costToChange, Change.REMOVE, sol, metric));
                }
            if (sol.secondPos > 0) {
                    T s = second.get(sol.secondPos-1);
                    int costToChange = metric.costToInsert(s);
                    add(new Solution(costToChange, Change.INSERT, sol, metric));
                }
        }
  }
  }

  public Solution compute(List<T> first, List<T> second) {
    Computation cache = new Computation(first, second);
    Solution result = cache.compute();

    if (DEBUG) {
        int computed = 0;
        int skipped = 0;

      for (int i = first.size(); i >= 0; i--) {
        for (int j = second.size(); j >= 0; j--)
          if (cache.explored[i][j] > 0) {
            System.out.printf("%3d ", cache.explored[i][j]);

            computed++;
          } else {
            System.out.print("  . ");
            skipped++;
          }

        System.out.println();

      }
      System.out.printf("Computed %d, skipped %d%n", computed, skipped);
    }

    return result;
  }

  /** Given a BitSet of changed lines, which ones should be shown?
   * All changed lines will be shown. Long stretches of unchanged lines will be elided.
   *
   * @param changed the BitSet of all changed lines.
   * @param sz
   * @return
   */
  public static BitSet showLines(BitSet changed, int sz) {
	  return showLines(changed, sz, 20, 8);
  }
  public static BitSet showLines(BitSet changed, int sz, int context) {
	  return showLines(changed, sz, context * 3, context);
  }
  public static BitSet showLines(BitSet changed, int sz, int lengthToElide, int context) {
    if (changed == null) {
      BitSet shown = new BitSet();
      for (int i = 0; i < sz; i++) {
        shown.set(i);
      }
      return shown;
    }
    BitSet results = (BitSet) changed.clone();
    int lastTrue = -1;
    int nextTrue = changed.nextSetBit(0);
    if (nextTrue == -1) {
      // No modified lines.
      return changed;
    }
    while (true) {
      if (nextTrue - lastTrue > lengthToElide) {
        if (lastTrue >= 0) {
          // Show at most CONTEXT lines after the last modified line.
          results.set(lastTrue + 1, Math.min(lastTrue + context, nextTrue - 1) + 1);
        }
        if (nextTrue < sz) {
          // Show at most CONTEXT lines before the next modified line.
          results.set(Math.max(lastTrue + 1, nextTrue - context), nextTrue);
        }
      } else if (lastTrue + 1 <= nextTrue - 1) {
        results.set(lastTrue + 1, nextTrue);
      }
      if (nextTrue == sz) {
        // All the lines have been checked.
        break;
      }
      lastTrue = changed.nextClearBit(nextTrue + 1);
      if (lastTrue < 0) {
        // All the remaining lines are modified.
        break;
      }
      lastTrue--;
      nextTrue = changed.nextSetBit(lastTrue + 1);
      if (nextTrue < 0)
        nextTrue = sz;
    }
    return results;
  }

  public BitSet whichAreNew(List<T> first, List<T> second) {
    Solution solution = compute(first, second);
    BitSet result = new BitSet(second.size());
    int pos = 0;
    for(Change  c : solution.edit)
      switch (c) {
      case KEEP:
        pos++;
        break;
      case REMOVE:
        break;
      case INSERT:
      case CHANGE:
        result.set(pos++);
      }

    assert pos == second.size();
    return result;
  }

  public void printDiff(List<T> first, List<T> second) {
    Solution solution = compute(first, second);
    int pos1 = 0;
    int pos2 = 0;
      for(Change  c : solution.edit)
        switch (c) {
        case KEEP:
          System.out.printf("%8s %4d %4d %s%n", c, pos1, pos2, second.get(pos2));
          pos2++;
          break;
        case REMOVE:
          System.out.printf("%8s %4d %4d %s%n", c, pos1, pos2, first.get(pos1));
          pos1++;
          break;
        case INSERT:
        case CHANGE:
          System.out.printf("%8s %4d %4d %s%n", c, pos1, pos2, second.get(pos2));
          pos1++;
          pos2++;

        }

  }

    public static final EditDistance<String> SOURCE_CODE_DIFF
       = new EditDistance<String>(new EditDistance.DistanceMetric<String>(){

          @Override
          public int costToInsert(String newValue) {
              newValue = newValue.trim();
              if (newValue.length() == 0)
                  return 2;
              return 5;

          }

          @Override
          public int costToChange(String oldValue, String newValue) {
              if (oldValue.equals(newValue))
                return 0;
              oldValue = oldValue.trim();
              newValue = newValue.trim();
              if (oldValue.equals(newValue))
                  return 1;
              return 5;
          }

          @Override
          public int costToRemove(String oldValue) {
              oldValue = oldValue.trim();
              if (oldValue.length() == 0)
                  return 2;
              return 3;
          }

            @Override
            public int minCostToInsert() {
               return 2;
            }

            @Override
            public int minCostToRemove() {
                return 2;
            }});


}
