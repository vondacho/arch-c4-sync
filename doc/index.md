# arch-c4-sync

A Kotlin project for synchronizing several corporate C4-compliant Structurizr models including the publishing 
to a C4 documentation hub hosted on Structurizr cloud for 3D-visualization.

This software architectural documentation engine proposes to gather the information elements related to the different
layers of the C4 model for one corporate system, and then publish them on a corporate Structurizr Cloud account for visualization.

Concretely, the C4 data elements with their relationships come from manual input and automatic search operations.
For example, the product owners are responsible for the maintenance of layers C1 (context) and C2 (business containers);
network infrastructure tools are automatically queried to harvest the C2 containers with their interactions;
code scanners are used to extract the C3 components.

The production of this living documentation is orchestrated by a pipeline triggered periodically
or upon human intervention by means of a pull request.

The scheduling of human intervention is managed by a process.

## Getting started

- To build the project with `gradle jar`
- To create your private free account on [Structurizr](https://www.structurizr.com/signup)
