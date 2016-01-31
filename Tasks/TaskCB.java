

package osp.Tasks;

import java.util.Vector;
import osp.IFLModules.*;
import osp.Threads.*;
import osp.Ports.*;
import osp.Memory.*;
import osp.FileSys.*;
import osp.Utilities.*;
import osp.Hardware.*;
import java.lang.Math;
import java.util.*;

/**
    The student module dealing with the creation and killing of
    tasks.  A task acts primarily as a container for threads and as
    a holder of resources.  Execution is associated entirely with
    threads.  The primary methods that the student will implement
    are do_create(TaskCB) and do_kill(TaskCB).  The student can choose
    how to keep track of which threads are part of a task.  In this
    implementation, an array is used.

    @OSPProject Tasks
*/
public class TaskCB extends IflTaskCB
{
	// Declaring collections for maintaining threads, files and ports
	private GenericList threads;
	private GenericList ports;
	private GenericList files;

    /**
       The task constructor. Must have

       	   super();

       as its first statement.

       @OSPProject Tasks
    */

	//Default Constructor
    public TaskCB()
    {

		super();
		threads = new GenericList();
		ports = new GenericList();
		files 	= new GenericList();
    }

    /**
       This method is called once at the beginning of the
       simulation. Can be used to initialize static variables.

       @OSPProject Tasks
    */
    public static void init()
    {


    }

    /**
        Sets the properties of a new task, passed as an argument.

        Creates a new thread list, sets TaskLive status and creation time,
        creates and opens the task's swap file of the size equal to the size
	(in bytes) of the addressable virtual memory.

	@return task or null

        @OSPProject Tasks
    */

	// Initializing resources for the task
    static public TaskCB do_create()
    {
		TaskCB task = new TaskCB();
		PageTable pageTable = new PageTable(task);

		task.setPageTable(pageTable);
		task.setCreationTime(HClock.get());
		task.setStatus(TaskLive);
		task.setPriority(4);

		int sizeOfSwapFile = (int)Math.pow(2,MMU.getVirtualAddressBits());		// Swap file size is determined by Virtual Address bits.
		String nameOfSwapFile = SwapDeviceMountPoint + task.getID();			// Name of swap file is same as ID of the task and is located in
																				// SwapDeviceMount directory.
		int success = FileSys.create(nameOfSwapFile, sizeOfSwapFile);

		OpenFile myFile = null;
		if(success == SUCCESS) {
			myFile = OpenFile.open(nameOfSwapFile, task);
		}
		else
		{
			task.atError();
			return null;
		}

		if(myFile != null) {
			task.setSwapFile(myFile);
			ThreadCB.create(task).dispatch();
			return task;
		}
		else {
			ThreadCB.dispatch();
			return null;
		}
    }

    /**
       Kills the specified task and all of it threads.

       Sets the status TaskTerm, frees all memory frames
       (reserved frames may not be unreserved, but must be marked
       free), deletes the task's swap file.

       @OSPProject Tasks
    */
    public void do_kill()
    {
		// Iterators for the collections of threads, open files and ports
		Enumeration enumThreads = threads.forwardIterator();
		Enumeration enumPorts = ports.forwardIterator();
		Enumeration enumFiles = files.forwardIterator();

		//Killing all active threads.
		while(enumThreads.hasMoreElements()){
			ThreadCB thread = (ThreadCB)enumThreads.nextElement();
			thread.kill();
		}

		//Destroying all open ports
		while(enumPorts.hasMoreElements()){
			PortCB port = (PortCB)enumPorts.nextElement();
			port.destroy();
		}

		this.setStatus(TaskTerm);
		this.getPageTable().deallocateMemory();

		//Closing all open files
		while(enumFiles.hasMoreElements()){
			OpenFile file = (OpenFile)enumFiles.nextElement();
			file.close();
		}

		String nameOfSwapFile = SwapDeviceMountPoint + this.getID();
		FileSys.delete(nameOfSwapFile);
    }

    /**
	Returns a count of the number of threads in this task.

	@OSPProject Tasks
    */
    public int do_getThreadCount()
    {
		return threads.length();
    }

    /**
       Adds the specified thread to this task.
       @return FAILURE, if the number of threads exceeds MaxThreadsPerTask;
       SUCCESS otherwise.

       @OSPProject Tasks
    */
    public int do_addThread(ThreadCB thread)
    {
		if(threads.length() < ThreadCB.MaxThreadsPerTask) {
			threads.insert(thread);
			return SUCCESS;
		}
		return FAILURE;

    }

    /**
       Removes the specified thread from this task.

       @OSPProject Tasks
    */
    public int do_removeThread(ThreadCB thread)
    {
		if(threads.length() == 0) {
			return FAILURE;
		}
		else if(threads.contains(thread)) {
			threads.remove(thread);
			return SUCCESS;
		}
		return FAILURE;
    }

    /**
       Return number of ports currently owned by this task.

       @OSPProject Tasks
    */
    public int do_getPortCount()
    {
		return ports.length();
    }

    /**
       Add the port to the list of ports owned by this task.

       @OSPProject Tasks
    */
    public int do_addPort(PortCB newPort)
    {
		if(ports.length() < PortCB.MaxPortsPerTask) {
			ports.insert(newPort);
			return SUCCESS;
		}
		return FAILURE;
    }

    /**
       Remove the port from the list of ports owned by this task.

       @OSPProject Tasks
    */
    public int do_removePort(PortCB oldPort)
    {
		if(ports.length() == 0) {
			return FAILURE;
		}
		else if(ports.contains(oldPort)) {
			ports.remove(oldPort);
			return SUCCESS;
		}
		return FAILURE;
    }

    /**
       Insert file into the open files table of the task.

       @OSPProject Tasks
    */
    public void do_addFile(OpenFile file)
    {

		files.insert(file);
    }

    /**
	Remove file from the task's open files table.

	@OSPProject Tasks
    */
    public int do_removeFile(OpenFile file)
    {
		if(files.length() == 0) {
			return FAILURE;
		}
		else if(files.contains(file)) {
			files.remove(file);
			return SUCCESS;
		}
		return FAILURE;
    }

    /**
       Called by OSP after printing an error message. The student can
       insert code here to print various tables and data structures
       in their state just after the error happened.  The body can be
       left empty, if this feature is not used.

       @OSPProject Tasks
    */
    public static void atError()
    {

		MyOut.print("osp.Tasks.TaskCB","Some Error has occurred!");
    }

    /**
       Called by OSP after printing a warning message. The student
       can insert code here to print various tables and data
       structures in their state just after the warning happened.
       The body can be left empty, if this feature is not used.

       @OSPProject Tasks
    */
    public static void atWarning()
    {

		MyOut.print("osp.Tasks.TaskCB","Warning!");
    }
}
