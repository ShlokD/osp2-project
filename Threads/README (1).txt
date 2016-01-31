
CIS 657 - Principles of OS
OSP2 Lab 3 - Threads

Group Number: M002_G3

Group members:
Shlok Desai
Akhil Panchal
Vijayendra Ghadge
Mandeep Singh Jhajj

ThreadCB.java

We have to implement scheduling algorithms for threads. To do this we need to implement methods of the ThreadCB and TimerInterrupt classes. 
To begin, we have created a GenericList that would contain the ready queue for the threads. We have initiated readyQueue in init() of ThreadCB

FCFS - Threads are serviced in the order they entered the ready queue. It is simplest scheduling algorithm
that has the tendency to favor long, CPU-intensive threads over short, I/O-bound threads.

Round Robin - In this algorithm, each thread gets to execute for a length of time known as the time slice or time quantum before it is preempted and placed back on the
ready queue.

First thread needs to be created, so we have written code for creating thread by following steps mentioned in the manual.
Then we have kill method and its implemented as per OSP2 manual. Same is the case with suspend and resume methods.

FCFS scheduling is implemented by placing the current running thread to end of queue and then removing the thread from the head.
Round robin is implemented by placing the current running thread to end to queue after a specified amount of time, and executing the thread which is present at head of queue.

For round robin algorithm we have added code in TimerInterruptHandler class which ensures that when interrupt occur our dispatch method is called.
So we have ThreadCB.dispatch(); in do_handleInterrupt() method.

To get average response time and throughput, we have defined the following variables: 
	private static int count = 0;
    private static long sumOfResponseTime = 0;
    private static long throughputTime = 0;
	
The following variable helps us determine weather the thread has been dispatched once.
    private boolean started;
	
Count maintains the number of threads created, and we add response time incrementally to get sum of response time for all threads. 
Using these values we are plotting the graphs.

Round robin algorithm has less response time than FCFS algorithm, because in round robin thread will be scheduled after specified interval of time which ensures that 
response time will be less.
Throughput of FCFS for short threads is more than Round Robin because in round robin thread needs to wait for its turn after completing its time slice.
Throughput for long threads is more for round robin since each thread gets equal opportunity to be scheduled, and does not need to to wait for long time.

Responsibilities of Group Members:

The group collectively implemented do_kill(), do_create(), do_suspend(), do_resume() methods.

Main focus of this lab is do_dispatch() method, where we need to implement different scheduling methods. 

FCFS - Mandeep Singh Jhajj
Round Robin - Shlok Desai
Response Time - Akhil Panchal 
Throughput - Vijendra Gadge

