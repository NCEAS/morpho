
import java.awt.*;
import java.awt.event.*;
import java.lang.reflect.*;


/*
 * Internal class to periodically sample memory usage
 */
class MemorySampler
  implements Runnable
{
  long[] freeMemory = new long[1000];
  long[] totalMemory = new long[1000];
  int sampleSize = 0;
  long max = 0;
  boolean keepGoing = true;

  MemorySampler()
  {
    //Start the object running in a separate maximum priority thread
    Thread t = new Thread(this);
    t.setDaemon(true);
    t.setPriority(Thread.MAX_PRIORITY);
    t.start();
  }

  public void stop()
  {
    //set to stop the thread when someone tells us
    keepGoing = false;
  }

  public void run()
  {
    //Just a loop that continues sampling memory values every 300 milliseconds
    //until the stop() method is called.
    Runtime runtime = Runtime.getRuntime();
    while(keepGoing)
    {
      try{Thread.sleep(300);}catch(InterruptedException e){};
      addSample(runtime);
    }
  }

  public void addSample(Runtime runtime)
  {   
//    System.out.println("freeMemory = "+runtime.freeMemory());
//    System.out.println("totalMemory = "+runtime.totalMemory());

    //Takes the actual samples, recording them in the two arrays.
    //We expand the arrays when they get full up.
    if (sampleSize >= freeMemory.length)
    {
      //just expand the arrays if they are now too small
      long[] tmp = new long[2 * freeMemory.length];
      System.arraycopy(freeMemory, 0, tmp, 0, freeMemory.length);
      freeMemory = tmp;
      tmp = new long[2 * totalMemory.length];
      System.arraycopy(totalMemory, 0, tmp, 0, totalMemory.length);
      totalMemory = tmp;
    }

    freeMemory[sampleSize] = runtime.freeMemory();
    totalMemory[sampleSize] = runtime.totalMemory();

    //Keep the maximum value of the total memory for convenience.
    if (max < totalMemory[sampleSize])
      max = totalMemory[sampleSize];
    sampleSize++;
  }
}


public class MemoryMonitor
  extends Frame
  implements WindowListener,Runnable
{
  //The sampler object
  MemorySampler sampler;

  //interval is the delay between calls to repaint the window
  long interval;
  static Color freeColor = Color.red;
  static Color totalColor = Color.blue;
  int[] xpoints = new int[2000];
  int[] yfrees = new int[2000];
  int[] ytotals = new int[2000];


  /*
   * Start a monitor and the graph, then start up the real class
   * with any arguments. This is given by the rest of the commmand
   * line arguments.
   */
  public static void main(String args[])
  {
    try
    {
      //Start the grapher with update interval of half a second
      MemoryMonitor m = new MemoryMonitor(1000);

      //Remaining arguments are the class with the main() method, and its arguments
      String classname = args[0];
      String[] argz = new String[args.length-1];
      System.arraycopy(args, 1, argz, 0, argz.length);
      Class clazz = Class.forName(classname);

      //main has one parameter, a String array.
      Class[] mainParamType = {args.getClass()}; 
      Method main = clazz.getMethod("main", mainParamType);
      Object[] mainParams = {argz};

      //start real class
      main.invoke(null, mainParams);

      //Tell the monitor the application finished
 //     m.testStopped();
    }
    catch(Exception e)
    {
      e.printStackTrace();
    }
  }

  public MemoryMonitor(long updateInterval)
  {
    //Create a graph window and start it in a separate thread
    super("Memory Monitor");
    interval = updateInterval;

    this.addWindowListener(this);
    this.setSize(600,200);
    this.show();

    //Start the sampler (it runs itself in a separate thread)
    sampler = new MemorySampler();

    //and put myself into a separate thread
    (new Thread(this)).start();
  }

  public void run()
  {
    //Simple loop, just repaints the screen every 'interval' milliseconds
    int sampleSize = sampler.sampleSize;
    for (;;)
    {
      try{Thread.sleep(interval);}catch(InterruptedException e){};
      if (sampleSize != sampler.sampleSize)
      {
        //Should just call repaint here
        //this.repaint();
        //but it doesn't always work, so I'll repaint in this thread.
        //I'm not doing anything else anyway in this thread.
        try{this.update(this.getGraphics());}catch(Exception e){e.printStackTrace();}
        sampleSize = sampler.sampleSize;
      }
    }
  }

  public void testStopped()
  {
    //just tell the sampler to stop sampling.
    //We won't exit ourselves until the window is explicitly closed
    //so that our user can examine the graph at leisure.
    sampler.stop();
  }

  public void paint(Graphics g)
  {
    //Straightforward - draw a graph for the latest N points of total and free memory
    //where N is the width of the window.
    try
    {
      java.awt.Dimension d = getSize();
      int width = d.width-20;
      int height = d.height - 40;
      long max = sampler.max;
      int sampleSize = sampler.sampleSize;
      if (sampleSize < 20)
        return;
      int free, total, free2, total2;
      int highIdx = width < (sampleSize-1) ? width : sampleSize-1;
      int idx = sampleSize - highIdx - 1;
      for (int x = 0 ; x < highIdx ; x++, idx++)
      {
        xpoints[x] = x+10;
        yfrees[x] = height - (int) ((sampler.freeMemory[idx] * height) / max) + 40;
        ytotals[x] = height - (int) ((sampler.totalMemory[idx] * height) / max) + 40;
      }
      g.setColor(freeColor);
      g.drawPolyline(xpoints, yfrees, highIdx);
      g.setColor(totalColor);
      g.drawPolyline(xpoints, ytotals, highIdx);
      g.setColor(Color.black);
      g.drawString("maximum: " + max + " bytes (total memory - blue line  |  free memory - red line)" , 10, 35);
    }
    catch (Exception e) {System.out.println("MemoryMonitor: " + e.getMessage());}
  }

  public void windowActivated(WindowEvent e){}
  public void windowClosed(WindowEvent e){}
  public void windowClosing(WindowEvent e) {System.exit(0);}
  public void windowDeactivated(WindowEvent e){}
  public void windowDeiconified(WindowEvent e){}
  public void windowIconified(WindowEvent e){}
  public void windowOpened(WindowEvent e) {}
}
