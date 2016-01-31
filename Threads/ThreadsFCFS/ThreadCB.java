package osp.Threads;
import java.util.Vector;
import java.util.Enumeration;
import osp.Utilities.*;
import osp.IFLModules.*;
import osp.Tasks.*;
import osp.EventEngine.*;
import osp.Hardware.*;
import osp.Devices.*;
import osp.Memory.*;
import osp.Resources.*;

/**
   This class is responsible for actions related to threads, including
   creating, killing, dispatching, resuming, and suspending threads.

   @OSPProject Threads
*/
public class ThreadCB extends IflThreadCB 
{
    
	private static GenericList readyQueue; // Queue for ready threads
	private static int count = 0; // Count of number of threads
	private static long sumOfResponseTime = 0; // Total response time of all threads
	private static long throughputTime = 0; // Total time for all threads
	private boolean started; // Flag to check if thread has started
	
	// Helper function to set started status
	public void setStarted(boolean status)
	{
		this.started = status;
	}
	
	//Helper function to get started status
	public boolean getStarted()
	{
		return this.started;
	}
	
	/**
       The thread constructor. Must call 

       	   super();

       as its first statement.

       @OSPProject Threads
    */
	
    public ThreadCB()
    {
       
		super();
		started = false;
    }

    /**
       This method will be called once at the beginning of the
       simulation. The student can set up static variables here.
       
       @OSPProject Threads
    */
    public static void init()
    {
       
		readyQueue = new GenericList();
    }

    /** 
        Sets up a new thread and adds it to the given task. 
        The method must set the ready status 
        and attempt to add thread to task. If the latter fails 
        because there are already too many threads in this task, 
        so does this method, otherwise, the thread is appended 
        to the ready queue and dispatch() is called.

	The priority of the thread can be set using the getPriority/setPriority
	methods. However, OSP itself doesn't care what the actual value of
	the priority is. These methods are just provided in case priority
	scheduling is required.

	@return thread or null

        @OSPProject Threads
    */
    static public ThreadCB do_create(TaskCB task)
    {
        // your code goes here
		ThreadCB thread = null;
        if(task == null)       //Empty task, start another thread                             
        {
            ThreadCB.dispatch();
            return null;
        }
        
        if(task.getThreadCount() >= MaxThreadsPerTask)      //Maximum threads per task exceeded, so start another task's thread
        {
            ThreadCB.dispatch();
            return null;
        }
        
		//Create new thread and set parameters
        thread = new ThreadCB();		
        thread.setPriority(task.getPriority());             
        thread.setStatus(ThreadReady);                      
        thread.setTask(task);                               
        if(task.addThread(thread) == 0)    //Adding thread to task unsuccessful?                 
        {
            ThreadCB.dispatch(); //Choose a new thread from the queue
            return null;
        }
        readyQueue.append(thread);
		count++;		//Increment count of total threads
        ThreadCB.dispatch();                                
        return thread; 
		
    }

    /** 
	Kills the specified thread. 

	The status must be set to ThreadKill, the thread must be
	removed from the task's list of threads and its pending IORBs
	must be purged from all device queues.
        
	If some thread was on the ready queue, it must removed, if the 
	thread was running, the processor becomes idle, and dispatch() 
	must be called to resume a waiting thread.
	
	@OSPProject Threads
    */
    public void do_kill()
    {
		throughputTime = HClock.get(); //Get current clock time
		MyOut.print("OSP.Threads.ThreadCB","Program Execution Time: " + throughputTime + " Count: " + count); // Print execution time to log
        // your code goes here
		int status = this.getStatus();
		
		if(status == ThreadReady){ //If ready, remove from queue
			if(readyQueue.remove(this) == null)
				return;
		}
		else if(status == ThreadRunning){ // If running, release resources and remove
			ThreadCB thread = null;
                try
                {
                    thread = MMU.getPTBR().getTask().getCurrentThread();
                    if(this == thread)
                    {
                        MMU.setPTBR(null);
                        getTask().setCurrentThread(null);					
                    }
                }
                catch(NullPointerException e){}
		}
		
		if((getTask().removeThread(this)) != SUCCESS)
			return;                                       
        setStatus(ThreadKill);                                              
        
        for(int i = 0; i<Device.getTableSize(); i++)                        
        {
            Device.get(i).cancelPendingIO(this);
        }
        
        ResourceCB.giveupResources(this);                                    
        ThreadCB.dispatch();                                                
        if(getTask().getThreadCount() == 0) //Kill task if it has no active threads
            getTask().kill();
        }
    }

    /** Suspends the thread that is currenly on the processor on the 
        specified event. 

        Note that the thread being suspended doesn't need to be
        running. It can also be waiting for completion of a pagefault
        and be suspended on the IORB that is bringing the page in.
	
	Thread's status must be changed to ThreadWaiting or higher,
        the processor set to idle, the thread must be in the right
        waiting queue, and dispatch() must be called to give CPU
        control to some other thread.

	@param event - event on which to suspend this thread.

        @OSPProject Threads
    */
    public void do_suspend(Event event)
    {
        
		int status = getStatus();                                      
        if(status>=ThreadWaiting)  //If thread is waiting, increase count of waiting threads                                    
        {
            setStatus(getStatus()+1);
        }
		else if(status == ThreadRunning)        // If running, save all resources and set the status to waiting                        
        {
            ThreadCB thread = null;
            try
            {
                thread = MMU.getPTBR().getTask().getCurrentThread();
                if(this==thread)
                {
                    MMU.setPTBR(null);
                    getTask().setCurrentThread(null);
                    setStatus(ThreadWaiting);                           
                }
            }
            catch(NullPointerException e){}          
        }
		if(!readyQueue.contains(this))
        {
            event.addThread(this);                                      
        }
        else
        {
            readyQueue.remove(this);
        }
        
        ThreadCB.dispatch();  
    }

    /** Resumes the thread.
        
	Only a thread with the status ThreadWaiting or higher
	can be resumed.  The status must be set to ThreadReady or
	decremented, respectively.
	A ready thread should be placed on the ready queue.
	
	@OSPProject Threads
    */
    public void do_resume()
    {
        // If thread is not waiting, don't resume
		if(getStatus() < ThreadWaiting) {
            MyOut.print(this, "Attempt to resume " + this + ", which wasn't waiting");
            return;
        }
      
        MyOut.print(this, "Resuming " + this);
        
		// If single thread is waiting put it in ready queue
		if(this.getStatus() == ThreadWaiting) {
            setStatus(ThreadReady);
        } else if (this.getStatus() > ThreadWaiting) { // else reduce the number of waiting threads by 1
            setStatus(getStatus()-1);
        }
        
        if (getStatus() == ThreadReady) {
            readyQueue.append(this);
        }
        
        ThreadCB.dispatch(); 

    }

    /** 
        Selects a thread from the run queue and dispatches it. 

        If there is just one theread ready to run, reschedule the thread 
        currently on the processor.

        In addition to setting the correct thread status it must
        update the PTBR.
	
	@return SUCCESS or FAILURE

        @OSPProject Threads
    */
    public static int do_dispatch()
    {
        
		ThreadCB thread = null;

        try
        {
            thread = MMU.getPTBR().getTask().getCurrentThread();    
        }
        catch(NullPointerException e){}
        //Place current running thread to end of queue
        if(thread != null)                                          
        {
            thread.getTask().setCurrentThread(null);
            MMU.setPTBR(null);
            thread.setStatus(ThreadReady);
            readyQueue.append(thread);
        }
        
        if(readyQueue.isEmpty())                                    
        {
            MMU.setPTBR(null);
            return FAILURE;
        }
        //remove the head from the queue and make it as current running thread
        else
        {
            thread = (ThreadCB) readyQueue.removeHead();            
            MMU.setPTBR(thread.getTask().getPageTable());           
            thread.getTask().setCurrentThread(thread);              
            thread.setStatus(ThreadRunning);
			if(!thread.getStarted())
			{
				thread.setStarted(true);

				sumOfResponseTime = sumOfResponseTime + (HClock.get() - (thread.getCreationTime()));
				MyOut.print("OSP.Threads.ThreadCB", "Sum of Response Time for " + count + " threads: " + sumOfResponseTime);
			}
        }
		//HTimer.set(30);
        return SUCCESS;  
    }

    /**
       Called by OSP after printing an error message. The student can
       insert code here to print various tables and data structures in
       their state just after the error happened.  The body can be
       left empty, if this feature is not used.

       @OSPProject Threads
    */
    public static void atError()
    {
        MyOut.print(this,"Some Error has occurred!");

    }

    /** Called by OSP after printing a warning message. The student
        can insert code here to print various tables and data
        structures in their state just after the warning happened.
        The body can be left empty, if this feature is not used.
       
        @OSPProject Threads
     */
    public static void atWarning()
    {
        MyOut.print(this,"Warning!");

    }


    /*
       Feel free to add methods/fields to improve the readability of your code
    */

}

/*
      Feel free to add local classes to improve the readability of your code
*/
