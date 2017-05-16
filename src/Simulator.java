// Implements the logic for the simulator.
// Author: Marcin Swieczkowski

import java.util.*;
import java.lang.Math;

public class Simulator {
  
    // generates random exponential values
    public static double RVExponential(double lambda) {
        Random generator = new Random();
        double y = generator.nextDouble();
        return - Math.log(1 - y) / lambda;
    }
  
    public static double ZRand() {
        Random generator = new Random();
        double sum = 0;
        for (int i = 0; i < 50; i ++)
            sum += generator.nextDouble();
        return sum / 50;
    }
  
    // generates random normal values around mean U with stddev S
    public static double GRand(double U, double S) {
        double rand = ZRand()*50;
        rand *= S/(Math.sqrt(50)/Math.sqrt(12));
        rand += U - (.5*50 * S/(Math.sqrt(50)/Math.sqrt(12)));
        return rand;
    }
  
    // generates random values uniformly distributed between a and b
    public static double URand(double a, double b) {
        Random generator = new Random();
        return generator.nextDouble() * (b-a) + a;
    }
  
    // generic Event class
    public class Event implements Comparable<Event> {
    
        double scheduledTime;
        String type = "Event";
    
        public int compareTo(Event other){
            if (scheduledTime < other.scheduledTime)
                return -1;
            else if (scheduledTime > other.scheduledTime)
                return 1;
            return 0;
        }
    
        public void handle() {}
    
    }
  
    // Arrival class extending Event
    public class Arrival extends Event {
    
        public Arrival(double t){
            scheduledTime = t;
            type = "Arrival";
        }
    
        public void handle(){
            Simulator.this.scheduler.arrival();
            Simulator.this.eventsQueue.add(new Arrival(scheduledTime+RVExponential(Simulator.this.lambda)));
        }
    
    }
  
    // Departure class extending Event
    public class Departure extends Event {
    
        public Departure(double t){
            scheduledTime = t;
            type = "Departure";
        }
    
        public void handle(){
            Simulator.this.scheduler.departure();
        }
    
    }

    // TimeOut class
    public class TimeOut extends Event {
    
        public TimeOut(double t) {
            scheduledTime = t;
            type = "TimeOut";
        }
    
        public void handle() {
            Simulator.this.scheduler.timeout();
        }
    }
  

    // Scheduler class
    public class Scheduler {
    
        // Request class, subclass of Scheduler
        public class Request {
      
            double arrivalTime;
            double requestedServiceTime;
            double givenServiceTime = 0;
            double usedQuantum;
      
            Request(double t) {
                arrivalTime = Simulator.this.currentTime;
                requestedServiceTime = t;
            }
      
            public double remainingServiceTime() {
                return requestedServiceTime - givenServiceTime;
            }
        }
    
        String schedulerType; // RR, VRR, FIFO, STRF, or custom
        String selectionFunction; // longest-waiting, min-remaining, or VRR
        String decisionMode; // non-preemptive, or quantum
        Request currentRequest = null;
        double quantum;
        double ts;
        ArrayList<Request> queueCPU = new ArrayList<Request>();
        ArrayList<Request> queueIO = new ArrayList<Request>();
        ArrayList<Request> queueAux = new ArrayList<Request>();
    
        Scheduler(String type, double q, double t) {
            schedulerType = type;
            if (schedulerType.equals("RR")) {
                selectionFunction = "longest-waiting";
                decisionMode = "quantum";
            }
            else if (schedulerType.equals("VRR")) {
                selectionFunction = "VRR";
                decisionMode = "quantum";
            }
            else if (schedulerType.equals("FIFO")) {
                selectionFunction = "longest-waiting";
                decisionMode = "non-preemptive";
            }
            else if (schedulerType.equals("STRF")) {
                selectionFunction = "min-remaining";
                decisionMode = "non-preemptive";
            }
            else
                assert false;
            quantum = q;
            ts = t;
        }
    
        Scheduler(String selection, String decision, double q, double t) {
            schedulerType = "custom";
            selectionFunction = selection;
            decisionMode = decision;
            quantum = q;
            ts = t;
        }
    
        // selects from the queue according to the selection function and takes request out of queue
        public Request select() {
            if (queueCPU.isEmpty() && queueAux.isEmpty())
                return null;
            if (selectionFunction.equals("longest-waiting")) {
                return queueCPU.remove(0);
            }
            else if (selectionFunction.equals("min-remaining")) {
                int min = 0;
                for (int i = 1; i < queueCPU.size(); i ++) {
                    if (queueCPU.get(i).remainingServiceTime() < queueCPU.get(min).remainingServiceTime())
                        min = i;
                }
                return queueCPU.remove(min);
            }
            else if (selectionFunction.equals("VRR")) {
        
            }
            else
                assert false;
            return null;
        }
    
        public void arrival() {
            if (currentRequest == null) { // immediately begin serving the request and continue until preempted
                // Quantum-based
                if (decisionMode.equals("quantum")) {
                    double requestedTime = RVExponential(1/ts);
                    currentRequest = new Request(requestedTime);
                    if (requestedTime <= quantum)
                        Simulator.this.eventsQueue.add(new Departure(Simulator.this.currentTime+requestedTime));
                    else
                        Simulator.this.eventsQueue.add(new TimeOut(Simulator.this.currentTime+quantum));
                }
                // Non-preemptive
                else if (decisionMode.equals("non-preemptive")) {
                    double requestedTime = RVExponential(1/ts);
                    currentRequest = new Request(requestedTime);
                    Simulator.this.eventsQueue.add(new Departure(Simulator.this.currentTime+requestedTime));
                }
                else
                    assert false;
            }
            else {
                queueCPU.add(new Request(RVExponential(1/ts)));
            }
        }
    
        public void departure() {
            Simulator.this.responseTimeData.add(Simulator.this.currentTime - currentRequest.arrivalTime); // add response time data
            Simulator.this.slowdownData.add((Simulator.this.currentTime-currentRequest.arrivalTime)/currentRequest.requestedServiceTime);
            // Quantum-based
            if (decisionMode.equals("quantum")) {
                currentRequest = select();
                if (currentRequest == null)
                    return;
                if (currentRequest.remainingServiceTime() <= quantum)
                    Simulator.this.eventsQueue.add(new Departure(Simulator.this.currentTime + currentRequest.remainingServiceTime()));
                else
                    Simulator.this.eventsQueue.add(new TimeOut(Simulator.this.currentTime + quantum));
            }
            // Non-preemptive
            else if (decisionMode.equals("non-preemptive")) {
                currentRequest = select();
                if (currentRequest == null)
                    return;
                Simulator.this.eventsQueue.add(new Departure(Simulator.this.currentTime + currentRequest.requestedServiceTime));
            }
            else
                assert false;
      
        }
    
        public void timeout() {
            // Quantum-based
            assert (decisionMode.equals("quantum"));
            currentRequest.givenServiceTime += quantum;
            queueCPU.add(currentRequest);
            currentRequest = select();
            if (currentRequest == null)
                return;
            if (currentRequest.remainingServiceTime() <= quantum)
                Simulator.this.eventsQueue.add(new Departure(Simulator.this.currentTime + currentRequest.remainingServiceTime()));
            else
                Simulator.this.eventsQueue.add(new TimeOut(Simulator.this.currentTime + quantum));
        }
    
    }
  
    // clears measurement data
    public void clearMeasurements() {
        responseTimeData.clear();
        slowdownData.clear();
    }
  
    public double averageResponseTime() {
        double sum = 0;
        for (Double d : responseTimeData) {
            sum += d;
        }
        return sum / responseTimeData.size();
    }
  
    public double averageSlowdown() {
        double sum = 0;
        for (Double d : slowdownData) {
            sum += d;
        }
        return sum / slowdownData.size();
    }
  
    // internal simulator variables
    PriorityQueue<Event> eventsQueue = new PriorityQueue<Event>();
    Scheduler scheduler;
    double currentTime = 0;
    double lambda;
    ArrayList<Double> responseTimeData = new ArrayList<Double>();
    ArrayList<Double> slowdownData = new ArrayList<Double>();
  
    // simulator initializor
    public void initializeSystem(int l, String select, String decision, double quantum, double ts) {
        lambda = l;
        scheduler = new Scheduler(select, decision, quantum, ts);
    
        eventsQueue.add(new Arrival(RVExponential(lambda)));
    }
  
    public void initializeSystem(int l, String type, double quantum, double ts) {
        lambda = l;
        scheduler = new Scheduler(type, quantum, ts);
    
        eventsQueue.add(new Arrival(RVExponential(lambda)));
    }
  
    // run simulator
    public void run(double targetTime) {
        while (currentTime < targetTime) {
            assert (eventsQueue.size() > 0);
            Event e = eventsQueue.remove();
            assert (e.scheduledTime >= currentTime);
            currentTime = e.scheduledTime;
            e.handle();
        }
    }

  
}
