
CIS 657 - Principles of OS
OSP2 Lab 3 - Memory

Group Number: M002_G3

Group members:
Shlok Desai
Akhil Panchal
Vijayendra Ghadge
Mandeep Singh Jhajj

do_handlePageFault method in PageFaultHandler.java maintains page fault count. countFault varible maintains the count. It is incremented when the do_handlePageFault method is invoked.
Whenever page fault occurs we extract a new frame and check if the page in the frame is dirty. If it is dirty then we swap out and return failure Or else we set the frame of the page to new frame and swap in.

GetNewFrame method is used for returning the next frame as per the scheduling algorithms.

FIFO - The operating system maintains a list of all pages currently in memory, with the page at the head of the list the oldest one and the page at the tail the most recent arrival. On a page fault, the page at the head is removed and the new page added to the tail of the list.
LRU - When a page fault occurs, it throws out the page that has been unused for the longest time. This strategy is called Least Uecently Used.

After comparing the reading we came to know that LRU has less number of page faults. Graph and readings are mentioned in the presentation.

Responsibilities of Group Members:

FCFS - Mandeep Singh Jhajj
LRU - Shlok Desai
FCFS- Akhil Panchal 
LRU - Vijayendra Gadge
The group collectively calculated the performance analysis.

