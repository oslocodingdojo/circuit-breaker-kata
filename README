# Oslo Coding Dojo, July 2011: Learning From Failure

## Motivation

In an attempt to find a more realistic coding dojo kata than the usual game or algorithm related problems presented at ..., the July coding kata

The example I latched on to was an implementation of the Circuit Breaker design pattern, used to mitigate the problem of failure cascades.

## Cascading Failure

In the Pragmatic Bookshelf Podcast of June 2011, Avdi Grimm recounts to Miles Forrest a story of failure handling gone bad.

He was working on a distributed system, where worker processes and other services performing unrelated tasks were communicating with one another through the use of queues. Workers would process jobs from a queue, and occasionally a job would fail.

The team had rolled their own exception notification system, doing the simplest thing that could possibly work; a code snippet which would catch the exceptions and email the entire dev team a notification using a shared gmail account.

One day they rolled out a new release which caused a great deal more of the jobs to fail. The jobs were failing, and consequently emailing the dev team with exception notifications. In fact, they failed so often that GMail started throttling the account. This resulted in SMTP exceptions being thrown when exception notifications for the job failures were being dispatched.

But the code that reported the exceptions had never been written to handle SMTP exceptions, so it in turn caused the worker to crash. So instead of reporting failures, the workers were now just crashing and staying dead.

But it doesn't stop there. While the crashing of workers was bad enough, the actual problem was worse than that. The same GMail account was being used for various notifications, not just this worker notification system. So other, unrelated systems, who were also using the same GMail account for sending emails, also started getting SMTP exceptions because of the throttling. So these unrelated systems were crashing as well. So as a result of some error reporting code that wasn't sufficiently robust in the face of exceptions itself, they had all of their systems go down. Even the ones unrelated to the job processing.

And that is a classic example of a cascading failure scenario. 


## Circuit Breaker

The Circuit Breaker pattern as explained by Avdi Grimm in the podcast, is presented by in the book [release it] by [author]. 

The Circuit Breaker pattern serves two purposes:

* Primarily, it protects clients from slow or broken services
* Secondarily, it protects services from demand in excess of capacity

