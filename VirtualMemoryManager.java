import java.io.*;
import java.util.*;

public class VirtualMemoryManager 
{
    //COMMENT TO TEST WITH NO DP
    //String FILENAMES[] = new String[]{"init-dp.txt","input-dp.txt","output-dp.txt"};
    //UNCOMMENT TO TEST NO DP
    String FILENAMES[] = new String[]{"init-no-dp.txt","input-no-dp.txt","output-no-dp.txt"};

    private static final int PAGE_SIZE = 512; // size of each page in words
    private static final int FRAME_SIZE = PAGE_SIZE; // size of each frame in words
    private static final int NUM_FRAMES = 1024; // number of frames in PM
    private static final int PM_SIZE = FRAME_SIZE * NUM_FRAMES; // total size of PM in words
    private static final int ST_SIZE = 1024; // size of segment table in words
    private static final int UNREACHABLE = PM_SIZE+100; // size of segment table in words

    
    int B = 1024;
    int currentDiskBlock = 0;
    private int[] PM = new int[PM_SIZE];
    private int[][] D = new int[B][512];
    private boolean[] isFreeFrames = new boolean[NUM_FRAMES];

    //contructor
    VirtualMemoryManager()
    {
        //Clear output file
        try {
            FileWriter writer = new FileWriter(FILENAMES[2]);
            writer.write("");
            writer.close();
        } catch (IOException e) {
            System.out.println("An error occurred while clearing the file.");
            e.printStackTrace();
        }
        for(int i = 0; i < PM.length; i++)
            PM[i] = 0; //
        for(int i = 0; i < 1024; i++)
            for(int j =0; j<512; j++)
                D[i][j] = 0; //
        isFreeFrames[0] = false;
        isFreeFrames[1] = false;
        for(int i = 2; i < NUM_FRAMES; i++)
            isFreeFrames[i] = true;
    }
    // Extract the components of a virtual address
    private int getS(int VA) 
    {
        return (VA >> 18);
    }

    private int getW(int VA) 
    {
        return (VA & 0x1FF);
    }

    private int getP(int VA) 
    {
        return (VA >> 9) & 0x1FF;
    }

    private int getPW(int VA) 
    {
        return (VA & 0x3FFFF);
    }

    private int getFF()
    {
        for(int i = 0;i<FRAME_SIZE; i++)
            if(isFreeFrames[i])
                return i;
        return -1;
    }
    private int translateVA(int VA)
    {
        int s = getS(VA);
        int p = getP(VA);
        int w = getW(VA);
        int pw = getPW(VA);
        int fNum = PM[2*s + 1];
        if(pw >= PM[2*s]) return -1;
        if(fNum < 0)
        {
            
            int freeFrame = getFF();
            isFreeFrames[freeFrame] = false;
            read_block(-1*fNum, freeFrame*512);
            PM[2*s+1] = freeFrame;
            //this.printPM();
        }
        int pNum = PM[PM[2*s+1]*512+p];
        //System.out.println("Herererere"+pNum);
        if(pNum < 0)
        {
            int freeFrame = getFF();
            //System.out.println("Herererere"+freeFrame);
            isFreeFrames[freeFrame] = false;
            read_block(-1*pNum, freeFrame*512);
            PM[PM[2*s+1]*512+p] = freeFrame;
            //this.printPM();
        }
        return PM[PM[2*s+1]*512+p]*512 + w;
    }
    private void produceOutput(String out)
    { 
        try
        {
            FileWriter fileWriter = new FileWriter(FILENAMES[2], true); // Append to file if it exists 
            fileWriter.write(out);
            if(out != "\n") fileWriter.write(" ");
            fileWriter.close();
        }catch(IOException e)
        {
            e.printStackTrace();
        }

    }
    private void read_block(int b, int m)
    {
        if(b<0) b*=-1;
        for(int i = 0; i < 512; i++)
        {
            PM[m+i] = D[b][i];
            //if(D[b][i] != 0) System.out.println("Herererere"+D[b][i]+(m+i));
        }
    }
    public void initializePM() 
    {
        try {
            Scanner scanner = new Scanner(new File(FILENAMES[0]));
            String line = scanner.nextLine().trim();
            String[] tokens = line.split("\\s+");
            //Line 1
            for (int i = 0; i < tokens.length; i += 3) 
            {
                int s = Integer.parseInt(tokens[i]);
                int z = Integer.parseInt(tokens[i + 1]);
                int f = Integer.parseInt(tokens[i + 2]);
                
                PM[s*2] = z;
                //System.out.println("PM[" + s*2 + "] = " + z);
                PM[s*2+1] = f;
                if(f>0) isFreeFrames[f] = false;
                // System.out.println("PM[" + (s*2+1) + "] = " + f);

                // Process the triple
                //System.out.println(s + " " + z + " " + f);
            }
            line = scanner.nextLine().trim();
            tokens = line.split("\\s+");

            //Line 2
            for (int i = 0; i < tokens.length; i += 3) 
            {
                int s = Integer.parseInt(tokens[i]);
                int p = Integer.parseInt(tokens[i + 1]);
                int f = Integer.parseInt(tokens[i + 2]);

                //Line 2
                int s2 = PM[2*s+1];
                if(s2 >= 0) 
                {
                    PM[s2*512+p] = f; //EASY TO CONFUSE s2/f2 with s/f DONT!! 
                    isFreeFrames[s2] = false;
                    if(f>0) isFreeFrames[f] = false;
                    // System.out.println("PM[ PM[" + (2*s + 1) +  "] = " + PM[2*s+1]+"*512+p = "+ (PM[2*s+1]*512+p) + "] = " + f);
                }
                else
                {
                    D[s2*(-1)][p] = f;
                    if(f>0) isFreeFrames[f] = false;
                    // System.out.println( "Negative => D[" +s2*(-1)+ "]["+p+"] = "+f );
                }
            }                
            scanner.close();
        } catch (FileNotFoundException e) {
            System.out.println("File not found: " + e.getMessage());
        }
    }
    public void processInput() throws IOException {
        // TA -> Translate Address
        // RP -> Read Physical Address
        try {
            Scanner scanner = new Scanner(new File(FILENAMES[1]));
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                if (line.isEmpty()) {
                    continue; // Skip empty lines
                }
                Scanner lineScanner = new Scanner(line);
                while (lineScanner.hasNext()) {
                    String command = lineScanner.next();
                    switch (command) {
                        case "TA":
                            int PA = translateVA(Integer.parseInt(lineScanner.next()));
                            produceOutput(Integer.toString(PA));
                            break;
                        case "RP":
                            produceOutput(Integer.toString(PM[Integer.parseInt(lineScanner.next())]));
                            break;
                        case "NL":
                            produceOutput("\n");
                            break;
                        default:
                            System.out.println(command);
                            break;
                    }
                }
                lineScanner.close();
            }
            scanner.close();
        } catch (FileNotFoundException e) {
            System.out.println("File not found: " + e.getMessage());
        }
    }    

    public void printPM()
    {
        System.out.println("**** Printing ****");
        for(int i = 0; i < PM.length; i++)
        {
            if(PM[i] != 0)
            {
                System.out.println("PM[" + i + "] = " + PM[i]);
            }
        }
        for(int i = 0; i < isFreeFrames.length; i++)
            if(!isFreeFrames[i]) System.out.println("iFF[" + i + "] = " + isFreeFrames[i]);
    }

    public static void main(String[] args) throws IOException
    {
        VirtualMemoryManager VM = new VirtualMemoryManager();
        try
        {
            VM.initializePM();
            VM.processInput();
        }   catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
