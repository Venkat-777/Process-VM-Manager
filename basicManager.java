import java.util.Queue;
import java.io.FileWriter;
import java.io.*;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

public class basicManager
{
    final int PCB_MAX = 16;
    final int RCB_MAX = 4; 
    int READY_SIZE = 3;
    private int newProcessIndex; 
    private int runningProcessIndex;
    PCB[] PCBArr;
    RCB[] RCBArr;
    Queue<Integer>[] ReadyList;
    Queue<Integer> WaitingList;


    void create(int priority)
    {
        if(priority >= READY_SIZE | newProcessIndex >= PCB_MAX) 
        { 
            runningProcessIndex = -1;
            return; 
        }

        PCBArr[newProcessIndex] = new PCB();
        PCBArr[newProcessIndex].state = ProcessState.READY;
        PCBArr[newProcessIndex].priority = priority;
        PCBArr[runningProcessIndex].children.add(newProcessIndex);
        PCBArr[newProcessIndex].parent = runningProcessIndex;
        ReadyList[priority].add(newProcessIndex);
        newProcessIndex++;
        scheduler();

    }
    void destroy(int j)
    {
        if(!PCBArr[runningProcessIndex].children.contains(j))
        {
            runningProcessIndex = -1;
            return;
        }
        destroyH(j);
    }
    void destroyH(int j)
    {
        //recursive case
        int sz = PCBArr[j].children.size();
        for(int i = 0; i < sz; i++) 
            destroyH(PCBArr[j].children.poll());

        ReadyList[PCBArr[j].priority].remove(j);
        WaitingList.remove(j);

        int size = PCBArr[j].resources.size();
        for(int i = 0; i<size; i++)
        {
            PCBArr[j].release(PCBArr[j].resources.get(0), 1, PCBArr, RCBArr, ReadyList, WaitingList);
        }
        return;
    }
    void request(int r, int k)
    {
        if(r >= RCB_MAX | runningProcessIndex == 0)
        {
            runningProcessIndex = -1;
            return;
        }
        else if(k > RCBArr[r].inventory)
        {
            runningProcessIndex = -1;
            return;
        }
        else if(RCBArr[r].state >= k )
        {
            RCBArr[r].state -= k;
            for(int i=0; i<k ; i++)
                PCBArr[runningProcessIndex].resources.add(r);
        }
        else
        {
            PCBArr[runningProcessIndex].state = ProcessState.BLOCKED;
            ReadyList[PCBArr[runningProcessIndex].priority].remove(runningProcessIndex);
            WaitingList.add(runningProcessIndex);
            RCBArr[r].waitlist.add(new WaitItem(runningProcessIndex, k));
            scheduler();           
        }
    }
    void release(int r, int k)
    {
        if(!PCBArr[runningProcessIndex].resources.contains(Integer.valueOf(r))) 
        {
            runningProcessIndex = -1;
            return;
        }
        else if(PCBArr[runningProcessIndex].release(r, k, PCBArr, RCBArr, ReadyList, WaitingList) == -1) //release too much
        {
            runningProcessIndex = -1;
            return;
        }
        scheduler();
    }
    void timeout()
    {
        int timedOutProcess = runningProcessIndex;
        ReadyList[PCBArr[runningProcessIndex].priority].poll();
        PCBArr[timedOutProcess].state = ProcessState.READY;
        ReadyList[PCBArr[timedOutProcess].priority].add(timedOutProcess);
        scheduler();
    }
    void scheduler()
    {
        int i = READY_SIZE-1;
        while (ReadyList[i].isEmpty()) i--;
        int headList = ReadyList[i].peek();
        PCBArr[runningProcessIndex].state = ProcessState.READY;
        runningProcessIndex = headList;
        PCBArr[runningProcessIndex].state = ProcessState.RUNNING;
    }
    void init()
    {
        //clear variables
        READY_SIZE = 3;
        newProcessIndex = 0; 
        runningProcessIndex = 0;
        PCBArr = new PCB[PCB_MAX];
        RCBArr = new RCB[RCB_MAX];
        WaitingList = new LinkedList<>();
        ReadyList = (Queue<Integer>[]) new Queue<?>[READY_SIZE]; // size is the desired size of the array
        for(int i = 1;i<PCB_MAX;i++)
            PCBArr[i] = null;
        for (int i = 0; i < READY_SIZE; i++)
            ReadyList[i] = new LinkedList<>();

        //add 0th process
        PCBArr[0] = new PCB();
        PCBArr[0].state = ProcessState.RUNNING;
        PCBArr[0].parent = -1; //if -1 then no parent
        PCBArr[0].priority = 0;
        ReadyList[0].add(0);
        newProcessIndex++;

        int[] ResourceArr = {1,1,2,3};
        for(int i = 0;i < RCB_MAX;i++)
        {
            RCBArr[i] = new RCB();
            RCBArr[i].state = ResourceArr[i];
            RCBArr[i].inventory = ResourceArr[i];
        }
    }
    void init(int readyL_size, int u0, int u1, int u2, int u3)
    {
        //clear variables
        READY_SIZE = readyL_size;
        newProcessIndex = 0; 
        runningProcessIndex = 0;
        PCBArr = new PCB[PCB_MAX];
        RCBArr = new RCB[RCB_MAX];
        WaitingList = new LinkedList<>();
        ReadyList = (Queue<Integer>[]) new Queue<?>[READY_SIZE]; // size is the desired size of the array
        for(int i = 1;i<PCB_MAX;i++)
            PCBArr[i] = null;
        for (int i = 0; i < READY_SIZE; i++)
            ReadyList[i] = new LinkedList<>();
        PCBArr[0] = new PCB();
        PCBArr[0].state = ProcessState.RUNNING;
        PCBArr[0].parent = -1; //if -1 then no parent
        PCBArr[0].priority = 0;
        ReadyList[0].add(0);
        newProcessIndex++;

        int[] ResourceArr = {u0,u1,u2,u3};
        for(int i = 0;i < RCB_MAX;i++)
        {
            RCBArr[i] = new RCB();
            RCBArr[i].state = ResourceArr[i];
            RCBArr[i].inventory = ResourceArr[i];
        }
    }
    void pcbArrPrint() //used for testing
    {
        int i = 0;
        for(var pcb: this.PCBArr)
            if(pcb != null) 
            {
                System.out.println("Process:" + i);
                pcb.print();
                i++;
                System.out.print("\n\n");
            }

        System.out.println("Ready List: ");
        for(var readyItem: ReadyList)    
            System.out.print(readyItem + "\t");
        System.out.print("\n");

        System.out.println("Waiting List: ");
        for(var WaitItem: WaitingList)    
            System.out.print(WaitItem + "\t");
        System.out.print("\n");

        i = 0;
        for(var rcb: this.RCBArr)
            if(rcb != null) 
            {
                System.out.println("Resource:" + i);
                rcb.print();
                i++;
                System.out.print("\n");
            }        

    }
    private static void writeToFile(String fileName, String content) {
        try {
            // Create a FileWriter with the given file name
            FileWriter fileWriter = new FileWriter(fileName, true); // Append to file if it exists

            // Write the content to the file
            fileWriter.write(content + " ");

            // Close the FileWriter
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void main(String[] args) 
    {
            File file = new File("input.txt");
            
            try
            {
                FileWriter fileWriter = new FileWriter("output.txt", false); // Append to file if it exists
                fileWriter.close();
                Scanner scanner = new Scanner(file);
                basicManager manager = new basicManager();
                boolean first = true;
                while (scanner.hasNext()) 
                {
                    String command = scanner.next();
                    if(manager.runningProcessIndex == -1 && (command != "id" | command != "in")) manager.runningProcessIndex = 0;
                    switch (command) 
                    {
                        case "cr":
                            manager.create(Integer.parseInt(scanner.next()));
                            writeToFile("output.txt", Integer.toString(manager.runningProcessIndex));
                            break;
                        case "de":
                            manager.destroy(Integer.parseInt(scanner.next()));
                            writeToFile("output.txt", Integer.toString(manager.runningProcessIndex));
                            break;
                        case "rq":
                            manager.request(Integer.parseInt(scanner.next()),Integer.parseInt(scanner.next()));
                            writeToFile("output.txt", Integer.toString(manager.runningProcessIndex));
                            break;
                        case "rl":
                            manager.release(Integer.parseInt(scanner.next()),Integer.parseInt(scanner.next()));
                            writeToFile("output.txt", Integer.toString(manager.runningProcessIndex));
                            break;
                        case "to":
                            manager.timeout();
                            writeToFile("output.txt", Integer.toString(manager.runningProcessIndex));
                            break;
                        case "id":
                            manager.init();
                            if (first) writeToFile("output.txt", Integer.toString(manager.runningProcessIndex));
                            else writeToFile("output.txt", "\n" + Integer.toString(manager.runningProcessIndex));
                            first = false;
                            break;
                        case "in":
                            manager.init(Integer.parseInt(scanner.next()),
                            Integer.parseInt(scanner.next()),
                            Integer.parseInt(scanner.next()),
                            Integer.parseInt(scanner.next()),
                            Integer.parseInt(scanner.next()));
                            if (first) writeToFile("output.txt", Integer.toString(manager.runningProcessIndex));
                            else writeToFile("output.txt", "\n" + Integer.toString(manager.runningProcessIndex));
                            first = false;
                            break;
                        case "pr": //used for testing
                            manager.pcbArrPrint();
                            break;
                        case "\n":
                            break;
                        default:
                            break;
                    }
                }
                scanner.close();
            }
            catch(FileNotFoundException e) 
            {
                System.out.println("File not found: " + e.getMessage());
            }
            catch(IOException e)
            {
                System.out.println("File not found: " + e.getMessage());
            }
    }

    basicManager(){}
}
enum ProcessState {BLOCKED,READY,RUNNING}
enum ResourceState {FREE,ALLOCATED}
class PCB
{
    public ProcessState state; // 0->blocked,1->ready,2->running
    public int parent;
    public int priority;
    public Queue<Integer> children = new LinkedList<>();
    public List<Integer> resources = new LinkedList<>();
    PCB()
    {
        state = ProcessState.BLOCKED;
        parent = 0;
    }
    int release(int r, int k, PCB[] PCBArr, RCB[] RCBArr, Queue<Integer>[] ReadyList, Queue<Integer> WaitingList)
    {
        int j = -1;
        int l = 0;

        RCBArr[r].state+=k;
        for(int i =0; i<k;i++)
        {
            if(!this.resources.contains(Integer.valueOf(r))) 
            {
                return -1;
            }
            this.resources.remove(Integer.valueOf(r));
        }
        int wlSiz = RCBArr[r].waitlist.size();
        for(int s =0; s< wlSiz;s++)
        {
            WaitItem wi = RCBArr[r].waitlist.peek();
            j = wi.process;
            l = wi.requestedResources;
            if(WaitingList.contains(j) && l <= RCBArr[r].state)
            {
                RCBArr[r].waitlist.poll();
                WaitingList.remove(j);
                ReadyList[PCBArr[j].priority].add(j);
                RCBArr[r].waitlist.remove(wi);
                PCBArr[j].state = ProcessState.READY;
                for(int i =0; i<l; i++)
                    PCBArr[j].resources.add(r);
                RCBArr[r].state -= l;
            }
        }
        return 0;
    }
    void print()
    {
        System.out.println("State:" + state);
        System.out.println("Parent:" + parent);
        for(var ch: children)
            System.out.print("Child:" + ch + "\t");
        System.out.print("\n");
        for(var re: resources)
            System.out.print("Resources:" + re + "\t");
    }
}
class RCB
{
    public int state;
    public Queue<WaitItem> waitlist = new LinkedList<>();
    public int inventory;
    RCB()
    {
        this.state = 0;
    }
    void print()
    {
        System.out.println("State:" + state);
        for(var wlItem: waitlist)
            System.out.print("WL Process:" + wlItem + "\t");
        System.out.print("\n");
    }
}

class WaitItem
{
    public int process;
    public int requestedResources;

    WaitItem(int process,int requestedResources)
    {
        this.process = process;
        this.requestedResources = requestedResources;
    }
}