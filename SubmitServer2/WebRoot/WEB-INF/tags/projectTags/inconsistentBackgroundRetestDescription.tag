  <script type="text/javascript">
                    function toggle2(item) {
                        obj = document.getElementById(item);
                        if (obj.style.display == "none") {
                            obj.style.display = "block";
                        } else {
                            obj.style.display = "none";
                        }
                    }
                </script>
                <p>
      <a href="javascript:toggle2('inconsistentResults')" title="Click to toggle display of explanation">
            Explanation of inconsistent test results (toggle) </a>
            </p>
  
 <div id="inconsistentResults" style="display: none">
   <blockquote>
  <p> To increase our confidence in the outcomes of test cases, we constantly re-test submissions whenever
there are no new submissions to be tested.  These are called <b>"background retests"</b>.
In a perfect world, each test case returns the same results after every execution.
However, in practice many things can go wrong--machines testing submission can crash or 
become over-loaded and cause test cases to timeout, the network can have problems, 
file systems can fail, test cases might be unreliable, and so on. Submissions can also have mistakes that result
in code that sometimes passes tests and sometimes failing those same tests. This might be due to varying iteration orders
for elements in a HashSet, or due to data races in a multithreaded program. 
<p>
Whenever we perform a "background retest" of a submission and notice results that look different,
we store the conflicting results and mark this situation as an <b><font color=red>inconsistent 
background retest</font></b>
<p>
Note that a submission may have inconsistent background retest results from Release or Secret tests
that you can't yet see.
</p>
<p>If a submission has inconsistent test results, and instructor may need to investigate to determine whether the problem is in the test case,
a problem at the time of submission, or if the submitted code is indeed flawed.
</p>
</blockquote>
</div>