To increase our confidence in the outcomes of test cases, we constantly re-test submissions whenever
there are no new submissions to be tested.  These are called <b>"background retests"</b>.
In a perfect world, each test case returns the same results after every execution.
However, in practice many things can go wrong--machines testing submission can crash or 
become over-loaded and cause test cases to timeout, the network can have problems, 
file systems can fail, test cases might be unreliable, and so on.  In addition, 
advanced projects using threads are by definition unpredictable, due to the 
unpredictable nature of threads.
<p>
Whenever we perform a "background retest" of a submission and notice results that look different,
we store the conflicting results and mark this situation as an <b><font color=red>inconsistent 
background retest</font></b>
<p>
Note that a submission may have inconsistent background retest results from Release or Secret tests
that you can't yet see.
