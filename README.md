# Oslo Coding Dojo, July 2011: Learning From Failure

## Background

In an attempt to find a more realistic coding dojo kata than the usual game or algorithm related problems suggested at [codingdojo.org](http://codingdojo.org/cgi-bin/wiki.pl?KataCatalogue), the July coding dojo was a first attempt at a more realistic kata assignment for OCD.

The kata chosen was an implementation of the Circuit Breaker design pattern. The hope was that this (like the [refreshing cache excercise](http://www.meetup.com/OsloCodingDojo/events/16086309/) from January) smacked more of a 'real' problem, and that the end result and the new knowledge gained through the kata could be of direct use in everyday professional work.

For an iterative implementation breakdown of requirements (an improvement of the kata discovered during the coding dojo retrospective), see the kata instructions. For more background on the design pattern, read on.

# The Circuit Breaker Design Pattern

The kata consists of implementing an adapted version of the Circuit Breaker pattern, presented in its original form by *Michael T. Nygard* in the book [Release It!](http://pragprog.com/book/mnee/release-it). The version used for the kata was presented by Avdi Grimm as a mitigation strategy for the problem of failure cascades.

## Cascading Failure

In his book, Michael Nygard describes the concept of cascading failure as follows:

> System failures start with a crack. That crack comes from some fundamental problem. 
> Various mechanisms can retard or stop the crack, which are the topics of the next chapter. 
> Absent those mechanisms, the crack can progress and even be ampliﬁed by some structural problems.
> A cascading failure occurs when a crack in one layer triggers a crack in a calling layer.

> Cascading failures require some mechanism to transmit the failure from one layer to another. 
> The failure “jumps the gap” when bad behavior in the calling layer gets triggered by the 
> failure condition in the called layer. 

> Just as integration points are the number-one source of cracks, cascading failures are the 
> number-one crack accelerator. Preventing cascading failures is the very key to resilience. 
> The most effective patterns to combat cascading failures are Circuit Breaker and Timeouts.

### An Example Failure Cascade

In the [Pragmatic Bookshelf Podcast of June 2011](http://pragprog.com/podcasts/show/37), Avdi Grimm recounts to Miles Forrest a story of failure handling gone bad.

He was working on a distributed system of worker processes and other services communicating with one another using queues. Workers would process jobs from a queue, and occasionally a job would fail.

The team had rolled their own exception notification system; a code snippet which would catch the exceptions and send an email notification to the entire dev team using a shared GMail account.

One day they rolled out a new release which caused a great deal more of the jobs to fail. Failing jobs caused workers to email the dev team with exception notifications, and the rate of failure notifications caused GMail to start throttling the account. This in turn caused SMTP exceptions when sending exception notifications. But the notification code had never been written to handle SMTP exceptions, so this in turn caused the workers to crash. Instead of reporting job failures, the workers were now just crashing and staying dead.

While the crashing of workers was bad enough, the problems at hand were even worse. The same GMail account was being used by other, unrelated systems, for various notifications. These systems now started receiving SMTP exceptions because of the throttling and started crashing as well. 

As a result of a bit of error reporting code that wasn't sufficiently robust in the face of exceptions itself, they had all of their systems go down. Even the ones unrelated to the job processing -- a classic example of a cascading failure scenario. 

## Circuit Breaker

Circuit breakers are a way to automatically degrade functionality when the system is under stress. 

In [Release It!](http://pragprog.com/book/mnee/release-it), the description of the Circuit Breaker design pattern is based on its electrical namesake, the successor of the residential fuse. 

> Now, circuit breakers protect overeager gadget hounds from burning their houses down. 
> The principle is the same: detect excess usage, fail first, and open the circuit. 
> More abstractly, the circuit breaker exists to allow one subsystem (an electrical circuit) 
> to fail (excessive current draw, possibly from a short-circuit) without destroying 
> the entire system (the house). Furthermore, once the danger has passed, the circuit
> breaker can be reset to restore full function to the system.

> You can apply the same technique to software by wrapping dangerous operations with a 
> component that can circumvent calls when the system is not healthy. This differs from retries, 
> in that circuit breakers exist to prevent operations rather than reexecute them.

### States

The circuit breaker is typically implemented as a wrapper or proxy component with different behaviour depending on its internal state. The standard version has three states: Closed, Open and Half-open.

#### Closed

> In the normal “closed” state, the circuit breaker executes operations as usual. 
> These can be calls out to another system, or they can be internal operations that 
> are subject to timeout or other execution failure. If the call succeeds, nothing 
> extraordinary happens. If it fails, however, the circuit breaker makes a note of 
> the failure. Once the number of failures (or frequency of failures, in more sophisticated
> cases) exceeds a threshold, the circuit breaker trips and “opens” the circuit.

#### Open
> When the circuit is “open,” calls to the circuit breaker fail immediately, 
> without any attempt to execute the real operation. After a suitable amount of time,
> the circuit breaker decides that the operation has a chance of succeeding, 
> so it goes into the “half-open” state. 

#### Half-open
> In this state, the next call to the circuit breaker is allowed to execute the 
> dangerous operation. Should the call succeed, the circuit breaker resets and 
> returns to the “closed” state, ready for more routine operation. If this trial 
> call fails, however, the circuit breaker returns to the “open” state until another
> timeout elapses.

Since all calls fail when the circuit breaker is open, it is recommended that this be indicated by a specific type of exception. This allows the calling code to handle this type of exception differently.

## Kata Requirements

Instead of preparing a complete set of requirements for the kata, a progression of iterations is specified, where the Circuit Breaker pattern is incrementally specified and expanded.

Implementations will most likely vary quite significantly depending on the programming language used. If a requirement does not make sense for a specific language; skip it.