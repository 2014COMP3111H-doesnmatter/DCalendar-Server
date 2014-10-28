Coding Standard for this project
================

For the main part of standard, please refer to http://www.oracle.com/technetwork/articles/javase/codeconvtoc-136057.html

Here we only present some commonly mistaken part, or some our project related standards.

#Naming Convention
- Class name, upper camel case. e.g. `BaseLoginView`, `EventObject`.
- Variable name, lower camel case. e.g. `paramString`, `startDay`.
- Functions, lower camel case with first word verbal. e.g. `fireEvent()`, `prepareStatement()`.
- Getters and setters follow common Java convention. e.g. `getId()`, `setId()`
- Boolean function should have prefix `is`. e.g. `isDirty()`, `isChanged()`
- Member variable of array like(collection) of objects should have prefix `a`. e.g. `aAppt`, `aUser`
- Member variable with integer value should have prefix `n`. e.g. `nAppt`

