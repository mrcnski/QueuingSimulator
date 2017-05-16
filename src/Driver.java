// Driver for the queue simulator in Simulator.java
// This driver provides some test cases and examples of usage for the simulator
// Author: Marcin Swieczkowski

import java.util.*;

public class Driver {
  
    public static void main(String [] args) {
    
        double[] c = {.001, .01, .03, .06};
        for (int i = 0; i < c.length; i ++) {
            Simulator s1 = new Simulator();
            s1.initializeSystem(30, "RR", c[i], .03);
            s1.run(100);
            s1.clearMeasurements();
            s1.run(200);
            System.out.println("Average response time with a quantum of "+Double.toString(c[i])+" ms: "+Double.toString(s1.averageResponseTime()));
        }
        for (int i = 0; i < 7; i ++) {
            double sum = 0;
            for (int j = 1; j <= 10; j ++) {
                Simulator s1 = new Simulator();
                s1.initializeSystem(30, "RR", .001, ((double)i*10+j)/1000); // we use arbitrary quantum value
                s1.run(100);
                s1.clearMeasurements();
                s1.run(200);
                sum += s1.averageSlowdown();
            }
            System.out.println(Integer.toString(i*10)+"-"+Integer.toString((i+1)*10)+"\t"+Double.toString(sum/10));
        }
    
        for (int i = 0; i < c.length; i ++) {
            Simulator s2 = new Simulator();
            s2.initializeSystem(30, "min-remaining", "quantum", c[i], .03);
            s2.run(100);
            s2.clearMeasurements();
            s2.run(200);
            System.out.println("Average response time with a quantum of "+Double.toString(c[i])+" ms: "+Double.toString(s2.averageResponseTime()));
            System.out.println("Average slowdown with a quantum of "+Double.toString(c[i])+" ms: "+Double.toString(s2.averageSlowdown()));
        }
        for (int i = 0; i < 7; i ++) {
            double sum = 0;
            for (int j = 1; j <= 10; j ++) {
                Simulator s2 = new Simulator();
                s2.initializeSystem(30, "min-remaining", "quantum", .001, ((double)i*10+j)/1000); // we use arbitrary quantum value
                s2.run(100);
                s2.clearMeasurements();
                s2.run(200);
                sum += s2.averageSlowdown();
            }
            System.out.println(Integer.toString(i*10)+"-"+Integer.toString((i+1)*10)+"\t"+Double.toString(sum/10));
        }
    }
}
