package osp.Memory;
/**
    The PageTable class represents the page table for a given task.
    A PageTable consists of an array of PageTableEntry objects.  This
    page table is of the non-inverted type.

    @OSPProject Memory
*/
import java.lang.Math;
import osp.Tasks.*;
import osp.Utilities.*;
import osp.IFLModules.*;
import osp.Hardware.*;

public class PageTable extends IflPageTable
{
    /** 
	The page table constructor. Must call
	
	    super(ownerTask)

	as its first statement.

	@OSPProject Memory
    */
    public PageTable(TaskCB ownerTask)
    {
        // your code goes here
		super(ownerTask);
		int numberOfPages = (int)Math.pow(2, MMU.getPageAddressBits());
    	pages = new PageTableEntry[numberOfPages];
		for(int i = 0; i < numberOfPages; i++)
    	{
    		pages[i] = new PageTableEntry(this, i);
    	}

    }

    /**
       Frees up main memory occupied by the task.
       Then unreserves the freed pages, if necessary.

       @OSPProject Memory
    */
    public void do_deallocateMemory()
    {
        // your code goes here
		TaskCB task = getTask();
		for(int i = 0; i < MMU.getFrameTableSize(); i++)
        {
        	FrameTableEntry tempFrameTableEntry = MMU.getFrame(i);
        	PageTableEntry tempPageTableEntry = tempFrameTableEntry.getPage();
        	if(tempPageTableEntry != null && tempPageTableEntry.getTask() == task)
        	{
        		
				tempFrameTableEntry.setPage(null);
				tempFrameTableEntry.setDirty(false);
				tempFrameTableEntry.setReferenced(false);
				if(tempFrameTableEntry.getReserved() == task)
        			tempFrameTableEntry.setUnreserved(task);
        	}
        }
    }


    /*
       Feel free to add methods/fields to improve the readability of your code
    */

}

/*
      Feel free to add local classes to improve the readability of your code
*/
