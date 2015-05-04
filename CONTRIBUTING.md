# Contributing

This file describes the way we work. Reading this file will help you to understand the organization of the Qabel project. If you want to contribute to Qabel -- depending on your experience with open source projects and GitHub -- please scan this file on how to do this.

## Who should read this

This file is for contributors, reviewers and integrators alike.

* **Contributor** Anyone that contributes to a Qabel GitHub repository in any form. Anyone can be a contributor.
* **Reviewer** Anyone who comments on a pull request, wiki page or proposal in the form of an issue -- after reading through it -- with the intention of improving it. Anyone can be a reviewer but when it comes to making something official (e.g. merging a pull request into upstream master), words from some reviewers may have more weight than words from others.
* **Integrator** Anyone with push access.

## HOW TO contribute

### Issues
We use [issues](https://help.github.com/articles/about-issues/) to ask questions, discuss ideas, organize tasks, look for help and so on -- not just for reporting software bugs. Issues are our main tool for collaboration.

### Pull requests
We are using [pull requests](https://help.github.com/articles/using-pull-requests/) with the [fork & pull model](https://help.github.com/articles/using-pull-requests/#fork--pull). If you are not familiar with collaborating on GitHub, see [this help](https://help.github.com/categories/collaborating/).

* Create a new branch in your fork for each new pull request. Base these branches on the latest commit in the branch you want your changes to be merged into (often this is *master*) from upstream -- keep your fork in [sync](https://help.github.com/articles/syncing-a-fork/).
* Address one bug fix, feature or proposal (on how to handle one particular topic) per pull request.
  * It is often a good idea to discuss the things you want to do in an issue prior to actually writing code for something which may not be merged anyway.
* Pull requests should be based upon an issue. If merging your pull request would resolve an issue, you can mention this with [special syntax](https://github.com/blog/1506-closing-issues-via-pull-requests).
* Only make your pull requests depend on an other (not yet merged) pull request if is technically necessary. E.g. the code in PR *b* uses functionality implemented in PR *a* but *a* is not yet merged.
  * Clearly mention this dependency in the initial description of the pull request.
* Reference all necessary resources (e.g. issues, pull requests, wiki pages, documentation, non GitHub resources, ...) in the description of your pull request.
* Force push `git push -f` only when asked for by an integrator. Rebasing should only be done if it is needed or really useful.
* Push additional (fix) commits to your pull request to incorporate changes (e.g. which are needed due to the review of the pull request).
* Never abandon a pull request and create a new one for the same thing unless you are asked for by an integrator. When doing so, always reference these pull requests to one another.

#### Merging
Prior merging a pull request, the following conditions must be met.
* All discussions must be resolved.
  * This includes comments on outdated diffs (of course they are resolved when the diff is outdated due to resolving).
* After merging a pull request the code in the affected branch must still compile and all the tests that work without the merge must still work.
* Two integrators need to acknowledge the pull request independently.
* The integrator who performs the merge takes responsibility and therefore should have reviewed the pull request *in the state that will be merged*.
  * Remember that force pushing can alter the patch set of the old commits -- not just the hashes ;)

The integrator who performs the merge decides on how to [perform the merge](https://help.github.com/articles/merging-a-pull-request/). Sometimes manual merges need to be done. Sometimes even a manual reabase and/or merge can be helpful (e.g. to fix history issues the contributor cannot fix or the integrator want to help the contributor with). Merging via the GitHub web interface (the *green button*) should be preferred.

#### Commits
* One commit can incorporate a big or a small patch but address **one** *thing* per commit.
* A commit does not include unnecessary changes. This is especially true for changes due to auto formating and fixing spelling errors in comments. Adding / changing documentation to added / changed functionality is **not** *unnecessary* here.
* If one specific commit solely resolves an issue, use [this](https://help.github.com/articles/closing-issues-via-commit-messages/) special syntax.

##### Commit messages
Commit messages have a subject + an optional description or additional information -- not a single long line or sentence. The description / additional information is a new paragraph. Just think of a commit message as an e-mail with a subject and a body with the diff attached to the e-mail.

Example commit message:
> Implemented multi sub-key support
>
> Old getters are marked as deprecated. They will be removed when all other
> methods have been changed to the newly returned lists.

## Coding style

### Style guide
We do not dogmatically follow a particular style guide but generally the [Code Conventions for the Java :tm: Programming Language](http://www.oracle.com/technetwork/java/javase/documentation/codeconvtoc-136057.html) are a good starting point.

Regarding the look and feel most -- but not all -- of this is reflected by the Eclipse default settings.

### Our style
* Use tabs instead of spaces.
* Try to fit your code into **80** characters but do not wrap your line of code just for the sake of wrapping it. Use **120** in your auto format settings.
* Statements like ```if``` always with ```{ }```. Do not just write ```if (foo) bar;```
* Our code does not need to be compatible to versions below Java 7. That means you can and should use Java 7 features and syntax.
* Developers want their software to always be in a defined and consistent state and this should be preferred but sometimes it is not possible. In such cases not only document these conditions but also handle unwanted conditions.
  * E.g. there are cases where an ```initialize()``` method needs to be called *after* an object has been created (i.e. the constructor has been invoked) to fully initialize this object but *before* other methods work as intended. Such dependencies must not only be documented (using Javadoc) but also the methods which depend on a call of ```initialize()``` must check if this call has happened and give a meaningful error if not. In cases like this we use an ```IllegalStateException```.

#### Documentation
Qabel is a big project and it has a lot of code which will be called from other components -- e.g. from a module. Code -- especially public methods or classes -- must be well documented. *Well* means class, method and parameter names should speak for themselves. Nevertheless often additional Javadoc is needed to understand what is going on. These comments should contain actual information and not repeat things the class, method or parameter names should provide anyway.

Bad example:
```Java
/**
 * Get the Runtime.
 * @return Returns the time.
 */
public long getTime();
```

Good example:
```Java
/*
 * Get the runtime since application start.
 * @return Returns runtime in milliseconds.
 */
public long getRuntime();
```

As it can be seen even simple getter or setter might need Javadoc to provide valuable information. Of course this does *not* mean every trivial getter and setter need Javadoc.

Code should speak for itself. Nevertheless there will be code which needs an inline comment to explain some details to the reader (i.e. developer).

#### Be consistent
The code should look the same regardless who wrote the code. Look into the code if you are unsure about how to do something.
Example:
If you often happen to find code like
```
catch (SomeException e) {
    logger.warning("SomeException occured while doing something. " + e.getMessage());
    return false;
}
```
don't do something like
```
catch(SomeException e1)
{
        System.out.println("SomeException occured while doing something.");
        return (false);
}
```

### Misguided behavior
You will find code which is not conform to our style (e.g. you will find spaces instead of tabs). Never address style issues in your pull request or commits. Big patch sets just for re-formatting the source code will not help anyone. Try to commit conform code in the future instead and eventually the style will become conform. Of course big refactoring patch sets may still be a good idea after reaching certain milestones but this has to be discussed (e.g. in an issue) beforehand.
