import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ConvexHull {
    /**
     * Declaring all the global variables
     */
    int n, s;
    int[] x, y;
    NPunkter17 p;
    int MAX_X, MAX_Y;
  
    /**
     * Constructor that initializes the global variables
     * @param n 
     * @param k Number of threads used for the parallel version
     */
    ConvexHull(int n, int s) {
      this.n = n;
      this.s = s;
      //Its not the max x coordinate value, its the point with max x value
      x = new int[n];
      y = new int[n];
      p = new NPunkter17(n, s);
      p.fyllArrayer(x, y);
      
      MAX_X = 0; MAX_Y = 0;
      int max_x_cord = x[0]; int max_y_cord = y[0];
      for (int i = 1; i < x.length; i++) {
        if (x[i] > max_x_cord) {
            max_x_cord = x[i];
            MAX_X = i;
        }
        if (y[i] > max_y_cord) {
            max_y_cord = y[i];
            MAX_Y = i;
        }
      }

    }
    /**
     * Finds min_x and max_x and starts recursion
     * @return the hull points list
     */
    IntList seqMethod() {
        IntList koList = new IntList();
        
        //Finding min_x and max_x and the points p1 and p2 at those locations
        int p1 = 0; int p2 = 0;
        int max_x = x[0]; int min_x = x[0];
        for (int i = 1; i < x.length; i++) {
            if (x[i] >= max_x) {max_x = x[i]; p1 = i;}
            else if (x[i] <= min_x) {min_x = x[i]; p2 = i;}
        }
        koList.add(p1);
        
        //The line goes from max_x, p1, to min_x, p2
        int a = y[p1] - y[p2]; int b = x[p2] - x[p1];
        int c = y[p2] * x[p1] - y[p1] * x[p2];

        /* Finding the point with biggest postive distance (left side of line) 
        and point with biggest negative distance (right side of line) */
        int rightP = -1; int leftP = -1;
        int maxPosD = -1; int maxNegD = 1;
        IntList mRight = new IntList(); IntList mLeft = new IntList();
        for (int i = 0; i < x.length; i++) {
          if (i == p1 || i == p2) continue;
          int d = a*x[i] + b*y[i] + c;
          if (d <= 0) mRight.add(i);
          if (d >= 0) mLeft.add(i);
          if (d < maxNegD){maxNegD = d; rightP = i;}
          if (d > maxPosD){maxPosD = d; leftP = i;}
        }
        //Call recursive method on right
        seqRec(p1, p2, rightP, mRight, koList);
        //Add p2
        koList.add(p2);
        //Call recursive on left
        seqRec(p2, p1, leftP, mLeft, koList);
        return koList;
    }    
    /**
     * Find the furthest point and all the points to the right for both line p1-p3 and p3-p2
     * then call the recursive method again for these two points
     * @param p1 startpoint of line
     * @param p2 endpoint of line
     * @param p3 point furthest away from the line
     * @param m all the points on the right side of the line
     * @param koList all the hull-points found
     */
    void seqRec(int p1, int p2, int p3, IntList m, IntList koList) {
      
      if (m.size() == 1 && m.get(0) == p3) {koList.add(p3); return;}
      //Line from p1 to p3, line to the right of p3
      int aR = y[p1] - y[p3]; int bR = x[p3] - x[p1];
      int cR = y[p3] * x[p1] - y[p1] * x[p3];

      //Line from p3 to p2, line to the left of p3
      int aL = y[p3] - y[p2]; int bL = x[p2] - x[p3];
      int cL = y[p2] * x[p3] - y[p3] * x[p2];

      /* Finding the point with maximal negative distance from line p1-p3, and all the points to the right of this line
      and finding the point with maximal negative distance from line p3-p2, and all the points to the right of this line */
      int newP3R = -1; int newP3L = -1;
      int maxDR = 1; int maxDL = 1;
      IntList mR = new IntList(); IntList mL = new IntList();

      for (int j = 0; j < m.size(); j++) {
          int i = m.get(j);
          if (i == p3) continue;
          int dR = aR*x[i] + bR*y[i] + cR; int dL = aL*x[i] + bL*y[i] + cL;
          
          //If the point is on the line p1-p3 we check if it is in between p1 - p3 before we add it to the list of points
          if (dR == 0) {
            if ((x[i] <= x[p1] && x[i] >= x[p3]) || (x[i] >= x[p1] && x[i] <= x[p3])) {
              if ((y[i] >= y[p1] && y[i] <= y[p3]) || (y[i] <= y[p1] && y[i] >= y[p3])) {
                mR.add(i);
                if (dR < maxDR){maxDR = dR; newP3R = i;}
              }
            }
          } 
          //If the point is on the line p3-p2 we check if it is in between p3 - p2 before we add it to the list of points
          if (dL == 0) {
            if ((x[i] <= x[p3] && x[i] >= x[p2]) || (x[i] >= x[p3] && x[i] <= x[p2])) {
              if ((y[i] >= y[p3] && y[i] <= y[p2]) || (y[i] <= y[p3] && y[i] >= y[p2])) {
                mL.add(i);
                if (dL < maxDL){maxDL = dL; newP3L = i;}
              }
            }
          } else {
            if (dR <= 0) mR.add(i); 
            if (dL <= 0) mL.add(i);
            if (dR < maxDR){maxDR = dR; newP3R = i;}
            if (dL < maxDL){maxDL = dL; newP3L = i;}
          }
      }
      
      //If there are new points to the right of p1-p3 we call on the seqRec with the new point
      if (mR.size() != 0 && p3 != newP3R) seqRec(p1,p3,newP3R,mR,koList);

      //Adding p3
      koList.add(p3);

      //If there are new points to the right of p3-p2 we call on the seqRec with the new point
      if (mL.size() != 0 && p3 != newP3L) seqRec(p3,p2,newP3L,mL,koList);
    }

    /**
     * Finds min_x and max_x and starts two threads, one for each side of the line 
     * @return the hull points list
     */
    
     IntList parMethod() {
        IntList koList = new IntList();
        
        //Finding min_x and max_x and the points p1 and p2 at those locations
        int p1 = 0; int p2 = 0;
        int max_x = x[0]; int min_x = x[0];
        for (int i = 1; i < x.length; i++) {
            if (x[i] >= max_x) {max_x = x[i]; p1 = i;}
            else if (x[i] <= min_x) {min_x = x[i]; p2 = i;}
        }
        //The line goes from max_x, p1, to min_x, p2
        int a = y[p1] - y[p2]; int b = x[p2] - x[p1];
        int c = y[p2] * x[p1] - y[p1] * x[p2];

        /* Finding the point with biggest postive distance (left side of line) 
        and point with biggest negative distance (right side of line) */
        int rightP = -1; int leftP = -1;
        int maxPosD = -1; int maxNegD = 1;
        IntList mRight = new IntList(); IntList mLeft = new IntList();
        for (int i = 0; i < x.length; i++) {
          if (i == p1 || i == p2) continue;
          int d = a*x[i] + b*y[i] + c; //The distance
            //System.out.println("Point " + i + " with distance " + d);
          if (d <= 0) mRight.add(i);
          if (d >= 0) mLeft.add(i);
          if (d < maxNegD){maxNegD = d; rightP = i;}
          if (d > maxPosD){maxPosD = d; leftP = i;}
        }

        /* Create two new empty koList and send them with when creating and starting up two new threads,
        one for the point on the left side of the line and one for the right side */
        ExecutorService executor = Executors.newFixedThreadPool(2);
        IntList koListR = new IntList(); IntList koListL = new IntList();
        final int fP1 = p1; final int fP2 = p2; final int rP = rightP; final int lP = leftP;
        executor.execute(() -> {parRec(fP1, fP2, rP, mRight, koListR, 2);});
        executor.execute(() -> {parRec(fP2, fP1, lP, mLeft, koListL, 2);});
        
        //Wait for both threads to finish, then add p1, the right koList, p2 and the left koList.
        executor.shutdown();
        try {
          executor.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
        koList.add(p1);
        koList.append(koListR);
        koList.add(p2);
        koList.append(koListL);

        return koList;
    }
    
    /**
     * Find the furthest point and all the points to the right for both line p1-p3 and p3-p2
     * then call the recursive method again with new threads for these two points
     * wait until both threads are done and add all hull-points from each to the koList
     * @param p1 startpoint of line
     * @param p2 endpoint of line
     * @param p3 point furthest away from the line
     * @param m all the points on the right side of the line
     * @param koList all the hull-points found
     * @param level the level of the recursion call we are on
     */
    void parRec(int p1, int p2, int p3, IntList m, IntList koList, int level) {
      
      if (m.size() == 1 && m.get(0) == p3) {koList.add(p3); return;}
      //Line from p1 to p3, line to the right of p3
      int aR = y[p1] - y[p3]; int bR = x[p3] - x[p1];
      int cR = y[p3] * x[p1] - y[p1] * x[p3];

      //Line from p3 to p2, line to the left of p3
      int aL = y[p3] - y[p2]; int bL = x[p2] - x[p3];
      int cL = y[p2] * x[p3] - y[p3] * x[p2];

      /* Finding the point with maximal positive distance from line p1-p3, and all the points to the right of this line
      and finding the point with maximal positive distance from line p3-p2, and all the points to the right of this line */
      int newP3R = -1; int newP3L = -1;
      int maxDR = 1; int maxDL = 1;
      IntList mR = new IntList(); IntList mL = new IntList();

      for (int j = 0; j < m.size(); j++) {
          int i = m.get(j);
          if (i == p3) continue;
          int dR = aR*x[i] + bR*y[i] + cR; int dL = aL*x[i] + bL*y[i] + cL;
          
          //If the point is on the line p1-p3 we check if it is in between p1 - p3 before we add it to the list of points
          if (dR == 0) {
            if ((x[i] <= x[p1] && x[i] >= x[p3]) || (x[i] >= x[p1] && x[i] <= x[p3])) {
              if ((y[i] >= y[p1] && y[i] <= y[p3]) || (y[i] <= y[p1] && y[i] >= y[p3])) {
                mR.add(i);
                if (dR < maxDR){maxDR = dR; newP3R = i;}
              }
            }
          //If the point is on the line p3-p2 we check if it is in between p3 - p2 before we add it to the list of points
          } if (dL == 0) {
            if ((x[i] <= x[p3] && x[i] >= x[p2]) || (x[i] >= x[p3] && x[i] <= x[p2])) {
              if ((y[i] >= y[p3] && y[i] <= y[p2]) || (y[i] <= y[p3] && y[i] >= y[p2])) {
                mL.add(i);
                if (dL < maxDL){maxDL = dL; newP3L = i;}
              }
            }
          } else {
            if (dR <= 0) mR.add(i); 
            if (dL <= 0) mL.add(i);
            if (dR < maxDR){maxDR = dR; newP3R = i;}
            if (dL < maxDL){maxDL = dL; newP3L = i;}
          }
      }
    

      //If the level is under 4, new koList are created and two new threads are started (one for left and one for right) with the new
      //furthest points and the new koLists
      if (level < 4) {
        ExecutorService executor = Executors.newFixedThreadPool(2);

        IntList koListR = new IntList(); IntList koListL = new IntList();
        final int fP1 = p1; final int fP2 = p2; final int fP3 = p3;
        final int fRP3 = newP3R; final int fLP3 = newP3L;
        if (mR.size() != 0) executor.execute(() -> {parRec(fP1, fP3, fRP3, mR, koListR, level + 1);});
        if (mL.size() != 0) executor.execute(() -> {parRec(fP3, fP2, fLP3, mL, koListL, level + 1);});
        
        //When all threads are done the right koList, p3 and the left koList are appended to the original koList
        executor.shutdown();
        try {
          executor.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
        koList.append(koListR);
        koList.add(p3);
        koList.append(koListL);
        
      } 
      //If the level is over 4 the regular sequential recursive methods are called instead
      else {
        if (mR.size() != 0) seqRec(p1,p3,newP3R,mR,koList);
        koList.add(p3);
        if (mL.size() != 0) seqRec(p3,p2,newP3L,mL,koList);
      }
      
    }
    
    
    public static void main(String[] args) {
        int n,s;
        String met = "seq";
  
        try {
          n = Integer.parseInt(args[0]);
          s = Integer.parseInt(args[1]);
          if (args.length == 3) met = args[2];
          if (n <= 2) throw new Exception();
        } catch(Exception e) {
          System.out.println("Correct use of program is: java ConvexHull <n> <s> '<met>'" +
          "\n<n>, number, greater than 2, for how many points generated" +
          "\n<s> seed number for point generator" +
          "\n<met> method type for output file points. Sequential is default, if you want parallel, write 'par'\n");
          return;
        }
        ConvexHull hull = new ConvexHull(n, s);

        long startTime = System.currentTimeMillis();
        IntList seqHull = hull.seqMethod();
        long endTime = System.currentTimeMillis();
        long seqTime = endTime - startTime;

        startTime = System.currentTimeMillis();
        IntList parHull = hull.parMethod();
        endTime = System.currentTimeMillis();
        long parTime = endTime - startTime;

        double speedupGenTime = (double) seqTime / parTime;
        System.out.printf("Sequential time: %d ms%n", seqTime);
        System.out.printf("Parallel time: %d ms%n", parTime);
        System.out.printf("Speedup: " + String.format("%.2f", speedupGenTime) + "\n\n");

        Oblig4Precode precodeSeq = new Oblig4Precode(hull,seqHull);
        Oblig4Precode precodePar = new Oblig4Precode(hull,parHull);
        
        if (met.equals("par")) {
          System.out.println("YES");
          precodePar.writeHullPoints();
        } else {
          precodeSeq.writeHullPoints();
        }
        
        precodeSeq.drawGraph();
        precodePar.drawGraph();

    }
     
    
}

