MemoryMonitor allows one to observe memory useage by a Java program as that program is running. 

Compiling the MemoryMonitor.java code produces 2 class files, MemoryMonitor and MemorySampler. Place both classes in the same director as the startup batch or shell file and modify the command line for starting the software by making MemoryMonitor the startup class and the desired program a parameter. For example, one can run Morpho with the command line

java -Xmx512m -Xss1m -cp %CPATH% MemoryMonitor edu.ucsb.nceas.morpho.Morpho




Dan Higgins
Aug 2003