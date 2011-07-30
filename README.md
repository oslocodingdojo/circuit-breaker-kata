# Oslo Coding Dojo, July 2011: Learning From Failure

## Background

In an attempt to find a more realistic coding dojo kata than the usual game or algorithm related problems suggested at [codingdojo.org](http://codingdojo.org/cgi-bin/wiki.pl?KataCatalogue), the July coding dojo was a first attempt at a more realistic kata assignment for OCD.

The kata chosen was an implementation of the Circuit Breaker design pattern. The hope was that this (like the [refreshing cache excercise](http://www.meetup.com/OsloCodingDojo/events/16086309/) from January) smacked more of a 'real' problem, and that the end result and the new knowledge gained through the kata could be of direct use in everyday professional work.

For an iterative implementation breakdown of requirements (an improvement of the kata discovered during the coding dojo retrospective), see the *documentation* directory. For more information about the kata (and the design pattern) itself, read on.

# The Circuit Breaker Kata

The kata consists of implementing an adapted version of the Circuit Breaker pattern, presented in its original form by *Michael T. Nygard* in the book [Release It!](http://pragprog.com/book/mnee/release-it). The version used for the kata was presented by Avdi Grimm as a mitigation strategy for the problem of failure cascades.

## Cascading Failure

In the Pragmatic Bookshelf Podcast of June 2011, Avdi Grimm recounts to Miles Forrest a story of failure handling gone bad.

He was working on a distributed system, where worker processes and other services performing unrelated tasks were communicating with one another through the use of queues. Workers would process jobs from a queue, and occasionally a job would fail.

The team had rolled their own exception notification system, doing the simplest thing that could possibly work; a code snippet which would catch the exceptions and email the entire dev team a notification using a shared GMail account.

One day they rolled out a new release which caused a great deal more of the jobs to fail. The jobs were failing, and consequently emailing the dev team with exception notifications. In fact, they failed so often that GMail started throttling the account. This resulted in SMTP exceptions being thrown when exception notifications for the job failures were being dispatched.

But the code that reported the exceptions had never been written to handle SMTP exceptions, so it in turn caused the worker to crash. So instead of reporting failures, the workers were now just crashing and staying dead.

But it doesn't stop there. While the crashing of workers was bad enough, the actual problem was worse than that. The same GMail account was being used for various notifications, not just this worker notification system. So other, unrelated systems, who were also using the same GMail account for sending emails, also started getting SMTP exceptions because of the throttling. So these unrelated systems were crashing as well. So as a result of some error reporting code that wasn't sufficiently robust in the face of exceptions itself, they had all of their systems go down. Even the ones unrelated to the job processing.

And that is a classic example of a cascading failure scenario. 


## Circuit Breaker

The Circuit Breaker pattern is presented in the book [Release It!](http://pragprog.com/book/mnee/release-it) by Michael T. Nygard. The following description, however, is based on online resources rather than the original source. See the list of references for details.

The Circuit Breaker pattern serves two purposes:

* Primarily, it protects clients from slow or broken services
* Secondarily, it protects services from demand in excess of capacity

